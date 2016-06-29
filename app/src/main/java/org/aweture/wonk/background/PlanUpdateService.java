package org.aweture.wonk.background;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.aweture.wonk.log.LogUtil;

import java.io.IOException;

public class PlanUpdateService extends IntentService {

    public static final String PLAN_UPDATE_SERVICE_FINISHED = "plan_update_service_finished";
    public static final String EXTRA_FINISHED_SUCCESSFULLY = "plan_update_service_finished_successfully";

    public PlanUpdateService() {
        super(PlanUpdateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            PlanDownloader pd = new PlanDownloader();
            pd.downloadAndSave(this);


            intent = new Intent(PLAN_UPDATE_SERVICE_FINISHED);
            intent.putExtra(EXTRA_FINISHED_SUCCESSFULLY, true);
            LogUtil.d("Plan update finished without exception.");
        } catch (IOException ex) {
            LogUtil.e(ex);

            intent = new Intent(PLAN_UPDATE_SERVICE_FINISHED);
            intent.putExtra(EXTRA_FINISHED_SUCCESSFULLY, false);
            LogUtil.d("Plan update finished with exception.");
        }
        LogUtil.d("Send intent informing that update Service finished.");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}