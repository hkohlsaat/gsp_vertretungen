package org.aweture.wonk.models;

public class Substitution implements Comparable<Substitution> {

	public String period;
	public Teacher substTeacher;
	public Teacher instdTeacher;
	public Subject instdSubject;
	public String kind;
	public String text;
	public String className;
	public Teacher taskProvider;

	public Substitution copy() {
		Substitution s = new Substitution();
		s.period = period;
		s.substTeacher = substTeacher.copy();
		s.instdTeacher = instdTeacher.copy();
		s.instdSubject = instdSubject.copy();
		s.kind = kind;
		s.text = text;
		s.className = className;
		s.taskProvider = taskProvider;

		return s;
	}

	@Override
	public int compareTo(Substitution another) {
		int g1 = getGrade(className);
		int g2 = getGrade(another.className);
		if (g1 - g2 != 0) {
			int difference = g1 < g2 ? -1 : 1;
			return difference;
		} else {
			return className.compareTo(another.className);
		}
	}

	public static int getGrade(String name) {
		char firstChar = name.charAt(0);

		// Test whether it's a number...
		if (Character.isDigit(firstChar)) {
			if (firstChar == '1') {
				return Integer.parseInt(name.substring(0, 2));
			} else {
				return Integer.parseInt(name.substring(0, 1));
			}
			// ...or a letter.
		} else if (firstChar == 'E') {
			return 11;
		} else {
			int plus = Integer.parseInt(name.substring(1, 2));
			return 11 + plus;
		}
	}
}
