package org.aweture.wonk.landing;

import org.aweture.wonk.Application;
import org.aweture.wonk.R;
import org.aweture.wonk.background.UpdateScheduler;
import org.aweture.wonk.background.UpdateService;
import org.aweture.wonk.internet.IServManager;
import org.aweture.wonk.internet.IServManager.LoginResult;
import org.aweture.wonk.internet.IServManager21;
import org.aweture.wonk.storage.SimpleData;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class Activity extends android.app.Activity {
	
	private EditText usernameInput, passwordInput;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    getActionBar().hide();
		setContentView(R.layout.activity_landing);
		usernameInput = (EditText) findViewById(R.id.login_username);
		passwordInput = (EditText) findViewById(R.id.login_password);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	public void login(View view) {
		String username = usernameInput.getText().toString();
		String password = passwordInput.getText().toString();
		
		Application application = (Application) getApplication();
		
		if (application.hasConnectivity()) {
			LoginJob login = new LoginJob();
			login.execute(new String[]{username, password});
		} else {
			showDialog(R.string.no_network, false, "NoNetwork");
		}
		
		SimpleData data = SimpleData.getInstance(this);
		data.setUsername(username);
		data.setPassword(password);
	}

	private void loginSuccess() {
		SimpleData data = SimpleData.getInstance(this);
		data.setUserdataInserted();
		startService(new Intent(this, UpdateService.class));
		UpdateScheduler updateScheduler = new UpdateScheduler(this);
		updateScheduler.schedule();
		Intent intent = new Intent(this, org.aweture.wonk.substitutions.Activity.class);
		startActivity(intent);
		finish();
	}

	private void loginNetworkFail() {
		showDialog(R.string.network_fail, false, "NetworkFail");
	}

	private void loginFail() {
		showDialog(R.string.login_fail, true, "LoginFail");
	}
	
	
	private class LoginJob extends AsyncTask<String, Void, IServManager.LoginResult> {
		
		@Override
		protected LoginResult doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			IServManager im = new IServManager21(username, password);
			return im.logIn();
		}
		
		@Override
		protected void onPostExecute(LoginResult result) {
			super.onPostExecute(result);
			if (result == LoginResult.Success) {
				loginSuccess();
			} else if (result == LoginResult.NetworkFail) {
				loginNetworkFail();
			} else if (result == LoginResult.WrongData) {
				loginFail();
			}
		}
	}
	
	private void showDialog(int messageId, boolean wipePassword, String tag) {
		Bundle arguments = new Bundle();
		arguments.putString(OKDialog.KEY_MESSAGE, getResources().getString(messageId));
		arguments.putBoolean(OKDialog.KEY_WIPE_PASSWORD, wipePassword);
		
		OKDialog dialog = new OKDialog();
		dialog.setArguments(arguments);
		dialog.show(getFragmentManager(), tag + "Dialog");
	}
	
	private static class OKDialog extends DialogFragment {
		
		public static final String KEY_MESSAGE = "message";
		public static final String KEY_WIPE_PASSWORD = "wipe";
		
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Bundle arguments = getArguments();
			
			String message = arguments.getString(KEY_MESSAGE);
			final boolean wipeInput = arguments.getBoolean(KEY_WIPE_PASSWORD);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(message);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if (wipeInput) {
						((Activity) getActivity()).passwordInput.setText("");
					}
				}
			});
			return builder.create();
		}
	}
}
