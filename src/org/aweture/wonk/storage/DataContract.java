package org.aweture.wonk.storage;


public final class DataContract {
	
	public enum TableColumns {
		
		_ID("INTEGER PRIMARY KEY AUTOINCREMENT"),
		DATE("TEXT"),
		CREATED("TEXT"),
		QUERIED("TEXT");
		
		public static final String TABLE_NAME = "tables_dir";
		
		
		private final String TYPE;
		
		private TableColumns(String type) {
			this.TYPE = type;
		}
		
		public String type() {
			return TYPE;
		}
	}
	
	public enum SubstitutionColumns {
		
		_ID("INTEGER PRIMARY KEY AUTOINCREMENT"),
		PERIOD("INTEGER"),
		SUBST_TEACHER("TEXT"),
		INSTD_TEACHER("TEXT"),
		INSTD_SUBJECT("TEXT"),
		KIND("TEXT"),
		TEXT("TEXT"),
		CLASS("TEXT");
		
		
		private final String TYPE;
		
		private SubstitutionColumns(String type) {
			this.TYPE = type;
		}
		
		public String type() {
			return TYPE;
		}
	}
	
	public enum LogColumns {
		
		_ID("INTEGER PRIMARY KEY AUTOINCREMENT"),
		MESSAGE("TEXT");
		
		public static final String TABLE_NAME = "log";
		
		
		private final String TYPE;
		
		private LogColumns(String type) {
			this.TYPE = type;
		}
		
		public String type() {
			return TYPE;
		}
	}
}
