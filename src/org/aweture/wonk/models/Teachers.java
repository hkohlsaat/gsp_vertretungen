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
	private final String ATTRIBUTE_ACCUSATIVE = "accusative";
	
    private Map<String, Teacher> teachers = new HashMap<String, Teacher>();
	
	public Teachers(Context context) {
		teachers = new HashMap<String, Teacher>();
        
		populateTeachersMap(context);
	}
	
	public Teacher getTeacher(String shortName) {
		Teacher teacher = teachers.get(shortName);
		if (teacher == null) {
			teacher = new Teacher();
			teacher.setShortName(shortName);
			teacher.setName(shortName);
			teacher.setAccusative(shortName);
		}
		return teacher;
	}
	
	
	private void populateTeachersMap(Context context) {
		try (InputStream inputStream = context.getAssets().open("teachers.xml")) {
			
	        XmlPullParser parser = Xml.newPullParser();
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	        parser.setInput(inputStream, null);
	        parser.nextTag();
	        
	        while (parser.nextTag() == XmlPullParser.START_TAG){
	        	String shortName = parser.getAttributeValue(null, ATTRIBUTE_SHORT);
	    		String accusative = parser.getAttributeValue(null, ATTRIBUTE_ACCUSATIVE);
	    		String name = parser.getAttributeValue(null, ATTRIBUTE_NAME);
	    		
	    		Teacher teacher = new Teacher();
	    		teacher.setShortName(shortName);
	    		teacher.setAccusative(accusative);
	    		teacher.setName(name);
	    		
	    		teachers.put(shortName, teacher);
	        	parser.nextText();
	        }
		} catch (IOException | XmlPullParserException e) {
			LogUtil.e(e);
		}
	}
	
	

	public class Teacher {
		private String name = "";
		private String accusative = "";
		private String shortName = "";
		
		public String getName() {
			return name;
		}
		public String getAccusative() {
			return accusative;
		}
		public String getShortName() {
			return shortName;
		}
		private void setName(String name) {
			this.name = name;
		}
		private void setAccusative(String accusative) {
			this.accusative = accusative;
		}
		private void setShortName(String shortName) {
			this.shortName = shortName;
		}
	}
}
