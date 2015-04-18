package org.aweture.wonk.log;

import static org.aweture.wonk.Application.IN_DEBUG_MODE;

import org.aweture.wonk.storage.DataContract.LogColumns;
import org.aweture.wonk.storage.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LogUtil {
	
	public static void d(String message) {
		if (IN_DEBUG_MODE)
			Log.d(getTag(), message);
	}
	
	public static void w(String message) {
		if (IN_DEBUG_MODE)
			Log.w(getTag(), message);
	}
	
	public static void e(Throwable e) {
		if (IN_DEBUG_MODE) {
			String stackTraceString = Log.getStackTraceString(e);
			Log.e(getTag(), stackTraceString);
		}
	}
	
	public static void currentMethod() {
		if (IN_DEBUG_MODE) {
			String tag = getTag();
			String message = "in " + tag + "#" + getMethodName();
			Log.d(tag, message);
		}	
	}
	
	public static void logToDB(Context context, String message) {
		if (IN_DEBUG_MODE) {
			DatabaseHelper helper = new DatabaseHelper(context);
			SQLiteDatabase database = helper.getWritableDatabase();
			ContentValues v = new ContentValues();
			String tag = getTag();
			v.put(LogColumns.MESSAGE.name(), tag + " |\t" + message);
			database.insert(LogColumns.TABLE_NAME, null, v);
			database.close();
		}
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
