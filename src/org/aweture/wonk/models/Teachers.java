package org.aweture.wonk.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.storage.DataContract;
import org.aweture.wonk.storage.DataContract.TeachersColumns;
import org.aweture.wonk.storage.DatabaseHelper;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Xml;

public class Teachers {
	
	public static final int VERSION = 0;

	private static final String ATTRIBUTE_SHORT= "short";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_COMPELLATION = "compellation";
	
	private Context context;
	private HashMap<String, Teacher> prefetchTeachers;
	
	public Teachers(Context context) {
		this.context = context;
	}
	
	public void rewriteTable() {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + TeachersColumns.TABLE_NAME);
		List<String> insertStatements = getInsetStatements();
		for (String statement : insertStatements) {
			db.execSQL(statement);
		}
		db.close();
	}
	
	public void prefetch() {
		prefetchTeachers = new HashMap<String, Teacher>();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TeachersColumns.TABLE_NAME, null, null, null, null, null, null);
		
		while(cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(TeachersColumns.NAME.name()));
			String abbreviation = cursor.getString(cursor.getColumnIndex(TeachersColumns.ABBREVIATION.name()));
			String compellation = cursor.getString(cursor.getColumnIndex(TeachersColumns.COMPELLATION.name()));
			
			Teacher teacher = new Teacher();
			teacher.setAbbreviation(abbreviation);
			teacher.setName(name);
			teacher.setCompellation(compellation);
			prefetchTeachers.put(abbreviation, teacher);
		}
		
		cursor.close();
		db.close();
	}
	
	public Teacher getTeacher(String abbreviation) {
		Teacher teacher = getTeacherOrNull(abbreviation);
		if (teacher == null) {
			teacher = new Teacher(abbreviation);
		}
		return teacher;
	}
	
	public Teacher getTeacherOrNull(String abbreviation) {
		if (prefetchTeachers != null) {
			return prefetchTeachers.get(abbreviation);
		} else {
			return querySingleTeacher(abbreviation);
		}
	}
	
	private Teacher querySingleTeacher(String abbreviation) {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TeachersColumns.TABLE_NAME, null, TeachersColumns.ABBREVIATION
				+ " = \"" + abbreviation + "\"", null, null, null, null);
		
		Teacher teacher = null;
		if (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex(TeachersColumns.NAME.name()));
			String compellation = cursor.getString(cursor.getColumnIndex(TeachersColumns.COMPELLATION.name()));
			
			teacher = new Teacher();
			teacher.setAbbreviation(abbreviation);
			teacher.setName(name);
			teacher.setCompellation(compellation);
		}
		
		cursor.close();
		db.close();
		return teacher;
	}
	
	
	private List<String> getInsetStatements() {
		List<String> sqlStatements = new ArrayList<String>();
		InputStream inputStream = null;
		try {
			inputStream = context.getAssets().open("teachers.xml");
					
	        XmlPullParser parser = Xml.newPullParser();
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	        parser.setInput(inputStream, null);
	        parser.nextTag();
	        
	        while (parser.nextTag() == XmlPullParser.START_TAG){
	        	String abbreviation = parser.getAttributeValue(null, ATTRIBUTE_SHORT);
	    		String name = parser.getAttributeValue(null, ATTRIBUTE_NAME);
	    		String compellation = parser.getAttributeValue(null, ATTRIBUTE_COMPELLATION);
	    		
	    		sqlStatements.add("INSERT INTO " + DataContract.TeachersColumns.TABLE_NAME + " ("
	    				+ TeachersColumns.ABBREVIATION + ", " + TeachersColumns.NAME + ", "
	    				+ TeachersColumns.COMPELLATION + ") VALUES (\"" + abbreviation + "\", \""
	    				+ name + "\", \"" + compellation + "\")");
	    		
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
	
	

	public static class Teacher {
		private String name = "";
		private String compellation = "";
		private String abbreviation = "";
		
		public Teacher() {}
		public Teacher(String abbreviation) {
			name = abbreviation;
			this.abbreviation = abbreviation;
		}
		
		private void setAbbreviation(String abbreviation) {
			this.abbreviation = abbreviation;
		}
		private void setName(String name) {
			this.name = name;
		}
		private void setCompellation(String compellation) {
			this.compellation = compellation;
		}

		public String getName() {
			return name;
		}
		public String getNameWithCompellation() {
			return compellation + " " + name;
		}
		public String getAccusative() {
			return getNameWithCompellation().replace("Herr", "Herrn");
		}
		public String getShortName() {
			return abbreviation;
		}
	}
}
