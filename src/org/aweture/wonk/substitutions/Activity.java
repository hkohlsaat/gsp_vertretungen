package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.SubstitutionsStore;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
}
