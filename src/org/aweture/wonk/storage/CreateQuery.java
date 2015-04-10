package org.aweture.wonk.storage;

import java.util.ArrayList;
import java.util.List;

public class CreateQuery {
	
	private String tableName;
	private List<String> columnDefinitions = new ArrayList<String>();
	
	public CreateQuery(String tableName) {
		this.tableName = tableName;
	}
	
	public void addColumn(String name, String type) {
		columnDefinitions.add(name + " " + type);
	}
	
	@Override
	public String toString() {
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE " + tableName + " (");
		for (int i = 0; i < columnDefinitions.size(); i++) {
			if (i != 0) query.append(" ,");
			query.append(columnDefinitions.get(i));
		}
		query.append(")");
		return query.toString();
	}
}
