package org.aweture.wonk.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aweture.wonk.models.Class;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Substitution;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseDataStore implements DataStore {
	
	private Context context;
	private List<Plan> plans;
	
	DatabaseDataStore(Context context) {
		this.context = context;
	}

	@Override
	public synchronized List<Plan> getCurrentPlans() {
		if (plans == null) {
			plans = queryPlans();
		}
		return plans;
	}

	@Override
	public synchronized Plan getPlanByDate(String date) {
		getCurrentPlans();
		for (Plan plan : plans) {
			if (plan.getDate().toDateString().equals(date)) {
				return plan;
			}
		}
		throw new RuntimeException("No such plan found for date: " + date);
	}

	@Override
	public synchronized void savePlans(Plan[] plans) {
		List<Plan> plansList = Arrays.asList(plans);
		insertPlans(plansList);
		this.plans = plansList;
	}
	
	private List<Plan> queryPlans() {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String tableName = DataContract.TableEntry.TABLE_NAME;
		Cursor plansCursor = database.query(tableName, null, null, null, null, null, null);
		
		final int dateIndex = plansCursor.getColumnIndexOrThrow(DataContract.TableEntry.COLUMN_DATE_NAME);
		final int createdIndex = plansCursor.getColumnIndexOrThrow(DataContract.TableEntry.COLUMN_CREATED_NAME);
		final int queriedIndex = plansCursor.getColumnIndexOrThrow(DataContract.TableEntry.COLUMN_QUERIED_NAME);
		
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

			final int periodIndex = substitutionsCursor.getColumnIndexOrThrow(DataContract.SubstitutionEntry.COLUMN_PERIOD_NAME);
			final int substTeacherIndex = substitutionsCursor.getColumnIndexOrThrow(DataContract.SubstitutionEntry.COLUMN_SUBST_TEACHER_NAME);
			final int instdTeacherIndex = substitutionsCursor.getColumnIndexOrThrow(DataContract.SubstitutionEntry.COLUMN_INSTD_TEACHER_NAME);
			final int instdSubjectIndex = substitutionsCursor.getColumnIndexOrThrow(DataContract.SubstitutionEntry.COLUMN_INSTD_SUBJECT_NAME);
			final int kindIndex = substitutionsCursor.getColumnIndexOrThrow(DataContract.SubstitutionEntry.COLUMN_KIND_NAME);
			final int textIndex = substitutionsCursor.getColumnIndexOrThrow(DataContract.SubstitutionEntry.COLUMN_TEXT_NAME);
			final int classIndex = substitutionsCursor.getColumnIndexOrThrow(DataContract.SubstitutionEntry.COLUMN_CLASS_NAME);
			
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

			String tableName = DataContract.TableEntry.TABLE_NAME;
			ContentValues planValues = new ContentValues();
			planValues.put(DataContract.TableEntry.COLUMN_DATE_NAME, dateString);
			planValues.put(DataContract.TableEntry.COLUMN_CREATED_NAME, createdString);
			planValues.put(DataContract.TableEntry.COLUMN_QUERIED_NAME, queriedString);
			database.insert(tableName, null, planValues);
			
			final String substitutionsTableName = "\"" + dateString + "\"";
			database.execSQL("CREATE TABLE IF NOT EXISTS " + substitutionsTableName + " ("
					+ DataContract.SubstitutionEntry.COLUMN_PERIOD_NAME + " " + DataContract.SubstitutionEntry.COLUMN_PERIOD_TYPE + ", "
					+ DataContract.SubstitutionEntry.COLUMN_SUBST_TEACHER_NAME + " " + DataContract.SubstitutionEntry.COLUMN_SUBST_TEACHER_TYPE + ", "
					+ DataContract.SubstitutionEntry.COLUMN_INSTD_TEACHER_NAME + " " + DataContract.SubstitutionEntry.COLUMN_INSTD_TEACHER_TYPE + ", "
					+ DataContract.SubstitutionEntry.COLUMN_INSTD_SUBJECT_NAME + " " + DataContract.SubstitutionEntry.COLUMN_INSTD_SUBJECT_TYPE + ", "
					+ DataContract.SubstitutionEntry.COLUMN_KIND_NAME + " " + DataContract.SubstitutionEntry.COLUMN_KIND_TYPE + ", "
					+ DataContract.SubstitutionEntry.COLUMN_TEXT_NAME + " " + DataContract.SubstitutionEntry.COLUMN_TEXT_TYPE + ", "
					+ DataContract.SubstitutionEntry.COLUMN_CLASS_NAME + " " + DataContract.SubstitutionEntry.COLUMN_CLASS_TYPE + ")");
			
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
					substitutionValues.put(DataContract.SubstitutionEntry.COLUMN_PERIOD_NAME, Integer.valueOf(period));
					substitutionValues.put(DataContract.SubstitutionEntry.COLUMN_SUBST_TEACHER_NAME, substTeacher);
					substitutionValues.put(DataContract.SubstitutionEntry.COLUMN_INSTD_TEACHER_NAME, instdTeacher);
					substitutionValues.put(DataContract.SubstitutionEntry.COLUMN_INSTD_SUBJECT_NAME, instdSubject);
					substitutionValues.put(DataContract.SubstitutionEntry.COLUMN_KIND_NAME, kind);
					substitutionValues.put(DataContract.SubstitutionEntry.COLUMN_TEXT_NAME, text);
					substitutionValues.put(DataContract.SubstitutionEntry.COLUMN_CLASS_NAME, className);
					database.insert(substitutionsTableName, null, substitutionValues);
				}
			}
		}
		database.close();
	}
	
	private void resetDatabase(SQLiteDatabase database) {
		String tableName = DataContract.TableEntry.TABLE_NAME;
		Cursor plansCursor = database.query(tableName, null, null, null, null, null, null);
		
		final int dateIndex = plansCursor.getColumnIndexOrThrow(DataContract.TableEntry.COLUMN_DATE_NAME);
		
		while (plansCursor.moveToNext()) {
			final String substitutionsTableName = "\"" + plansCursor.getString(dateIndex) + "\"";
			database.execSQL("DROP TABLE IF EXISTS " + substitutionsTableName);
		}
		
		plansCursor.close();
		database.delete(tableName, null, null);
	}

}
