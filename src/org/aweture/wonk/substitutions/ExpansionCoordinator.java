package org.aweture.wonk.substitutions;


public class ExpansionCoordinator {
	
	private Expandable expanded;
	
	ExpansionCoordinator(){
	}
	
	
	public void clicked(Expandable expandable) {
		Expandable formerExpanded = expanded;
		expanded = expandable;
		
		if (formerExpanded == null) {
			expanded.changeExpansionState(true);
		} else if (formerExpanded == expanded) {
			expanded = null;
			formerExpanded.changeExpansionState(true);
		} else {
			formerExpanded.changeExpansionState(true);
			expanded.changeExpansionState(true);
		}
	}
	
	public void removeAsExpanded(Expandable expandable) {
		if (expanded == expandable) {
			expanded = null;
			expandable.changeExpansionState(false);
		}
	}
	
	public boolean isExpanded(Expandable expandable) {
		return expanded == expandable;
	}
	
}