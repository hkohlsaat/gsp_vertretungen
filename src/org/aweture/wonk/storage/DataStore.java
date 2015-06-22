package org.aweture.wonk.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	
	public static final String NEW_PLANS_ACTION = "org.aweture.wonk.storage.DataStore.NEW_PLANS_ACTION";
	
	private static final Object LOCK_OBJECT = new Object();
	private Context context;
	private List<Plan> studentPlans;
	private List<Plan> teacherPlans;
	
	private Teachers teachers;
	private Subjects subjects;
	
	public DataStore(Context context) {
		this.context = context;
		teachers = new Teachers(context);
		teachers.prefetch();
		subjects = new Subjects(context);
		subjects.prefetch();
	}

	public List<Plan> getStudentPlans() {
		List<Plan> studentPlans = null;
		synchronized (LOCK_OBJECT) {
			studentPlans = this.studentPlans;
		}
		if (studentPlans == null) {
			studentPlans = queryStudentPlans();
			synchronized (LOCK_OBJECT) {
				this.studentPlans = studentPlans;
			}
		}
		return studentPlans;
	}

	public List<Plan> getTeacherPlans() {
		List<Plan> teacherPlans = null;
		synchronized (LOCK_OBJECT) {
			teacherPlans = this.teacherPlans;
		}
		if (teacherPlans == null) {
			teacherPlans = queryTeacherPlans();
			synchronized (LOCK_OBJECT) {
				this.teacherPlans = teacherPlans;
			}
		}
		return teacherPlans;
	}

	public Plan getStudentPlanByDate(String date) {
		for (Plan plan : getStudentPlans()) {
			if (plan.getDate().toDateString().equals(date)) {
				return plan;
			}
		}
		throw new RuntimeException("No such plan found for date: " + date);
	}

	public Plan getTeacherPlanByDate(String date) {
		for (Plan plan : getTeacherPlans()) {
			if (plan.getDate().toDateString().equals(date)) {
				return plan;
			}
		}
		throw new RuntimeException("No such plan found for date: " + date);
	}

	public void savePlans(List<Plan> plansList) {
		insertPlans(plansList);
		synchronized (LOCK_OBJECT) {
			this.studentPlans = null;
			this.teacherPlans = null;
		}
		fireNewPlansBroadcast();
	}

	private List<Plan> queryStudentPlans() {
		List<Plan> plans = new ArrayList<Plan>();
		
		synchronized (LOCK_OBJECT) {
			DatabaseHelper dbHelper = new DatabaseHelper(context);
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			Cursor plansCursor = database.query(TableColumns.TABLE_NAME, null, null, null, null, null, null);
			
			final int dateIndex = plansCursor.getColumnIndexOrThrow(TableColumns.DATE.name());
			final int createdIndex = plansCursor.getColumnIndexOrThrow(TableColumns.CREATED.name());
			final int queriedIndex = plansCursor.getColumnIndexOrThrow(TableColumns.QUERIED.name());
			
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
				
				String tableName = "\"" + dateString + SubstitutionColumns.STUDENT_SUFIX + "\"";
				Cursor substitutionsCursor = database.query(tableName, null, null, null, null, null, null);
	
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
					
					Class currentClass = new Class();
					currentClass.baseUppon(className);
					
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
					substitution.setClassName(className);
					
					substitutions.add(substitution);
				}
				
				substitutionsCursor.close();
			}
			
			plansCursor.close();
			database.close();
		}
		return plans;
	}

	private List<Plan> queryTeacherPlans() {
		List<Plan> plans = new ArrayList<Plan>();
		
		synchronized (LOCK_OBJECT) {
			DatabaseHelper dbHelper = new DatabaseHelper(context);
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			Cursor plansCursor = database.query(TableColumns.TABLE_NAME, null, null, null, null, null, null);
			
			final int dateIndex = plansCursor.getColumnIndexOrThrow(TableColumns.DATE.name());
			final int createdIndex = plansCursor.getColumnIndexOrThrow(TableColumns.CREATED.name());
			final int queriedIndex = plansCursor.getColumnIndexOrThrow(TableColumns.QUERIED.name());
			
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
				
				String tableName = "\"" + dateString + SubstitutionColumns.TEACHER_SUFIX + "\"";
				Cursor substitutionsCursor = database.query(tableName, null, null, null, null, null, null);
	
				final int periodIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.PERIOD.name());
				final int substTeacherIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.SUBST_TEACHER.name());
				final int instdTeacherIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.INSTD_TEACHER.name());
				final int instdSubjectIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.INSTD_SUBJECT.name());
				final int kindIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.KIND.name());
				final int textIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.TEXT.name());
				final int classIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.CLASS.name());
				final int taskProviderIndex = substitutionsCursor.getColumnIndexOrThrow(SubstitutionColumns.TASK_PROVIDER.name());
				
				while (substitutionsCursor.moveToNext()) {
					final int period = substitutionsCursor.getInt(periodIndex);
					final String substTeacherShort = substitutionsCursor.getString(substTeacherIndex);
					final String instdTeacherShort = substitutionsCursor.getString(instdTeacherIndex);
					final String instdSubjectShort = substitutionsCursor.getString(instdSubjectIndex);
					final String kind = substitutionsCursor.getString(kindIndex);
					final String text = substitutionsCursor.getString(textIndex);
					final String className = substitutionsCursor.getString(classIndex);
					final String taskProviderShort = substitutionsCursor.getString(taskProviderIndex);
					
					Teacher instdTeacher = teachers.getTeacher(instdTeacherShort);
					Teacher substTeacher = teachers.getTeacher(substTeacherShort);
					Teacher taskProvider = teachers.getTeacher(taskProviderShort);
					Subject instdSubject = subjects.getSubject(instdSubjectShort);
					
					Substitute substitute = new Substitute();
					if (taskProviderShort.isEmpty()) {
						substitute.baseUppon(substTeacher);
					} else {
						substitute.baseUppon(taskProvider);
					}
					
					List<Substitution> substitutions = plan.get(substitute);
					if(substitutions == null) {
						substitutions = new ArrayList<Substitution>();
						plan.put(substitute, substitutions);
					}
					
					Substitution substitution = new Substitution();
					substitution.setPeriodNumber(period);
					substitution.setSubstTeacher(substTeacher);
					substitution.setInstdTeacher(instdTeacher);
					substitution.setInstdSubject(instdSubject);
					substitution.setKind(kind);
					substitution.setText(text);
					substitution.setClassName(className);
					substitution.setTaskProvider(taskProvider);
					
					substitutions.add(substitution);
				}
				
				substitutionsCursor.close();
			}
			
			plansCursor.close();
			database.close();
		}
		return plans;
	}
	
	private void insertPlans(List<Plan> plans) {
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		dbHelper.resetDatabase(database);
		
		synchronized (LOCK_OBJECT) {
			
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
				
				final String studentPlan = "\"" + dateString + SubstitutionColumns.STUDENT_SUFIX + "\"";
				CreateQuery createQuery = new CreateQuery(studentPlan);
				for (SubstitutionColumns column : SubstitutionColumns.values()) {
					createQuery.addColumn(column.name(), column.type());
				}
				database.execSQL(createQuery.toString());
				
				final String teacherPlan = "\"" + dateString + SubstitutionColumns.TEACHER_SUFIX + "\"";
				createQuery = new CreateQuery(teacherPlan);
				for (SubstitutionColumns column : SubstitutionColumns.values()) {
					createQuery.addColumn(column.name(), column.type());
				}
				database.execSQL(createQuery.toString());
				
				Set<SubstitutionsGroup> substitutionsGroups = plan.keySet();
				
				for (SubstitutionsGroup substitutionsGroup : substitutionsGroups) {
					List<Substitution> substitutions = plan.get(substitutionsGroup);
					
					for (Substitution substitution : substitutions) {
						final int period = substitution.getPeriodNumber();
						final String substTeacher = substitution.getSubstTeacher().getShortName();
						final String instdTeacher = substitution.getInstdTeacher().getShortName();
						final String instdSubject = substitution.getInstdSubject().getAbbreviation();
						final String kind = substitution.getKind();
						final String text = substitution.getText();
						final String compactClassName = substitution.getClassName();
						final String[] classNames = compactClassName.split(",");
						final String compactTaskProvider = substitution.getTaskProvider().getShortName();
						final String[] taskProviders = compactTaskProvider.split(",");
						
						for (String className : classNames) {
							insertSubstitution(database, studentPlan, period,
									substTeacher, instdTeacher, instdSubject,
									kind, text, className, compactTaskProvider);
						}
						for (String taskProvider : taskProviders) {
							if (taskProvider.isEmpty() && substTeacher.isEmpty()) {
								continue;
							}
							insertSubstitution(database, teacherPlan, period,
									substTeacher, instdTeacher, instdSubject,
									kind, text, compactClassName, taskProvider);
						}
						
					}
				}
			}
			database.close();
		}
	}
	
	private void insertSubstitution(SQLiteDatabase db, String tableName, Integer period, 
			String substTeacher, String instdTeacher, String instdSubject, String kind,
			String text, String className, String taskProvider) {
		
		ContentValues substitutionValues = new ContentValues();
		substitutionValues.put(SubstitutionColumns.PERIOD.name(), Integer.valueOf(period));
		substitutionValues.put(SubstitutionColumns.SUBST_TEACHER.name(), substTeacher);
		substitutionValues.put(SubstitutionColumns.INSTD_TEACHER.name(), instdTeacher);
		substitutionValues.put(SubstitutionColumns.INSTD_SUBJECT.name(), instdSubject);
		substitutionValues.put(SubstitutionColumns.KIND.name(), kind);
		substitutionValues.put(SubstitutionColumns.TEXT.name(), text);
		substitutionValues.put(SubstitutionColumns.CLASS.name(), className);
		substitutionValues.put(SubstitutionColumns.TASK_PROVIDER.name(), taskProvider);
		db.insert(tableName, null, substitutionValues);
	}
	
	private void fireNewPlansBroadcast() {
		Intent intent = new Intent(NEW_PLANS_ACTION);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

}
