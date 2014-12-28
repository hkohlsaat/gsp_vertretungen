package org.aweture.wonk.substitutions;

import org.aweture.wonk.R;
import org.aweture.wonk.storage.WonkContract;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;

public class Activity extends android.support.v7.app.ActionBarActivity {
	
	private static final int LOADER_ID = 1;
	
	ViewPager viewPager;
	TabStrip tabStrip;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_substitutions);
		
		viewPager = (ViewPager) findViewById(R.id.pager);
		tabStrip = (TabStrip) findViewById(R.id.tabStrip);
		tabStrip.setViewPager(viewPager);
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		LoaderManager loaderManager = getSupportLoaderManager();
		loaderManager.initLoader(LOADER_ID, null, new CursorLoaderCallbacks());
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		LoaderManager loaderManager = getSupportLoaderManager();
		loaderManager.destroyLoader(LOADER_ID);
	}
	
	private class CursorLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			Uri queryUri = WonkContract.SubstitutionEntry.CONTENT_URI;
			String[] projection = new String[]{WonkContract.SubstitutionEntry.DATE + " as _id", WonkContract.SubstitutionEntry.DATE};
			ContentResolver cr = getContentResolver();
			return new CursorLoader(Activity.this, queryUri, projection, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
			if (c != null) {
				Fragment[] fragments = new Fragment[c.getCount()];
				c.moveToFirst();
				for (int i = 0; c.moveToNext(); i++) {
					fragments[i] = new SubstitutionsFragment();
				}
				FragmentPagerAdapter fpa = new FragmentPagerAdapter(getSupportFragmentManager(), fragments);
				viewPager.setAdapter(fpa);
				tabStrip.setTabsFromPagerAdapter(fpa);
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
		}
	}
	
	private class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
		
		private Fragment[] fragments;
		
		public FragmentPagerAdapter(FragmentManager fm, Fragment[] fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragments[arg0];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return "Tab " + position;
		}
		
	}
}
