package org.aweture.wonk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SimpleData {

	private static final String KEY_USERDATA_INSERTED = "userdata_inserted";
	private static final String KEY_USERNAME = "username";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_UPDATE_INTERVAL = "update_interval";
	private static final String KEY_UPDATE_INTERVAL_RETRY = "retry_update_interval";
	private static final String KEY_SUBSTITUTIONS_PLAN_CREATION = "substitutions_plan_creation";
	
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
		editor.putBoolean(KEY_USERDATA_INSERTED, isInserted).apply();
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
	
	public long getNormalUpdateInterval(long defaultLong) {
		return sharedPreferences.getLong(KEY_UPDATE_INTERVAL, defaultLong);
	}
	public void setNormalUpdateDelay(long delay) {
		Editor editor = sharedPreferences.edit();
		editor.putLong(KEY_UPDATE_INTERVAL, delay).apply();
	}
	
	public long getRetryUpdateInterval(long defaultLong) {
		return sharedPreferences.getLong(KEY_UPDATE_INTERVAL_RETRY, defaultLong);
	}
	public void setRetryUpdateDelay(long delay) {
		Editor editor = sharedPreferences.edit();
		editor.putLong(KEY_UPDATE_INTERVAL_RETRY, delay).apply();
	}
	
	public String getTimeOfSubstitutionsPlanCreation(String defaultString) {
		return sharedPreferences.getString(KEY_SUBSTITUTIONS_PLAN_CREATION, defaultString);
	}
	public void setTimeOfSubstitutionsPlanCreation(String creationTime) {
		Editor editor = sharedPreferences.edit();
		editor.putString(KEY_SUBSTITUTIONS_PLAN_CREATION, creationTime).apply();
	}
}
