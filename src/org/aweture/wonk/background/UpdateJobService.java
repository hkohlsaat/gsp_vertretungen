package org.aweture.wonk.background;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class UpdateJobService extends JobService {
	
	private AsyncUpdateHandler updateHandler;

	@Override
	public boolean onStartJob(JobParameters params) {
		updateHandler = new AsyncUpdateHandler();
		updateHandler.execute(params);
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		return true;
	}
	
	private class AsyncUpdateHandler extends AsyncTask<JobParameters, Void, JobParameters> {
		
		private UpdateProcedure updateProcedure;
		
		public AsyncUpdateHandler() {
			updateProcedure = new UpdateProcedure(getApplicationContext());
		}
		
		@Override
		protected JobParameters doInBackground(JobParameters... params) {
			updateProcedure.run();
			return params[0];
		}
		
		@Override
		protected void onPostExecute(JobParameters jobParameters) {
			super.onPostExecute(jobParameters);
			jobFinished(jobParameters, false);
		}
	}

}