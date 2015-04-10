package org.aweture.wonk.storage;

import org.aweture.wonk.storage.DataContract.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "wonk.db";
	private static final int DATABASE_VERSION = 3;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		CreateQuery createTables = new CreateQuery(TableColumns.TABLE_NAME);
		for (TableColumns column : TableColumns.values()) {
			createTables.addColumn(column.name(), column.type());
		}
		db.execSQL(createTables.toString());
		
		CreateQuery createLog = new CreateQuery(LogColumns.TABLE_NAME);
		for (LogColumns column : LogColumns.values()) {
			createLog.addColumn(column.name(), column.type());
		}
		db.execSQL(createLog.toString());
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