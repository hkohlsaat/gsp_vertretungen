package org.aweture.wonk.background;

import java.util.List;

import org.aweture.wonk.Application;
import org.aweture.wonk.R;
import org.aweture.wonk.log.LogUtil;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;

public class UpdateScheduler {
	
	private Context context;
	
	public UpdateScheduler(Context context) {
		this.context = context;
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void schedule() {
		if (Application.lollipopOrAbove()) {
			ComponentName updateJopService = new ComponentName(context, UpdateJobService.class);
			JobInfo jobInfo = new JobInfo.Builder(R.id.update_scheduler_job, updateJopService)
			        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
			        .setPeriodic(1000 * 60 * 60 * 5)
			        .setPersisted(true)
			        .build();
			JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
			int result = scheduler.schedule(jobInfo);
			if (result == JobScheduler.RESULT_SUCCESS) {
				LogUtil.logToDB(context, "Update scheduled with JobScheduler");
			} else {
				LogUtil.logToDB(context, "Failed to schedule update with JobScheduler");
			}
		} else {
			// Setup values specifying update behaviour.
			int alarmType = AlarmManager.RTC_WAKEUP;
			long interval = 5 * AlarmManager.INTERVAL_HOUR;
			long delay = System.currentTimeMillis() + interval;
			
			// Create a PendingIntent
			Intent intentToFire = new Intent(context, UpdateService.class);
			PendingIntent alarmIntent = PendingIntent.getService(context, 0, intentToFire, 0);
			
			// Register alarm
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.setInexactRepeating(alarmType, delay, interval, alarmIntent);
			LogUtil.logToDB(context, "Update scheduled with AlarmManager");
		}
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void unschedule() {
		if (isScheduledViaAlarmManager()) {
			PendingIntent alarmIntent = getPendingIntent();
			if (alarmIntent != null) {
				AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarmManager.cancel(alarmIntent);
			}
		}
		if (Application.lollipopOrAbove() && isScheduledViaJobScheduler()) {
			JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
			jobScheduler.cancel(R.id.update_scheduler_job);
		}
	}
	
	public void updateNow() {
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				new UpdateProcedure(context);
				return null;
			}
		}.execute();
	}
	
	public boolean isScheduled() {
		boolean isScheduled = isScheduledViaAlarmManager();
		if (Application.lollipopOrAbove()) {
			isScheduled = isScheduled || isScheduledViaJobScheduler();
		}
		return isScheduled;
	}
	
	private boolean isScheduledViaAlarmManager() {
		PendingIntent intent = getPendingIntent();
		return intent != null;
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private boolean isScheduledViaJobScheduler() {
		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		List<JobInfo> jobInfos = jobScheduler.getAllPendingJobs();
		for (JobInfo jobInfo : jobInfos) {
			if (jobInfo.getId() == R.id.update_scheduler_job) {
				return true;
			}
		}
		return false;
	}
	
	private PendingIntent getPendingIntent() {
		Intent updateIntent = new Intent(context, UpdateService.class);
		int noCreateFlag = PendingIntent.FLAG_NO_CREATE;
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, updateIntent, noCreateFlag);
		return pendingIntent;
	}
}
