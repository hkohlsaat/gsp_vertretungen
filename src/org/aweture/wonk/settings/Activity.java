package org.aweture.wonk.settings;

import org.aweture.wonk.R;
import org.aweture.wonk.storage.SimpleData;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

public class Activity extends android.support.v7.app.ActionBarActivity {
	
	private RadioButton studentButton;
	private RadioButton teacherButton;
	private EditText filterEditText;
	
	private boolean isStudent;
	private String filter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);

		SimpleData simpleData = SimpleData.getInstance(this);
		isStudent = simpleData.isStudent();
		filter = simpleData.getFilter("");
		
		setUpRadioButtons();
		setUpFilterEditText();
	}
	
	@Override
	protected void onPause() {
		super.onStop();
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
		SimpleData simpleData = SimpleData.getInstance(this);
		simpleData.setWhetherStudent(isStudent);
		hintFilter();
	}
	
	private void saveFilter() {
		String filter = filterEditText.getText().toString();
		SimpleData simpleData = SimpleData.getInstance(this);
		simpleData.setFilter(filter);
	}

}
