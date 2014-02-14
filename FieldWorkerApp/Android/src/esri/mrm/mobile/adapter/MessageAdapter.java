package esri.mrm.mobile.adapter;

import java.util.List;

import esri.mrm.mobile.Notification;
import esri.mrm.mobile.R;
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

public class MessageAdapter extends ArrayAdapter<Notification> {
	private final Activity context;
	private final List<Notification> messages;

	public MessageAdapter(Activity context, List<Notification> messages) {
		super(context, R.layout.messageslayout, messages);
		this.context = context;
		this.messages = messages;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.messageslayout, null, true);
		TextView textviewSubject = (TextView) rowView.findViewById(R.id.message_subject);
		TextView textViewFrom = (TextView) rowView.findViewById(R.id.message_from);
		TextView textViewTime = (TextView) rowView.findViewById(R.id.message_time);
		//TextView textViewDescription = (TextView)rowView.findViewById(R.id.description);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		Notification notification = messages.get(position);
//		String name = notification.getFromResource().getFirstName() + " " + notification.getFromResource().getLastName();
		String subject = notification.getSubject();
		textviewSubject.setText(subject);
		String s =  notification.getTimeString();
		textViewTime.setText(s);
		String sender = notification.getMessageFrom();
		textViewFrom.setText(sender);
		/*
		switch(type)
		{
			case 1:
				textViewDispatchType.setText("Dispatch");
			break;
			case 2:
				textViewDispatchType.setText("Running Late to #" + message.getWorkorderId());
			break;
			case 3:
				textViewDispatchType.setText("You have exited your territory");
			break;
			default:
			break;		
		}*/
//		String description = notification.getBody();
		//textViewDescription.setText(description);
		if (notification.getStatus().equals("Complete") == true)
		{
			imageView.setImageResource(R.drawable.ic_mail_replied_32);
		}
		else
		{
		  imageView.setImageResource(R.drawable.ic_mail_unread_32);
		}

		return rowView;
	}	
}
