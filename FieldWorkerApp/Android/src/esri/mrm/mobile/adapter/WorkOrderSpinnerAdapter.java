package esri.mrm.mobile.adapter;

import java.util.List;

import esri.mrm.mobile.R;
import esri.mrm.mobile.WorkOrder;
import esri.mrm.mobile.WorkOrderStatus;
import esri.mrm.mobile.R.drawable;
import esri.mrm.mobile.R.id;
import esri.mrm.mobile.R.layout;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WorkOrderSpinnerAdapter extends ArrayAdapter<WorkOrder> {
		private final Activity context;
		private final List<WorkOrder> workorders;

		public WorkOrderSpinnerAdapter(Activity context, List<WorkOrder> workorders) {
			super(context, R.layout.workorder_spinner_item, workorders);
			this.context = context;
			this.workorders = workorders;
		}
		
		public int getCount() {
	       return workorders.size();
	    }

	    public WorkOrder getItem(int position) {
	       return workorders.get(position);
	    }

	    public long getItemId(int position) {
	       return position;
	    }		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View rowView = inflater.inflate(R.layout.workorder_spinner_item, null, true);
			TextView textViewWorkorderId = (TextView) rowView.findViewById(R.id.workorder_id);
//			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
			WorkOrder workorder = workorders.get(position);
			String workorderId = "#" + workorder.getSequence() + " " + workorder.getStopName();
			textViewWorkorderId.setText(workorderId);
			//Status
			TextView textViewStatus = (TextView) rowView.findViewById(R.id.workorder_status);
			textViewStatus.setText(workorder.getStatus());
			
			String str = "";
			if(workorder.getStatus().equals(WorkOrderStatus.Completed.toString()) || workorder.getStatus().equals(WorkOrderStatus.Exception.toString()))
			  str = "Departed: " + workorder.getActualDeparture();
			else if (workorder.getStatus().equals(WorkOrderStatus.AtStop.toString()))
			  str = "Projected Departure: " + workorder.getProjectedDeparture();
			else
			  str = "ETA: " + workorder.getEtaTime() + "  Duration: " + workorder.getScheduledDuration();
			//String eta =  "ETA: " + workorder.getEtaTime();
			TextView textViewEta = (TextView) rowView.findViewById(R.id.eta);
			textViewEta.setText(str);

			return rowView;
		}
		
		// And here is when the "chooser" is popped up
	    // Normally is the same view, but you can customize it if you want
	    @Override
	    public View getDropDownView(int position, View convertView,
	            ViewGroup parent) {
//			LayoutInflater inflater = context.getLayoutInflater();
//			View rowView = inflater.inflate(R.layout.workorder_spinner_item, null, true);
//			TextView textViewWorkorderId = (TextView) rowView.findViewById(R.id.workorder_id);
////			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
//			WorkOrder workorder = workorders.get(position);
//			String workorderId = "#" + workorder.getId();
//			textViewWorkorderId.setText(workorderId);
//			String eta =  "\t\t\tETA: " + workorder.getEtaTime();
//			TextView textViewEta = (TextView) rowView.findViewById(R.id.eta);
//			textViewEta.setText(eta);
//
//			return rowView;
	      return getView(position, convertView, parent);
	    } 
		
}
