package org.aweture.wonk.background;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.aweture.wonk.log.LogUtil;
import org.aweture.wonk.storage.PlanStorage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class PlanDownloader {

	public static final String PLAN_DOWNLOADER_FINISHED_DOWNLOADING = "plan_downloader_finished_downloading";

	private URL planURL;

	public PlanDownloader() {
		try {
			planURL = new URL("https://vtr.aweture.org/plan");
		} catch (MalformedURLException e) {
			LogUtil.e(e);
		}
	}

	public void downloadAndSave(Context context) throws IOException {
		String json = download();
		PlanStorage storage = new PlanStorage(context);
		storage.savePlan(json);

		Intent intent = new Intent(PLAN_DOWNLOADER_FINISHED_DOWNLOADING);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		LogUtil.d("Saved new plan, send informing intent.");
	}

	private String download() throws IOException {
		// Open the connection and then connect to the plan location.
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