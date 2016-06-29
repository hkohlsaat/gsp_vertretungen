package org.aweture.wonk.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.aweture.wonk.log.LogUtil;

public class AppUpdatedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String packageName = context.getPackageName();
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            final int versionCode = packageInfo.versionCode;

            if (versionCode == 13) {
                // In this version the database is dropped and the plan is saved in an extra
                // file in json format.
                context.deleteDatabase("wonk.db");
                // Also some SharedPreferences values aren't used any more.
                String name = this.getClass().getName();
                SharedPreferences sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("userdata_inserted");
                editor.remove("username");
                editor.remove("subjects_version");
                editor.remove("teachers_version");
                editor.apply();
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(e);
        }
    }
}
