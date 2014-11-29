package org.aweture.wonk.storage;

import android.net.Uri;
import android.provider.BaseColumns;

public class WonkContract {
	
	public static final String CONTENT_AUTHORITY = "org.aweture.android.wonk";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	public static final String PATH_SUBSTITUTIONS = "substitutions";
	public static final String PATH_TIMETABLE = "timetable";
	
	public static final class TimetableEntry implements BaseColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_TIMETABLE).build();
		
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_TIMETABLE;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_TIMETABLE;
		
		public static final String TABLE_NAME = "timetable";
		
		public static final String PERIOD_NUMBER = "period_number";
		public static final String DAY_OF_WEEK = "day";
		public static final String TEACHER = "teacher";
		public static final String SUBJECT = "subject";
		public static final String ROOM = "room";
	}
	
	public static final class SubstitutionEntry implements BaseColumns {
		public static final Uri CONTENT_URI =
				BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBSTITUTIONS).build();
		
		public static final String CONTENT_TYPE =
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SUBSTITUTIONS;
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SUBSTITUTIONS;
		
		public static final String TABLE_NAME = "substitutions";
		
		public static final String DATE = "date";
		public static final String CLASS = "class";
		public static final String PERIOD_NUMBER = "period_number";
		public static final String SUBST_TEACHER = "subst_teacher";
		public static final String INSTD_TEACHER = "instd_teacher";
		public static final String INSTD_SUBJECT = "instd_subject";
		public static final String KIND = "kind";
		public static final String TEXT = "room";
	}
}
