package org.aweture.wonk.substitutions;

public class ExpansionCoordinator {
	
	private Expandable expanded;
	
	public void clicked(Expandable expandable) {
		Expandable formerExpanded = expanded;
		expanded = expandable;
		
		if (formerExpanded == null) {
			expanded.expand();
		} else if (formerExpanded == expanded) {
			expanded = null;
			formerExpanded.collapse();
		} else {
			formerExpanded.collapse();
			expanded.expand();
		}
	}
	
	public boolean isExpanded(Expandable expandable) {
		return expanded == expandable;
	}
}