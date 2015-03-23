package tk.giesecke.phoenix;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Phoenix extends ActionBarActivity implements View
.OnClickListener {

	// For debug uses
	private static final String LOG_TAG = "Phoenix_Aktivity";

	// For access to shared preferences
	private SharedPreferences mPrefs;

	// For display of current settings
	private TextView tv_currApp;
	private TextView tv_currInterval;
	private TextView tv_currTime;

	// For application list handling
	private final ArrayList<String> installedAppNames = new ArrayList<>();
	private final ArrayList<String> installedPackageNames = new ArrayList<>();
	private final ArrayList<Drawable> installedAppIcons = new ArrayList<>();
	private String[] installedAppsNamesArray;
	private String[] installedPacksNamesArray;

	// For reboot day and time calculation
	private String packageToStart;
	private String appToStart;
	private int rebootInterval;
	private int rebootTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().setStatusBarColor(getResources().getColor(R.color.holo_orange_dark));
		}

		setContentView(R.layout.phoenix);

		// Get pointer to shared preferences
		mPrefs = getSharedPreferences("AutoReboot", 0);

		// Get list of current installed launch able apps
		getInstalledApps();

		// Register listeners for the buttons
		ImageButton b_cancel = (ImageButton) this.findViewById(R.id.b_cancel);
		b_cancel.setOnClickListener(this);
		ImageButton b_ok = (ImageButton) this.findViewById(R.id.b_apply);
		b_ok.setOnClickListener(this);
		ImageButton b_clear = (ImageButton) this.findViewById(R.id.b_clear);
		b_clear.setOnClickListener(this);

		// Get pointers for current settings
		tv_currApp = (TextView) this.findViewById(R.id.tv_currApp);
		tv_currInterval = (TextView) this.findViewById(R.id.tv_currInterval);
		tv_currTime = (TextView) this.findViewById(R.id.tv_currTime);

		packageToStart = mPrefs.getString("package","");
		appToStart = mPrefs.getString("app",getResources().getString(R.string.app_name));
		rebootInterval = mPrefs.getInt("interval",99);
		rebootTime = mPrefs.getInt("time",99);

		tv_currApp.setText(appToStart);
		tv_currInterval.setText(String.valueOf(rebootInterval)+" day");
		tv_currTime.setText(String.valueOf(rebootTime)+" o'clock");

		// Show the list of installed apps
		concurrentSort(installedAppNames, installedAppNames, installedAppIcons,installedPackageNames);

		installedAppsNamesArray = new String[installedAppNames.size()];
		installedAppsNamesArray = installedAppNames.toArray(installedAppsNamesArray);
		installedPacksNamesArray = new String[installedAppNames.size()];
		installedPacksNamesArray = installedPackageNames.toArray(installedPacksNamesArray);
		Drawable[] installedAppsIconArray = new Drawable[installedAppIcons.size()];
		installedAppsIconArray = installedAppIcons.toArray(installedAppsIconArray);

		boolean hasHistory = false;
		int nameIndex = 0;
		int intervalIndex = 0;
		int timeIndex = 0;
		if (!packageToStart.equalsIgnoreCase("")) {
			if (installedAppNames.contains(appToStart)) {
				nameIndex = installedAppNames.indexOf(appToStart);
				intervalIndex = rebootInterval-1;
				if (rebootTime == 0) {
					timeIndex=24;
				} else {
					timeIndex = rebootTime-1;
				}
				hasHistory = true;
			}
		}

		InstalledAppsList appsListAdapter = new InstalledAppsList(this,installedAppsNamesArray,
				installedAppsIconArray);

		ListView lv_installedApps = (ListView) findViewById(R.id.lv_installedApps);
		lv_installedApps.setAdapter(appsListAdapter);
		if (hasHistory) {
			lv_installedApps.setItemChecked(nameIndex, true);
			lv_installedApps.smoothScrollToPosition(nameIndex);
		}
		lv_installedApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {
				packageToStart = installedPacksNamesArray[position];
				appToStart = installedAppsNamesArray[position];
				tv_currApp.setText(appToStart);
			}
		});

		// Show the list with selectable reset interval
		String[] intervalDays = getResources().getStringArray(R.array.intervals);
		ListView lv_rebootDay = (ListView) findViewById(R.id.lv_rebootDay);
		IntervalList intervalArrayAdapter = new IntervalList(this,intervalDays);
		lv_rebootDay.setAdapter(intervalArrayAdapter);
		if (hasHistory) {
			lv_rebootDay.setItemChecked(intervalIndex, true);
			lv_rebootDay.smoothScrollToPosition(intervalIndex);
		}
		lv_rebootDay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {
				rebootInterval = (int) id+1;
				tv_currInterval.setText(String.valueOf(rebootInterval)+" day");
			}
		});

		// Show the list with selectable reset times
		String[] resetTime = getResources().getStringArray(R.array.times);
		ListView lv_rebootTimer = (ListView) findViewById(R.id.lv_rebootTimer);
		TimeList resetTimeArrayAdapter = new TimeList(this,resetTime);
		lv_rebootTimer.setAdapter(resetTimeArrayAdapter);
		if (hasHistory) {
			lv_rebootTimer.setItemChecked(timeIndex, true);
			lv_rebootTimer.smoothScrollToPosition(timeIndex);
		}
		lv_rebootTimer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {
				rebootTime = (int) id + 1;
				tv_currTime.setText(String.valueOf(rebootTime) + " o'clock");
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.b_apply:
				// Request for root access
				try {
					Runtime.getRuntime().exec(new String[] { "su" });
				} catch (Exception e) {
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Could not get root access", e);
				}

				// Check if an app was selected
				if (packageToStart.equalsIgnoreCase("")) {
					myAlert(this, getString(R.string.error_txt),
							getString(R.string.miss_app_txt));
					return;
				}
				// Check if an interval was selected
				if (rebootInterval == 99) {
					myAlert(this, getString(R.string.error_txt),
							getString(R.string.miss_int_txt));
					return;
				}
				// Check if a time was selected
				if (rebootTime == 99) {
					myAlert(this, getString(R.string.error_txt),
							getString(R.string.miss_time_txt));
					return;
				}
				// Calculate reboot date and time
				if (rebootTime == 24) { // midnight
					rebootTime = 0; // set time to midnight
				}
				Calendar cur_cal = new GregorianCalendar();
				cur_cal.setTimeInMillis(System.currentTimeMillis()); // Set calendar to current date/time
				cur_cal.add(Calendar.DAY_OF_MONTH,rebootInterval); // add interval days to calendar
				// TODO just for testing, if interval is 1 day, start the reboot the same day
				if (rebootInterval == 1) cur_cal.add(Calendar.DAY_OF_MONTH,(rebootInterval*(-1)));
				cur_cal.set(Calendar.HOUR_OF_DAY,rebootTime); // set calendar to requested time
				cur_cal.set(Calendar.MINUTE,0);
				cur_cal.set(Calendar.SECOND,0);
				if (BuildConfig.DEBUG) {
					cur_cal.setTimeInMillis(System.currentTimeMillis()); // Set calendar to current time
					int curYear = cur_cal.get(Calendar.YEAR);
					int curMonth = cur_cal.get(Calendar.MONTH)+1;
					int curDay = cur_cal.get(Calendar.DAY_OF_MONTH);
					int curHour = cur_cal.get(Calendar.HOUR_OF_DAY);
					int curMinute = cur_cal.get(Calendar.MINUTE);
					int curSecond = cur_cal.get(Calendar.SECOND);
					String curDateTime = String.valueOf(curYear)+"-"+
							String.valueOf(curMonth)+"-"+
							String.valueOf(curDay)+" "+
							String.valueOf(curHour)+":"+
							String.valueOf(curMinute)+":"+
							String.valueOf(curSecond);
					Log.d(LOG_TAG, "Current = "+curDateTime);
					cur_cal.add(Calendar.DAY_OF_MONTH,rebootInterval); // add interval days to calendar
					// TODO just for testing, if interval is 1 day, start the reboot the same day
					if (rebootInterval == 1) cur_cal.add(Calendar.DAY_OF_MONTH,(rebootInterval*(-1)));
					cur_cal.set(Calendar.HOUR_OF_DAY,rebootTime); // set calendar to requested time
					cur_cal.set(Calendar.MINUTE,0);
					cur_cal.set(Calendar.SECOND,0);
					int newYear = cur_cal.get(Calendar.YEAR);
					int newMonth = cur_cal.get(Calendar.MONTH)+1;
					int newDay = cur_cal.get(Calendar.DAY_OF_MONTH);
					int newHour = cur_cal.get(Calendar.HOUR_OF_DAY);
					int newMinute = cur_cal.get(Calendar.MINUTE);
					int newSecond = cur_cal.get(Calendar.SECOND);
					String newDateTime = String.valueOf(newYear)+"-"+
							String.valueOf(newMonth)+"-"+
							String.valueOf(newDay)+" "+
							String.valueOf(newHour)+":"+
							String.valueOf(newMinute)+":"+
							String.valueOf(newSecond);
					Log.d(LOG_TAG, "New = "+newDateTime);
				}
				// Safe the current settings in the shared preferences
				mPrefs.edit().putInt("time", rebootTime).apply();
				mPrefs.edit().putInt("interval", rebootInterval).apply();
				mPrefs.edit().putString("package", packageToStart).apply();
				mPrefs.edit().putString("app", appToStart).apply();
				mPrefs.edit().putLong("schedule",cur_cal.getTimeInMillis()).apply();
				if (BuildConfig.DEBUG) {
					Log.d(LOG_TAG, "Schedule = "+cur_cal.getTimeInMillis());
					Log.d(LOG_TAG, "Time = "+rebootTime);
					Log.d(LOG_TAG, "Interval = "+rebootInterval);
					Log.d(LOG_TAG, "Package = "+ packageToStart);
					Log.d(LOG_TAG, "App = "+ appToStart);
				}
				// Start the countdown for the reboot
				Intent rebootIntent = new Intent(this, ReBoot.class);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						this, 11121963, rebootIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				AlarmManager alarmManager = (AlarmManager) this.getSystemService
						(Context.ALARM_SERVICE);
				alarmManager.set(AlarmManager.RTC_WAKEUP,
						cur_cal.getTimeInMillis(), pendingIntent);

				// Close the app
				finish();
				System.exit(0);
				break;
			case R.id.b_cancel:
				// Close the app
				finish();
				System.exit(0);
				break;
			case R.id.b_clear:
				// Close the app and clear settings
				mPrefs.edit().clear().apply();
				finish();
				System.exit(0);
				break;
		}
	}

	private void getInstalledApps() {
		List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
		String appName;
		String packageName;
		Drawable appIcon;
		int numPacks = 0;
		for(int i=0;i<packs.size();i++) {
			PackageInfo p = packs.get(i);
			if (!isAppLaunchAble(p.packageName)) {
				// apps that cannot be launched will be excluded
				continue;
			}

			appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
			installedAppNames.add(numPacks, appName);
			packageName = p.packageName;
			installedPackageNames.add(numPacks,packageName);
			appIcon = p.applicationInfo.loadIcon(getPackageManager());
			installedAppIcons.add(numPacks, appIcon);
			numPacks++;
		}
	}

	private static <T extends Comparable<T>> void concurrentSort(
			final List<T> key, List<?>... lists){
		// Create a List of indices
		List<Integer> indices = new ArrayList<>();
		for(int i = 0; i < key.size(); i++)
			indices.add(i);

		// Sort the indices list based on the key
		Collections.sort(indices, new Comparator<Integer>(){
			@Override public int compare(Integer i, Integer j) {
				return key.get(i).compareTo(key.get(j));
			}
		});

		// Create a mapping that allows sorting of the List by N swaps.
		// Only swaps can be used since we do not know the type of the lists
		@SuppressLint("UseSparseArrays") Map<Integer,Integer> swapMap = new HashMap<>(indices.size());
		List<Integer> swapFrom = new ArrayList<>(indices.size()),
				swapTo   = new ArrayList<>(indices.size());
		for(int i = 0; i < key.size(); i++){
			int k = indices.get(i);
			while(i != k && swapMap.containsKey(k))
				k = swapMap.get(k);

			swapFrom.add(i);
			swapTo.add(k);
			swapMap.put(i, k);
		}

		// use the swap order to sort each list by swapping elements
		for(List<?> list : lists)
			for(int i = 0; i < list.size(); i++)
				Collections.swap(list, swapFrom.get(i), swapTo.get(i));
	}

	/**
	 * Check if application can be launched
	 *
	 * @param packageName
	 *            package name of application. Can not be null.
	 * @return <code>true</code> if package can be launched.
	 */
	boolean isAppLaunchAble(String packageName) {
		if (packageName == null) {
			return false;
		}
		PackageManager mPackageManager = this.getPackageManager();
		return mPackageManager.getLaunchIntentForPackage(packageName) != null;
	}

	//*******************************************************
	//  Customized alert
	// *******************************************************
	private static void myAlert(Context context, String title, String message) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(context.getResources().getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
}
