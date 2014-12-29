package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.SubstitutionsStore;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class Activity extends android.support.v7.app.ActionBarActivity {
	
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
	
	private void dataLoaded(Plan[] plans) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentPagerAdapter adapter = new FragmentPagerAdapter(fm, plans);
		viewPager.setAdapter(adapter);
		tabStrip.setTabsFromPagerAdapter(adapter);
		
		View loadingPlaceholder = findViewById(R.id.progressPlaceholder);
		LinearLayout container = (LinearLayout) findViewById(R.id.container);
		container.removeView(loadingPlaceholder);
	}
	
	private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
		
		private SubstitutionsFragment[] fragments;
		
		public FragmentPagerAdapter(FragmentManager fm, Plan[] plans) {
			super(fm);
			fragments = new SubstitutionsFragment[plans.length];
			Log.d(this.getClass().getSimpleName(), plans.length + " plans");
			for (int i = 0; i < plans.length; i++) {
				Plan plan = plans[i];
				SubstitutionsFragment fragment = new SubstitutionsFragment();
				fragment.setPlan(plan);
				fragments[i] = fragment;
			}
		}

		@Override
		public Fragment getItem(int index) {
			return fragments[index];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			SubstitutionsFragment fragment = fragments[position];
			Plan plan = fragment.getPlan();
			String date = plan.getDate();
			return date;
		}
		
	}
	
	private class PlanLoader extends AsyncTask<Void, Void, Plan[]> {

		@Override
		protected Plan[] doInBackground(Void... params) {
			SubstitutionsStore dataStore = SubstitutionsStore.getInstance(Activity.this);
			Plan[] plans = dataStore.getCurrentPlans();
			return plans;
		}
		
		@Override
		protected void onPostExecute(Plan[] plans) {
			dataLoaded(plans);
		}
	}
}
