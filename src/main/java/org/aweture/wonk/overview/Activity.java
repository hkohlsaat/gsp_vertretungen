package org.aweture.wonk.overview;

import org.aweture.wonk.R;
import org.aweture.wonk.storage.SimpleData;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Activity extends android.app.Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SimpleData data = SimpleData.getInstance(this);
		if (data.isUserdataInserted(false)) {
			Intent intent = new Intent(this, org.aweture.wonk.landing.Activity.class);
			startActivity(intent);
			finish();
		} else {
			setContentView(R.layout.activity_overview);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	public void showSubstitutions(View view) {
		Toast.makeText(this, "Substitutions", Toast.LENGTH_SHORT).show();
	}
	
	public void showTimetable(View view) {
		Toast.makeText(this, "Timetable", Toast.LENGTH_SHORT).show();
	}
}
