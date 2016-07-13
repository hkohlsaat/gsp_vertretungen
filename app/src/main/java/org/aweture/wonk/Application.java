package org.aweture.wonk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Application extends android.app.Application {
	
	public final static boolean IN_DEBUG_MODE = false;

	public static boolean hasConnectivity(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
}
