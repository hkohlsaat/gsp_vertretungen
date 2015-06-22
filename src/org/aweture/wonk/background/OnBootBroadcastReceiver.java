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
		UpdateScheduler updateScheduler = new UpdateScheduler(context);
		if (!updateScheduler.isScheduled()) {
			updateScheduler.schedule();
		}
	}

}
