package org.aweture.wonk.substitutions;

import java.util.List;

import org.aweture.wonk.LogUtil;
import org.aweture.wonk.R;
import org.aweture.wonk.background.UpdateService;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.DataStore;
import org.aweture.wonk.storage.DataStoreFactory;
import org.aweture.wonk.storage.DownloadInformationIntent;
import org.aweture.wonk.storage.SimpleData;
import org.aweture.wonk.storage.DownloadInformationIntent.DownloadStates;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class Activity extends android.support.v7.app.ActionBarActivity {
	
	private BroadcastReceiver dataInfoBroadcastReceiver;
	
	private ViewPager viewPager;
	private TabStrip tabStrip;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SimpleData data = SimpleData.getInstance(this);
		if (!data.isUserdataInserted(false)) {
			Intent intent = new Intent(this, org.aweture.wonk.landing.Activity.class);
			startActivity(intent);
			finish();
		} else {
			setContentView(R.layout.activity_substitutions);
			
			viewPager = (ViewPager) findViewById(R.id.pager);
			tabStrip = (TabStrip) findViewById(R.id.tabStrip);
			tabStrip.setViewPager(viewPager);
			
	    	PlanLoader loader = new PlanLoader();
	    	loader.execute();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.substitutes, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startService(new Intent(this, UpdateService.class));
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		dataInfoBroadcastReceiver = new DataInformationBroadcastReceiver();
		IntentFilter filter = new DownloadInformationIntent.DownloadInformationIntentFilter();
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		manager.registerReceiver(dataInfoBroadcastReceiver, filter);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		manager.unregisterReceiver(dataInfoBroadcastReceiver);
	}
	
	private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
		
		private int planCount;
		
		public FragmentPagerAdapter(int planCount) {
			super(getSupportFragmentManager());
			this.planCount = planCount;
		}

		@Override
		public Fragment getItem(int index) {
			LogUtil.d("new SubstitutionFragment at index " + index);
			SubstitutionsFragment fragment = new SubstitutionsFragment();
			fragment.setPlanIndex(index);
			return fragment;
		}
		
		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}

		@Override
		public int getCount() {
			return planCount;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			DataStore dataStore = DataStoreFactory.getDataStore(Activity.this);
			Plan plan = dataStore.getCurrentPlans().get(position);
			Date date = plan.getDate();
			String relativeWord = date.resolveToRelativeWord();
			if (relativeWord != null) {
				return relativeWord;
			}
			return date.toDateString();
		}
	}
	
	private class PlanLoader extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			DataStore dataStore = DataStoreFactory.getDataStore(Activity.this);
			List<Plan> plans = dataStore.getCurrentPlans();
			return plans.size();
		}
		
		@Override
		protected void onPostExecute(Integer planCount) {
			FragmentPagerAdapter adapter = new FragmentPagerAdapter(planCount);
			viewPager.setAdapter(adapter);
			tabStrip.setTabsFromPagerAdapter(adapter);
			
			View loadingPlaceholder = findViewById(R.id.progressPlaceholder);
			LinearLayout container = (LinearLayout) findViewById(R.id.container);
			container.removeView(loadingPlaceholder);
		}
	}
	
	private class DataInformationBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent i) {
			DownloadInformationIntent intent = (DownloadInformationIntent) i;
			DownloadStates state = intent.getState();
			switch (state) {
			case DOWNLOAD_STARTING:
				// TODO: Start a progress bar.
				break;
			case DOWNLOAD_ABORTED:
				// TODO: Stop things from DOWLOAD_STARTING case.
				break;
			case DOWNLOAD_COMPLETE:
				// TODO: Stop things from DOWLOAD_STARTING case.
				break;
			case NEW_DATA_SAVED:
				DataStore dataStore = DataStoreFactory.getDataStore(Activity.this);
				int planCount = dataStore.getCurrentPlans().size();
				FragmentPagerAdapter adapter = (FragmentPagerAdapter) viewPager.getAdapter();
				adapter.planCount = planCount;
				adapter.notifyDataSetChanged();
				break;
			}
		}
	}
}
