package com.bernard.beaconportal.activities.activity.setup;

import java.net.URI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bernard.beaconportal.activities.Account;
import com.bernard.beaconportal.activities.MAIL;
import com.bernard.beaconportal.activities.Preferences;
import com.bernard.beaconportal.activities.R;
import com.bernard.beaconportal.activities.activity.MAILActivityMaterial;

/**
 * Prompts the user to select an account type. The account type, along with the
 * passed in email address, password and makeDefault are then passed on to the
 * AccountSetupIncoming activity.
 */
public class AccountSetupAccountType extends MAILActivityMaterial implements
		OnClickListener {
	private static final String EXTRA_ACCOUNT = "account";

	private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

	private Account mAccount;

	private boolean mMakeDefault;

	public static void actionSelectAccountType(Context context,
			Account account, boolean makeDefault) {
		Intent i = new Intent(context, AccountSetupAccountType.class);
		i.putExtra(EXTRA_ACCOUNT, account.getUuid());
		i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
		context.startActivity(i);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup_account_type);
		((Button) findViewById(R.id.pop)).setOnClickListener(this);
		((Button) findViewById(R.id.imap)).setOnClickListener(this);
		((Button) findViewById(R.id.webdav)).setOnClickListener(this);
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");

		TextView abTitle = (TextView) findViewById(titleId);
		abTitle.setTextColor(getResources().getColor((R.color.white)));

		SharedPreferences sharedpref = getSharedPreferences("actionbar_color",
				Context.MODE_PRIVATE);

		if (!sharedpref.contains("actionbar_color")) {

			getActionBar().setBackgroundDrawable(
					new ColorDrawable(Color.parseColor("#4285f4")));

			if (Build.VERSION.SDK_INT >= 21) {
				Window window = getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				window.setStatusBarColor(Color.parseColor("#3367d6"));
			}

		} else {

			String actionbar_colors = sharedpref.getString("actionbar_color",
					null);

			getActionBar().setBackgroundDrawable(

			new ColorDrawable(Color.parseColor(actionbar_colors)));

			final int splitBarId = getResources().getIdentifier(
					"split_action_bar", "id", "android");

			final View splitActionBar = findViewById(splitBarId);

			if (splitActionBar != null) {

				splitActionBar.setBackgroundDrawable(

				new ColorDrawable(Color.parseColor(actionbar_colors)));

			}

			if (Build.VERSION.SDK_INT >= 21) {
				Window window = getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				window.setStatusBarColor(Color.parseColor(actionbar_colors));
			}

		}

		android.app.ActionBar bar = getActionBar();

		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));

		String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
		mMakeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false);
	}

	private void onPop() {
		try {
			URI uri = new URI(mAccount.getStoreUri());
			uri = new URI("pop3+ssl+", uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setStoreUri(uri.toString());

			uri = new URI(mAccount.getTransportUri());
			uri = new URI("smtp+tls+", uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setTransportUri(uri.toString());

			AccountSetupIncoming.actionIncomingSettings(this, mAccount,
					mMakeDefault);
			finish();
		} catch (Exception use) {
			failure(use);
		}

	}

	private void onImap() {
		try {
			URI uri = new URI(mAccount.getStoreUri());
			uri = new URI("imap+ssl+", uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setStoreUri(uri.toString());

			uri = new URI(mAccount.getTransportUri());
			uri = new URI("smtp+tls+", uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setTransportUri(uri.toString());

			AccountSetupIncoming.actionIncomingSettings(this, mAccount,
					mMakeDefault);
			finish();
		} catch (Exception use) {
			failure(use);
		}

	}

	private void onWebDav() {
		try {
			URI uri = new URI(mAccount.getStoreUri());
			uri = new URI("webdav+ssl+", uri.getUserInfo(), uri.getHost(),
					uri.getPort(), null, null, null);
			mAccount.setStoreUri(uri.toString());
			AccountSetupIncoming.actionIncomingSettings(this, mAccount,
					mMakeDefault);
			finish();
		} catch (Exception use) {
			failure(use);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pop:
			onPop();
			break;
		case R.id.imap:
			onImap();
			break;
		case R.id.webdav:
			onWebDav();
			break;
		}
	}

	private void failure(Exception use) {
		Log.e(MAIL.LOG_TAG, "Failure", use);
		String toastText = getString(R.string.account_setup_bad_uri,
				use.getMessage());

		Toast toast = Toast.makeText(getApplication(), toastText,
				Toast.LENGTH_LONG);
		toast.show();
	}
}
