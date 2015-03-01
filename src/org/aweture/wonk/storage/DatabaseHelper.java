package org.aweture.wonk.storage;

import org.aweture.wonk.storage.DataContract.TableEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


class DatabaseHelper extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "wonk.db";
	private static final int DATABASE_VERSION = 3;

	public DatabaseHelper(Context context, CursorFactory factory) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String creationSQL = "CREATE TABLE " + TableEntry.TABLE_NAME + " (" +
				TableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				TableEntry.COLUMN_DATE_NAME + " " + TableEntry.COLUMN_DATE_TYPE + ", " +
				TableEntry.COLUMN_CREATED_NAME + " " + TableEntry.COLUMN_CREATED_TYPE + ", " +
				TableEntry.COLUMN_QUERIED_NAME + " " + TableEntry.COLUMN_QUERIED_TYPE + ", " +
				TableEntry.COLUMN_NAME_NAME + " " + TableEntry.COLUMN_NAME_TYPE + ")";
		db.execSQL(creationSQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 3) {
			dropPreVersion3Tables(db);
			onCreate(db);
		} else {
			throwBecauseOldVersionNotHandled(oldVersion);
		}
	}
	
	private void dropPreVersion3Tables(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS substitutes");
		db.execSQL("DROP TABLE IF EXISTS timetable");
	}
	
	private void throwBecauseOldVersionNotHandled(int oldVersion) {
		String message = "Old version " + oldVersion + " is not handled."; 
		RuntimeException exception = new RuntimeException(message);
		throw exception;
	}
}