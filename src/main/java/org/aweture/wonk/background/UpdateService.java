package org.aweture.wonk.background;

import org.aweture.wonk.Application;
import org.aweture.wonk.internet.IServManager;
import org.aweture.wonk.internet.IServManager.LoginResult;
import org.aweture.wonk.internet.IServManager21;
import org.aweture.wonk.models.SubstitutionsPlan;
import org.aweture.wonk.storage.SimpleData;
import org.aweture.wonk.storage.WonkContract;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateService extends IntentService {
	private static final String TAG = UpdateService.class.getSimpleName();

	private static final long DEFAULT_DELAY_NORMAL = 5 * AlarmManager.INTERVAL_HOUR;
	private static final long DEFAULT_DELAY_RETRY = AlarmManager.INTERVAL_HOUR;
	
	public static final String EXTRA_NORMAL_INTERVAL = "normal_interval";

	public UpdateService() {
		super(TAG);
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
		try {
			String plan = iServManager.downloadSubstitutionPlan();
			getContentResolver().delete(WonkContract.SubstitutionEntry.CONTENT_URI, null, null);
			saveSubstitutionsPlan(plan);
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			return false;
		}
		return true;
	}
	
	private void saveSubstitutionsPlan(String plan) throws Exception {
		SubstitutionsPlan substitutionsPlan = new SubstitutionsPlan(plan);
		substitutionsPlan.save(getApplicationContext());
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
