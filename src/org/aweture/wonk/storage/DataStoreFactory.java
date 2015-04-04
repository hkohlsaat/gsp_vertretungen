package org.aweture.wonk.storage;

import android.content.Context;

public class DataStoreFactory {
	
	private static DataStore store;
	
	public static DataStore getDataStore(Context context) {
		if (store == null) {
			store = createStore(context);
		}
		return store;
	}
	
	private static DataStore createStore(Context context) {
		return new DatabaseDataStore(context);
	}

}
