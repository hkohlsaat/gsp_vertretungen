package org.aweture.wonk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SimpleData {

	private static final String KEY_USERDATA_INSERTED = "userdata_inserted";
	private static final String KEY_USERNAME = "username";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_IS_STUDENT = "is_student";
	private static final String KEY_FILTER = "filter";
	
	
	private static final boolean STD_VALUE_IS_STUDENT = true;
	
	private static SimpleData singletonInstance;
	
	private SharedPreferences sharedPreferences;
	
	private SimpleData(Context context) {
		String name = this.getClass().getName();
		sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}
	
	public static SimpleData getInstance(Context context) {
		if (singletonInstance == null) {
			singletonInstance = new SimpleData(context);
		}
		return singletonInstance;
	}

	public boolean isUserdataInserted(boolean defaultBoolean) {
		return sharedPreferences.getBoolean(KEY_USERDATA_INSERTED, defaultBoolean);
	}
	public void setUserdataInserted() {
		setUserdataInserted(true);
	}
	public void setUserdataInserted(boolean isInserted) {
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(KEY_USERDATA_INSERTED, isInserted);
		editor.apply();
	}
	
	public String getUsername(String defaultString) {
		return sharedPreferences.getString(KEY_USERNAME, defaultString);
	}
	public void setUsername(String newUsername) {
		Editor editor = sharedPreferences.edit();
		editor.putString(KEY_USERNAME, newUsername).apply();
	}
	
	public String getPassword(String defaultString) {
		return sharedPreferences.getString(KEY_PASSWORD, defaultString);
	}
	public void setPassword(String newPassword) {
		Editor editor = sharedPreferences.edit();
		editor.putString(KEY_PASSWORD, newPassword).apply();
	}
	
	public boolean isStudent() {
		return sharedPreferences.getBoolean(KEY_IS_STUDENT, STD_VALUE_IS_STUDENT);
	}
	public void setWhetherStudent(boolean isStudent) {
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(KEY_IS_STUDENT, isStudent).apply();
	}
	
	public boolean hasFilter() {
		String filter = getFilter("");
		return !filter.isEmpty();
	}
	public String getFilter(String defaultFilter) {
		return sharedPreferences.getString(KEY_FILTER, "");
	}
	public void setFilter(String filter) {
		Editor editor = sharedPreferences.edit();
		editor.putString(KEY_FILTER, filter).apply();
	}
}
