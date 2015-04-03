package org.aweture.wonk.storage;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DataContract {
	
	private static final String CONTENT_AUTHORITY = "org.aweture.wonk";
	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	
	private static final String PATH_TABLE = "tables";
	private static final String PATH_SUBSTITUTION = "substitutions";
	
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
		
		// The table name is set dynamically. They are stored in tables_dir table.
		
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
