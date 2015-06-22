package org.aweture.wonk.substitutions;

import java.util.List;

import org.aweture.wonk.Application;
import org.aweture.wonk.LicensesDialogFragment;
import org.aweture.wonk.R;
import org.aweture.wonk.background.UpdateProcedure;
import org.aweture.wonk.background.UpdateScheduler;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.storage.PlansLoader;
import org.aweture.wonk.storage.SimpleData;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
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
	
	@SuppressLint("NewApi")
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
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				tabStrip.setElevation(5);

			LoaderManager manager = getSupportLoaderManager();
			manager.initLoader(R.id.substitutions_Activty_PlansLoader, null, adapter);
		}
	}
	
	private boolean shouldDisplayLanding() {
		SimpleData data = new SimpleData(this);
		return !data.isUserdataInserted(false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    if (Application.IN_DEBUG_MODE) {
	    	inflater.inflate(R.menu.substitutes_debug_mode, menu);
	    } else {
	    	inflater.inflate(R.menu.substitutes, menu);
	    }
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_update:
			UpdateScheduler scheduler = new UpdateScheduler(this);
			scheduler.updateNow();
			return true;
		case R.id.action_show_licenses:
			LicensesDialogFragment fragment = new LicensesDialogFragment();
			fragment.show(getFragmentManager(), "LicensesDialog");
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, org.aweture.wonk.settings.Activity.class));
			return true;
		case R.id.action_see_queries:
			startActivity(new Intent(this, org.aweture.wonk.log.Activity.class));
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
		
		private int count = -1;
		private List<Plan> plans;
		
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
			Plan plan = plans.get(position);
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
				plans = data;
				notifyDataSetChanged();
			}
			if (count == 0 && !UpdateProcedure.isUpdating()) {
				findViewById(R.id.progressBar).setVisibility(View.GONE);
				findViewById(R.id.progressPlaceholder).setVisibility(View.VISIBLE);
				findViewById(R.id.noData).setVisibility(View.VISIBLE);
			} else if (count != 0) {
				findViewById(R.id.progressPlaceholder).setVisibility(View.GONE);
			}
			
			tabStrip.setViewPager(viewPager);
		}

		@Override
		public void onLoaderReset(Loader<List<Plan>> loader) {
			count = -1;
			notifyDataSetChanged();
			
			findViewById(R.id.progressPlaceholder).setVisibility(View.VISIBLE);
			findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
		}
	}
}
