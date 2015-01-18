package org.aweture.wonk.background;

import java.io.IOException;

import org.aweture.wonk.Application;
import org.aweture.wonk.internet.IServManager;
import org.aweture.wonk.internet.IServManager.LoginResult;
import org.aweture.wonk.internet.IServManager21;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.DownloadInformationIntent;
import org.aweture.wonk.storage.DownloadInformationIntent.DownloadStates;
import org.aweture.wonk.storage.SimpleData;
import org.aweture.wonk.storage.SubstitutionsStore;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class UpdateService extends IntentService {
	private static final String LOG_TAG = UpdateService.class.getSimpleName();

	private static final long DEFAULT_DELAY_NORMAL = 5 * AlarmManager.INTERVAL_HOUR;
	private static final long DEFAULT_DELAY_RETRY = AlarmManager.INTERVAL_HOUR;
	
	public static final String EXTRA_NORMAL_INTERVAL = "normal_interval";

	public UpdateService() {
		super(LOG_TAG);
	}
	
	public static void startToUpdate(Context context) {
		Intent intent = new Intent(context, UpdateService.class);
		context.startService(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Application application = (Application) getApplication();
		
		boolean isNotFiredByAlarm = !intent.hasExtra(EXTRA_NORMAL_INTERVAL);
		boolean isFiredWithNormalInterval = intent.getBooleanExtra(EXTRA_NORMAL_INTERVAL, false);
		
		if (application.hasConnectivity()) {
			
			IServManager iServManager = getIServManager();
			LoginResult loginResult = LoginResult.Success;
			if (!iServManager.isLoggedIn()) {
				loginResult = iServManager.logIn();
			}
			
			if (loginResult == LoginResult.Success) {
				
				boolean nextUpdateNormally = update(iServManager);
				// Schedule the next updates if the wished interval is not the currently set one.
				if (isNotFiredByAlarm || (isFiredWithNormalInterval ^ nextUpdateNormally)) {
					scheduleNextUpdate(nextUpdateNormally);
				}
				
			} else if (loginResult == LoginResult.NetworkFail) {
				
				// Schedule the next updates if the wished interval is not the currently set one.
				if (isNotFiredByAlarm || isFiredWithNormalInterval) {
					scheduleNextUpdate(false);
				}
				
			} else if (loginResult == LoginResult.WrongData) {
				
				SimpleData data = SimpleData.getInstance(getApplicationContext());
				data.setUserdataInserted(false);
				unscheduleUpdates();
				
			}
		} else {
			
			// Schedule the next updates if the wished interval is not the currently set one.
			if (isNotFiredByAlarm || isFiredWithNormalInterval) {
				scheduleNextUpdate(false);
			}
		}
	}
	
	private IServManager getIServManager() {
		SimpleData data = SimpleData.getInstance(getApplicationContext());
		String username = data.getUsername("");
		String password = data.getPassword("");
		return new IServManager21(username, password);
	}
	
	private boolean update(IServManager iServManager) {
		// Inform about update start.
		reportUpdateStart();
		try {
			String plan = iServManager.downloadSubstitutionPlan();
			// Inform about update completeness.
			reportUpdateComplete();
			
			saveSubstitutionsPlan(plan);
		} catch (IOException e) {
			Log.e(LOG_TAG, Log.getStackTraceString(e));
			// This catch-clause will only get executed when the download procedure
			// failed. Inform about update abortion.
			reportUpdateAborted();
			return false;
		} catch (Exception e) {
			Log.e(LOG_TAG, Log.getStackTraceString(e));
			return false;
		}
		return true;
	}
	
	private void reportUpdateStart() {
		DownloadInformationIntent intent = new DownloadInformationIntent();
		intent.setState(DownloadStates.DOWNLOAD_STARTING);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}
	
	private void reportUpdateAborted() {
		DownloadInformationIntent intent = new DownloadInformationIntent();
		intent.setState(DownloadStates.DOWNLOAD_ABORTED);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}
	
	private void reportUpdateComplete() {
		DownloadInformationIntent intent = new DownloadInformationIntent();
		intent.setState(DownloadStates.DOWNLOAD_COMPLETE);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}
	
	private void saveSubstitutionsPlan(String plan) throws Exception {
		IServHtmlUtil iServHtmlTable = new IServHtmlUtil(plan);
		Plan[] plans = iServHtmlTable.toPlans();
		Context context = getApplicationContext();
		SubstitutionsStore substitutionsStore = SubstitutionsStore.getInstance(context);
		substitutionsStore.savePlans(plans);
	}
	
	private void scheduleNextUpdate(boolean normally) {
		// Setup values specifying update behaviour.
		int alarmType = AlarmManager.RTC_WAKEUP;
		long interval = getInterval(normally);
		long delay = System.currentTimeMillis() + interval;
		
		// Create a PendingIntent
		Intent intentToFire = new Intent(this, UpdateService.class);
		intentToFire.putExtra(EXTRA_NORMAL_INTERVAL, normally);
		PendingIntent alarmIntent = PendingIntent.getService(this, 0, intentToFire, 0);
		
		// Register alarm
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setInexactRepeating(alarmType, delay, interval, alarmIntent);
	}
	
	private void unscheduleUpdates() {
		// Create a PendingIntent
		Intent intent = new Intent(this, UpdateService.class);
		PendingIntent alarmIntent = PendingIntent.getService(this, 0, intent, 0);
		
		// Cancel alarm
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.cancel(alarmIntent);
	}
	
	private long getInterval(boolean normally) {
		SimpleData data = SimpleData.getInstance(this);
		if (normally) {
			return data.getNormalUpdateInterval(DEFAULT_DELAY_NORMAL);
		} else {
			return data.getRetryUpdateInterval(DEFAULT_DELAY_RETRY);
		}
	}
}
