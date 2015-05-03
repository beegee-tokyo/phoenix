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

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.List;

/** ReBoot
 *
 * receiver for reboot countdown timer
 *
 * @author Bernd Giesecke
 * @version 4.0 May 3, 2015.
 */
public class ReBoot extends BroadcastReceiver {
	/** Debug tag */
	final static String LOG_TAG = "Phoenix_ReBoot";

	public ReBoot() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Received reboot alarm");

		/** Package name of home launcher */
		String launcherName = getLauncherName(context);

		// Try to get all running apps and kill them before rebooting
		killRunningApps(context, launcherName);

		// Now try to reboot the device
		rebootDevice(context);
	}

	/**
	 * Get launcher name and make it visible
	 * @see <a href="https://www.linkedin.com/groups/How-close-all-activities-in-86481.S.235755042">
	 * Neeraj R. - How-close-all-activities...</a>
	 *
	 * @param context
	 *            application context. Can not be null.
	 */
	public static String getLauncherName(Context context) {
		// Get name of launcher (we don't want to kill him yet!)
		/** Intent for home screen launcher */
		Intent home = new Intent("android.intent.action.MAIN");
		home.addCategory("android.intent.category.HOME");
		// Switch from current foreground task to launcher
		// otherwise we cannot kill the current foreground task
		home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(home);
		/** Package info of home screen launcher */
		final ResolveInfo mInfo = context.getPackageManager().resolveActivity(home, 0);
		/** Package name of home screen launcher */
		return mInfo.activityInfo.processName;
	}

	/**
	 * Kill all running apps
	 * @see <a href="http://stackoverflow.com/questions/6996536/how-to-close-all-active-applications-from-my-android-app/6996635#6996635">
	 * How to close all active applications...</a>
	 *
	 * @param context
	 *            application context. Can not be null.
	 * @param launcherName
	 *            package name of home launcher.
	 */
	public static void killRunningApps(Context context, String launcherName) {
		// Try to get all running apps and kill them before rebooting
		/** Package manager */
		PackageManager pm = context.getPackageManager();
		/** List holding package info of all installed apps */
		List<ApplicationInfo> packages = pm.getInstalledApplications(0);

		/** Activity manager */
		ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context
				.ACTIVITY_SERVICE);

		for (ApplicationInfo packageInfo : packages) {
			// Don't kill ourselves and certain system apps
			if(packageInfo.packageName.equals("tk.giesecke.phoenix") ||
					packageInfo.packageName.equals("system") ||
					packageInfo.packageName.equals("com.android.systemui") ||
					packageInfo.packageName.equals(launcherName)) {
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Not killing "+packageInfo.packageName);
				continue;
			}
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Killing "+packageInfo.packageName);
			mActivityManager.killBackgroundProcesses(packageInfo.packageName);
		}
	}

	/**
	 * Reboot the device
	 * @see <a href="http://stackoverflow.com/questions/4580254/android-2-2-reboot-device-programmatically">
	 * Reboot device programmatically</a>
	 * @see <a href="http://stackoverflow.com/questions/12431304/programmatically-hotboot-hot-reboot-android-device">Programmatically HotBoot</a>
	 *
	 * @param context
	 *            application context. Can not be null.
	 */
	public static void rebootDevice(Context context) {
		// Now try to reboot the device
		/** Access to shared preferences */
		SharedPreferences mPrefs = context.getSharedPreferences("AutoReboot", 0);
		/** Setting for soft or hard reboot */
		boolean isSoftReboot = mPrefs.getBoolean("soft_reboot", false);
		/** Part 1 of reboot command */
		String suCommand1 = "su";
		/** Part 2 of reboot command */
		String suCommand2 = "-c";
		/** Part 3 of reboot command */
		String suCommand3 = "reboot"; // For hard reboot
		if (isSoftReboot) { // If soft reboot is requested change 3rd part of reboot command to
			suCommand3 = "busybox killall system_server";
		}
		/** Complete reboot command */
		String[] rebootCommand = {suCommand1,suCommand2,suCommand3};
		try {
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Reboot now");
			/** Process for reboot */
			Process proc = Runtime.getRuntime().exec(rebootCommand);
			proc.waitFor();
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Could not reboot", e);
		}
	}
}
