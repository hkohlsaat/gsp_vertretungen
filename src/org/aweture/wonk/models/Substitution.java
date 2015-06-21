package org.aweture.wonk.models;

import org.aweture.wonk.models.Subjects.Subject;
import org.aweture.wonk.models.Teachers.Teacher;

public class Substitution {

	private int periodNumber;
	private Teacher substTeacher;
	private Teacher instdTeacher;
	private Subject instdSubject;
	private String kind;
	private String text;
	private String className;
	
	public int getPeriodNumber() {
		return periodNumber;
	}
	public Teacher getSubstTeacher() {
		return substTeacher;
	}
	public Teacher getInstdTeacher() {
		return instdTeacher;
	}
	public Subject getInstdSubject() {
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
	public void setSubstTeacher(Teacher substTeacher) {
		this.substTeacher = substTeacher;
	}
	public void setInstdTeacher(Teacher instdTeacher) {
		this.instdTeacher = instdTeacher;
	}
	public void setInstdSubject(Subject instdSubject) {
		this.instdSubject = instdSubject;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean hasText() {
		return !text.isEmpty();
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
}
