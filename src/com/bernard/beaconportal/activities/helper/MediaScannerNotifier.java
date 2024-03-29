package com.bernard.beaconportal.activities.helper;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class MediaScannerNotifier implements MediaScannerConnectionClient {
	private MediaScannerConnection mConnection;
	private File mFile;
	private Context mContext;

	public MediaScannerNotifier(Context context, File file) {
		mFile = file;
		mConnection = new MediaScannerConnection(context, this);
		mConnection.connect();
		mContext = context;

	}

	@Override
	public void onMediaScannerConnected() {
		mConnection.scanFile(mFile.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		try {
			if (uri != null) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				mContext.startActivity(intent);
			}
		} finally {
			mConnection.disconnect();
		}
	}
}
