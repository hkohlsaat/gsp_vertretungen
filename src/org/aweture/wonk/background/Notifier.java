package org.aweture.wonk.background;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.aweture.wonk.R;
import org.aweture.wonk.log.LogUtil;
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

	public void notifyIfNecessary(Context context) {
		SimpleData simpleData = new SimpleData(context);
		String filter = simpleData.getFilter("");
		if (!filter.isEmpty()) {
			DataStore ds = new DataStore(context);
			List<Plan> plans = ds.getStudentPlans();
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
		
		cursor.close();
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
				    .setAutoCancel(true);
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(1, notificationBuilder.build());
			LogUtil.logToDB(context, "Notification: " + title + " -- " + message);
		}
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
