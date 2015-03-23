package tk.giesecke.phoenix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class TimeList extends ArrayAdapter<String>  {
	private final Activity context;
	private final String[] resetTime;

	public TimeList(Activity context,
	                         String[] resetTime) {
		super(context, R.layout.times, resetTime);
		this.context = context;
		this.resetTime = resetTime;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		@SuppressLint({"ViewHolder", "InflateParams"}) View rowView= inflater.inflate
				(R.layout.times,	null, true);
		TextView tv_times = (TextView) rowView.findViewById(R.id.tv_times);
		tv_times.setText(resetTime[position]);
		return rowView;
	}
}
