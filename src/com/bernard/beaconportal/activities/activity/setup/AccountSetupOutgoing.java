package com.bernard.beaconportal.activities.activity.setup;

import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bernard.beaconportal.activities.Account;
import com.bernard.beaconportal.activities.K9;
import com.bernard.beaconportal.activities.Preferences;
import com.bernard.beaconportal.activities.R;
import com.bernard.beaconportal.activities.activity.K9ActivityMaterial;
import com.bernard.beaconportal.activities.activity.setup.AccountSetupCheckSettings.CheckDirection;
import com.bernard.beaconportal.activities.helper.Utility;
import com.bernard.beaconportal.activities.mail.AuthType;
import com.bernard.beaconportal.activities.mail.ConnectionSecurity;
import com.bernard.beaconportal.activities.mail.ServerSettings;
import com.bernard.beaconportal.activities.mail.Transport;
import com.bernard.beaconportal.activities.mail.transport.SmtpTransport;

public class AccountSetupOutgoing extends K9ActivityMaterial implements
		OnClickListener, OnCheckedChangeListener {
	private static final String EXTRA_ACCOUNT = "account";

	private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

	private static final String SMTP_PORT = "587";
	private static final String SMTP_SSL_PORT = "465";

	private EditText mUsernameView;
	private EditText mPasswordView;
	private EditText mServerView;
	private EditText mPortView;
	private CheckBox mRequireLoginView;
	private ViewGroup mRequireLoginSettingsView;
	private Spinner mSecurityTypeView;
	private Spinner mAuthTypeView;
	private ArrayAdapter<AuthType> mAuthTypeAdapter;
	private Button mNextButton;
	private Account mAccount;
	private boolean mMakeDefault;

	public static void actionOutgoingSettings(Context context, Account account,
			boolean makeDefault) {
		Intent i = new Intent(context, AccountSetupOutgoing.class);
		i.putExtra(EXTRA_ACCOUNT, account.getUuid());
		i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
		context.startActivity(i);
	}

	public static void actionEditOutgoingSettings(Context context,
			Account account) {
		context.startActivity(intentActionEditOutgoingSettings(context, account));
	}

	public static Intent intentActionEditOutgoingSettings(Context context,
			Account account) {
		Intent i = new Intent(context, AccountSetupOutgoing.class);
		i.setAction(Intent.ACTION_EDIT);
		i.putExtra(EXTRA_ACCOUNT, account.getUuid());
		return i;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup_outgoing);

		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");

		TextView abTitle = (TextView) findViewById(titleId);
		abTitle.setTextColor(getResources().getColor((R.color.white)));

		SharedPreferences sharedpref = getSharedPreferences("actionbar_color",
				Context.MODE_PRIVATE);

		if (!sharedpref.contains("actionbar_color")) {

			getActionBar().setBackgroundDrawable(
					new ColorDrawable(Color.parseColor("#1976D2")));

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

		}

		android.app.ActionBar bar = getActionBar();

		bar.setIcon(new ColorDrawable(getResources().getColor(
				android.R.color.transparent)));

		String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

		try {
			if (new URI(mAccount.getStoreUri()).getScheme()
					.startsWith("webdav")) {
				mAccount.setTransportUri(mAccount.getStoreUri());
				AccountSetupCheckSettings.actionCheckSettings(this, mAccount,
						CheckDirection.OUTGOING);
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mUsernameView = (EditText) findViewById(R.id.account_username);
		mPasswordView = (EditText) findViewById(R.id.account_password);
		mServerView = (EditText) findViewById(R.id.account_server);
		mPortView = (EditText) findViewById(R.id.account_port);
		mRequireLoginView = (CheckBox) findViewById(R.id.account_require_login);
		mRequireLoginSettingsView = (ViewGroup) findViewById(R.id.account_require_login_settings);
		mSecurityTypeView = (Spinner) findViewById(R.id.account_security_type);
		mAuthTypeView = (Spinner) findViewById(R.id.account_auth_type);
		mNextButton = (Button) findViewById(R.id.next);

		mNextButton.setOnClickListener(this);
		mRequireLoginView.setOnCheckedChangeListener(this);

		ArrayAdapter<ConnectionSecurity> securityTypesAdapter = new ArrayAdapter<ConnectionSecurity>(
				this, android.R.layout.simple_spinner_item,
				ConnectionSecurity.values());
		securityTypesAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSecurityTypeView.setAdapter(securityTypesAdapter);

		mAuthTypeAdapter = AuthType.getArrayAdapter(this);
		mAuthTypeView.setAdapter(mAuthTypeAdapter);

		/*
		 * Calls validateFields() which enables or disables the Next button
		 * based on the fields' validity.
		 */
		TextWatcher validationTextWatcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				validateFields();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		};
		mUsernameView.addTextChangedListener(validationTextWatcher);
		mPasswordView.addTextChangedListener(validationTextWatcher);
		mServerView.addTextChangedListener(validationTextWatcher);
		mPortView.addTextChangedListener(validationTextWatcher);

		/*
		 * Only allow digits in the port field.
		 */
		mPortView.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

		// FIXME: get Account object again?
		accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
		mMakeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false);

		/*
		 * If we're being reloaded we override the original account with the one
		 * we saved
		 */
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
			accountUuid = savedInstanceState.getString(EXTRA_ACCOUNT);
			mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
		}

		try {
			ServerSettings settings = Transport.decodeTransportUri(mAccount
					.getTransportUri());
			String username = settings.username;
			String password = settings.password;

			if (username != null) {
				mUsernameView.setText(username);
				mRequireLoginView.setChecked(true);
			}

			if (password != null) {
				mPasswordView.setText(password);
			}

			updateAuthPlainTextFromSecurityType(settings.connectionSecurity);

			// The first item is selected if settings.authenticationType is null
			// or is not in mAuthTypeAdapter
			int position = mAuthTypeAdapter
					.getPosition(settings.authenticationType);
			mAuthTypeView.setSelection(position, false);

			// Select currently configured security type
			mSecurityTypeView.setSelection(
					settings.connectionSecurity.ordinal(), false);

			/*
			 * Updates the port when the user changes the security type. This
			 * allows us to show a reasonable default which the user can change.
			 * 
			 * Note: It's important that we set the listener *after* an initial
			 * option has been selected by the code above. Otherwise the
			 * listener might be called after onCreate() has been processed and
			 * the current port value set later in this method is overridden
			 * with the default port for the selected security type.
			 */
			mSecurityTypeView
					.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {
							updatePortFromSecurityType();
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) { /* unused */
						}
					});

			if (settings.host != null) {
				mServerView.setText(settings.host);
			}

			if (settings.port != -1) {
				mPortView.setText(Integer.toString(settings.port));
			} else {
				updatePortFromSecurityType();
			}

			validateFields();
		} catch (Exception e) {
			/*
			 * We should always be able to parse our own settings.
			 */
			failure(e);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
	}

	private void validateFields() {
		mNextButton.setEnabled(Utility.domainFieldValid(mServerView)
				&& Utility.requiredFieldValid(mPortView)
				&& (!mRequireLoginView.isChecked() || (Utility
						.requiredFieldValid(mUsernameView) && Utility
						.requiredFieldValid(mPasswordView))));
		Utility.setCompoundDrawablesAlpha(mNextButton,
				mNextButton.isEnabled() ? 255 : 128);
	}

	private void updatePortFromSecurityType() {
		ConnectionSecurity securityType = (ConnectionSecurity) mSecurityTypeView
				.getSelectedItem();
		mPortView.setText(getDefaultSmtpPort(securityType));
		updateAuthPlainTextFromSecurityType(securityType);
	}

	private String getDefaultSmtpPort(ConnectionSecurity securityType) {
		String port;
		switch (securityType) {
		case NONE:
		case STARTTLS_REQUIRED:
			port = SMTP_PORT;
			break;
		case SSL_TLS_REQUIRED:
			port = SMTP_SSL_PORT;
			break;
		default:
			port = "";
			Log.e(K9.LOG_TAG, "Unhandled ConnectionSecurity type encountered");
		}
		return port;
	}

	private void updateAuthPlainTextFromSecurityType(
			ConnectionSecurity securityType) {
		switch (securityType) {
		case NONE:
			AuthType.PLAIN.useInsecureText(true, mAuthTypeAdapter);
			break;
		default:
			AuthType.PLAIN.useInsecureText(false, mAuthTypeAdapter);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
				mAccount.save(Preferences.getPreferences(this));
				finish();
			} else {
				AccountSetupOptions.actionOptions(this, mAccount, mMakeDefault);
				finish();
			}
		}
	}

	protected void onNext() {
		ConnectionSecurity securityType = (ConnectionSecurity) mSecurityTypeView
				.getSelectedItem();
		String uri;
		String username = null;
		String password = null;
		AuthType authType = null;
		if (mRequireLoginView.isChecked()) {
			username = mUsernameView.getText().toString();
			password = mPasswordView.getText().toString();
			authType = (AuthType) mAuthTypeView.getSelectedItem();
		}

		String newHost = mServerView.getText().toString();
		int newPort = Integer.parseInt(mPortView.getText().toString());
		String type = SmtpTransport.TRANSPORT_TYPE;
		ServerSettings server = new ServerSettings(type, newHost, newPort,
				securityType, authType, username, password);
		uri = Transport.createTransportUri(server);
		mAccount.deleteCertificate(newHost, newPort, CheckDirection.OUTGOING);
		mAccount.setTransportUri(uri);
		AccountSetupCheckSettings.actionCheckSettings(this, mAccount,
				CheckDirection.OUTGOING);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.next:
			onNext();
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mRequireLoginSettingsView.setVisibility(isChecked ? View.VISIBLE
				: View.GONE);
		validateFields();
	}

	private void failure(Exception use) {
		Log.e(K9.LOG_TAG, "Failure", use);
		String toastText = getString(R.string.account_setup_bad_uri,
				use.getMessage());

		Toast toast = Toast.makeText(getApplication(), toastText,
				Toast.LENGTH_LONG);
		toast.show();
	}
}
