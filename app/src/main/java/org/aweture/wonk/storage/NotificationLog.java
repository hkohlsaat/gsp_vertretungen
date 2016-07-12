package org.aweture.wonk.storage;

import android.content.Context;

import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Notification;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * NotificationLog is responsible for saving the fired notifications to prevent firing the same
 * notification twice. It does so by keeping a list in a file, where each line represents an own
 * notification.
 */
public class NotificationLog {
    public static final String FILENAME = "notifications.txt";

    public static ArrayList<Notification> saveUnkonwNotifications(Context context, ArrayList<Notification> notifications) {
        // Create the list of notifications unknown to the user and therefore to be shown.
        ArrayList<Notification> unknownNotifications = new ArrayList<Notification>();

        try {
            // Read the known notifications.
            ArrayList<Notification> readNotifications = read(context);

            // Iterate over the given notifications.
            for (int i = 0; i < notifications.size(); i++) {
                Notification nToTest = notifications.get(i);

                boolean hasNotification = false;

                //Iterate over the known notifications.
                for (int j = 0; j < readNotifications.size(); j++) {
                    Notification nFromStorage = readNotifications.get(j);

                    // If a known notification equals the notification of the given notifications,
                    // that notification is known and should not be shown to the user twice.
                    if (nFromStorage.equals(nToTest)) {
                        hasNotification = true;
                        break;
                    }
                }

                if (!hasNotification) {
                    // The notification is unknown to the user and should be shown.
                    unknownNotifications.add(nToTest);
                }
            }

            // All unkown notifications are now known. So they are in the list of notifications to
            // be written back.
            ArrayList<Notification> writeNotifications = new ArrayList<Notification>(unknownNotifications);

            // Iterate over all read notifications.
            for (int i = 0; i < readNotifications.size(); i++) {
                Notification notification = readNotifications.get(i);

                // If the date is not in the last week, the notification is not written back.
                // So it gets forgotten.
                if (Date.fromStringDate(notification.date).isInLastWeek()) {
                    writeNotifications.add(notification);
                }
            }

            // Write the filtered list of notifications back to the file.
            write(context, writeNotifications);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return unknownNotifications;
    }

    private static ArrayList<Notification> read(Context context) throws IOException {
        synchronized (FILENAME) {
            FileInputStream inputStream = context.openFileInput(FILENAME);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader in = new BufferedReader(inputReader);

            try {
                ArrayList<Notification> notifications = new ArrayList<Notification>();
                String line;

                while ((line = in.readLine()) != null) {
                    notifications.add(new Notification(line));
                }

                return notifications;
            } finally {
                in.close();
                inputReader.close();
                inputStream.close();
            }
        }
    }

    private static void write(Context context, ArrayList<Notification> notifications) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < notifications.size(); i++) {
            Notification n = notifications.get(i);
            if (n != null) {
                count++;
                sb.append(n.toString());
                sb.append("\n");
            }
        }

        synchronized (FILENAME) {
            try {
                FileOutputStream outputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                outputStream.write((count + "\n" + sb.toString()).getBytes("UTF-8"));
                outputStream.close();
            } catch (IOException e) {
                LogUtil.e(e);
            }
        }
    }
}
