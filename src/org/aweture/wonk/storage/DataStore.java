package org.aweture.wonk.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Substitution;
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
	
	DataStore(Context context) {
		this.context = context;
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
				final String substTeacher = substitutionsCursor.getString(substTeacherIndex);
				final String instdTeacher = substitutionsCursor.getString(instdTeacherIndex);
				final String instdSubject = substitutionsCursor.getString(instdSubjectIndex);
				final String kind = substitutionsCursor.getString(kindIndex);
				final String text = substitutionsCursor.getString(textIndex);
				final String className = substitutionsCursor.getString(classIndex);
				
				Class currentClass = new Class();
				currentClass.setName(className);
				
				List<Substitution> substitutions = plan.get(currentClass);
				if(substitutions == null) {
					substitutions = new ArrayList<Substitution>();
					plan.put(currentClass, substitutions);
				}
				
				Substitution substitution = new Substitution();
				substitution.setPeriodNumber(period);
				substitution.setSubstTeacher(substTeacher);
				substitution.setInstdTeacher(instdTeacher);
				substitution.setInstdSubject(instdSubject);
				substitution.setKind(kind);
				substitution.setText(text);
				
				substitutions.add(substitution);
			}
			
			substitutionsCursor.close();
		}
		
		plansCursor.close();
		database.close();
		return plans;
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
			
			Set<Class> classes = plan.keySet();
			
			for (Class currentClass : classes) {
				final String className = currentClass.getName();
				List<Substitution> substitutions = plan.get(currentClass);
				
				for (Substitution substitution : substitutions) {
					final int period = substitution.getPeriodNumber();
					final String substTeacher = substitution.getSubstTeacher();
					final String instdTeacher = substitution.getInstdTeacher();
					final String instdSubject = substitution.getInstdSubject();
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
