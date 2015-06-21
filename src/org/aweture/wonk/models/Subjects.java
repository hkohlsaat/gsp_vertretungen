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


public class Subjects {
	
	private final String ATTRIBUTE_SHORT= "short";
	private final String ATTRIBUTE_NAME = "name";
	private final String ATTRIBUTE_CONCURRENTLY_TAUGHT = "concurrentlyTaught";
	
    Map<String, Subject> subjects;
	
	public Subjects(Context context) {
		subjects = new HashMap<String, Subject>();
        
		populateSubjectsMap(context);
	}
	
	public Subject getSubject(String abbreviation) {
		Subject subject = subjects.get(abbreviation);
		if (subject == null) {
			subject = new Subject();
			subject.setName(abbreviation);
			subject.setAbbreviation(abbreviation);
		}
		return subject;
	}
	
	
	private void populateSubjectsMap(Context context) {
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
	    		boolean concurrentlyTaught = Boolean.parseBoolean(concurrentlyTaughtString);
	    		
	    		Subject subject = new Subject();
	    		subject.setAbbreviation(abbreviation);
	    		subject.setName(name);
	    		subject.setConcurrentlyTaught(concurrentlyTaught);
	    		
	    		subjects.put(abbreviation, subject);
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
	
	

	public static class Subject {
		private String name = "";
		private String abbreviation = "";
		private boolean concurrentlyTaught = false;
		
		public Subject() {}
		public Subject(String abbreviation) {
			name = abbreviation;
			this.abbreviation = abbreviation;
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
		private void setName(String name) {
			this.name = name;
		}
		private void setAbbreviation(String abbreviation) {
			this.abbreviation = abbreviation;
		}
		private void setConcurrentlyTaught(boolean concurrentlyTaught) {
			this.concurrentlyTaught = concurrentlyTaught;
		}
		
	}

}
