package org.aweture.wonk.landing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class OkDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_WIPE_PASSWORD = "wipe_password";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        String message = arguments.getString(KEY_MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setPositiveButton("Ok", this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Bundle arguments = getArguments();
        boolean wipe = arguments.getBoolean(KEY_WIPE_PASSWORD, true);

        if (wipe) {
            ((Activity) getActivity()).resetPassword();
        }
    }
}