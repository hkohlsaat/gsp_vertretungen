package org.aweture.wonk.models;


public interface SubstitutionsGroup extends Comparable<SubstitutionsGroup> {

	public String getName();
	public void baseUppon(Object base);
	public void setBaseInData(String baseString);
	public boolean isBasedUppon(Object potentialBase);
	public String getBaseInData();
	
}
