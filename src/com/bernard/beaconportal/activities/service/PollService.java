package com.bernard.beaconportal.activities.service;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.bernard.beaconportal.activities.Account;
import com.bernard.beaconportal.activities.MAIL;
import com.bernard.beaconportal.activities.controller.MessagingController;
import com.bernard.beaconportal.activities.controller.MessagingListener;
import com.bernard.beaconportal.activities.helper.power.TracingPowerManager;
import com.bernard.beaconportal.activities.helper.power.TracingPowerManager.TracingWakeLock;

public class PollService extends CoreService {
	private static String START_SERVICE = "com.bernard.beaconportal.activities.service.PollService.startService";
	private static String STOP_SERVICE = "com.bernard.beaconportal.activities.service.PollService.stopService";

	private Listener mListener = new Listener();

	public static void startService(Context context) {
		Intent i = new Intent();
		i.setClass(context, PollService.class);
		i.setAction(PollService.START_SERVICE);
		addWakeLock(context, i);
		context.startService(i);
	}

	public static void stopService(Context context) {
		Intent i = new Intent();
		i.setClass(context, PollService.class);
		i.setAction(PollService.STOP_SERVICE);
		addWakeLock(context, i);
		context.startService(i);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setAutoShutdown(false);
	}

	@Override
	public int startService(Intent intent, int startId) {
		if (START_SERVICE.equals(intent.getAction())) {
			if (MAIL.DEBUG)
				Log.i(MAIL.LOG_TAG, "PollService started with startId = "
						+ startId);

			MessagingController controller = MessagingController
					.getInstance(getApplication());
			Listener listener = (Listener) controller.getCheckMailListener();
			if (listener == null) {
				if (MAIL.DEBUG)
					Log.i(MAIL.LOG_TAG,
							"***** PollService *****: starting new check");
				mListener.setStartId(startId);
				mListener.wakeLockAcquire();
				controller.setCheckMailListener(mListener);
				controller.checkMail(this, null, false, false, mListener);
			} else {
				if (MAIL.DEBUG)
					Log.i(MAIL.LOG_TAG,
							"***** PollService *****: renewing WakeLock");
				listener.setStartId(startId);
				listener.wakeLockAcquire();
			}
		} else if (STOP_SERVICE.equals(intent.getAction())) {
			if (MAIL.DEBUG)
				Log.i(MAIL.LOG_TAG, "PollService stopping");
			stopSelf();
		}

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	class Listener extends MessagingListener {
		HashMap<String, Integer> accountsChecked = new HashMap<String, Integer>();
		private TracingWakeLock wakeLock = null;
		private int startId = -1;

		// wakelock strategy is to be very conservative. If there is any reason
		// to release, then release
		// don't want to take the chance of running wild
		public synchronized void wakeLockAcquire() {
			TracingWakeLock oldWakeLock = wakeLock;

			TracingPowerManager pm = TracingPowerManager
					.getPowerManager(PollService.this);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"PollService wakeLockAcquire");
			wakeLock.setReferenceCounted(false);
			wakeLock.acquire(MAIL.WAKE_LOCK_TIMEOUT);

			if (oldWakeLock != null) {
				oldWakeLock.release();
			}

		}

		public synchronized void wakeLockRelease() {
			if (wakeLock != null) {
				wakeLock.release();
				wakeLock = null;
			}
		}

		@Override
		public void checkMailStarted(Context context, Account account) {
			accountsChecked.clear();
		}

		@Override
		public void checkMailFailed(Context context, Account account,
				String reason) {
			release();
		}

		@Override
		public void synchronizeMailboxFinished(Account account, String folder,
				int totalMessagesInMailbox, int numNewMessages) {
			if (account.isNotifyNewMail()) {
				Integer existingNewMessages = accountsChecked.get(account
						.getUuid());
				if (existingNewMessages == null) {
					existingNewMessages = 0;
				}
				accountsChecked.put(account.getUuid(), existingNewMessages
						+ numNewMessages);
			}
		}

		private void release() {

			MessagingController controller = MessagingController
					.getInstance(getApplication());
			controller.setCheckMailListener(null);
			MailService.saveLastCheckEnd(getApplication());

			MailService.actionReschedulePoll(PollService.this, null);
			wakeLockRelease();
			if (MAIL.DEBUG)
				Log.i(MAIL.LOG_TAG, "PollService stopping with startId = "
						+ startId);

			stopSelf(startId);
		}

		@Override
		public void checkMailFinished(Context context, Account account) {

			if (MAIL.DEBUG)
				Log.v(MAIL.LOG_TAG,
						"***** PollService *****: checkMailFinished");
			release();
		}

		public int getStartId() {
			return startId;
		}

		public void setStartId(int startId) {
			this.startId = startId;
		}
	}

}
