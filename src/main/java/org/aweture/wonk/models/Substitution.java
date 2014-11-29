package org.aweture.wonk.models;

import org.aweture.wonk.storage.WonkContract;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

public class Substitution {
	
	private String date;
	private String className;
	private int period;
	private String substTeacher;
	private String instdTeacher;
	private String instdSubject;
	private String kind;
	private String text;
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
	
	public String getSubstTeacher() {
		return substTeacher;
	}
	public void setSubstTeacher(String substitute) {
		if (substitute.equals("---") || substitute.equals("+")){
			this.substTeacher = "";
		} else {
			this.substTeacher = Teacher.getFullName(substitute);
		}
	}
	
	public String getInstdTeacher() {
		return instdTeacher;
	}
	public void setInstdTeacher(String instdTeacher) {
		this.instdTeacher = Teacher.getFullName(instdTeacher);
	}
	
	public String getInstdSubject() {
		return instdSubject;
	}
	public void setInstdSubject(String instdSubject) {
		int firstPart = instdSubject.indexOf(" ");
		firstPart = firstPart == -1 ? instdSubject.length() : firstPart;
		this.instdSubject = Subject.getFullName(instdSubject.substring(0, firstPart));
	}
	
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		if (kind == null) {
			this.kind = "";
		} else if (kind.equals("Statt-Vertretung")) {
			this.kind = "Vertretung";
		} else {
			this.kind = kind;
		}
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		if (text == null) {
			this.text = "";
		} else {
			String[] whitespaceParts = text.replaceAll("regul.r", "regulär").split("\\s");
			text = "";
			for (String whitespacePart : whitespaceParts) {
				
				for (String commaPart : whitespacePart.split(",")) {
					text += Teacher.getFullName(commaPart) + ",";
				}
				// remove last comma.
				text = text.substring(0, text.length() - 1);
				text += " ";
			}
			this.text = text.trim();
		}
	}
	
	public void save(Context context) {
		ContentValues values = new ContentValues();
		values.put(WonkContract.SubstitutionEntry.DATE, date);
		values.put(WonkContract.SubstitutionEntry.CLASS, className);
		values.put(WonkContract.SubstitutionEntry.PERIOD_NUMBER, period);
		values.put(WonkContract.SubstitutionEntry.SUBST_TEACHER, substTeacher);
		values.put(WonkContract.SubstitutionEntry.INSTD_TEACHER, instdTeacher);
		values.put(WonkContract.SubstitutionEntry.INSTD_SUBJECT, instdSubject);
		values.put(WonkContract.SubstitutionEntry.KIND, kind);
		values.put(WonkContract.SubstitutionEntry.TEXT, text);
		
		ContentResolver cr = context.getContentResolver();
		cr.insert(WonkContract.SubstitutionEntry.CONTENT_URI, values);
	}

}
