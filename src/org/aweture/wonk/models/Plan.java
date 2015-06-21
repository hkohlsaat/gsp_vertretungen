package org.aweture.wonk.models;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class Plan extends HashMap<SubstitutionsGroup, List<Substitution>> {
	
	private Date date;
	private Date created;
	private Date queried; 

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public Date getCreation() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getQueried() {
		return queried;
	}

	public void setQueried(Date queried) {
		this.queried = queried;
	}
}
