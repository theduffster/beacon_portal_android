package com.bernard.beaconportal.activities.activity;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.bernard.beaconportal.activities.BaseAccount;
import com.bernard.beaconportal.activities.R;
import com.bernard.beaconportal.activities.provider.UnreadWidgetProvider;

/**
 * Activity to select an account for the unread widget.
 */
public class UnreadWidgetConfiguration extends AccountList {
	/**
	 * Name of the preference file to store the widget configuration.
	 */
	private static final String PREFS_NAME = "unread_widget_configuration.xml";

	/**
	 * Prefix for the preference keys.
	 */
	private static final String PREF_PREFIX_KEY = "unread_widget.";

	/**
	 * The ID of the widget we are configuring.
	 */
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Find the widget ID from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		SharedPreferences sharedpref = getSharedPreferences("actionbar_color",
				Context.MODE_PRIVATE);

		if (!sharedpref.contains("actionbar_color")) {

			getActionBar().setBackgroundDrawable(
					new ColorDrawable(Color.parseColor("#4285f4")));

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

		// If they gave us an intent without the widget ID, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}

		setTitle(R.string.unread_widget_select_account);
	}

	@Override
	protected boolean displaySpecialAccounts() {
		return true;
	}

	@Override
	protected void onAccountSelected(BaseAccount account) {
		// Save widget configuration
		String accountUuid = account.getUuid();
		saveAccountUuid(this, mAppWidgetId, accountUuid);

		// Update widget
		Context context = getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		UnreadWidgetProvider.updateWidget(context, appWidgetManager,
				mAppWidgetId, accountUuid);

		// Let the caller know that the configuration was successful
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

	private static void saveAccountUuid(Context context, int appWidgetId,
			String accountUuid) {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(PREF_PREFIX_KEY + appWidgetId, accountUuid);
		editor.commit();
	}

	public static String getAccountUuid(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		String accountUuid = prefs.getString(PREF_PREFIX_KEY + appWidgetId,
				null);
		return accountUuid;
	}

	public static void deleteWidgetConfiguration(Context context,
			int appWidgetId) {
		Editor editor = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE).edit();
		editor.remove(PREF_PREFIX_KEY + appWidgetId);
		editor.commit();
	}
}
