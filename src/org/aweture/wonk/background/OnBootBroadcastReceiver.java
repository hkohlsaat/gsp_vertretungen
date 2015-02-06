package org.aweture.wonk.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Simple {@link BroadcastReceiver} starting {@link UpdateService}.
 * 
 * @author Hannes Kohlsaat
 *
 */
public class OnBootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		UpdateService.startToUpdate(context);
	}

}
