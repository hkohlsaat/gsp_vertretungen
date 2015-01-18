package org.aweture.wonk.substitutions;

public class ExpansionCoordinator {
	
	private Expandable expanded;
	
	public void clicked(Expandable expandable) {
		Expandable formerExpanded = expanded;
		expanded = expandable;
		
		if (formerExpanded == null) {
			expanded.changeExpansionState();
		} else if (formerExpanded == expanded) {
			expanded = null;
			formerExpanded.changeExpansionState();
		} else {
			formerExpanded.changeExpansionState();
			expanded.changeExpansionState();
		}
	}
	
	public void removeAsExpanded(Expandable expandable) {
		if (expanded == expandable) {
			expanded.changeExpansionState();
			expanded = null;
		}
	}
	
	public boolean isExpanded(Expandable expandable) {
		return expanded == expandable;
	}
}