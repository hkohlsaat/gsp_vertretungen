package org.aweture.wonk.models;

public class Substitution {

	private int periodNumber;
	private String substTeacher;
	private String instdTeacher;
	private String instdSubject;
	private String kind;
	private String text;
	
	public int getPeriodNumber() {
		return periodNumber;
	}
	public String getSubstTeacher() {
		return substTeacher;
	}
	public String getInstdTeacher() {
		return instdTeacher;
	}
	public String getInstdSubject() {
		return instdSubject;
	}
	public String getKind() {
		return kind;
	}
	public String getText() {
		return text;
	}
	public void setPeriodNumber(int periodNumber) {
		this.periodNumber = periodNumber;
	}
	public void setSubstTeacher(String substTeacher) {
		this.substTeacher = substTeacher;
	}
	public void setInstdTeacher(String instdTeacher) {
		this.instdTeacher = instdTeacher;
	}
	public void setInstdSubject(String instdSubject) {
		this.instdSubject = instdSubject;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public void setText(String text) {
		this.text = text;
	}
}
