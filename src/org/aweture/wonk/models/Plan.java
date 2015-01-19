package org.aweture.wonk.models;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class Plan extends HashMap<Class, List<Substitution>> {
	
	private Date date;
	private String creationTime;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	public void setDate(String date) {
		this.date = new Date(date);
	}
	
	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}
}
