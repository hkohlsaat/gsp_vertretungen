package org.aweture.wonk.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.aweture.wonk.Application;
import org.aweture.wonk.R;
import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.models.Notification;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.storage.NotificationLog;
import org.aweture.wonk.storage.PlanStorage;
import org.aweture.wonk.storage.SimpleData;
import org.aweture.wonk.substitutions.Activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class FcmListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        // The listener service received a message. Since the only message sent by the server
        // is one informing about a new plan, the implementation of this callback happens to
        // download the new plan without processing the message any further.
        if (Application.hasConnectivity(this)) {
            try {
                getNewPlan();
            } catch (IOException e) {
                LogUtil.e(e);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ComponentName updateJopService = new ComponentName(this, PlanUpdateJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(R.id.update_scheduler_job, updateJopService)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int result = scheduler.schedule(jobInfo);

            if (result == JobScheduler.RESULT_SUCCESS) {
                LogUtil.d("Update scheduled with JobScheduler");
            } else {
                LogUtil.w("Failed to schedule update with JobScheduler");
            }
        }
    }

    private void getNewPlan() throws IOException {
        // Download the plan.
        String planJSON = PlanDownloader.download();
        // Save the plan.
        PlanStorage.savePlan(this, planJSON);
        // Get object representation.
        boolean student = new SimpleData(this).isStudent();
        Plan plan = PlanStorage.readPlan(this, student);
        // Create notifications for the filter.
        ArrayList<Notification> allNotifications = notifications(plan, student);
        // Unknown notifications are saved. Only the unknown ones are returned to be shown.
        ArrayList<Notification> unknownNotifications = NotificationLog.saveUnkonwNotifications(this, allNotifications);

        // If there are notifications to be shown, fire them.
        if (!unknownNotifications.isEmpty()) {
            showNotifications(unknownNotifications);
        }
    }

    private ArrayList<Notification> notifications(Plan plan, boolean student) {
        String filter = new SimpleData(this).getFilter(null);

        ArrayList<Notification> notifications = new ArrayList<Notification>();

        if (filter == null) {
            return notifications;
        }

        for (int i = 0; i < plan.parts.length; i++) {
            Plan.Part part = plan.parts[i];
            for (int j = 0; j < part.substitutions.length; j++) {
                Substitution s = part.substitutions[j];

                if (student) {
                    if (s.className.equalsIgnoreCase(filter)) {
                        notifications.add(new Notification(filter, s.className, s.period, part.day.toDateString()));
                    }
                } else {
                    if ((s.modeTaskProvider && s.taskProvider.abbr.equals(filter)) || (s.modeTaskProvider ^ s.substTeacher.abbr.equals(filter))) {
                        notifications.add(new Notification(filter, s.className, s.period, part.day.toDateString()));
                    }
                }
            }
        }

        return notifications;
    }

    private void showNotifications(ArrayList<Notification> notifications) {
        // Compress the notifications to dates.
        Set<String> dates = new TreeSet<String>();
        for (int i = 0; i < notifications.size(); i++) {
            dates.add(notifications.get(i).date);
        }

        // Form a message.
        StringBuilder message = new StringBuilder("Neue Einträge für den ");
        int counter = 0;
        for (String date : dates) {
            message.append(date);
            counter++;
            if (dates.size() > 1) {
                if (dates.size() - 1 == counter) {
                    message.append("und ");
                } else if (dates.size() > counter) {
                    message.append(", ");
                }
            }
        }

        // Fire the notification.
        fireNotification(message.toString());
    }

    private void fireNotification(String message) {
        String title = "Neuer Vertretungsplan";
        Intent resultIntent = new Intent(this, Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setColor(this.getResources().getColor(R.color.accent))
                        .setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }
}
