package org.aweture.wonk.models;


public interface SubstitutionsGroup extends Comparable<SubstitutionsGroup> {

	public String getName();
	public void baseUppon(Object base);
	public boolean isBasedUppon(Object potentialBase);
	public boolean isStudentMode();
}
