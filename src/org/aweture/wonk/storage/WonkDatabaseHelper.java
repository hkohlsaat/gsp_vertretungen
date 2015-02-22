package org.aweture.wonk.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * This class is not longer needed for the functionality of this app.
 * Nevertheless it is contained within the source code to inform all future developers of this app
 * that in nearly ancient times a database was used. To avoid ugly side effects its existance on some
 * devices should be considered. Please get all further information from this classes source code.
 * 
 */
class WonkDatabaseHelper extends SQLiteOpenHelper{
	
	static final String DATABASE_NAME = "wonk.db";
	static final int DATABASE_VERSION = 2;

	static final String SUBSTITUTES_TABLE = "substitutes";
	static final String TIMETABLE = "timetable";

	public WonkDatabaseHelper(Context context, CursorFactory factory) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + SUBSTITUTES_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + TIMETABLE);
	}
}