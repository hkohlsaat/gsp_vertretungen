package org.aweture.wonk.storage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DataContract {
	
	private static final String CONTENT_AUTHORITY = "org.aweture.wonk";
	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	
	private static final String PATH_TABLE = "tables";
	private static final String PATH_SUBSTITUTION = "substitutions";
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
	
	public static String dateToString(Calendar calendar) {
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String string = sdf.format(date);
		return string;
	}
	public static String datetimeToString(Calendar calendar) {
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
		String string = sdf.format(date);
		return string;
	}
	public static Calendar dateToCalendar(String dateString) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		Date date = sdf.parse(dateString);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar;
	}
	public static Calendar datetimeToCalendar(String dateString) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
		Date date = sdf.parse(dateString);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar;
	}
	
	public static final class TableEntry implements BaseColumns {
		
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
				appendPath(PATH_TABLE).build();

		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_TABLE;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_TABLE;
		
		public static final String TABLE_NAME = "tables_dir";
		
		public static final String COLUMN_DATE_NAME = "date";
		public static final String COLUMN_DATE_TYPE = "TEXT";
		public static final String COLUMN_CREATED_NAME = "created";
		public static final String COLUMN_CREATED_TYPE = "TEXT";
		public static final String COLUMN_QUERIED_NAME = "queried";
		public static final String COLUMN_QUERIED_TYPE = "TEXT";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_TYPE = "TEXT";
		
		public static Uri appendId(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}
	
	public static final class SubstitutionEntry implements BaseColumns {
		
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().
				appendPath(PATH_SUBSTITUTION).build();

		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SUBSTITUTION;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SUBSTITUTION;
		
		// The table name is set dynamically. See tables_dir table for runtime name(s).
		
		public static final String COLUMN_PERIOD_NAME = "period";
		public static final String COLUMN_PERIOD_TYPE = "INTEGER";
		public static final String COLUMN_SUBST_TEACHER_NAME = "subst_teacher";
		public static final String COLUMN_SUBST_TEACHER_TYPE = "TEXT";
		public static final String COLUMN_INSTD_TEACHER_NAME = "instd_teacher";
		public static final String COLUMN_INSTD_TEACHER_TYPE = "TEXT";
		public static final String COLUMN_INSTD_SUBJECT_NAME = "instd_subject";
		public static final String COLUMN_INSTD_SUBJECT_TYPE = "TEXT";
		public static final String COLUMN_KIND_NAME = "kind";
		public static final String COLUMN_KIND_TYPE = "TEXT";
		public static final String COLUMN_TEXT_NAME = "text";
		public static final String COLUMN_TEXT_TYPE = "TEXT";
		public static final String COLUMN_CLASS_NAME = "class";
		public static final String COLUMN_CLASS_TYPE = "TEXT";
		
		public static Uri appendId(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}
}
