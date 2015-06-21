package org.aweture.wonk.models;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a data structure meant to have a set of items. These items can belong
 * to different categories. So a specific item does or does not belong to a specific
 * known category. Therefore this structure can get called boolean returning 
 * methods about the affiliation of every of the items to every of the categories.
 * 
 * @author Hannes Kohlsaat
 *
 */
public class CategoryBucket<I> {

	private List<I> items;
	
	public CategoryBucket() {
		items = new ArrayList<I>();
	}
	
	public void add(I item) {
		items.add(item);
	}
}
