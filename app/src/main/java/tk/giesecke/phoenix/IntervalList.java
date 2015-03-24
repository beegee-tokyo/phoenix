package tk.giesecke.phoenix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/** IntervalList
 *
 * array adapter for interval list view
 *
 * @author Bernd Giesecke
 * @version 1.0a March 23, 2015.
 */
class IntervalList extends ArrayAdapter<String> {
	/** Application context */
	private final Activity context;
	/** List of intervals */
	private final String[] intervalDays;

	/**
	 * Array adapter for time list view
	 *
	 * @param context
	 *            application context
	 * @param intervalDays
	 *            list of intervals
	 */
	public IntervalList(Activity context,
	                String[] intervalDays) {
		super(context, R.layout.intervals, intervalDays);
		this.context = context;
		this.intervalDays = intervalDays;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		/** Layout inflater for interval list view */
		LayoutInflater inflater = context.getLayoutInflater();
		/** Row inflater */
		@SuppressLint({"ViewHolder", "InflateParams"}) View rowView= inflater.inflate
				(R.layout.intervals,	null, true);
		/** Text view to show interval */
		TextView tv_intervals = (TextView) rowView.findViewById(R.id.tv_intervals);
		tv_intervals.setText(intervalDays[position]);
		return rowView;
	}
}