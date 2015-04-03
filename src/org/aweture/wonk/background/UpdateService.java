package org.aweture.wonk.background;

import java.io.IOException;

import org.aweture.wonk.Application;
import org.aweture.wonk.internet.IServManager;
import org.aweture.wonk.internet.IServManager.LoginResult;
import org.aweture.wonk.internet.IServManager21;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.DataStoreFactory;
import org.aweture.wonk.storage.DownloadInformationIntent;
import org.aweture.wonk.storage.DownloadInformationIntent.DownloadStates;
import org.aweture.wonk.storage.DataStore;
import org.aweture.wonk.storage.SimpleData;
import org.aweture.wonk.storage.XmlSubstitutionsStore;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class UpdateService extends IntentService {
	private static final String LOG_TAG = UpdateService.class.getSimpleName();

	public UpdateService() {
		super(LOG_TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Application application = (Application) getApplication();
		
		if (application.hasConnectivity()) {
			
			IServManager iServManager = getIServManager();
			LoginResult result = login(iServManager);
			
			switch (result) {
			case Success:
				update(iServManager);
				break;
			case WrongData:
				SimpleData data = SimpleData.getInstance(getApplicationContext());
				data.setUserdataInserted(false);
				UpdateScheduler updateScheduler = new UpdateScheduler(getApplicationContext());
				updateScheduler.unschedule();
				break;
			}
		}
	}
	
	private LoginResult login(IServManager iServManager) {
		if (!iServManager.isLoggedIn()) {
			return iServManager.logIn();
		} else {
			return LoginResult.Success;
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
		DataStore dataStore = DataStoreFactory.getDataStore(context);
		dataStore.savePlans(plans);
	}
}
