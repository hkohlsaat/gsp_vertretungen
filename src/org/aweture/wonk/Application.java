package org.aweture.wonk;

import org.aweture.wonk.models.Subjects;
import org.aweture.wonk.models.Teachers;
import org.aweture.wonk.storage.SimpleData;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;

public class Application extends android.app.Application {
	
	public final static boolean IN_DEBUG_MODE = true;
	
	public static boolean lollipopOrAbove() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}
	
	public static boolean hasConnectivity(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void[] params) {
				initTeachers();
				initSubjects();
				return null;
			};
		}.execute();
	}

	private void initTeachers() {
		SimpleData simpleData = new SimpleData(this);
		int currentVersion = simpleData.getTeachersVersion(-1);
		if (currentVersion < Teachers.VERSION) {
			Teachers teachers = new Teachers(this);
			teachers.rewriteTable();
			simpleData.setTeachersVersion(Teachers.VERSION);
		}
	}
	private void initSubjects() {
		SimpleData simpleData = new SimpleData(this);
		int currentVersion = simpleData.getSubjectsVersion(-1);
		if (currentVersion < Subjects.VERSION) {
			Subjects subjects = new Subjects(this);
			subjects.rewriteTable();
			simpleData.setSubjectsVersion(Subjects.VERSION);
		}
	}
}
