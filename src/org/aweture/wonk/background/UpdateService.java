package org.aweture.wonk.background;

import android.app.IntentService;
import android.content.Intent;

public class UpdateService extends IntentService {
	
	public UpdateService() {
		super(UpdateService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		new UpdateProcedure(getApplicationContext()).run();
	}
}
