package org.aweture.wonk;

import android.util.Log;

public class LogUtil {
	
	public static void d(String message) {
		Log.d(getTag(), message);
	}
	
	public static void w(String message) {
		Log.w(getTag(), message);
	}
	
	public static void e(Throwable e) {
		String stackTraceString = Log.getStackTraceString(e);
		Log.e(getTag(), stackTraceString);
	}
	
	private static String getTag() {
		String fileName = Thread.currentThread().getStackTrace()[4].getFileName();
		String tag = fileName.replace(".java", "");
		return tag;
	}

}
