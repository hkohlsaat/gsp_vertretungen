package org.aweture.wonk;

import org.aweture.wonk.background.UpdateService;
import org.aweture.wonk.storage.SimpleData;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Application extends android.app.Application {
	
	public boolean hasConnectivity() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	
	
}
