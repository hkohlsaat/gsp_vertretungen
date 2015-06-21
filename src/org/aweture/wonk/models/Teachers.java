package org.aweture.wonk.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.aweture.wonk.log.LogUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Xml;

public class Teachers {

	private final String ATTRIBUTE_SHORT= "short";
	private final String ATTRIBUTE_NAME = "name";
	private final String ATTRIBUTE_NAME_COMPELLATION = "name_compellation";
	private final String ATTRIBUTE_ACCUSATIVE = "accusative";
	
    private Map<String, Teacher> teachers = new HashMap<String, Teacher>();
	
	public Teachers(Context context) {
		teachers = new HashMap<String, Teacher>();
        
		populateTeachersMap(context);
	}
	
	public Teacher getTeacher(String shortName) {
		Teacher teacher = getTeacherOrNull(shortName);
		if (teacher == null) {
			teacher = new Teacher(shortName);
		}
		return teacher;
	}
	
	public Teacher getTeacherOrNull(String shortName) {
		return teachers.get(shortName);
	}
	
	
	private void populateTeachersMap(Context context) {
		InputStream inputStream = null;
		try {
			inputStream = context.getAssets().open("teachers.xml");
					
	        XmlPullParser parser = Xml.newPullParser();
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	        parser.setInput(inputStream, null);
	        parser.nextTag();
	        
	        while (parser.nextTag() == XmlPullParser.START_TAG){
	        	String shortName = parser.getAttributeValue(null, ATTRIBUTE_SHORT);
	    		String accusative = parser.getAttributeValue(null, ATTRIBUTE_ACCUSATIVE);
	    		String name = parser.getAttributeValue(null, ATTRIBUTE_NAME);
	    		String nameWithCompellation = parser.getAttributeValue(null, ATTRIBUTE_NAME_COMPELLATION);
	    		
	    		Teacher teacher = new Teacher();
	    		teacher.setShortName(shortName);
	    		teacher.setName(name);
	    		teacher.setAccusative(accusative);
	    		teacher.setNameWithCompellation(nameWithCompellation);
	    		
	    		teachers.put(shortName, teacher);
	        	parser.nextText();
	        }
		} catch (IOException | XmlPullParserException e) {
			LogUtil.e(e);
		} finally {
			closeInputStream(inputStream);
		}
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
		private String nameWithCompellation = "";
		private String accusative = "";
		private String shortName = "";
		
		public Teacher() {}
		public Teacher(String shortName) {
			name = shortName;
			nameWithCompellation = shortName;
			accusative = shortName;
			this.shortName = shortName;
		}

		public String getName() {
			return name;
		}
		public String getNameWithCompellation() {
			return nameWithCompellation;
		}
		public String getAccusative() {
			return accusative;
		}
		public String getShortName() {
			return shortName;
		}
		private void setNameWithCompellation(String name) {
			this.nameWithCompellation = name;
		}
		private void setAccusative(String accusative) {
			this.accusative = accusative;
		}
		private void setShortName(String shortName) {
			this.shortName = shortName;
		}
		private void setName(String name) {
			this.name = name;
		}
	}
}
