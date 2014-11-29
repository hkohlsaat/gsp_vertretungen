package org.aweture.wonk.storage;

import org.aweture.wonk.storage.WonkContract.SubstitutionEntry;
import org.aweture.wonk.storage.WonkContract.TimetableEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class WonkDatabaseHelper extends SQLiteOpenHelper {
	public static final String TAG = WonkDatabaseHelper.class.getSimpleName();
	
	static final String DATABASE_NAME = "wonk.db";
	static final int DATABASE_VERSION = 3;

	public WonkDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String createSubstitutionTable = "CREATE TABLE " + SubstitutionEntry.TABLE_NAME + " ("
				+ SubstitutionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ SubstitutionEntry.DATE + " TEXT, "
				+ SubstitutionEntry.CLASS + " TEXT, "
				+ SubstitutionEntry.PERIOD_NUMBER + " INTEGER, "
				+ SubstitutionEntry.SUBST_TEACHER + " TEXT, "
				+ SubstitutionEntry.INSTD_TEACHER + " TEXT, "
				+ SubstitutionEntry.INSTD_SUBJECT + " TEXT, "
				+ SubstitutionEntry.KIND + " TEXT, "
				+ SubstitutionEntry.TEXT + " TEXT)";
		String createTimetable = "CREATE TABLE " + TimetableEntry.TABLE_NAME + " ("
				+ TimetableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ TimetableEntry.PERIOD_NUMBER + " INTEGER, "
				+ TimetableEntry.DAY_OF_WEEK + " INTEGER, "
				+ TimetableEntry.TEACHER + " TEXT, "
				+ TimetableEntry.SUBJECT + " TEXT, "
				+ TimetableEntry.ROOM + " TEXT)";
		
		database.execSQL(createSubstitutionTable);
		database.execSQL(createTimetable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		if (oldVersion == 2) {
			database.execSQL("DROP TABLE IF EXISTS substitutes");
			database.execSQL("DROP TABLE IF EXISTS timetable");
			onCreate(database);
		}
	}
}
