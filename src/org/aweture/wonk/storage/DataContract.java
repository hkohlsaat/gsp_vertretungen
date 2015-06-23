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
		CLASS("TEXT"),
		TASK_PROVIDER("TEXT");
		

		public static final String STUDENT_SUFIX = "_student";
		public static final String TEACHER_SUFIX = "_teacher";
		
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

	public enum TeachersColumns {
		
		_ID("INTEGER PRIMARY KEY AUTOINCREMENT"),
		ABBREVIATION("TEXT"),
		NAME("TEXT"),
		COMPELLATION("TEXT");
		
		public static final String TABLE_NAME = "teachers";
		
		
		private final String TYPE;
		
		private TeachersColumns(String type) {
			this.TYPE = type;
		}
		
		public String type() {
			return TYPE;
		}
	}

	public enum SubjectsColumns {
		
		_ID("INTEGER PRIMARY KEY AUTOINCREMENT"),
		ABBREVIATION("TEXT"),
		NAME("TEXT"),
		CONCURRENTLY_TAUGHT("BOOLEAN");
		
		public static final String TABLE_NAME = "subjects";
		
		
		private final String TYPE;
		
		private SubjectsColumns(String type) {
			this.TYPE = type;
		}
		
		public String type() {
			return TYPE;
		}
	}
	
	public enum NotifiedSubstitutionColumns {

		_ID("INTEGER PRIMARY KEY AUTOINCREMENT"),
		DATE("TEXT"),
		PERIOD("INTEGER"),
		FILTER("TEXT");
		
		public static final String TABLE_NAME = "notified_substitutions";
		
		
		private final String TYPE;
		
		private NotifiedSubstitutionColumns(String type) {
			this.TYPE = type;
		}
		
		public String type() {
			return TYPE;
		}
	}
}
