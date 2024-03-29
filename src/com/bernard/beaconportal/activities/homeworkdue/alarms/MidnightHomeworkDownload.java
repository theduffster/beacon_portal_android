package com.bernard.beaconportal.activities.homeworkdue.alarms;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;

import com.bernard.beaconportal.activities.homeworkdue.FragmentsHomeworkDue;
import com.bernard.beaconportal.activities.schedule.view.FragmentsSchedule;
import com.bernard.beaconportal.activities.settings.FragmentSettings;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MidnightHomeworkDownload extends BroadcastReceiver {

	private String due_today_shared, due_today_shared_content;

	public static final String PREF_NAME = "pref_name";

	static final Uri mailAccountsUri = Uri
			.parse("content://com.bernard.beaconportal.activities.messageprovider/accounts/");
	static final String mailUnreadUri = "content://com.bernard.beaconportal.activities.messageprovider/account_unread/";
	static final String mailMessageProvider = "content://com.bernard.beaconportal.activities.messageprovider/";

	ContentObserver contentObserver = null;
	BroadcastReceiver receiver = null;
	IntentFilter filter = null;

	DrawerLayout mDrawerLayout;
	LinearLayout mDrawerLinear;
	TextView mWelcomePerson;
	TextView mWelcome;
	ListView mDrawerList;
	ActionBarDrawerToggle mDrawerToggle;
	String actionbar_colors, actionbar_colorsString;
	String[] title;
	String[] count;
	int[] icon;
	Fragment fragment1 = new FragmentsSchedule();
	Fragment fragment2 = new FragmentsHomeworkDue();
	Fragment fragment3 = new FragmentSettings();
	ProgressDialog LoginDialog;

	private HttpResponse response;

	private String due_tommorow_shared, due_tommorow_shared_content;

	private int shared;

	private Context context;

	private String date;

	@Override
	public void onReceive(Context receive_context, Intent intent) {

		Log.d("Beacon Portal", "alarm activated midnight");

		context = receive_context;

		new Update().execute();

	}

	public class Update extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... urls) {

			SharedPreferences bDay = context.getSharedPreferences("Login_Info",
					Context.MODE_PRIVATE);

			String day1 = Integer.toString(bDay.getInt("Day", 0));

			String year1 = Integer.toString(bDay.getInt("Year", 0));

			String month1 = Integer.toString(1 + bDay.getInt("Month", 0));

			SharedPreferences userName = context.getSharedPreferences(
					"Login_Info", Context.MODE_PRIVATE);

			String day = day1.replaceFirst("^0+(?!$)", "");

			String month = month1.replaceFirst("^0+(?!$)", "");

			String year = year1.replaceFirst("^0+(?!$)", "");

			String birthday = month + "/" + day + "/" + year;

			System.out.println("Birthday = " + birthday);

			String user = userName.getString("username", "");

			// String user = (username).split("@")[0];

			System.out.println("Username = " + user);

			try {

				new BasicHttpContext();

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"http://www.beaconschool.org/~markovic/lincoln.php");

				try {
					// Add your data
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs
							.add(new BasicNameValuePair("username", user));
					nameValuePairs.add(new BasicNameValuePair("birthday",
							birthday));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					// Execute HTTP Post Request
					response = httpclient.execute(httppost);

					Log.d("Http Response:", response.toString());

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}

				try {
					Log.d("receiver", "animation stopped and downloaded file");

					String homework = new Scanner(response.getEntity()
							.getContent(), "UTF-8").useDelimiter("\\A").next();

					// String homework =
					// Html.fromHtml(homework_html).toString();

					SharedPreferences.Editor localEditor = context
							.getSharedPreferences("homework",
									Context.MODE_PRIVATE).edit();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"MM/dd hh:mm a");
					Calendar cal = Calendar.getInstance();
					String downloaded = dateFormat.format(cal.getTime());

					localEditor.putString("homework_content", homework);

					localEditor.putString("download_date", downloaded);

					localEditor.apply();

					SharedPreferences.Editor log = context
							.getSharedPreferences("AlarmDownload",
									Context.MODE_PRIVATE).edit();

					log.putString("date", downloaded);

					log.apply();

					Log.d("receiver", "information given to shared preferences");

					parse_due_tommorow_string();

					parse_due_today_string();

				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {

					parse_due_tommorow_string();

					parse_due_today_string();

					SharedPreferences.Editor localEditor = context
							.getSharedPreferences("homework",
									Context.MODE_PRIVATE).edit();

					localEditor.putString("download_error", "yes");

					localEditor.apply();

					e.printStackTrace();
				}

				catch (NoSuchElementException e) {

					parse_due_tommorow_string();

					parse_due_today_string();

					SharedPreferences.Editor localEditor = context
							.getSharedPreferences("homework",
									Context.MODE_PRIVATE).edit();

					localEditor.putString("download_error", "yes");

					localEditor.apply();

					e.printStackTrace();
				}

				catch (RuntimeException e) {
					parse_due_tommorow_string();

					parse_due_today_string();

					SharedPreferences.Editor localEditor = context
							.getSharedPreferences("homework",
									Context.MODE_PRIVATE).edit();

					localEditor.putString("download_error", "yes");

					localEditor.apply();

					e.printStackTrace();

				}

			} finally {

			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {

			Log.d("sender", "Broadcasting message");

			Intent intent = new Intent("up_navigation");

			intent.putExtra("message", "This is my message!");
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

			SharedPreferences download_error = context.getSharedPreferences(
					"homework", Context.MODE_PRIVATE);

			String error = download_error.getString("download_error", "no");

			String download_date = "Download error, refreshed homework using homework downloaded at "
					+ download_error.getString("download_date", "");

			if (error.equals("yes")) {

				SharedPreferences.Editor localEditor = context
						.getSharedPreferences("homework", Context.MODE_PRIVATE)
						.edit();

				Toast.makeText(context, download_date, Toast.LENGTH_LONG)
						.show();

				localEditor.putString("download_error", "no");

				localEditor.commit();

			}

		}

	}

	public void parse_due_tommorow_string() {

		SharedPreferences Tommorow_Homework = context.getSharedPreferences(
				"homework", Context.MODE_PRIVATE);

		String Due_Tommorow = Tommorow_Homework.getString("homework_content",
				"");

		Due_Tommorow = Due_Tommorow.replaceAll("^\"|\"$", "");

		Due_Tommorow = Due_Tommorow.substring(3);

		Log.d("homework due tommorow", Due_Tommorow);

		InputStream is = new ByteArrayInputStream(Due_Tommorow.getBytes());

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		try {

			int value = 0;
			int state = 0;
			shared = 0;
			int shared_add = 0;
			StringBuilder strb = new StringBuilder();

			while ((value = reader.read()) != -1) {

				char c = (char) value;

				if (c == ',') {
					if (state == 1) {
						state = 2;
					} else {
						state = 0;
						strb.append(c);
					}
				}

				else if (c == '"') {
					if (state == 2) {

						System.out.println("shared_add= " + shared_add + " "
								+ strb);

						String strrr = strb.toString()
								.replaceAll("^\"|\"$", "");

						if (strrr.length() < 3 && isStringNumeric(strrr)) {
							shared_add = 0;
							shared++;

							Log.d("restart", "yes");

							due_tommorow_shared = "due_tommorow"
									+ Integer.toString(shared);

							SharedPreferences Band = context
									.getApplicationContext()
									.getSharedPreferences("last band tommorow",
											Context.MODE_PRIVATE);

							String band = Band.getString("last string",
									"ZZZZZZ");

							SharedPreferences.Editor localEditor = context
									.getSharedPreferences(due_tommorow_shared,
											Context.MODE_PRIVATE).edit();

							localEditor.putString("due_tommorow0", band);

							localEditor.apply();

							shared_add++;
						}

						if (shared_add > 8) {

							SharedPreferences Band = context
									.getSharedPreferences("last band tommorow",
											Context.MODE_PRIVATE);

							SharedPreferences Description = context
									.getSharedPreferences(due_tommorow_shared,
											Context.MODE_PRIVATE);

							String last = Band.getString("last string",
									"ZZZZZZ");

							String description = Description.getString(
									"due_tommorow7", "");

							String fixed = description + last;

							Log.d("fixed", fixed);

							SharedPreferences.Editor localEditor = context
									.getSharedPreferences(due_tommorow_shared,
											Context.MODE_PRIVATE).edit();

							localEditor.putString("due_tommorow7", fixed);

							localEditor.apply();

						}

						SharedPreferences.Editor localEditors = context
								.getSharedPreferences("last band tommorow",
										Context.MODE_PRIVATE).edit();

						localEditors.putString("last string", strrr);

						localEditors.apply();

						due_tommorow_shared = "due_tommorow"
								+ Integer.toString(shared);

						due_tommorow_shared_content = "due_tommorow"
								+ Integer.toString(shared_add);

						String strr = strb.toString().replaceAll("^\"|\"$", "");

						System.out.println("shared_pref= " + strr);

						SharedPreferences.Editor localEditor = context
								.getSharedPreferences(due_tommorow_shared,
										Context.MODE_PRIVATE).edit();

						localEditor
								.putString(due_tommorow_shared_content, strr);

						localEditor.apply();

						System.out.println("shared= " + shared);

						strb.setLength(0);
						state = 0;
						shared_add++;

					} else {
						state = 1;
						strb.append(c);
					}
				} else {
					strb.append(c);
				}

			}

			String strr = strb.toString().replaceAll("^\"|\"$", "");

			System.out.println("shared_pref_final= " + strr);

			due_tommorow_shared_content = "due_tommorow7";

			SharedPreferences.Editor localEditor = context
					.getSharedPreferences(due_tommorow_shared,
							Context.MODE_PRIVATE).edit();

			localEditor.putString(due_tommorow_shared_content, strr);

			localEditor.apply();

			SharedPreferences.Editor localEditor1 = context
					.getSharedPreferences("due_tommorow_counter",
							Context.MODE_PRIVATE).edit();

			localEditor1.putInt("last shared preference", shared);

			localEditor1.apply();

			strb.setLength(0);

			SharedPreferences.Editor localEditors = context
					.getSharedPreferences("last band tommorow",
							Context.MODE_PRIVATE).edit();

			localEditors.clear();

			localEditors.apply();

			Calendar calendar = Calendar.getInstance();

			int day = calendar.get(Calendar.DAY_OF_WEEK);

			if (day == 6) {

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				Calendar c = Calendar.getInstance();

				c.add(Calendar.DATE, 3);

				date = sdf.format(c.getTime());

				System.out.println("friday");

			} else if (day == 7) {

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				Calendar c = Calendar.getInstance();

				c.add(Calendar.DATE, 2);

				date = sdf.format(c.getTime());

				System.out.println("saturday");

			} else {

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				Calendar c = Calendar.getInstance();

				c.add(Calendar.DATE, 1);

				date = sdf.format(c.getTime());

			}

			due_tommorow_shared = "due_tommorow" + Integer.toString(shared + 1);

			SharedPreferences.Editor dummy_item = context.getSharedPreferences(
					due_tommorow_shared, Context.MODE_PRIVATE).edit();

			dummy_item.putString("due_tommorow0", "ZZZZZ");

			dummy_item.putString("due_tommorow1", "2");

			dummy_item.putString("due_tommorow2", "Test");

			dummy_item.putString("due_tommorow3", "Teacher");

			dummy_item.putString("due_tommorow4", "Title");

			dummy_item.putString("due_tommorow5", date);

			dummy_item.putString("due_tommorow6", "Type");

			dummy_item.putString("due_tommorow7", "Description");

			dummy_item.apply();

		} catch (IOException e) {

			e.printStackTrace();
		}

		finally {

		}
	}

	public void parse_due_today_string() {

		SharedPreferences Today_Homework = context.getSharedPreferences(
				"homework", Context.MODE_PRIVATE);

		String Due_Today = Today_Homework.getString("homework_content", "");

		Due_Today = Due_Today.replaceAll("^\"|\"$", "");

		Due_Today = Due_Today.substring(7);

		Log.d("homework due today", Due_Today);

		InputStream is = new ByteArrayInputStream(Due_Today.getBytes());

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		try {

			int value = 0;
			int state = 0;
			shared = 0;
			int shared_add = 0;
			StringBuilder strb = new StringBuilder();

			while ((value = reader.read()) != -1) {

				char c = (char) value;

				if (c == ',') {
					if (state == 1) {
						state = 2;
					} else {
						state = 0;
						strb.append(c);
					}
				}

				else if (c == '"') {
					if (state == 2) {

						System.out.println("shared_add= " + shared_add + " "
								+ strb);

						String strrr = strb.toString()
								.replaceAll("^\"|\"$", "");

						if (strrr.length() < 3 && isStringNumeric(strrr)) {
							shared_add = 0;
							shared++;

							Log.d("restart", "yes");

							due_today_shared = "due_today"
									+ Integer.toString(shared);

							SharedPreferences Band = context
									.getSharedPreferences("last band today",
											Context.MODE_PRIVATE);

							String band = Band.getString("last string",
									"ZZZZZZ");

							SharedPreferences.Editor localEditor = context
									.getSharedPreferences(due_today_shared,
											Context.MODE_PRIVATE).edit();

							localEditor.putString("due_today0", band);

							localEditor.apply();

							shared_add++;
						}

						if (shared_add > 8) {

							SharedPreferences Band = context
									.getSharedPreferences("last band today",
											Context.MODE_PRIVATE);

							SharedPreferences Description = context
									.getSharedPreferences(due_today_shared,
											Context.MODE_PRIVATE);

							String last = Band.getString("last string",
									"ZZZZZZ");

							String description = Description.getString(
									"due_today7", "");

							String fixed = description + last;

							Log.d("fixed", fixed);

							SharedPreferences.Editor localEditor = context
									.getSharedPreferences(due_today_shared,
											Context.MODE_PRIVATE).edit();

							localEditor.putString("due_today7", fixed);

							localEditor.apply();

						}

						SharedPreferences.Editor localEditors = context
								.getSharedPreferences("last band today",
										Context.MODE_PRIVATE).edit();

						localEditors.putString("last string", strrr);

						localEditors.apply();

						due_today_shared = "due_today"
								+ Integer.toString(shared);

						due_today_shared_content = "due_today"
								+ Integer.toString(shared_add);

						String strr = strb.toString().replaceAll("^\"|\"$", "");

						System.out.println("shared_pref= " + strr);

						SharedPreferences.Editor localEditor = context
								.getSharedPreferences(due_today_shared,
										Context.MODE_PRIVATE).edit();

						localEditor.putString(due_today_shared_content, strr);

						localEditor.apply();

						System.out.println("shared= " + shared);

						strb.setLength(0);
						state = 0;
						shared_add++;

					} else {
						state = 1;
						strb.append(c);
					}
				} else {
					strb.append(c);
				}

			}

			String strr = strb.toString().replaceAll("^\"|\"$", "");

			System.out.println("shared_pref_final= " + strr);

			due_today_shared_content = "due_today7";

			SharedPreferences.Editor localEditor = context
					.getSharedPreferences(due_today_shared,
							Context.MODE_PRIVATE).edit();

			localEditor.putString(due_today_shared_content, strr);

			localEditor.apply();

			SharedPreferences.Editor localEditor1 = context
					.getSharedPreferences("due_today_counter",
							Context.MODE_PRIVATE).edit();

			localEditor1.putInt("last shared preference", shared);

			localEditor1.apply();

			strb.setLength(0);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			Calendar c = Calendar.getInstance();

			date = sdf.format(c.getTime());

			due_today_shared = "due_tommorow" + Integer.toString(shared + 1);

			SharedPreferences.Editor dummy_item = context.getSharedPreferences(
					due_today_shared, Context.MODE_PRIVATE).edit();

			dummy_item.putString("due_today0", "ZZZZZ");

			dummy_item.putString("due_today1", "2");

			dummy_item.putString("due_today2", "Test");

			dummy_item.putString("due_today3", "Teacher");

			dummy_item.putString("due_today4", "Title");

			dummy_item.putString("due_today5", date);

			dummy_item.putString("due_today6", "Type");

			dummy_item.putString("due_today7", "Description");

			dummy_item.apply();

		} catch (IOException e) {

			e.printStackTrace();
		}

		finally {

		}
	}

	public static boolean isStringNumeric(String str) {
		DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols
				.getInstance();
		char localeMinusSign = currentLocaleSymbols.getMinusSign();

		try {

			if (!Character.isDigit(str.charAt(0))
					&& str.charAt(0) != localeMinusSign)
				return false;

			boolean isDecimalSeparatorFound = false;
			char localeDecimalSeparator = currentLocaleSymbols
					.getDecimalSeparator();

			for (char c : str.substring(1).toCharArray()) {
				if (!Character.isDigit(c)) {
					if (c == localeDecimalSeparator && !isDecimalSeparatorFound) {
						isDecimalSeparatorFound = true;
						continue;
					}
					return false;
				}
			}
			return true;

		} catch (StringIndexOutOfBoundsException e) {

			e.printStackTrace();

			return false;

		}
	}

}