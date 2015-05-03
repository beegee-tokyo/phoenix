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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/** InstalledAppsList
 *
 * array adapter for app list view
 *
 * @author Bernd Giesecke
 * @version 3.0 May 3, 2015.
 */
class InstalledAppsList extends ArrayAdapter<String> {
	/** Application context */
	private final Activity context;
	/** List of app names */
	private final String[] appName;
	/** List of app icons */
	private final Drawable[] appIcon;

	/**
	 * Array adapter for app list view
	 *
	 * @param context
	 *            application context
	 * @param appName
	 *            list of app names
	 * @param appIcon
	 *            list of app icons
	 */
	public InstalledAppsList(Activity context,
	                  String[] appName, Drawable[] appIcon) {
		super(context, R.layout.installed_apps, appName);
		this.context = context;
		this.appName = appName;
		this.appIcon = appIcon;
	}
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		/** Layout inflater for apps list view */
		LayoutInflater inflater = context.getLayoutInflater();
		/** Row inflater */
		@SuppressLint({"ViewHolder", "InflateParams"}) View rowView= inflater.inflate
				(R.layout.installed_apps, null, true);
		/** Text view to show app name */
		TextView txtAppName = (TextView) rowView.findViewById(R.id.tv_app_name);
		/** Image view to show app icon */
		ImageView imageView = (ImageView) rowView.findViewById(R.id.iv_app_icon);
		txtAppName.setText(appName[position]);
		imageView.setImageDrawable(appIcon[position]);
		return rowView;
	}
}
