package org.aweture.wonk.background;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.aweture.wonk.Application;
import org.aweture.wonk.R;
import org.aweture.wonk.log.LogUtil;

import java.io.IOException;

public class FcmListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        if (Application.hasConnectivity(this)) {
            try {
                PlanDownloader planDownloader = new PlanDownloader();
                planDownloader.downloadAndSave(this);
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
}
