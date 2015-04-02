package org.aweture.wonk.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class UpdateScheduler {
	
	private Context context;
	
	public UpdateScheduler(Context context) {
		this.context = context;
	}
	
	public void schedule() {
		// Setup values specifying update behaviour.
		int alarmType = AlarmManager.RTC_WAKEUP;
		long interval = 5 * AlarmManager.INTERVAL_HOUR;
		long delay = System.currentTimeMillis() + interval;
		
		// Create a PendingIntent
		Intent intentToFire = getUpdateIntent();
		PendingIntent alarmIntent = PendingIntent.getService(context, 0, intentToFire, 0);
		
		// Register alarm
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(alarmType, delay, interval, alarmIntent);
	}
	
	public void unschedule() {
		PendingIntent alarmIntent = getPendingIntent();
		if (alarmIntent != null) {
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(alarmIntent);
		}
	}
	
	public boolean isScheduled() {
		PendingIntent intent = getPendingIntent();
		return intent != null;
	}
	
	private PendingIntent getPendingIntent() {
		Intent updateIntent = getUpdateIntent();
		int noCreateFlag = PendingIntent.FLAG_NO_CREATE;
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, updateIntent, noCreateFlag);
		return pendingIntent;
	}
	
	private Intent getUpdateIntent() {
		return new Intent(context, UpdateService.class);
	}
}
