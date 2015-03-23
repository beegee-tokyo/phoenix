package tk.giesecke.phoenix;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class AutoStart extends BroadcastReceiver {

	private static final String LOG_TAG = "AutoStart_Bernd";

	public AutoStart() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Boot completed");
			SharedPreferences mPrefs = context.getSharedPreferences("AutoReboot", 0);
			long rebootSchedule = mPrefs.getLong("schedule", 0);
			int rebootTime = mPrefs.getInt("time", 0);
			int rebootInterval = mPrefs.getInt("interval", 0);
			String packageToStart = mPrefs.getString("package", "");
			String appToStart = mPrefs.getString("app", "");
			if (BuildConfig.DEBUG) {
				Log.d(LOG_TAG, "Day = "+rebootSchedule);
				Log.d(LOG_TAG, "Time = "+rebootTime);
				Log.d(LOG_TAG, "Package = "+packageToStart);
				Log.d(LOG_TAG, "App = "+appToStart);
			}
			if ((rebootSchedule != 0) && (rebootTime != 0) && (!packageToStart.equalsIgnoreCase("")))
			{
				// Check if reboot date was due before reset
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
				Intent rebootIntent = new Intent(context, ReBoot.class);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context, 11121963, rebootIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				AlarmManager alarmManager = (AlarmManager) context.getSystemService
						(Context.ALARM_SERVICE);
				alarmManager.set(AlarmManager.RTC_WAKEUP,
						cur_cal.getTimeInMillis(), pendingIntent);

				// Start application
				Intent packIntent = context.getPackageManager().getLaunchIntentForPackage
						(packageToStart);
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Starting now = "+packageToStart);
				context.startActivity(packIntent);
			}
		}
	}
}
