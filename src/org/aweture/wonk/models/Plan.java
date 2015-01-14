package org.aweture.wonk.models;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class Plan extends HashMap<Class, List<Substitution>> {
	
	private Date date;
	private String asOfTime;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	public void setDate(String date) {
		this.date = new Date(date);
	}
	
	public String getAsOfTime() {
		return asOfTime;
	}

	public void setAsOfTime(String asOfTime) {
		this.asOfTime = asOfTime;
	}
}
