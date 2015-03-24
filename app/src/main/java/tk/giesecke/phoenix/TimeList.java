package tk.giesecke.phoenix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/** TimeList
 *
 * array adapter for time list view
 *
 * @author Bernd Giesecke
 * @version 1.0a March 23, 2015.
 */
class TimeList extends ArrayAdapter<String>  {
	/** Application context */
	private final Activity context;
	/** List of times */
	private final String[] resetTime;

	/**
	 * Array adapter for time list view
	 *
	 * @param context
	 *            application context
	 * @param resetTime
	 *            list of reset times
	 */
	public TimeList(Activity context,
	                         String[] resetTime) {
		super(context, R.layout.times, resetTime);
		this.context = context;
		this.resetTime = resetTime;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		/** Layout inflater for time list view */
		LayoutInflater inflater = context.getLayoutInflater();
		/** Row inflater */
		@SuppressLint({"ViewHolder", "InflateParams"}) View rowView= inflater.inflate
				(R.layout.times,	null, true);
		/** Text view to show time */
		TextView tv_times = (TextView) rowView.findViewById(R.id.tv_times);
		tv_times.setText(resetTime[position]);
		return rowView;
	}
}
