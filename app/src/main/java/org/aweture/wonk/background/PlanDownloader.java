package org.aweture.wonk.background;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.aweture.wonk.storage.PlanStorage;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class PlanDownloader {

	public static final String ACTION_NEW_PLAN_DOWNLOADED_AND_SAVED = "new_plan_downloaded_and_saved";

	public static final String SERVER_URL_OF_PLAN = "https://vtr.aweture.org/plan";

	/**
	 * downloadAndSave() downloads the current plan from the server and saves it to an instance of
	 * {@link PlanStorage}. After saving, an Intent with the action
	 * {@link #ACTION_NEW_PLAN_DOWNLOADED_AND_SAVED} is broadcasted via {@link LocalBroadcastManager}.
	 *
	 * @throws IOException
     */
	public static void downloadAndSave(Context context) throws IOException {
		// Download the plan as json.
		String json = download();
		// Save the plan to the storage.
		PlanStorage.savePlan(context, json);

		// Fire an intent, because a download was successfully saved.
		Intent intent = new Intent(ACTION_NEW_PLAN_DOWNLOADED_AND_SAVED);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/**
	 * download() downloads the plan in json format and returns it as String.
	 *
	 * @return Plan in json format.
	 * @throws IOException if an error occurs while downloading.
     */
	public static String download() throws IOException {
		// Open the connection and then connect to the plan location.
		URL planURL = new URL(SERVER_URL_OF_PLAN);
		HttpsURLConnection conn = (HttpsURLConnection) planURL.openConnection();
		conn.connect();

		// Read the stream line by line (and set linebreaks after them, which is easier to debug);
		Scanner scanner = new Scanner(conn.getInputStream());
		StringBuilder plan = new StringBuilder();
		while (scanner.hasNextLine()) {
			plan.append(scanner.nextLine() + "\n");
		}

		// Close all things having to be closed.
		scanner.close();
		conn.disconnect();

		return plan.toString();
	}
}
