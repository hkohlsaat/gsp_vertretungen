package org.aweture.wonk.models;

public class Plan {
	
	public Date created;
	public Part[] parts;

	public static class Part {
		public Date day;
		public Substitution[] substitutions;
	}

}
