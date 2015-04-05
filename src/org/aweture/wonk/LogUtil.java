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
	
	public static void currentMethod() {
		String tag = getTag();
		String message = "in " + tag + "#" + getMethodName();
		Log.d(tag, message);
	}
	
	private static String getTag() {
		String fullClassName = Thread.currentThread().getStackTrace()[4].getClassName();
		int simpleNameStart = fullClassName.lastIndexOf(".") + 1;
		String tag = fullClassName.substring(simpleNameStart);
		return tag;
	}
	
	private static String getMethodName() {
		return Thread.currentThread().getStackTrace()[4].getMethodName();
	}

}
