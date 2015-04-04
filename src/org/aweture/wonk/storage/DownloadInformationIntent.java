package org.aweture.wonk.storage;

import org.aweture.wonk.LogUtil;

import android.content.Intent;
import android.content.IntentFilter;

public class DownloadInformationIntent extends Intent {
	
	public enum DownloadStates {
		/** The download is about to start after informing about it. */
		DOWNLOAD_STARTING,
		/** The download is aborted. */
		DOWNLOAD_ABORTED,
		/** The download completed but the data isn't saved by now */
		DOWNLOAD_COMPLETE,
		/** The just downloaded data is NEW and saved. */
		NEW_DATA_SAVED
	}
	
	private static final String NAME = "download_information";
	private static final String STATE_EXTRA = "state_extra";
	
	public DownloadInformationIntent() {
		super(NAME);
	}
	
	public void setState(DownloadStates state) {
		putExtra(STATE_EXTRA, state);
		LogUtil.d("Creating Intent with state " + state);
	}
	
	public DownloadStates getState() {
		return (DownloadStates) getSerializableExtra(STATE_EXTRA);
	}
	
	public static class DownloadInformationIntentFilter extends IntentFilter {
		
		public DownloadInformationIntentFilter() {
			super(NAME);
		}
	}
}
