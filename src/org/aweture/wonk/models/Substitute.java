package org.aweture.wonk.models;


public class Substitute implements SubstitutionsGroup {
	
	private Object base;
	private String name = "";
	private String baseString = "";

	@Override
	public void baseUppon(Object base) {
		this.base = base;
		name = base.toString();
	}
	
	@Override
	public void setBaseInData(String baseString) {
		this.baseString = baseString;
	}
	
	@Override
	public boolean isBasedUppon(Object potentialBase) {
		return base.equals(potentialBase);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getBaseInData() {
		return baseString;
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
		Substitute other = (Substitute) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(SubstitutionsGroup another) {
		return name.compareTo(another.getName());
	}

}
