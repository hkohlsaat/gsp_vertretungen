package org.aweture.wonk.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Subjects;
import org.aweture.wonk.models.Subjects.Subject;
import org.aweture.wonk.models.Substitute;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.models.SubstitutionsGroup;
import org.aweture.wonk.models.Teachers;
import org.aweture.wonk.models.Teachers.Teacher;
import org.aweture.wonk.storage.DataContract.SubstitutionColumns;
import org.aweture.wonk.storage.DataContract.TableColumns;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;

public class DataStore {
	
	private static DataStore singletonInstance;
	
	public static DataStore getInstance(Context context) {
		if (singletonInstance == null) {
			singletonInstance = new DataStore(context);
		} else {
			synchronized (singletonInstance) {
				singletonInstance.context = context;
			}
		}
		return singletonInstance;
	}
	
	public static final String NEW_PLANS_ACTION = "org.aweture.wonk.storage.DataStore.NEW_PLANS_ACTION";

	private Context context;
	private List<Plan> plans;
	
	private Teachers teachers;
	private Subjects subjects;
	
	DataStore(Context context) {
		this.context = context;
		this.teachers = new Teachers(context);
		this.subjects = new Subjects(context);
	}

	public synchronized List<Plan> getCurrentPlans() {
		if (plans == null) {
			plans = queryPlans();
		}
		return plans;
	}

	public synchronized Plan getPlanByDate(String date) {
		getCurrentPlans();
		for (Plan plan : plans) {
			if (plan.getDate().toDateString().equals(date)) {
				return plan;
			}
		}
		throw new RuntimeException("No such plan found for date: " + date);
	}

	public synchronized void savePlans(List<Plan> plansList) {
		insertPlans(plansList);
		this.plans = plansList;
		fireNewPlansBroadcast();
	}

	private List<Plan> queryPlans() {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String tableName = TableColumns.TABLE_NAME;
		Cursor plansCursor = database.query(tableName, null, null, null, null, null, null);
		
		final int dateIndex = plansCursor.getColumnIndexOrThrow(TableColumns.DATE.name());
		final int createdIndex = plansCursor.getColumnIndexOrThrow(TableColumns.CREATED.name());
		final int queriedIndex = plansCursor.getColumnIndexOrThrow(TableColumns.QUERIED.name());
		
		List<Plan> plans = new ArrayList<Plan>();
		
		SimpleData simpleData = SimpleData.getInstance(context);
		boolean studentRepresentation = simpleData.isStudent();
		
		while (plansCursor.moveToNext()) {
			final String dateString = plansCursor.getString(dateIndex);
			final String createdString = plansCursor.getString(createdIndex);
			final String queriedString = plansCursor.getString(queriedIndex);
			
			Date date = Date.fromStringDate(dateString);
			Date created = Date.fromStringDateTime(createdString);
			Date queried = Date.fromStringDateTime(queriedString);
			
			Plan plan = new Plan();
			plan.setDate(date);
			plan.setCreated(created);
			plan.setQueried(queried);
			
			plans.add(plan);
			
			final String substitutionsTableName = "\"" + dateString + "\"";
			Cursor substitutionsCursor = database.query(substitutionsTableName, null, null, null, null, null, null);

			final int periodIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.PERIOD.name());
			final int substTeacherIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.SUBST_TEACHER.name());
			final int instdTeacherIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.INSTD_TEACHER.name());
			final int instdSubjectIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.INSTD_SUBJECT.name());
			final int kindIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.KIND.name());
			final int textIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.TEXT.name());
			final int classIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.CLASS.name());
			
			while (substitutionsCursor.moveToNext()) {
				final int period = substitutionsCursor.getInt(periodIndex);
				final String substTeacherShort = substitutionsCursor.getString(substTeacherIndex);
				final String instdTeacherShort = substitutionsCursor.getString(instdTeacherIndex);
				final String instdSubjectShort = substitutionsCursor.getString(instdSubjectIndex);
				final String kind = substitutionsCursor.getString(kindIndex);
				final String text = substitutionsCursor.getString(textIndex);
				final String className = substitutionsCursor.getString(classIndex);
				
				Teacher instdTeacher = teachers.getTeacher(instdTeacherShort);
				Teacher substTeacher = teachers.getTeacher(substTeacherShort);
				Subject instdSubject = subjects.getSubject(instdSubjectShort);
				
				SubstitutionsGroup currentGroup = null;
				if (studentRepresentation) {
					currentGroup = new Class();
					currentGroup.setName(className);
				} else if (!substTeacherShort.isEmpty()){
					currentGroup = new Substitute();
					currentGroup.setName(substTeacher.getName() + " (" + substTeacherShort + ")");
				} else {
					continue;
				}
				
				List<Substitution> substitutions = plan.get(currentGroup);
				if(substitutions == null) {
					substitutions = new ArrayList<Substitution>();
					plan.put(currentGroup, substitutions);
				}
				
				Substitution substitution = new Substitution();
				substitution.setPeriodNumber(period);
				substitution.setSubstTeacher(substTeacher);
				substitution.setInstdTeacher(instdTeacher);
				substitution.setInstdSubject(instdSubject);
				substitution.setKind(kind);
				substitution.setText(text);
				substitution.setClassName(className);
				
				if (!studentRepresentation) {
					boolean foundSame = false;
					Substitution same = null;
					for (Substitution s : substitutions) {
						if (foundSame = s.getPeriodNumber() == period) {
							same = s;
							break;
						}
					}
					if (foundSame) {
						same.setClassName(same.getClassName() + ", " + className);
						continue;
					}
				}
				substitutions.add(substitution);
			}
			
			HashMap<SubstitutionsGroup, List<Substitution>> planCopy = new HashMap<SubstitutionsGroup, List<Substitution>>(plan);
			if (!studentRepresentation) {
				Pattern pattern = Pattern.compile("Aufg(\\.|abe) [A-Z][a-z]{1,2}");
				Set<SubstitutionsGroup> groups = planCopy.keySet();
				for (SubstitutionsGroup group : groups) {
					List<Substitution> substitutions = planCopy.get(group);
					for (Substitution substitution : substitutions) {
						Matcher matcher = pattern.matcher(substitution.getText());
						while (matcher.find()) {
							int end = matcher.end();
							int start = end - 3;
							String shortName = substitution.getText().substring(start, end).trim();
							Teacher taskProvider = teachers.getTeacherOrNull(shortName);
							if (taskProvider != null) {
								Substitute substitute = new Substitute();
								substitute.setName(taskProvider.getName() + " (" + taskProvider.getShortName() + ")");
								List<Substitution> _substitutions = plan.get(substitute);
								if (_substitutions == null) {
									_substitutions = new ArrayList<Substitution>();
									plan.put(substitute, _substitutions);
								}
								_substitutions.add(substitution);
							}
						}
					}
				}
			}
			
			substitutionsCursor.close();
		}
		
		plansCursor.close();
		database.close();
		return plans;
	}
	
	private SubstitutionsGroup newSubstitutionsGroup() {
		SimpleData simpleData = SimpleData.getInstance(context);
		if (simpleData.isStudent()) {
			return new Class();
		} else {
			return new Substitute();
		}
	}
	
	private void insertPlans(List<Plan> plans) {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		resetDatabase(database);
		
		for (Plan plan : plans) {
			final String dateString = plan.getDate().toDateString();
			final String createdString = plan.getCreation().toDateTimeString();
			final String queriedString = plan.getQueried().toDateTimeString();

			String tableName = TableColumns.TABLE_NAME;
			ContentValues planValues = new ContentValues();
			planValues.put(TableColumns.DATE.name(), dateString);
			planValues.put(TableColumns.CREATED.name(), createdString);
			planValues.put(TableColumns.QUERIED.name(), queriedString);
			database.insert(tableName, null, planValues);
			
			final String substitutionsTableName = "\"" + dateString + "\"";
			CreateQuery createQuery = new CreateQuery(substitutionsTableName);
			for (SubstitutionColumns column : SubstitutionColumns.values()) {
				createQuery.addColumn(column.name(), column.type());
			}
			database.execSQL(createQuery.toString());
			
			Set<SubstitutionsGroup> substitutionsGroups = plan.keySet();
			
			for (SubstitutionsGroup substitutionsGroup : substitutionsGroups) {
				final String className = substitutionsGroup.getName();
				List<Substitution> substitutions = plan.get(substitutionsGroup);
				
				for (Substitution substitution : substitutions) {
					final int period = substitution.getPeriodNumber();
					final String substTeacher = substitution.getSubstTeacher().getShortName();
					final String instdTeacher = substitution.getInstdTeacher().getShortName();
					final String instdSubject = substitution.getInstdSubject().getAbbreviation();
					final String kind = substitution.getKind();
					final String text = substitution.getText();
					
					ContentValues substitutionValues = new ContentValues();
					substitutionValues.put(SubstitutionColumns.PERIOD.name(), Integer.valueOf(period));
					substitutionValues.put(SubstitutionColumns.SUBST_TEACHER.name(), substTeacher);
					substitutionValues.put(SubstitutionColumns.INSTD_TEACHER.name(), instdTeacher);
					substitutionValues.put(SubstitutionColumns.INSTD_SUBJECT.name(), instdSubject);
					substitutionValues.put(SubstitutionColumns.KIND.name(), kind);
					substitutionValues.put(SubstitutionColumns.TEXT.name(), text);
					substitutionValues.put(SubstitutionColumns.CLASS.name(), className);
					database.insert(substitutionsTableName, null, substitutionValues);
				}
			}
		}
		database.close();
	}
	
	private void resetDatabase(SQLiteDatabase database) {
		String tableName = TableColumns.TABLE_NAME;
		Cursor plansCursor = database.query(tableName, null, null, null, null, null, null);
		
		final int dateIndex = plansCursor.getColumnIndexOrThrow(TableColumns.DATE.name());
		
		while (plansCursor.moveToNext()) {
			final String substitutionsTableName = "\"" + plansCursor.getString(dateIndex) + "\"";
			database.execSQL("DROP TABLE IF EXISTS " + substitutionsTableName);
		}
		
		plansCursor.close();
		database.delete(tableName, null, null);
	}
	
	private void fireNewPlansBroadcast() {
		Intent intent = new Intent(NEW_PLANS_ACTION);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

}
