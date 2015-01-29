package com.bernard.beaconportal.activities.mail;

import com.bernard.beaconportal.activities.MAIL;
import com.bernard.beaconportal.activities.R;

public enum ConnectionSecurity {
	NONE(R.string.account_setup_incoming_security_none_label), STARTTLS_REQUIRED(
			R.string.account_setup_incoming_security_tls_label), SSL_TLS_REQUIRED(
			R.string.account_setup_incoming_security_ssl_label);

	private final int mResourceId;

	private ConnectionSecurity(int id) {
		mResourceId = id;
	}

	@Override
	public String toString() {
		return MAIL.app.getString(mResourceId);
	}
}
