package org.aweture.wonk.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.util.Log;

public class Date {
	private static final String LOG_TAG = Date.class.getSimpleName();
	private static final String DATE_FORMAT = "dd.MM.yyyy";
	
	private SimpleDateFormat sdf;
	private String date;
	
	@SuppressLint("SimpleDateFormat")
	public Date(String date) {
		this.date = date;
		sdf = new SimpleDateFormat(DATE_FORMAT);
	}
	
	@Override
	public String toString() {
		return date;
	}
	
	public String resolveToRelativeWord() {
		try {
			Calendar givenDate = GregorianCalendar.getInstance();
			givenDate.setTime(sdf.parse(date));
			
			Calendar yesterday = GregorianCalendar.getInstance();
			yesterday.add(Calendar.DAY_OF_YEAR, -1);
			
			Calendar today = GregorianCalendar.getInstance();

			Calendar tomorrow = GregorianCalendar.getInstance();
			tomorrow.add(Calendar.DAY_OF_YEAR, 1);

			if (isSameDay(today, givenDate)) {
				return "heute";
			} else if (isSameDay(yesterday, givenDate)) {
				return "gestern";
			} else if (isSameDay(tomorrow, givenDate)) {
				return "morgen";
			}
		} catch (ParseException e) {
			Log.e(LOG_TAG, Log.getStackTraceString(e));
		}
		return null;
	}
	
	private boolean isSameDay(Calendar calendar1, Calendar calendar2) {
		boolean sameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
		boolean sameDayOfYear = calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
		return sameYear && sameDayOfYear;
	}

}
