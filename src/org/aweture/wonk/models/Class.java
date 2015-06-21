package org.aweture.wonk.models;


public class Class implements SubstitutionsGroup {
	
	private Object base;
	private String name;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void baseUppon(Object base) {
		this.base = base;
		name = base.toString();
	}

	@Override
	public boolean isBasedUppon(Object potentialBase) {
		return base.equals(base);
	}
	
	@Override
	public boolean isStudentMode() {
		return true;
	}
	
	public boolean isLetterGrader() {
		char firstChar = name.charAt(0);
		return Character.isLetter(firstChar);
	}
	
	public boolean isOberstufe() {
		return getGrade() > 10;
	}

	public int getGrade() {
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
			int plus = Integer.parseInt(name.substring(1, 2)) +1;
			return 11 + (plus / 2);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Class other = (Class) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(SubstitutionsGroup another) {
		Class other = (Class) another;
		
		int difference;
		if (areBothOberstufe(other) && isOnlyOneALetterGrader(other)) {
			difference = isLetterGrader() ? -1 : 1;
		} else {
			difference = getGrade() - other.getGrade();
		}
		if (difference != 0) {
			return difference;
		} else {
			return getName().compareTo(other.getName());
		}
	}
	
	private boolean areBothOberstufe(Class other) {
		return isOberstufe() && other.isOberstufe();
	}
	
	private boolean isOnlyOneALetterGrader(Class other) {
		return isLetterGrader() ^ other.isLetterGrader();
	}
}
