package org.aweture.wonk.log;

import org.aweture.wonk.R;
import org.aweture.wonk.background.UpdateScheduler;
import org.aweture.wonk.storage.DataContract.LogColumns;
import org.aweture.wonk.storage.DatabaseHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Activity extends android.app.Activity {
	
	private LinearLayout ll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tmp);
		ll = (LinearLayout) findViewById(R.id.container);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		DatabaseHelper helper = new DatabaseHelper(this);
		SQLiteDatabase database = helper.getReadableDatabase();
		Cursor c = database.query(LogColumns.TABLE_NAME, null, null, null, null, null, null);
		writeCursor(c);
		c.close();
		database.close();
		
		UpdateScheduler scheduler = new UpdateScheduler(this);
		boolean isScheduled = scheduler.isScheduled();
		writeNewLine("\nGeneral info: scheduled = " + Boolean.toString(isScheduled));
	}
	
	private void writeCursor(Cursor c) {
		final int columnIndex = c.getColumnIndex(LogColumns.MESSAGE.name());
		
		while (c.moveToNext()) {
			String info = c.getString(columnIndex);
			writeNewLine(info);
		}
	}
	
	private void writeNewLine(String info) {
		TextView tv = new TextView(this);
		tv.setText(info);
		ll.addView(tv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}
}
