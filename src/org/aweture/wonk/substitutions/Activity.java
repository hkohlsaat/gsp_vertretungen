package org.aweture.wonk.substitutions;

import java.util.List;

import org.aweture.wonk.R;
import org.aweture.wonk.background.UpdateScheduler;
import org.aweture.wonk.background.UpdateService;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.DataStore;
import org.aweture.wonk.storage.PlansLoader;
import org.aweture.wonk.storage.SimpleData;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class Activity extends android.support.v7.app.ActionBarActivity {
	
	private SubstitutionsFragmentAdapter adapter;
	private ViewPager viewPager;
	private TabStrip tabStrip;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (shouldDisplayLanding()) {
			Intent intent = new Intent(this, org.aweture.wonk.landing.Activity.class);
			startActivity(intent);
			finish();
		} else {
			setContentView(R.layout.activity_substitutions);
			
			viewPager = (ViewPager) findViewById(R.id.pager);
			tabStrip = (TabStrip) findViewById(R.id.tabStrip);
			adapter = new SubstitutionsFragmentAdapter();
			
			viewPager.setAdapter(adapter);

			LoaderManager manager = getSupportLoaderManager();
			manager.initLoader(R.id.substitutions_Activty_PlansLoader, null, adapter);
		}
	}
	
	private boolean shouldDisplayLanding() {
		SimpleData data = SimpleData.getInstance(this);
		return !data.isUserdataInserted(false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.substitutes, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_update:
			startService(new Intent(this, UpdateService.class));
			UpdateScheduler scheduler = new UpdateScheduler(this);
			scheduler.schedule();
			return true;
		case R.id.action_see_queries:
			startActivity(new Intent(this, org.aweture.wonk.tmp.Activity.class));
			return true;

		default:
			return false;
		}
	}
	
	@Override
	protected void onDestroy() {
		LoaderManager manager = getSupportLoaderManager();
		manager.destroyLoader(R.id.substitutions_Activty_PlansLoader);
		super.onDestroy();
	}
	
	private class SubstitutionsFragmentAdapter extends FragmentPagerAdapter implements LoaderCallbacks<List<Plan>> {
		
		private int count = 0;
		
		public SubstitutionsFragmentAdapter() {
			super(getSupportFragmentManager());
		}

		@Override
		public Fragment getItem(int index) {
			SubstitutionsFragment fragment = new SubstitutionsFragment();
			fragment.setPlanIndex(index);
			return fragment;
		}
		
		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public int getCount() {
			return count;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			DataStore dataStore = DataStore.getInstance(Activity.this);
			Plan plan = dataStore.getCurrentPlans().get(position);
			Date date = plan.getDate();
			String relativeWord = date.resolveToRelativeWord();
			if (relativeWord != null) {
				return relativeWord;
			}
			return date.toDateString();
		}

		@Override
		public Loader<List<Plan>> onCreateLoader(int id, Bundle args) {
			return new PlansLoader(Activity.this);
		}

		@Override
		public void onLoadFinished(Loader<List<Plan>> loader, List<Plan> data) {
			if (count != data.size()) {
				count = data.size();
				notifyDataSetChanged();

				View loadingPlaceholder = findViewById(R.id.progressPlaceholder);
				loadingPlaceholder.setVisibility(View.GONE);
			}
			
			tabStrip.setViewPager(viewPager);
		}

		@Override
		public void onLoaderReset(Loader<List<Plan>> loader) {
			count = 0;
			notifyDataSetChanged();
			
			View loadingPlaceholder = findViewById(R.id.progressPlaceholder);
			loadingPlaceholder.setVisibility(View.VISIBLE);
		}
	}
}
