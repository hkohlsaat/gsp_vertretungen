package org.aweture.wonk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SimpleData {

	private static final String KEY_PASSWORD_ENTERED = "password_entered";
	private static final String KEY_IS_STUDENT = "is_student";
	private static final String KEY_FILTER = "filter";

	private static final boolean STD_VALUE_IS_PASSWORD_ENTERED = false;
	private static final boolean STD_VALUE_IS_STUDENT = true;
	
	private SharedPreferences sharedPreferences;
	
	public SimpleData(Context context) {
		String name = this.getClass().getName();
		sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	public boolean isPasswordEntered() {
		return sharedPreferences.getBoolean(KEY_PASSWORD_ENTERED, STD_VALUE_IS_PASSWORD_ENTERED);
	}
	public void setPasswordEntered() {
			Editor editor = sharedPreferences.edit();
			editor.putBoolean(KEY_PASSWORD_ENTERED, true);
			editor.apply();
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
		return sharedPreferences.getString(KEY_FILTER, defaultFilter);
	}
	public void setFilter(String filter) {
		Editor editor = sharedPreferences.edit();
		editor.putString(KEY_FILTER, filter).apply();
	}
}
