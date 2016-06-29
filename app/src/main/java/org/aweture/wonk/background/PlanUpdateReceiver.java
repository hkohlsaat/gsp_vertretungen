package org.aweture.wonk.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PlanUpdateReceiver extends BroadcastReceiver {

    private Handler handler;

    public PlanUpdateReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        handler.handleEvent(intent);
    }

    public IntentFilter getIntentFilter() {
        return new IntentFilter(PlanUpdateService.PLAN_UPDATE_SERVICE_FINISHED);
    }
    public static interface Handler {
        public void handleEvent(Intent intent);
    }
}
