package org.aweture.wonk.background;

import java.io.IOException;
import java.util.List;

import org.aweture.wonk.Application;
import org.aweture.wonk.internet.IServManager;
import org.aweture.wonk.internet.IServManagerImpl;
import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.DataStore;
import org.aweture.wonk.storage.SimpleData;

import android.content.Context;

public class UpdateProcedure implements Runnable {
	
	private static boolean isUpdating = false;
	
	public static boolean isUpdating() {
		return isUpdating;
	}
	
	private Context context;
	private SimpleData simpleData;
	private IServManager iServManager;
	
	private String downloadedPlan;
	
	public UpdateProcedure(Context context) {
		this.context = context;
		simpleData = new SimpleData(context);
		String username = simpleData.getUsername("");
		String password = simpleData.getPassword("");
		iServManager = new IServManagerImpl(username, password);
	}
	

	@Override
	public void run() {
		if (isUpdating) {
			return;
		} else {
			isUpdating = true;
		}
		
		if (Application.hasConnectivity(context)) {
			switch (iServManager.logIn()) {
			case Success:
				tryUpdate();
				break;
			case WrongData:
				simpleData.setUserdataInserted(false);
				LogUtil.logToDB(context, new Date().toDateTimeString() + "\tLogin fail: wrong data");
				new UpdateScheduler(context).unschedule();
			default:
				LogUtil.logToDB(context, new Date().toDateTimeString() + "\tNetwork fail while login");
				break;
			}
		}
		
		isUpdating = false;
	}
	
	private void tryUpdate() {
		try {
			downloadedPlan = iServManager.downloadSubstitutionPlan();
			LogUtil.logToDB(context, new Date().toDateTimeString() + "\tDowload success");
			saveSubstitutionsPlan();
			LogUtil.logToDB(context, new Date().toDateTimeString() + "\tSaving success");
		} catch (IOException ioe) {
			LogUtil.e(ioe);
			LogUtil.logToDB(context, new Date().toDateTimeString() + "\tDowload fail");
		} catch (Exception e) {
			LogUtil.e(e);
			LogUtil.logToDB(context, new Date().toDateTimeString() + "\tSaving fail");
		}
	}
	
	private void saveSubstitutionsPlan() throws Exception {
		IServHtmlUtil iServHtmlTable = new IServHtmlUtil(downloadedPlan);
		List<Plan> plans = iServHtmlTable.toPlans();
		DataStore dataStore = new DataStore(context);
		dataStore.savePlans(plans);
	}

}
