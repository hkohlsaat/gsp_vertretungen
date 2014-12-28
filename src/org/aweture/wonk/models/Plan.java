package org.aweture.wonk.models;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class Plan extends HashMap<Class, List<Substitution>> {
	
	private String date;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
