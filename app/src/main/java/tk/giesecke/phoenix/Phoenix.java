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

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** Phoenix
 *
 * configuration activity
 *
 * @author Bernd Giesecke
 * @version 4.0 May 3, 2015.
 */
public class Phoenix extends ActionBarActivity implements View.OnClickListener {

	/** Debug tag */
	private static final String LOG_TAG = "Phoenix_Aktivity";

	/** Access to shared preferences */
	private SharedPreferences mPrefs;

	/** Textview to show selected app */
	private TextView tv_currApp;
	/** Textview to show selected daily reboot interval */
	private TextView tv_currInterval;
	/** Textview to show selected reboot time of day */
	private TextView tv_currTime;

	/** Array list holding installed app names */
	private final ArrayList<String> installedAppNames = new ArrayList<>();
	/** Array list holding installed package names */
	private final ArrayList<String> installedPackageNames = new ArrayList<>();
	/** Array list holding installed icon drawables */
	private final ArrayList<Drawable> installedAppIcons = new ArrayList<>();
	/** List holding the installed app names */
	private String[] installedAppsNamesArray;
	/** List holding the installed package names */
	private String[] installedPacksNamesArray;

	// For reboot day and time calculation
	/** Selected package name */
	private String packageToStart;
	/** Selected application name */
	private String appToStart;
	/** Selected daily reboot interval */
	private int rebootInterval;
	/** selected reboot time of day */
	private int rebootTime;
	/** selected app to start or none */
	private boolean hasAppToStart;
	/** selected soft or hard reboot */
	private boolean isSoftReboot;

	// For initial display of saved preferences
	/** Saved values available */
	private boolean hasHistory;
	/** Saved interval */
	private int intervalIndex;
	/** Saved time */
	private int timeIndex;
	/** Saved app to start */
	private int nameIndex;
	/** List with available intervals for reboot */
	String[] intervalDays;
	/** List with available time of day for reboot */
	String[] resetTime;

	// For delayed closing of the application
	/** Activity context */
	private static Activity activity;
	/** Boolean for myAlert to close app or not */
	private static boolean doFinish = false;
	/** Boolean for myAlert to reset device or not */
	private static boolean doReset = false;

	/**
	 * configuration activity
	 */
	public Phoenix() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(R.style.AppTheme_Base);

		if (android.os.Build.VERSION.SDK_INT >= 21) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().setStatusBarColor(getResources().getColor(R.color.holo_orange_dark));
		}

		setContentView(R.layout.phoenix);

		// Activate toolbar
		Toolbar actionBar = (Toolbar) findViewById(R.id.toolbar);
		if (actionBar != null) {
			setSupportActionBar(actionBar);
		}

		activity = this;

		// Get pointer to shared preferences
		mPrefs = getSharedPreferences("AutoReboot", 0);

		// Register listeners for the buttons
		/** Button to cancel changes */
		ImageButton b_cancel = (ImageButton) this.findViewById(R.id.b_cancel);
		b_cancel.setOnClickListener(this);
		/** Button to apply action */
		ImageButton b_ok = (ImageButton) this.findViewById(R.id.b_apply);
		b_ok.setOnClickListener(this);
		/** Button to clear data and exit */
		ImageButton b_clear = (ImageButton) this.findViewById(R.id.b_clear);
		b_clear.setOnClickListener(this);
		/** Button to test reboot function */
		Button b_testReboot = (Button) this.findViewById(R.id.b_testReboot);
		b_testReboot.setOnClickListener(this);
		/** Button to show interval dialog on small screens */
		Button b_rebootDay = (Button) this.findViewById(R.id.b_rebootDay);
		b_rebootDay.setOnClickListener(this);
		/** Button to show time of day dialog on small screens */
		Button b_rebootTime = (Button) this.findViewById(R.id.b_rebootTime);
		b_rebootTime.setOnClickListener(this);

		// Get pointers for current settings
		tv_currApp = (TextView) this.findViewById(R.id.tv_currApp);
		tv_currInterval = (TextView) this.findViewById(R.id.tv_currInterval);
		tv_currTime = (TextView) this.findViewById(R.id.tv_currTime);

		packageToStart = mPrefs.getString("package","");
		appToStart = mPrefs.getString("app","");
		rebootInterval = mPrefs.getInt("interval", 1);
		rebootTime = mPrefs.getInt("time",1);
		hasAppToStart = mPrefs.getBoolean("app_start",true);
		isSoftReboot = mPrefs.getBoolean("soft_reboot",false);

		// Get list of current installed launch able apps
		getInstalledApps();

		// Show the list of installed apps
		concurrentSort(installedAppNames, installedAppNames, installedAppIcons, installedPackageNames);

		installedAppsNamesArray = new String[installedAppNames.size()];
		installedAppsNamesArray = installedAppNames.toArray(installedAppsNamesArray);
		installedPacksNamesArray = new String[installedAppNames.size()];
		installedPacksNamesArray = installedPackageNames.toArray(installedPacksNamesArray);
		/** List holding the application icons */
		Drawable[] installedAppsIconArray = new Drawable[installedAppIcons.size()];
		installedAppsIconArray = installedAppIcons.toArray(installedAppsIconArray);

		hasHistory = false;
		nameIndex = 0;
		intervalIndex = rebootInterval-1;
		if (rebootTime == 0) {
			timeIndex=23;
		} else {
			timeIndex = rebootTime-1;
		}
		if (!packageToStart.equalsIgnoreCase("")) {
			if (installedAppNames.contains(appToStart)) {
				nameIndex = installedAppNames.indexOf(appToStart);
				hasHistory = true;
			}
		}

		/** ListView adapter for app list */
		InstalledAppsList appsListAdapter = new InstalledAppsList(this,installedAppsNamesArray,
				installedAppsIconArray);

		/** pointer to ListView for app list */
		ListView lv_installedApps = (ListView) findViewById(R.id.lv_installedApps);
		lv_installedApps.setAdapter(appsListAdapter);
		lv_installedApps.setItemChecked(nameIndex, true);
		lv_installedApps.smoothScrollToPosition(nameIndex);
		lv_installedApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			                        int position, long id) {
				packageToStart = installedPacksNamesArray[position];
				appToStart = installedAppsNamesArray[position];
				tv_currApp.setText(appToStart);
				nameIndex = position;
			}
		});

		// Initialize the NumberPicker with selectable reset interval
		NumberPicker.OnValueChangeListener onIntervalChanged
				=new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(
					NumberPicker picker,
					int oldVal,
					int newVal) {
				String[] values=picker.getDisplayedValues();
				rebootInterval = newVal+1;
				intervalIndex = newVal;
				tv_currInterval.setText(values[newVal]);
			}
		};

		// Get list with available intervals for reboot
		intervalDays = getResources().getStringArray(R.array.intervals);
		/** pointer to NumberPicker for interval list */
		NumberPicker np_rebootDay=
				(NumberPicker) findViewById(R.id.np_rebootDay);
		np_rebootDay.setSaveFromParentEnabled(false);
		np_rebootDay.setSaveEnabled(true);
		np_rebootDay.setMaxValue(intervalDays.length-1);
		np_rebootDay.setMinValue(0);
		np_rebootDay.setDisplayedValues(intervalDays);
		np_rebootDay.setOnValueChangedListener(onIntervalChanged);
		np_rebootDay.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		np_rebootDay.setValue(intervalIndex);

		// Initialize the NumberPicker with selectable time of days
		NumberPicker.OnValueChangeListener onTimeChanged
				=new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(
					NumberPicker picker,
					int oldVal,
					int newVal) {
				String[] values=picker.getDisplayedValues();
				rebootTime = newVal + 1;
				timeIndex = newVal;
				tv_currTime.setText(values[newVal]);
			}
		};

		// Get list with available time of day for reboot
		resetTime = getResources().getStringArray(R.array.times);
		/** pointer to NumberPicker for time of day list */
		NumberPicker np_rebootTime=
				(NumberPicker) findViewById(R.id.np_rebootTime);
		np_rebootTime.setSaveFromParentEnabled(false);
		np_rebootTime.setSaveEnabled(true);
		np_rebootTime.setMaxValue(resetTime.length-1);
		np_rebootTime.setMinValue(0);
		np_rebootTime.setDisplayedValues(resetTime);
		np_rebootTime.setOnValueChangedListener(onTimeChanged);
		np_rebootTime.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		np_rebootTime.setValue(timeIndex);

		// Show the Switch for app autostart selection
		/** pointer to Switch for app autostart selection */
		Switch sw_autoStart = (Switch) findViewById(R.id.sw_autoStart);
		sw_autoStart.setChecked(hasAppToStart);
		sw_autoStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
			                             boolean isChecked) {
				hasAppToStart = isChecked;
			}
		});

		// Show the Switch for soft or hard reboot selection
		/** pointer to Switch for soft or hard reboot selection */
		Switch sw_softReboot = (Switch) findViewById(R.id.sw_softReboot);
		sw_softReboot.setChecked(isSoftReboot);
		sw_softReboot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
			                             boolean isChecked) {
				isSoftReboot = isChecked;
			}
		});

		// Show current selection
		tv_currApp.setText(appToStart);
		tv_currInterval.setText(intervalDays[rebootInterval-1]);
		if (rebootTime == 0) {
			tv_currTime.setText(resetTime[23]);
		} else {
			tv_currTime.setText(resetTime[rebootTime-1]);
		}

		// Activate the advertisements
		// Enable access to internet
		if (android.os.Build.VERSION.SDK_INT > 9) {
			/** ThreadPolicy to get permission to access internet */
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		/** View for Google Adsense ads */
		AdView mAdView = (AdView) findViewById(R.id.adView);
		/** Request for ad from Google */
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			/**
			 * apply
			 * requests root access (needs root and su installed)
			 * checks selected app, interval and time
			 * info is saved in shared preferences
			 * start countdown timer to initiate reboot
			 */
			case R.id.b_apply:
				// Request for root access
				try {
					Runtime.getRuntime().exec(new String[] { "su" });
				} catch (Exception e) {
					if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Could not get root access", e);
				}

				// Check if an app was selected
				if (packageToStart.equalsIgnoreCase("") && hasAppToStart) {
					myAlert(this, getString(R.string.error_txt),
							getString(R.string.miss_app_txt), false);
					return;
				}
				// Check if an interval was selected
				if (rebootInterval == 99) {
					myAlert(this, getString(R.string.error_txt),
							getString(R.string.miss_int_txt), false);
					return;
				}
				// Check if a time was selected
				if (rebootTime == 99) {
					myAlert(this, getString(R.string.error_txt),
							getString(R.string.miss_time_txt), false);
					return;
				}
				// Calculate reboot date and time
				if (rebootTime == 24) { // midnight
					rebootTime = 0; // set time to midnight
				}
				/** Calendar for reboot time calculation */
				Calendar cur_cal = new GregorianCalendar();
				cur_cal.setTimeInMillis(System.currentTimeMillis()); // Set calendar to current date/time
				/** Current hour */
				int curHour = cur_cal.get(Calendar.HOUR_OF_DAY);
				cur_cal.add(Calendar.DAY_OF_MONTH,rebootInterval); // add interval days to calendar
				// if daily reboot and reboot time is after current time then reboot 1st time today
				if ((rebootInterval == 1) && (curHour < rebootTime)) {
					cur_cal.add(Calendar.DAY_OF_MONTH,(rebootInterval*(-1)));
				}
				cur_cal.set(Calendar.HOUR_OF_DAY,rebootTime); // set calendar to requested time
				cur_cal.set(Calendar.MINUTE,0);
				cur_cal.set(Calendar.SECOND,0);
				if (BuildConfig.DEBUG) {
					cur_cal.setTimeInMillis(System.currentTimeMillis()); // Set calendar to current time
					int curYear = cur_cal.get(Calendar.YEAR);
					int curMonth = cur_cal.get(Calendar.MONTH)+1;
					int curDay = cur_cal.get(Calendar.DAY_OF_MONTH);
					curHour = cur_cal.get(Calendar.HOUR_OF_DAY);
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
					if ((rebootInterval == 1) && (curHour < rebootTime)) {
						cur_cal.add(Calendar.DAY_OF_MONTH,(rebootInterval*(-1)));
					}
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
				mPrefs.edit().putLong("schedule", cur_cal.getTimeInMillis()).apply();
				mPrefs.edit().putBoolean("app_start", hasAppToStart).apply();
				mPrefs.edit().putBoolean("soft_reboot",isSoftReboot).apply();
				if (BuildConfig.DEBUG) {
					Log.d(LOG_TAG, "Schedule = "+cur_cal.getTimeInMillis());
					Log.d(LOG_TAG, "Time = "+rebootTime);
					Log.d(LOG_TAG, "Interval = "+rebootInterval);
					Log.d(LOG_TAG, "Package = "+ packageToStart);
					Log.d(LOG_TAG, "App = "+ appToStart);
					Log.d(LOG_TAG, "App start = "+ mPrefs.getBoolean("app_start",
							true)+" hasAppToStart = "+hasAppToStart);
					Log.d(LOG_TAG, "Soft reboot = "+ mPrefs.getBoolean("soft_reboot",
							true)+" isSoftReboot = "+isSoftReboot);
				}
				// Start the countdown for the reboot
				/** Reboot.class intent */
				Intent rebootIntent = new Intent(this, ReBoot.class);
				/** Pending intent for alarm manager */
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						this, 11121963, rebootIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				/** Alarm manager for reboot countdown */
				AlarmManager alarmManager = (AlarmManager) this.getSystemService
						(Context.ALARM_SERVICE);
				alarmManager.set(AlarmManager.RTC_WAKEUP,
						cur_cal.getTimeInMillis(), pendingIntent);

				String alertApply = getString(R.string.app_name) + " " +getString(R.string
						.apply_dialog1) + " " + intervalDays[rebootInterval-1] + " ";

				if (rebootTime == 0) {
					alertApply = alertApply+resetTime[23];
				} else {
					alertApply = alertApply+resetTime[rebootTime-1];
				}

				if (hasAppToStart) {
					alertApply = alertApply+" "+getString(R.string.apply_dialog2)+" "+appToStart;
				}
				alertApply = alertApply+".";
				doFinish = true;
				myAlert(this, getString(R.string.app_name),alertApply, true);
				break;
			/**
			 * cancel
			 * just close the app
			 * no stored data is changed
			 */
			case R.id.b_cancel:
				String alertCancel = getString(R.string.cancel_dialog) + " " +getString(R.string
						.app_name) + " " +getString(R.string
						.apply_dialog1) + " " + intervalDays[(mPrefs.getInt("interval",1))-1] + " ";
				rebootTime = mPrefs.getInt("time",1);
				if (rebootTime == 0) {
					alertCancel = alertCancel+resetTime[23];
				} else {
					alertCancel = alertCancel+resetTime[rebootTime-1];
				}

				if (hasAppToStart) {
					alertCancel = alertCancel+" "+getString(R.string.apply_dialog2)+" "+mPrefs.getString("app","");
				}
				alertCancel = alertCancel+".";
				doFinish = true;
				myAlert(this, getString(R.string.app_name),alertCancel, true);
				break;
			/**clear
			 * deletes all stored data
			 * stop eventually running countdown timer for reboot
			 * close the app
			 */
			case R.id.b_clear:
				rebootIntent = new Intent(this, ReBoot.class);
				pendingIntent = PendingIntent.getBroadcast(
						this, 11121963, rebootIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				alarmManager = (AlarmManager) this.getSystemService
						(Context.ALARM_SERVICE);
				alarmManager.cancel(pendingIntent);
				mPrefs.edit().clear().apply();
				String alertClear = getString(R.string.clear_dialog);
				doFinish = true;
				myAlert(this, getString(R.string.app_name), alertClear, true);
				break;
			/**rebootDay
			 * Show dialog for interval settings on small screens
			 */
			case R.id.b_rebootDay:
				AlertDialog.Builder intervalListBuilder = new AlertDialog.Builder(this);
				LayoutInflater intervalListInflater = getLayoutInflater();
				@SuppressLint("InflateParams") View intervalListView = intervalListInflater.inflate(R.layout.intervals, null);
				intervalListBuilder.setView(intervalListView);
				AlertDialog intervalList = intervalListBuilder.create();
				intervalList.setTitle(getString(R.string.rebootDay));

				intervalList.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.close_txt),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						});

				intervalList.show();
				// Initialize the NumberPicker with selectable reset interval
				NumberPicker.OnValueChangeListener onIntervalChanged
						=new NumberPicker.OnValueChangeListener() {
					@Override
					public void onValueChange(
							NumberPicker picker,
							int oldVal,
							int newVal) {
						String[] values=picker.getDisplayedValues();
						rebootInterval = newVal+1;
						tv_currInterval.setText(values[newVal]);
					}
				};

				/** List with available intervals for reboot */
				String[] intervalDays = getResources().getStringArray(R.array.intervals);
				/** pointer to NumberPicker for interval list */
				NumberPicker np_rebootDay=
						(NumberPicker) intervalListView.findViewById(R.id.np_rebootDay);
				np_rebootDay.setMaxValue(intervalDays.length-1);
				np_rebootDay.setMinValue(0);
				np_rebootDay.setDisplayedValues(intervalDays);
				np_rebootDay.setOnValueChangedListener(onIntervalChanged);
				np_rebootDay.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				if (hasHistory) {
					np_rebootDay.setValue(intervalIndex);
				}
				break;
			/**rebootTime
			 * Show dialog for time of day settings on small screens
			 */
			case R.id.b_rebootTime:
				AlertDialog.Builder timeListBuilder = new AlertDialog.Builder(this);
				LayoutInflater timeListInflater = getLayoutInflater();
				@SuppressLint("InflateParams") View timeListView = timeListInflater.inflate(R.layout.times, null);
				timeListBuilder.setView(timeListView);
				AlertDialog timeList = timeListBuilder.create();
				timeList.setTitle(getString(R.string.rebootTime));

				timeList.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.close_txt),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						});

				timeList.show();
				// Initialize the NumberPicker with selectable time of days
				NumberPicker.OnValueChangeListener onTimeChanged
						=new NumberPicker.OnValueChangeListener() {
					@Override
					public void onValueChange(
							NumberPicker picker,
							int oldVal,
							int newVal) {
						String[] values=picker.getDisplayedValues();
						rebootTime = newVal + 1;
						tv_currTime.setText(values[newVal]);
					}
				};

				/** List with available time of day for reboot */
				String[] resetTime = getResources().getStringArray(R.array.times);
				/** pointer to NumberPicker for time of day list */
				NumberPicker np_rebootTime=
						(NumberPicker) timeListView.findViewById(R.id.np_rebootTime);
				np_rebootTime.setMaxValue(resetTime.length-1);
				np_rebootTime.setMinValue(0);
				np_rebootTime.setDisplayedValues(resetTime);
				np_rebootTime.setOnValueChangedListener(onTimeChanged);
				np_rebootTime.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				if (hasHistory) {
					np_rebootTime.setValue(timeIndex);
				}
				break;
			/**testReboot
			 * tests if this device allows reboots
			 * ATTENTION, if this device allows reboots, it will actually reboot now!
			 */
			case R.id.b_testReboot:
				if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Testing reboot");
				doReset = true;
				myAlert(this, getString(R.string.app_name), getString(R.string.alertReset), true);
				break;
		}
	}

	/**
	 * Get a list of all installed packages
	 * @see <a href="http://www.androidsnippets
	 * .com/get-installed-applications-with-name-package-name-version-and-icon">Get installed
	 * Applications...</a>
	 *  <p> installedAppNames => storage for the app names.
	 *  <p> installedPackageNames => storage for the package names.
	 *  <p> installedAppIcons => storage for the app icons.
	 */
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

	/**
	 * Sort lists in the same order as the key list
	 * @see <a href="https://ideone.com/cXdw6T">ideone.com - Sort multiple lists</a>
	 *
	 * @param key
	 *            list that is used for sorting
	 * @param lists
	 *            lists that are sorted in the same order as the key list
	 */
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
	 * @see <a href="http://stackoverflow
	 * .com/questions/2695746/how-to-get-a-list-of-installed-android-applications-and-pick-one-to
	 * -run">rashant Agrawal - You can Find the List of installed apps ... ,
	 * can start application</a>
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

	/**
	 * Display custom alert dialog
	 *
	 * @param context
	 *            application context. Can not be null.
	 * @param title
	 *            title of the dialog box.
	 * @param message
	 *            text inside the dialog box.
	 */
	private static void myAlert(Context context, String title, String message,
	                            boolean hasCancel) {

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
								if (doFinish) {
									activity.finish();
									System.exit(0);
								}
								if (doReset) {
									if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Testing reboot");

									/** Package name of home launcher */
									String launcherName = ReBoot.getLauncherName(activity);

									// Try to get all running apps and kill them before rebooting
									ReBoot.killRunningApps(activity, launcherName);

									// Now try to reboot the device
									ReBoot.rebootDevice(activity);
								}
							}
						});
		if (hasCancel) {
			alertDialogBuilder.setNegativeButton(context.getResources().getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							doReset = false;
						}
					});
		}

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
}
