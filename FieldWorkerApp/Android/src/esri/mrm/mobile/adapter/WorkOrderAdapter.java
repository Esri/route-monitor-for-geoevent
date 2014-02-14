package esri.mrm.mobile.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import esri.mrm.mobile.NonServiceWorkOrderType;
import esri.mrm.mobile.R;
import esri.mrm.mobile.StopsConfigurations;
import esri.mrm.mobile.WorkOrder;

public class WorkOrderAdapter extends ArrayAdapter<WorkOrder> {
		private final Activity context;
		private final List<WorkOrder> workorders;
		private final StopsConfigurations stopsConfigs;

		public WorkOrderAdapter(Activity context, List<WorkOrder> workorders, StopsConfigurations stopsConfigs) {
			super(context, R.layout.workorder_item, workorders);
			this.context = context;
			this.workorders = workorders;
			this.stopsConfigs = stopsConfigs;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View rowView = inflater.inflate(R.layout.workorder_item, null, true);
			TextView textViewWorkorderId = (TextView) rowView.findViewById(R.id.workorder_id);
			TextView textView = (TextView) rowView.findViewById(R.id.label);
			TextView textViewAddress = (TextView)rowView.findViewById(R.id.address);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
			WorkOrder workorder = workorders.get(position);
			String workorderId = "#" + workorder.getId();
			textViewWorkorderId.setText(workorderId);
			String s =  "Sched: " + workorder.getScheduledArrival() + " ETA: " + workorder.getProjectedArrival();
			textView.setText(s);
			//Address address = workorder.getAddress();
			//String addressStr = address.getStreet() + ", " + address.getCity() + ", " + address.getState() + " " + address.getZip(); 
			if(workorder.getType().equals(NonServiceWorkOrderType.Break.toString()))
			  textViewAddress.setText("");
			else
			  textViewAddress.setText(workorder.getAddress());
			
			String type = workorder.getType();

			if(workorder.getType().equals(NonServiceWorkOrderType.Break.toString()))
			  imageView.setImageResource(R.drawable.ic_breaktime_w);
			else
			  imageView.setImageResource(R.drawable.ic_ok);

			return rowView;
		}

}
