package org.aweture.wonk.models;

import java.util.Comparator;

public class Substitution {

	public String period;
	public Teacher substTeacher;
	public Teacher instdTeacher;
	public Subject instdSubject;
	public String kind;
	public String text;
	public String className;
	public Teacher taskProvider;

	// Whether this instance of substitution is meant to inform the task provider of their duty.
	public boolean modeTaskProvider = false;

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

	public static class ClassComparator implements Comparator<Substitution> {
		@Override
		public int compare(Substitution lhs, Substitution rhs) {
			int g1 = getGrade(lhs.className);
			int g2 = getGrade(rhs.className);
			if (g1 - g2 != 0) {
				int comp = g1 < g2 ? -1 : 1;
				return comp;
			} else {
				int comp = lhs.className.compareTo(rhs.className);
				if (comp == 0) {
					return lhs.period.compareTo(rhs.period);
				}
				return comp;
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

	public static class TeacherComparator implements Comparator<Substitution> {
		@Override
		public int compare(Substitution lhs, Substitution rhs) {
			String lname = lhs.modeTaskProvider ? lhs.taskProvider.getName() : lhs.substTeacher.getName();
			String rname = rhs.modeTaskProvider ? rhs.taskProvider.getName() : rhs.substTeacher.getName();
			int comp = lname.compareTo(rname);
			if (comp == 0) {
				comp = lhs.period.compareTo(rhs.period);
			}
			return comp;
		}
	}

}
