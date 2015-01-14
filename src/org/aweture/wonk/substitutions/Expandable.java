package org.aweture.wonk.substitutions;

import android.view.View;

/**
 * Interface to be implemented by expandable {@link View}s in {@link SubstitutionsFragment}.
 * 
 * @author Hannes Kohlsaat
 *
 */
public interface Expandable {
	
	/**
	 * The {@link View} should expand. This method is the right place
	 * to animate a transition.
	 */
	public void expand();
	
	/**
	 * The {@link View} should collapse. This method is the right place
	 * to animate a transition.
	 */
	public void collapse();
	
	/**
	 * The {@link View} should set itself to collapse without animating.
	 */
	//public void collapseSilently();
}
