package org.aweture.wonk.storage;

import java.util.List;

import org.aweture.wonk.models.Plan;

public interface DataStore {
	public List<Plan> getCurrentPlans();
	
	/**
	 * Get a plan by the date it concerns.
	 * @param date
	 * @return {@link Plan}
	 */
	public Plan getPlanByDate(String date) ;
	
	public void savePlans(Plan[] plans);
}