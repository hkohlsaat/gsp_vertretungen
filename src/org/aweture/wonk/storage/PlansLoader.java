package org.aweture.wonk.storage;

import java.util.List;

import org.aweture.wonk.models.Plan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

public class PlansLoader extends AsyncTaskLoader<List<Plan>> {

	private BroadcastReceiver receiver = new NewPlansBroadcastReceiver();
	
	public PlansLoader(Context context) {
		super(context);
	}
	
	@Override
	protected void onStartLoading() {
		forceLoad();
		
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
		IntentFilter filter = new IntentFilter(DataStore.NEW_PLANS_ACTION);
		manager.registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onStopLoading() {
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
		manager.unregisterReceiver(receiver);
	}
	
	@Override
	public List<Plan> loadInBackground() {
		DataStore dataStore = DataStore.getInstance(getContext());
		return dataStore.getCurrentPlans();
	}
	
	private class NewPlansBroadcastReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			forceLoad();
		}
	}

}
