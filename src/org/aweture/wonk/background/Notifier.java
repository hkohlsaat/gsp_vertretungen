package org.aweture.wonk.background;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.aweture.wonk.R;
import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.models.Date;
import org.aweture.wonk.models.Plan;
import org.aweture.wonk.models.Substitution;
import org.aweture.wonk.models.SubstitutionsGroup;
import org.aweture.wonk.storage.DataContract.NotifiedSubstitutionColumns;
import org.aweture.wonk.storage.DataStore;
import org.aweture.wonk.storage.DatabaseHelper;
import org.aweture.wonk.storage.SimpleData;
import org.aweture.wonk.substitutions.Activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;

class Notifier {
	
	private Context context;
	private Date now = new Date();

	public void notifyIfNecessary(Context context) {
		this.context = context;
		SimpleData simpleData = new SimpleData(context);
		String filter = simpleData.getFilter("");
		if (!filter.isEmpty()) {
			DataStore ds = new DataStore(context);
			List<Plan> plans = null;
			if (simpleData.isStudent()) {
				plans = ds.getStudentPlans();
			} else {
				plans = ds.getTeacherPlans();
			}
			notify(filter, plans, context);
		}
	}
	
	private void notify(String filter, List<Plan> plans, Context context) {
		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		SQLiteDatabase database = databaseHelper.getWritableDatabase();
		Cursor cursor = database.query(NotifiedSubstitutionColumns.TABLE_NAME, null,
				NotifiedSubstitutionColumns.FILTER.name() + " = ?", new String[]{filter}, null, null, null);
		
		HashMap<String, Int> notifiedSubstCounts = new HashMap<String, Int>(); 
		for (Plan plan : plans) {
			Set<SubstitutionsGroup> sgs = plan.keySet();
			for (SubstitutionsGroup sg : sgs) {
				String baseInData = sg.getBaseInData();
				if (baseInData.equals(filter)) {
					
					final int dateIndex = cursor.getColumnIndexOrThrow(NotifiedSubstitutionColumns.DATE.name());
					final int periodIndex = cursor.getColumnIndexOrThrow(NotifiedSubstitutionColumns.PERIOD.name());

					String date = plan.getDate().toDateString();
					List<Substitution> substitutions = plan.get(sg);
					
					if (dateIsRelevant(date)) {
						for (Substitution substitution : substitutions) {
							int period = substitution.getPeriodNumber();
							
							if (!isAlreadyNotificated(cursor, dateIndex, periodIndex, date, period)) {
								Int notifiedSubstitutionCount = notifiedSubstCounts.get(date);
								if (notifiedSubstitutionCount == null) {
									notifiedSubstitutionCount = new Int();
									notifiedSubstCounts.put(date, notifiedSubstitutionCount);
								}
								notifiedSubstitutionCount.value++;
								saveAsNotificated(database, date, filter, period);
							}
						}
					}
				}
			}
		}
		
		cursor.close();
		
		deleteOldNotifications(database);
		database.close();

		Set<String> dates = notifiedSubstCounts.keySet();
		if (dates.size() > 0) {
			StringBuilder message = new StringBuilder();
			for (String date : dates) {
				int substCount = notifiedSubstCounts.get(date).value;
				message.append(substCount + " ");
				message.append((substCount == 1 ? "Eintrag" : "Einträge")
						+ " am " + date + ",\n");
			}
			int length = message.length();
			if (length >= 2) {
				message.delete(length - 2, length);
			}
			String title = "Neue Vertretungen für \"" + filter + "\"";
			Intent resultIntent = new Intent(context, Activity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationCompat.Builder notificationBuilder =
				    new NotificationCompat.Builder(context)
				    .setSmallIcon(R.drawable.ic_notification)
				    .setContentTitle(title)
				    .setContentText(message)
				    .setContentIntent(pendingIntent)
				    .setColor(context.getResources().getColor(R.color.accent))
				    .setAutoCancel(true);
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(1, notificationBuilder.build());
			LogUtil.logToDB(context, "Notification: " + title + " -- " + message);
		}
	}
	
	private void deleteOldNotifications(SQLiteDatabase db) {
		Cursor cursor = db.query(NotifiedSubstitutionColumns.TABLE_NAME,
				new String[]{NotifiedSubstitutionColumns._ID.name(),
				NotifiedSubstitutionColumns.DATE.name()},
				null, null, null, null, null);
		
		final int dateIndex = cursor.getColumnIndexOrThrow(NotifiedSubstitutionColumns.DATE.name());
		final int idIndex = cursor.getColumnIndexOrThrow(NotifiedSubstitutionColumns._ID.name());
		
		while (cursor.moveToNext()) {
			String date = cursor.getString(dateIndex);
			if (!dateIsRelevant(date)) {
				int id = cursor.getInt(idIndex);
				int count = db.delete(NotifiedSubstitutionColumns.TABLE_NAME, NotifiedSubstitutionColumns._ID.name() + " = " + id, null);
				if (count == 1) {
					LogUtil.logToDB(context, "Deleted notification id=" + id + " from " + date);
				} else {
					LogUtil.logToDB(context, "Failed to delete notification id=" + id + " from " + date);
				}
			}
		}
		cursor.close();
	}
	
	private boolean dateIsRelevant(String date) {
		Date then = Date.fromStringDate(date);
		then.add(Date.HOUR_OF_DAY, 14);
		return now.before(then);
	}

	private boolean isAlreadyNotificated(Cursor cursor, int dateIndex, int periodIndex, String date, int period) {
		boolean isAlreadyNotificated = false;
		while ((!isAlreadyNotificated) && cursor.moveToNext()) {
			isAlreadyNotificated = cursor.getString(dateIndex).equals(date)
							&& cursor.getInt(periodIndex) == period;
		}
		cursor.moveToPosition(-1);
		return isAlreadyNotificated;
	}
	
	private void saveAsNotificated(SQLiteDatabase db, String date, String filter, int period) {
		ContentValues values = new ContentValues();
		values.put(NotifiedSubstitutionColumns.DATE.name(), date);
		values.put(NotifiedSubstitutionColumns.FILTER.name(), filter);
		values.put(NotifiedSubstitutionColumns.PERIOD.name(), period);
		db.insert(NotifiedSubstitutionColumns.TABLE_NAME, null, values);
	}
	
	private class Int {
		public int value = 0;
	}
}
