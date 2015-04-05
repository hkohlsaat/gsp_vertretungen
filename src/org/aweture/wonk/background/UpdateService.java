package org.aweture.wonk.background;

import java.util.List;

import org.aweture.wonk.Application;
import org.aweture.wonk.LogUtil;
import org.aweture.wonk.internet.IServManager;
import org.aweture.wonk.internet.IServManager.LoginResult;
import org.aweture.wonk.internet.IServManager21;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.DataContract;
import org.aweture.wonk.storage.DataStore;
import org.aweture.wonk.storage.DatabaseHelper;
import org.aweture.wonk.storage.SimpleData;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class UpdateService extends IntentService {

	public UpdateService() {
		super(UpdateService.class.getSimpleName());
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
		try {
			String plan = iServManager.downloadSubstitutionPlan();
			saveSubstitutionsPlan(plan);
			return true;
		} catch (Exception e) {
			LogUtil.e(e);
			return false;
		}
	}
	
	private void saveSubstitutionsPlan(String plan) throws Exception {
		IServHtmlUtil iServHtmlTable = new IServHtmlUtil(plan);
		List<Plan> plans = iServHtmlTable.toPlans();
		Context context = getApplicationContext();
		DataStore dataStore = DataStore.getInstance(context);
		dataStore.savePlans(plans);
		
		// WHEN DELETING THE TEST CODE HEREAFTER:
		// MAKE DATABASEHELPER CLASS DEFAULT AGAIN !!!!!!
		DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
		SQLiteDatabase database = helper.getWritableDatabase();
		ContentValues v = new ContentValues();
		v.put(DataContract.TableEntry.COLUMN_QUERIED_NAME, new Date().toDateTimeString());
		database.insert("queries", null, v);
		database.close();
	}
}
