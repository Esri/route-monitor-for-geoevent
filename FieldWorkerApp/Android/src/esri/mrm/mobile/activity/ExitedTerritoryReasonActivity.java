package esri.mrm.mobile.activity;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.Graphic;

import esri.mrm.mobile.AGSObjects;
import esri.mrm.mobile.Notification;
import esri.mrm.mobile.R;

public class ExitedTerritoryReasonActivity extends Activity {
  
  private AGSObjects agsObjects;
  private ArcGISFeatureLayer msgLayer;
  private ArcGISFeatureLayer msgPendingLayer;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getParent()).inflate(R.layout.territory_watcher, null);
        setContentView(view);
        Notification notification;
        Bundle b = this.getIntent().getExtras();
        if (b != null)
        {
        	notification = b.getParcelable("Notification");
            if (notification != null)
            {
            	fillView(notification);
            }
        }       
        
    }
    
	public void fillView(final Notification notification) {
		TextView textViewName = (TextView) findViewById(R.id.message_name);
		TextView textViewReceivedTime = (TextView) findViewById(R.id.received_time);
		TextView textViewDispatchType = (TextView) findViewById(R.id.dispatch_type);
		TextView textViewDescription = (TextView)findViewById(R.id.description);
		ImageView imageView = (ImageView) findViewById(R.id.icon);
		String name = notification.getMessageFrom();
		textViewName.setText(name);
		String s =  notification.getTimeString();
		textViewReceivedTime.setText(s);
		
		textViewDispatchType.setText(notification.getSubject());
		String description = notification.getBody();
		textViewDescription.setText(description);

		if (notification.getStatus().equals("Complete") == true)
		{
			imageView.setImageResource(R.drawable.ic_ok);
		}
		
		Button buttonAcknowledge = (Button) findViewById(R.id.buttonAcknowledge);
    if(notification.getStatus().equals("Complete"))
    {
      String msg = getResources().getString(R.string.dispatch_acknowledged);
      buttonAcknowledge.setText(msg);
      buttonAcknowledge.setEnabled(false);
    }
    else
    {
      buttonAcknowledge.setOnClickListener(new OnClickListener() {      
        public void onClick(View v) {
          notification.setStatus("Complete");
          updateMessage(notification);
          Button button = (Button)v;
          String msg = getResources().getString(R.string.dispatch_acknowledged);
          button.setText(msg);
          button.setEnabled(false);
        }
      });
    }
         
	}	   
	
	public void updateMessage(Notification notification)
  {
    agsObjects = ((AGSObjects)getApplicationContext());
    msgLayer = agsObjects.getMessagesLayer();
    
    Map<String, Object> attrs = notification.getUpdateAttributes();

    //TODO: use actual xy from location manager
    Point pt = new Point(0,0);
    final Graphic newGraphic = new Graphic(pt, null, attrs);
    msgLayer.applyEdits(null, null, new Graphic[] { newGraphic }, new CallbackListener<FeatureEditResult[][]>()
    {
      public void onCallback(FeatureEditResult[][] result) {
          msgPendingLayer = agsObjects.getMessagesPendingLayer();
          msgPendingLayer.applyEdits(new Graphic[] { newGraphic }, null, null, new CallbackListener<FeatureEditResult[][]>()
          {
            
            public void onError(Throwable e)
            {
              Log.d(this.getClass().getName(), e.getMessage());
              showUpdateResult(false);
            }
            
            public void onCallback(FeatureEditResult[][] arg0)
            {
              showUpdateResult(true);
              ((MessagesActivity)getParent()).loadMessages();
            }
          });
        
      }

      public void onError(Throwable e) {
        Log.d(this.getClass().getName(), e.getMessage());
        showUpdateResult(false);
      }
    });
  }
  
  private void showUpdateResult(final boolean successful)
  {
    ExitedTerritoryReasonActivity.this.runOnUiThread(new Runnable()
    {

      public void run()
      {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ExitedTerritoryReasonActivity.this.getParent());

        // set title
        alertDialogBuilder.setTitle(R.string.updateResult);

        // set dialog message
        alertDialogBuilder.setMessage(successful ? R.string.updateSucceeded : R.string.updateFailed).setCancelable(false).setPositiveButton(R.string.okButtonLabel, new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int id)
          {
            dialog.cancel();
          }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
      }
    });
  }
	
}
