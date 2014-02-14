package esri.mrm.mobile.activity;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class DispatchActivity extends Activity implements OnSharedPreferenceChangeListener
{
  
  private AGSObjects agsObjects;
  private ArcGISFeatureLayer msgLayer;
  private ArcGISFeatureLayer msgPendingLayer;
  private Notification notification;
  private SharedPreferences        sharedPrefs;
  private boolean needToClose;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getParent()).inflate(R.layout.dispatchlayout, null);
        setContentView(view);

        Bundle b = this.getIntent().getExtras();
        if (b != null)
        {
        	notification = b.getParcelable("Notification");
            if (notification != null)
            {
            	fillView(notification);
            }
        }    
        
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        needToClose=false;
    }
   
	public void fillView(Notification notification) {
		TextView textViewSubject = (TextView) findViewById(R.id.message_subject);
		TextView textViewFrom = (TextView) findViewById(R.id.message_from);
		TextView textViewTime = (TextView) findViewById(R.id.message_time);
		TextView textViewDescription = (TextView)findViewById(R.id.description);
		ImageView imageView = (ImageView) findViewById(R.id.icon);

		textViewSubject.setText(notification.getSubject());
		textViewFrom.setText(notification.getMessageFrom());
		textViewTime.setText(notification.getTimeString());
		
		String description = notification.getBody();
		textViewDescription.setText(description);
		
		Button buttonViewWO = (Button) findViewById(R.id.buttonViewWorkOrders);
		buttonViewWO.setOnClickListener(new OnClickListener()
    {
      
      public void onClick(View v)
      {
        MessagesTabGroupActivity p = (MessagesTabGroupActivity)getParent();
        EsriMrmActivity gp = (EsriMrmActivity) p.getParent();
        gp.getTabHost().setCurrentTab(1);
      }
    });
		
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
  				getNotification().setStatus("Complete");
  				updateMessage();
  				Button button = (Button)v;
  				String msg = getResources().getString(R.string.dispatch_acknowledged);
  				button.setText(msg);
  				button.setEnabled(false);
  			}
  		});
		}
	}	   
	
	public void updateMessage()
	{
	  agsObjects = ((AGSObjects)getApplicationContext());
    msgLayer = agsObjects.getMessagesLayer();
    
    Map<String, Object> attrs = getNotification().getUpdateAttributes();

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
	  DispatchActivity.this.runOnUiThread(new Runnable()
    {

      public void run()
      {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DispatchActivity.this.getParent());

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

  public Notification getNotification()
  {
    return notification;
  }

  public void setNotification(Notification notification)
  {
    this.notification = notification;
  }

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
  {
    needToClose=true;
  }
  
  public boolean isNeedToClose()
  {
    return needToClose;
  }

}
