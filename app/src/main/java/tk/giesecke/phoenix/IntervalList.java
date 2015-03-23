package tk.giesecke.phoenix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class IntervalList extends ArrayAdapter<String> {
	private final Activity context;
	private final String[] intervalDays;

	public IntervalList(Activity context,
	                String[] intervalDays) {
		super(context, R.layout.intervals, intervalDays);
		this.context = context;
		this.intervalDays = intervalDays;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		@SuppressLint({"ViewHolder", "InflateParams"}) View rowView= inflater.inflate
				(R.layout.intervals,	null, true);
		TextView tv_intervals = (TextView) rowView.findViewById(R.id.tv_intervals);
		tv_intervals.setText(intervalDays[position]);
		return rowView;
	}
}