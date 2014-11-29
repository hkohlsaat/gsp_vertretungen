package org.aweture.wonk.storage;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WonkProvider extends ContentProvider {

	private static final int SUBSTITUTION = 1;
	private static final int SUBSTITUTION_ID = 2;
	private static final int TIMETABLE = 3;
	private static final int TIMETABLE_ID = 4;
	
	private static final UriMatcher uriMatcher = buildUriMatcher();
	
	private WonkDatabaseHelper dbHelper;
	
	@Override
	public boolean onCreate() {
		dbHelper = new WonkDatabaseHelper(getContext());
		return true;
	}
	
	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = WonkContract.CONTENT_AUTHORITY;
		
		matcher.addURI(authority, WonkContract.PATH_SUBSTITUTIONS, SUBSTITUTION);
		matcher.addURI(authority, WonkContract.PATH_SUBSTITUTIONS + "/#", SUBSTITUTION_ID);
		matcher.addURI(authority, WonkContract.PATH_TIMETABLE, TIMETABLE);
		matcher.addURI(authority, WonkContract.PATH_TIMETABLE + "/#", TIMETABLE_ID);
		return matcher;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case SUBSTITUTION:
				return WonkContract.SubstitutionEntry.CONTENT_TYPE;
			case SUBSTITUTION_ID:
				return WonkContract.SubstitutionEntry.CONTENT_ITEM_TYPE;
			case TIMETABLE:
				return WonkContract.TimetableEntry.CONTENT_TYPE;
			case TIMETABLE_ID:
				return WonkContract.TimetableEntry.CONTENT_ITEM_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		Uri returnUri;
		
		switch (uriMatcher.match(uri)) {
			case SUBSTITUTION: {
				long _id = db.insert(WonkContract.SubstitutionEntry.TABLE_NAME, null, values);
				if (_id > 0) {
					Uri contentUri = WonkContract.SubstitutionEntry.CONTENT_URI;
					returnUri = ContentUris.withAppendedId(contentUri, _id);
				} else {
					throw new android.database.SQLException("Failed to insert row into " + uri);
				}
				break;
			}
			case TIMETABLE: {
				long _id = db.insert(WonkContract.TimetableEntry.TABLE_NAME, null, values);
				if (_id > 0) {
					Uri contentUri = WonkContract.TimetableEntry.CONTENT_URI;
					returnUri = ContentUris.withAppendedId(contentUri, _id);
				} else {
					throw new android.database.SQLException("Failed to insert row into " + uri);
				}
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		
		return returnUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		
		switch (uriMatcher.match(uri)) {
			case SUBSTITUTION: {
				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				queryBuilder.setTables(WonkContract.SubstitutionEntry.TABLE_NAME);
				cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			}
			case SUBSTITUTION_ID: {
				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				queryBuilder.setTables(WonkContract.SubstitutionEntry.TABLE_NAME);
				queryBuilder.appendWhere(WonkContract.SubstitutionEntry._ID + " = " + ContentUris.parseId(uri));
				cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			}
			case TIMETABLE: {
				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				queryBuilder.setTables(WonkContract.TimetableEntry.TABLE_NAME);
				cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			}
			case TIMETABLE_ID: {
				SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
				queryBuilder.setTables(WonkContract.TimetableEntry.TABLE_NAME);
				queryBuilder.appendWhere(WonkContract.TimetableEntry._ID + " = " + ContentUris.parseId(uri));
				cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		int rowsUpdated;
		
		switch (uriMatcher.match(uri)) {
			case SUBSTITUTION: {
				rowsUpdated = database.update(WonkContract.SubstitutionEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			}
			case TIMETABLE: {
				rowsUpdated = database.update(WonkContract.TimetableEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		
		if (rowsUpdated != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowsUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsDeleted;
		
		switch (uriMatcher.match(uri)) {
			case SUBSTITUTION: {
				rowsDeleted = db.delete(WonkContract.SubstitutionEntry.TABLE_NAME, selection, selectionArgs);
				break;
			}
			case TIMETABLE: {
				rowsDeleted = db.delete(WonkContract.TimetableEntry.TABLE_NAME, selection, selectionArgs);
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		
		if (selection == null || rowsDeleted != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowsDeleted;
	}
}
