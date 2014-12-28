package org.aweture.wonk.models;

public class Class {
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	
	
}
