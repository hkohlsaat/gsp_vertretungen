package org.aweture.wonk.log;

import android.util.Log;

import static org.aweture.wonk.Application.IN_DEBUG_MODE;

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
