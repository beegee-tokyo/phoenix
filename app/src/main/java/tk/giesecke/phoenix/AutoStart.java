package tk.giesecke.phoenix;

/**
 * Reboot your phone in a given interval and start an application.
 * Copyright (C) 2015  Bernd Giesecke

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

/** AutoStart
 *
 * receiver for reboot countdown timer
 *
 * @author Bernd Giesecke
 * @version 4.0 May 3, 2015.
 */
public class AutoStart extends BroadcastReceiver {

	/** Debug tag */
	private static final String LOG_TAG = "Phoenix_AutoStart";

	/**
	 * receiver for reboot countdown timer
	 */
	public AutoStart() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Boot completed");
			/** Access to shared preferences */
			SharedPreferences mPrefs = context.getSharedPreferences("AutoReboot", 0);
			/** Time in milliseconds to next reboot */
			long rebootSchedule = mPrefs.getLong("schedule", 0);
			/** Time of day for reboot */
			int rebootTime = mPrefs.getInt("time", 0);
			/** Interval between reboots in days */
			int rebootInterval = mPrefs.getInt("interval", 0);
			/** Package name of the app-to-start */
			String packageToStart = mPrefs.getString("package", "");
			/** App name of the app-to-start */
			String appToStart = mPrefs.getString("app", "");
			/** App name of the app-to-start */
			Boolean hasAppToStart = mPrefs.getBoolean("app_start", true);
			if (BuildConfig.DEBUG) {
				Log.d(LOG_TAG, "Day = "+rebootSchedule);
				Log.d(LOG_TAG, "Time = "+rebootTime);
				Log.d(LOG_TAG, "Package = "+packageToStart);
				Log.d(LOG_TAG, "App = "+appToStart);
				Log.d(LOG_TAG, "App start = "+hasAppToStart);
			}
			if ((rebootSchedule != 0) && (rebootTime != 0))
			{
				// Check if reboot date was due before reset
				/** Calendar for reboot time calculation */
				Calendar cur_cal = new GregorianCalendar();
				cur_cal.setTimeInMillis(System.currentTimeMillis()); // Set calendar to current date/time
				if (cur_cal.getTimeInMillis() > rebootSchedule) {
					// Reboot was after scheduled date/time
					// recalculate the new scheduled reboot date/time
					if (BuildConfig.DEBUG) {
						Log.d(LOG_TAG, "Reboot was on schedule");
						int curYear = cur_cal.get(Calendar.YEAR);
						int curMonth = cur_cal.get(Calendar.MONTH) + 1;
						int curDay = cur_cal.get(Calendar.DAY_OF_MONTH);
						int curHour = cur_cal.get(Calendar.HOUR_OF_DAY);
						int curMinute = cur_cal.get(Calendar.MINUTE);
						int curSecond = cur_cal.get(Calendar.SECOND);
						String curDateTime = String.valueOf(curYear) + "-" +
								String.valueOf(curMonth) + "-" +
								String.valueOf(curDay) + " " +
								String.valueOf(curHour) + ":" +
								String.valueOf(curMinute) + ":" +
								String.valueOf(curSecond);
						Log.d(LOG_TAG, "Current = " + curDateTime);
					}
					cur_cal.add(Calendar.DAY_OF_MONTH,rebootInterval); // add interval days to calendar
					cur_cal.set(Calendar.HOUR_OF_DAY,rebootTime); // set calendar to requested time
					cur_cal.set(Calendar.MINUTE,0);
					cur_cal.set(Calendar.SECOND,0);
				} else {
					// Reboot was before scheduled date/time
					// use existing reboot date/time
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Reboot was before schedule");
					cur_cal.setTimeInMillis(rebootSchedule);
				}
				if (BuildConfig.DEBUG) {
					int curYear = cur_cal.get(Calendar.YEAR);
					int curMonth = cur_cal.get(Calendar.MONTH) + 1;
					int curDay = cur_cal.get(Calendar.DAY_OF_MONTH);
					int curHour = cur_cal.get(Calendar.HOUR_OF_DAY);
					int curMinute = cur_cal.get(Calendar.MINUTE);
					int curSecond = cur_cal.get(Calendar.SECOND);
					String curDateTime = String.valueOf(curYear) + "-" +
							String.valueOf(curMonth) + "-" +
							String.valueOf(curDay) + " " +
							String.valueOf(curHour) + ":" +
							String.valueOf(curMinute) + ":" +
							String.valueOf(curSecond);
					Log.d(LOG_TAG, "Scheduled = " + curDateTime);
				}

				// Start the countdown for the reboot
				/** Reboot.class intent */
				Intent rebootIntent = new Intent(context, ReBoot.class);
				/** Pending intent for alarm manager */
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context, 11121963, rebootIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				/** Alarm manager for reboot countdown */
				AlarmManager alarmManager = (AlarmManager) context.getSystemService
						(Context.ALARM_SERVICE);
				alarmManager.set(AlarmManager.RTC_WAKEUP,
						cur_cal.getTimeInMillis(), pendingIntent);

				// Start application (if requested)
				if (appToStart != null) {
					if (hasAppToStart && !appToStart.equalsIgnoreCase("")) {
						/** app-to-start intent */
						Intent packIntent = context.getPackageManager().getLaunchIntentForPackage
								(packageToStart);
						if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Starting now = "+packageToStart);
						context.startActivity(packIntent);
					}
				}
			}
		}
	}
}
