package org.aweture.wonk.models;

public class Teacher {

	public final String abbr;
	public final String name;
	public final String sex;

	public Teacher(String abbr, String name, String sex) {
		this.abbr = abbr;
		this.name = name;
		this.sex = sex;
	}

	public Teacher copy() {
		return new Teacher(abbr, name, sex);
	}

}
