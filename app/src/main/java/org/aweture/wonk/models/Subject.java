package org.aweture.wonk.models;

public class Subject {

	public final String abbr;
	public final String name;
	public final boolean splitClass;

	public Subject(String abbr, String name, boolean splitClass) {
		this.abbr = abbr;
		this.name = name;
		this.splitClass = splitClass;
	}

	public Subject copy() {
		return new Subject(abbr, name, splitClass);
	}
}
