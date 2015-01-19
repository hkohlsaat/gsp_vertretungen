package org.aweture.wonk.substitutions;

public class ExpansionCoordinator {
	
	private static ExpansionCoordinator instance = new ExpansionCoordinator();
	
	private Expandable expanded;
	
	private ExpansionCoordinator(){
		// Singleton
	}
	
	public static ExpansionCoordinator getInstance() {
		return instance;
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