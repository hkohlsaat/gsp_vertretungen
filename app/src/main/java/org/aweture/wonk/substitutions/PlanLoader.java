package org.aweture.wonk.substitutions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import org.aweture.wonk.background.PlanDownloader;
import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.settings.Activity;
import org.aweture.wonk.storage.PlanStorage;
import org.aweture.wonk.storage.SimpleData;

public class PlanLoader extends AsyncTaskLoader<Plan> {

    private PlanDownloaderFinishedReveiver receiver;

    public PlanLoader(Context context) {
        super(context);
        receiver = new PlanDownloaderFinishedReveiver();
    }

    @Override
    protected void onStartLoading() {
        LogUtil.currentMethod();
        forceLoad();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(receiver, receiver.getIntentFilter());
    }

    @Override
    protected void onStopLoading() {
        LogUtil.currentMethod();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.unregisterReceiver(receiver);
    }

    @Override
    public Plan loadInBackground() {
        Plan plan = null;
        try {
            plan = PlanStorage.readPlan(getContext(), new SimpleData(getContext()).isStudent());
            LogUtil.d("Finished loading sucessfully.");
        } catch (Exception e) {
            LogUtil.w(e.getMessage());
            LogUtil.d("Returning mock plan with no content.");
        }

        if (plan == null) {
            plan = new Plan();
            plan.parts = new Plan.Part[0];
        }
        return plan;
    }

    private class PlanDownloaderFinishedReveiver extends BroadcastReceiver {

        public IntentFilter getIntentFilter() {
            IntentFilter intentFilter = new IntentFilter(PlanDownloader.ACTION_NEW_PLAN_DOWNLOADED_AND_SAVED);
            intentFilter.addAction(Activity.STUDENT_TEACHER_MODE_CHANGED);
            return intentFilter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d("Start loading process.");
            onContentChanged();
        }

    }
}