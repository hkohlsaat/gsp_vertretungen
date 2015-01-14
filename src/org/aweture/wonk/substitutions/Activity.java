package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.background.UpdateService;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.OnSubstitutesSavedListener;
import org.aweture.wonk.storage.SubstitutionsStore;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class Activity extends android.support.v7.app.ActionBarActivity implements OnSubstitutesSavedListener {
	
	private ViewPager viewPager;
	private TabStrip tabStrip;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_substitutions);
		
		viewPager = (ViewPager) findViewById(R.id.pager);
		tabStrip = (TabStrip) findViewById(R.id.tabStrip);
		tabStrip.setViewPager(viewPager);
		
    	PlanLoader loader = new PlanLoader();
    	loader.execute();
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
		SubstitutionsStore dataStore = SubstitutionsStore.getInstance(this);
		dataStore.registerOnSubstitutesSavedListener(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		SubstitutionsStore dataStore = SubstitutionsStore.getInstance(this);
		dataStore.removeOnSubstitutesSavedListener(this);
	}
	
	private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
		
		private int planCount;
		
		public FragmentPagerAdapter(int planCount) {
			super(getSupportFragmentManager());
			this.planCount = planCount;
		}

		@Override
		public Fragment getItem(int index) {
			SubstitutionsFragment fragment = new SubstitutionsFragment();
			fragment.setPlanIndex(index);
			return fragment;
		}

		@Override
		public int getCount() {
			return planCount;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			SubstitutionsStore dataStore = SubstitutionsStore.getInstance(Activity.this);
			Plan plan = dataStore.getCurrentPlans()[position];
			Date date = plan.getDate();
			String relativeWord = date.resolveToRelativeWord();
			if (relativeWord != null) {
				return relativeWord;
			}
			return date.toString();
		}
	}
	
	private class PlanLoader extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			SubstitutionsStore dataStore = SubstitutionsStore.getInstance(Activity.this);
			Plan[] plans = dataStore.getCurrentPlans();
			return plans.length;
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

	@Override
	public void onNewSubstitutesSaved(int planCount) {
		FragmentPagerAdapter adapter = (FragmentPagerAdapter) viewPager.getAdapter();
		adapter.planCount = planCount;
		adapter.notifyDataSetChanged();
	}
}
