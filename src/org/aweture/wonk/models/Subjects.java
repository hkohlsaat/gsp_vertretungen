package org.aweture.wonk.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.storage.DataContract;
import org.aweture.wonk.storage.DataContract.SubjectsColumns;
import org.aweture.wonk.storage.DatabaseHelper;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Xml;


public class Subjects {
	
	public static final int VERSION = 0;
	
	private static final String ATTRIBUTE_SHORT= "short";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_CONCURRENTLY_TAUGHT = "concurrentlyTaught";
	
    private Context context;
    private HashMap<String, Subject> prefetchSubjects;
	
	public Subjects(Context context) {
		this.context = context;
	}
	
	public void rewriteTable() {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + DataContract.SubjectsColumns.TABLE_NAME);
		List<String> insertStatements = getInsertStatements();
		for (String statement : insertStatements) {
			db.execSQL(statement);
		}
		db.close();
	}
	
	public void prefetch() {
		prefetchSubjects = new HashMap<String, Subject>();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(SubjectsColumns.TABLE_NAME, null, null, null, null, null, null);
		
		while(cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(SubjectsColumns.NAME.name()));
			String abbreviation = cursor.getString(cursor.getColumnIndex(SubjectsColumns.ABBREVIATION.name()));
			int concurrentlyTaught = cursor.getInt(cursor.getColumnIndex(SubjectsColumns.CONCURRENTLY_TAUGHT.name()));
			
			Subject subject = new Subject();
			subject.setAbbreviation(abbreviation);
			subject.setName(name);
			subject.setConcurrentlyTaught(concurrentlyTaught == 1);
			prefetchSubjects.put(abbreviation, subject);
		}
		
		cursor.close();
		db.close();
	}
	
	public Subject getSubject(String abbreviation) {
		if (prefetchSubjects != null) {
			Subject subject = prefetchSubjects.get(abbreviation);
			if (subject == null) {
				subject = new Subject(abbreviation);
			}
			return subject;
		} else {
			return querySingleSubject(abbreviation);
		}
	}
	
	private Subject querySingleSubject(String abbreviation) {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(SubjectsColumns.TABLE_NAME, null, SubjectsColumns.ABBREVIATION
				+ " = \"" + abbreviation + "\"", null, null, null, null);
		
		Subject subject = null;
		if (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(SubjectsColumns.NAME.name()));
			int concurrentlyTaught = cursor.getInt(cursor.getColumnIndex(SubjectsColumns.CONCURRENTLY_TAUGHT.name()));
			
			subject = new Subject();
			subject.setAbbreviation(abbreviation);
			subject.setName(name);
			subject.setConcurrentlyTaught(concurrentlyTaught == 1);
		} else {
			subject = new Subject(abbreviation);
		}
		
		cursor.close();
		db.close();
		return subject;
	}
	
	
	public List<String> getInsertStatements() {
		List<String> sqlStatements = new ArrayList<String>();
		InputStream inputStream = null;
		try {
			inputStream = context.getAssets().open("subjects.xml");
			
	        XmlPullParser parser = Xml.newPullParser();
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	        parser.setInput(inputStream, null);
	        parser.nextTag();
	        
	        while (parser.nextTag() == XmlPullParser.START_TAG){
	        	String abbreviation = parser.getAttributeValue(null, ATTRIBUTE_SHORT);
	    		String name = parser.getAttributeValue(null, ATTRIBUTE_NAME);
	    		String concurrentlyTaughtString = parser.getAttributeValue(null, ATTRIBUTE_CONCURRENTLY_TAUGHT);
	    		
	    		sqlStatements.add("INSERT INTO " + DataContract.SubjectsColumns.TABLE_NAME + " ("
	    				+ SubjectsColumns.ABBREVIATION + ", " + SubjectsColumns.NAME + ", "
	    				+ SubjectsColumns.CONCURRENTLY_TAUGHT + ") VALUES (\"" + abbreviation + "\", \""
	    				+ name + "\", " + concurrentlyTaughtString + ")");
	        	parser.nextText();
	        }
		} catch (IOException | XmlPullParserException e) {
			LogUtil.e(e);
		} finally {
			closeInputStream(inputStream);
		}
		return sqlStatements;
	}
	
	private void closeInputStream(InputStream i) {
		try {
			if (i != null)
				i.close();
		} catch (Exception e) {
			LogUtil.e(e);
		}
	}
	
	

	public static class Subject {
		private String name = "";
		private String abbreviation = "";
		private boolean concurrentlyTaught = false;
		
		public Subject() {}
		public Subject(String abbreviation) {
			name = abbreviation;
			this.abbreviation = abbreviation;
		}
		
		private void setName(String name) {
			this.name = name;
		}
		private void setAbbreviation(String abbreviation) {
			this.abbreviation = abbreviation;
		}
		private void setConcurrentlyTaught(boolean concurrentlyTaught) {
			this.concurrentlyTaught = concurrentlyTaught;
		}
		
		public String getName() {
			return name;
		}
		public String getAbbreviation() {
			return abbreviation;
		}
		public boolean isConcurrentlyTaught() {
			return concurrentlyTaught;
		}
	}
}
