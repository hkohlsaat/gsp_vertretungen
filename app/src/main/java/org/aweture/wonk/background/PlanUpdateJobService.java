package org.aweture.wonk.background;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;

import org.aweture.wonk.log.LogUtil;

import java.io.IOException;

public class PlanUpdateJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new DownloadTask().execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class DownloadTask extends AsyncTask<JobParameters, Void, JobParameters> {
        @Override
        protected JobParameters doInBackground(JobParameters... params) {
            try {
                PlanDownloader planDownloader = new PlanDownloader();
                planDownloader.downloadAndSave(PlanUpdateJobService.this);
            } catch (IOException e) {
                LogUtil.e(e);
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            super.onPostExecute(jobParameters);
            jobFinished(jobParameters, false);
        }

    }
}