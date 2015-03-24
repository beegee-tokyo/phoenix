package tk.giesecke.phoenix;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
 * @version 1.0a March 23, 2015.
 */
public class ReBoot extends BroadcastReceiver {
	public ReBoot() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		/** Debug tag */
		final String LOG_TAG = "Phoenix_ReBoot";

		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Received reboot alarm");
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
		String launcherName = mInfo.activityInfo.processName;

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

		// Now try to reboot the device
		try {
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Reboot now");
			/** Process for reboot */
			Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "reboot" });
			proc.waitFor();
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Could not reboot", e);
		}
	}
}
