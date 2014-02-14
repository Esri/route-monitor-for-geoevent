package esri.mrm.mobile.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.query.Query;

import esri.mrm.mobile.AGSObjects;
import esri.mrm.mobile.LayerUtility;
import esri.mrm.mobile.Notification;
import esri.mrm.mobile.R;
import esri.mrm.mobile.adapter.MessageAdapter;

public class MessagesActivity extends Activity 
{

  // Graphic route;
  final List<Notification>         notifications     = new ArrayList<Notification>();
  private Toast                    toast;
  private long                     lastBackPressTime = 0;
  private ArcGISFeatureLayer       messagesLayer;
  private AGSObjects               agsObjects;
  private ListView                 list;
  private Context                  context;
  private ScheduledExecutorService scheduledTaskExecutor;
  private int                      reloadInterval;
  private SharedPreferences        sharedPrefs;

  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.message_list);

    context = this;

    list = (ListView) findViewById(R.id.messages);
    list.setOnItemClickListener(new OnItemClickListener()
    {

      public void onItemClick(AdapterView<?> av, View v, int position, long id)
      {
        MessageAdapter messageAdapter = (MessageAdapter) av.getAdapter();
        ShowItemClick(messageAdapter, v, position);
      }
    });

    agsObjects = ((AGSObjects) getApplicationContext());
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    if (scheduledTaskExecutor == null)
    {
      scheduledTaskExecutor = Executors.newScheduledThreadPool(1);

      scheduledTaskExecutor.scheduleAtFixedRate(new Runnable()
      {
        public void run()
        {
          loadMessages();
        }
      }, 0, 20, TimeUnit.SECONDS);
    }
  }
  
  private void loadPreferences()
  {
    reloadInterval = Integer.parseInt(sharedPrefs.getString("wo_update_interval", "10000"));
  }
  
  private void scheduleLoadMessages()
  {
    notifications.clear();
    if (scheduledTaskExecutor != null)
    {
      scheduledTaskExecutor.shutdown();
      scheduledTaskExecutor = null;
    }

    scheduledTaskExecutor = Executors.newScheduledThreadPool(1);

    scheduledTaskExecutor.scheduleAtFixedRate(new Runnable()
    {
      public void run()
      {
        loadMessages();
      }
    }, 0, reloadInterval / 1000, TimeUnit.SECONDS);
  }

  public void loadMessages()
  {
    messagesLayer = agsObjects.getMessagesLayer();
    if (messagesLayer == null)
    {
      MessagesActivity.this.runOnUiThread(new Runnable()
      {
        public void run()
        {
          SetMessageAdapter(notifications);
        }
      });
      return;
    }

    Query query = new Query();
    String routeFieldName = LayerUtility.getFieldNamebyAlias(messagesLayer, getResources().getString(R.string.ALIAS_MESSAGESLAYER_MESSAGE_TO));
    String whereClause = routeFieldName + "='" + agsObjects.getRouteId() + "'";
    query.setWhere(whereClause); 
    query.setOutFields(new String[] { "*" });
    messagesLayer.queryFeatures(query, new CallbackListener<FeatureSet>()
    {

      public void onError(Throwable e)
      {
        Log.d("MessagesActivity", "Select Features Error" + e.getLocalizedMessage());
      }

      public void onCallback(FeatureSet result)
      {
        notifications.clear();
        Graphic[] grs = result.getGraphics();
        Map<String, String> fieldAliases = new HashMap<String, String>();
        for (Field field : result.getFields())
        {
          fieldAliases.put(field.getName(), field.getAlias());
        }
        
        for (int i = 0; i < grs.length; i++)
        {
          Graphic graphic = grs[i];

          Notification notification = new Notification(graphic, context, result.getObjectIdFieldName(), fieldAliases);
          notifications.add(notification);
        }
        Collections.sort(notifications, new messageComparator());

        MessagesActivity.this.runOnUiThread(new Runnable()
        {

          public void run()
          {

            SetMessageAdapter(notifications);

          }
        });

      }
    });
  }

  @Override
  public void onBackPressed()
  {
    if (this.lastBackPressTime < System.currentTimeMillis() - 4000)
    {
      toast = Toast.makeText(this, "Press Back button agian to close the app", Toast.LENGTH_LONG);
      toast.show();
      this.lastBackPressTime = System.currentTimeMillis();
    }
    else
    {
      if (toast != null)
      {
        toast.cancel();
      }
      super.onBackPressed();
    }
  }

  private void ShowItemClick(MessageAdapter messageAdapter, View v, int position)
  {
    Notification message = messageAdapter.getItem(position);
    Class<?> cls = ExitedTerritoryReasonActivity.class;
    String activity = "";
    if (message.getType().equals("Notification") && message.getSubject().equals("Dispatch"))
    {
      activity = "DispatchActivity";
      cls = DispatchActivity.class;
    }

    // Toast.makeText(this, message.getName(), Toast.LENGTH_SHORT).show();
    Intent intent = new Intent(getParent(), cls);
    Bundle b = new Bundle();
    b.putParcelable("Notification", message);
    intent.putExtras(b);
    TabGroupActivity parentActivity = (TabGroupActivity) getParent();
    parentActivity.startChildActivity(activity, intent);
  }

  public void SetMessageAdapter(List<Notification> notifications)
  {
    // ListView list=(ListView)findViewById(R.id.messages);
    MessageAdapter adapter = (MessageAdapter) list.getAdapter();
    if (adapter == null)
    {
      adapter = new MessageAdapter(this, notifications);
      list.setAdapter(adapter);
    }
    adapter.notifyDataSetChanged();
    updateTab();
  }

  private void updateTab()
  {
    try
    {
      boolean hasOpenMsg = false;
      MessagesTabGroupActivity p = (MessagesTabGroupActivity) getParent();
      EsriMrmActivity gp = (EsriMrmActivity) p.getParent();
      ViewGroup msgTab = (ViewGroup) gp.getTabWidget().getChildAt(0);
      ImageView v = (ImageView) msgTab.getChildAt(0);

      for (Notification n : notifications)
      {
        if (!n.getStatus().equals("Complete"))
        {
          hasOpenMsg = true;
        }
      }
      if (hasOpenMsg)
      {
        v.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_messages_ex));
      }
      else
      {
        v.setImageDrawable(getResources().getDrawable(R.drawable.ic_tab_messages));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    if (scheduledTaskExecutor != null)
    {
      scheduledTaskExecutor.shutdownNow();
      scheduledTaskExecutor = null;
    }
  }

  @Override
  protected void onStop()
  {
    super.onStop();
  }

  public void doSomething()
  {
    Log.d("DoSomething", "Doing Something");
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    loadPreferences();
    scheduleLoadMessages();
  }
  
  private class messageComparator implements Comparator<Notification>
  {

    public int compare(Notification lhs, Notification rhs)
    {
      Long lhs_time = lhs.getTime();
      Long rhs_time = rhs.getTime();
      //return lhs_time.compareTo(rhs_time);
      return rhs_time.compareTo(lhs_time);
    }
    
  }
  
}
