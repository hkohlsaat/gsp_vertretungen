package org.aweture.wonk.landing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.messaging.FirebaseMessaging;

import org.aweture.wonk.Application;
import org.aweture.wonk.R;
import org.aweture.wonk.background.PlanUpdateReceiver;
import org.aweture.wonk.background.PlanUpdateService;
import org.aweture.wonk.storage.SimpleData;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Activity extends android.app.Activity implements PlanUpdateReceiver.Handler {

    private PlanUpdateReceiver receiver;

    private EditText passwordInput;

    private View loginButton;
    private View progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing);

        receiver = new PlanUpdateReceiver(this);

        passwordInput = (EditText) findViewById(R.id.login_password);
        loginButton = findViewById(R.id.button1);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }


    public void login(View view) {
        String password = passwordInput.getText().toString();

        try {
            String hex = hashToHexString(password);
            if (!hex.equals("518c2a4bba28865df3fc4fa62f09b48ed2d30ed8")) {
                loginFail();
                return;
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException("Failed to hash and compare password.", ex);
        }

        if (!Application.hasConnectivity(this)) {
            noNetwork();
            return;
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, receiver.getIntentFilter());

        Intent intent = new Intent(this, PlanUpdateService.class);
        startService(intent);

        SimpleData data = new SimpleData(this);
        data.setPasswordEntered();

        loginButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void resetPassword() {
        passwordInput.setText("");
    }

    private String hashToHexString(String in) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digester = MessageDigest.getInstance("SHA-1");
        byte[] hash = digester.digest(in.getBytes("UTF-8"));
        StringBuilder builder = new StringBuilder();
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }


    @Override
    public void handleEvent(Intent intent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginSuccess();
            }
        });
    }

    private void loginSuccess() {
        // Register for updates.
        FirebaseMessaging.getInstance().subscribeToTopic("newplan");

        Intent intent = new Intent(this, org.aweture.wonk.substitutions.Activity.class);
        startActivity(intent);
        // Don't add this activity to the backstack.
        finish();
    }


    private void noNetwork() {
        showDialog(R.string.no_network, false, "NoNetwork");
    }

    private void loginFail() {
        showDialog(R.string.login_fail, true, "LoginFail");
    }

    private void showDialog(int messageId, boolean wipePassword, String tag) {
        Bundle arguments = new Bundle();
        String message = getResources().getString(messageId);
        arguments.putString(OkDialog.KEY_MESSAGE, message);
        arguments.putBoolean(OkDialog.KEY_WIPE_PASSWORD, wipePassword);

        OkDialog dialog = new OkDialog();
        dialog.setArguments(arguments);
        dialog.show(getFragmentManager(), tag + "Dialog");
    }
}
