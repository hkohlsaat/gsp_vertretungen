package org.aweture.wonk;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Scanner;

import org.aweture.wonk.log.LogUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;

public class LicensesDialogFragment extends DialogFragment {
	
	private static final String LICENSES = "licenses.html";
	
	private WebView webView;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		webView = new WebView(getActivity());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.licenses);
		builder.setView(webView);
		return builder.create();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		new LoadTask().execute();
	}
	
	
	private class LoadTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			String html = "";
			InputStream inputStream = null;
			Scanner scanner = null;
			try {
				inputStream = getResources().getAssets().open(LICENSES);
				scanner = new Scanner(inputStream).useDelimiter("\\A");
				
				if (scanner.hasNext()) {
					html = scanner.next();
					html = URLEncoder.encode(html, "utf-8").replaceAll("\\+", "%20");
				}
			} catch (IOException e) {
				LogUtil.e(e);
			} finally {
				if (scanner != null) {
					scanner.close();
				}
				closeResource(inputStream);
			}
			
			return html;
		}
		
		
		@Override
		protected void onPostExecute(String result) {
			webView.loadData(result, "text/html", "utf-8");
			webView.getSettings().setBuiltInZoomControls(true);
			webView.getSettings().setDisplayZoomControls(false);
		}
		
		
		private void closeResource(Closeable c) {
			if (c != null) {
				try {
					c.close();
				} catch (Exception e) {
					LogUtil.e(e);
				}
			}
		}
	}

}
