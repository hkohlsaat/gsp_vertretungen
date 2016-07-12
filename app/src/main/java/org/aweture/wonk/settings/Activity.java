package org.aweture.wonk.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.aweture.wonk.R;
import org.aweture.wonk.storage.SimpleData;

public class Activity extends android.support.v7.app.AppCompatActivity {

	public static final String STUDENT_TEACHER_MODE_CHANGED = "student_teacher_mode_changed";

	private RadioButton studentButton;
	private RadioButton teacherButton;
	private EditText filterEditText;
	
	private boolean isStudent;
	private String filter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);

		SimpleData simpleData = new SimpleData(this);
		isStudent = simpleData.isStudent();
		filter = simpleData.getFilter("");
		
		setUpRadioButtons();
		setUpFilterEditText();
		setUpVersionNumper();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		saveFilter();
	}
	
	private void setUpRadioButtons() {
		studentButton = (RadioButton) findViewById(R.id.radio_student);
		teacherButton = (RadioButton) findViewById(R.id.radio_teacher);
		
		if (isStudent) {
			studentButton.setChecked(true);
		} else {
			teacherButton.setChecked(true);
		}
	}
	
	private void setUpFilterEditText() {
		filterEditText = (EditText) findViewById(R.id.edit_filter);
		hintFilter();
		setText();
	}
	
	private void setUpVersionNumper() {
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			TextView versionTextView = (TextView) findViewById(R.id.versionTextView);
			versionTextView.setText("Version " + pInfo.versionCode);
		} catch (NameNotFoundException e) {}
	}
	
	private void hintFilter() {
		if (isStudent) {
			filterEditText.setHint(R.string.filter_hint_student);
		} else {
			filterEditText.setHint(R.string.filter_hint_teacher);
		}
	}
	
	private void setText() {
		filterEditText.setText(filter);
	}
	
	public void onRadioButtonClicked(View radioButton) {
		isStudent = studentButton == radioButton;
		SimpleData simpleData = new SimpleData(this);
		simpleData.setWhetherStudent(isStudent);
		hintFilter();
		Intent intent = new Intent(STUDENT_TEACHER_MODE_CHANGED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	private void saveFilter() {
		String filter = filterEditText.getText().toString().trim();
		SimpleData simpleData = new SimpleData(this);
		simpleData.setFilter(filter);
	}

}
