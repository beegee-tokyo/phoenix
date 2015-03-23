package tk.giesecke.phoenix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class InstalledAppsList extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] appName;
	private final Drawable[] appIcon;

	public InstalledAppsList(Activity context,
	                  String[] appName, Drawable[] appIcon) {
		super(context, R.layout.installed_apps, appName);
		this.context = context;
		this.appName = appName;
		this.appIcon = appIcon;
	}
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		@SuppressLint({"ViewHolder", "InflateParams"}) View rowView= inflater.inflate
				(R.layout.installed_apps, null, true);
		TextView txtAppName = (TextView) rowView.findViewById(R.id.tv_app_name);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.iv_app_icon);
		txtAppName.setText(appName[position]);
		imageView.setImageDrawable(appIcon[position]);
		return rowView;
	}
}
