package esri.mrm.mobile.activity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
import esri.mrm.mobile.NonServiceWorkOrderType;
import esri.mrm.mobile.R;
import esri.mrm.mobile.StopsConfigurations;
import esri.mrm.mobile.WorkOrder;
import esri.mrm.mobile.WorkOrderStatus;
import esri.mrm.mobile.WorkOrderUtility;
import esri.mrm.mobile.adapter.WorkOrderAdapter;
import esri.mrm.mobile.adapter.WorkOrderSpinnerAdapter;
import esri.mrm.mobile.task.CompleteListener;
import esri.mrm.mobile.task.SaveStopTask;

public class WorkOrdersActivity extends Activity implements CompleteListener
{

  StopsConfigurations              configs;
  Graphic                          route;
  final List<WorkOrder>            workOrders        = new ArrayList<WorkOrder>();
  private Toast                    toast;
  private long                     lastBackPressTime = 0;
  private Context                  context;
  private ListView                 list;

  private AGSObjects               agsObjects;
  private ArcGISFeatureLayer       stopLayer;
  private String                   whereClause;
  private ScheduledExecutorService scheduledTaskExecutor;
  private boolean                  refreshStops;
  private int                      reloadInterval;
  private SharedPreferences        sharedPrefs;
  private ProgressDialog progressDialog;
  private AlertDialog resequenceDialog;

  public static final String       TAG               = "WorkOrdersActivity";
  public static final String stopConfigFileName = "stops-configurations.xml";

  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    // setContentView(R.layout.workorder_list);

    View viewToLoad = LayoutInflater.from(this.getParent()).inflate(R.layout.workorder_list, null);
    this.setContentView(viewToLoad);
    setContext(viewToLoad.getContext());

    list = (ListView) findViewById(R.id.workorders);
    list.setOnItemClickListener(new OnItemClickListener()
    {

      public void onItemClick(AdapterView<?> av, View v, int position, long id)
      {
        WorkOrderAdapter workorderAdapter = (WorkOrderAdapter) av.getAdapter();
        ShowItemClick(workorderAdapter, v, position);
      }
    });
    readStopConfigurations();

    agsObjects = ((AGSObjects) getApplicationContext());

    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    list.setOnItemLongClickListener(new OnItemLongClickListener()
    {
      public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id)
      {
        
        WorkOrderAdapter workorderAdapter = (WorkOrderAdapter) av.getAdapter();
        final WorkOrder longPressedWorkOrder = workorderAdapter.getItem(position);
        final WorkOrder previousWorkOrder = getPreviousWorkOrder(longPressedWorkOrder);
        if(longPressedWorkOrder.getStatus().equals(WorkOrderStatus.AtStop.toString()) || longPressedWorkOrder.getStatus().equals(WorkOrderStatus.Completed.toString()) || longPressedWorkOrder.getStatus().equals(WorkOrderStatus.Exception.toString()) )
        {
          showAlertMessage("Warning", "Cannot move a stop with the status " + longPressedWorkOrder.getStatus());
          return true;
        }
        
        if(longPressedWorkOrder.getType().equals(NonServiceWorkOrderType.Base.toString()))
        {
          showAlertMessage("Warning", "Cannot move a base stop. ");
          return true;
        }
        
        refreshStops = false;
        List<WorkOrder> newList = new ArrayList<WorkOrder>();
        if( longPressedWorkOrder.getType().equals(NonServiceWorkOrderType.Break.toString()) || longPressedWorkOrder.getType().equals(NonServiceWorkOrderType.Lunch.toString()) )
          newList = WorkOrderUtility.getWorkOrderListForBreaks(workOrders);
        else
          newList = WorkOrderUtility.getWorkOrderListForWorkOrder(workOrders, longPressedWorkOrder);
        final WorkOrderSpinnerAdapter adapterWorkOrders = new WorkOrderSpinnerAdapter(WorkOrdersActivity.this, newList);
        
        
        
        ListView listView = new ListView(getContext());
        listView.setAdapter(adapterWorkOrders);
        listView.setOnItemClickListener(new OnItemClickListener()
        {

          public void onItemClick(AdapterView<?> listView, View itemView, int position, long itemId)
          {
            try
            {
              final WorkOrder woBefore = adapterWorkOrders.getItem(position);
              //showAlertMessage("Test", "selected wo " + woBefore.getStopName());
              if(woBefore.getStopName().equals(previousWorkOrder.getStopName()))
              {
                showAlertMessage("Info", "The work order is inserted into the same position.  No route update is needed.");
              }
              else
              {
                new AlertDialog.Builder(getContext())
                .setTitle("Please Confirm")
                .setMessage("Are you sure you want to move work order " + longPressedWorkOrder.getStopName() + " after " + woBefore.getStopName() + "?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                      if(longPressedWorkOrder.getSequence()<woBefore.getSequence())
                        longPressedWorkOrder.setSequence(woBefore.getSequence());
                      else
                        longPressedWorkOrder.setSequence(woBefore.getSequence()+1);

                      if(resequenceDialog != null)
                        resequenceDialog.dismiss();
                      
                      String url = sharedPrefs.getString("gep_url_port", "") + sharedPrefs.getString("gep_update_stop_path", "");
                      new SaveStopTask(longPressedWorkOrder, WorkOrdersActivity.this, getContext(), WorkOrdersActivity.this).execute(url);
                      
                    }})
                 .setNegativeButton(android.R.string.no, null).show();
              }
            }
            catch (Exception e)
            {
              Log.d(TAG, e.getMessage());
              refreshStops = true;
            }
            
          }
        });
        listView.setBackgroundColor(Color.WHITE);

        resequenceDialog = new AlertDialog.Builder(getContext()).setTitle("Move work order after:").setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
        {
          public void onClick(DialogInterface dialog, int i) 
          {
            refreshStops = true;
            dialog.dismiss();
          }
        }).setView(listView).create();

        resequenceDialog.show();
        
        return true;
      }
    });
  }
  
  private WorkOrder getPreviousWorkOrder(WorkOrder thisWorkOrder)
  {
    int seq = thisWorkOrder.getSequence();
    for (WorkOrder wo : workOrders)
    {
      if(wo.getSequence()==seq-1)
        return wo;
    }
    return null;
  }
  
  
  private void readStopConfigurations()
  {
    try
    {
      File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File stopConfigDownload = new File(downloadFolder.getAbsolutePath() + File.separator + stopConfigFileName);
      if(stopConfigDownload.exists())
      {
        System.out.println(">>>>>>>>>> Stop Configuration Download Exists. <<<<<<<<<<<<<<<<");
        copyStopConfigFromDownload(stopConfigDownload);
      }
      else
      {
        System.out.println(">>>>>>>>>> Stop Configuration Download Does NOT Exist. <<<<<<<<<<<<<<<<");
        try
        {
          InputStream inputStream = openFileInput(stopConfigFileName);
          if(inputStream != null)
          {
            inputStream.close();
            System.out.println(">>>>>>>>>> Stop Configuration Local File Exists. <<<<<<<<<<<<<<<<");
          }
        }
        catch(FileNotFoundException e)
        {
          System.out.println(">>>>>>>>>> Stop Configuration Local File Does NOT Exist. <<<<<<<<<<<<<<<<");
          copyStopConfigFromAsset();
        }
      }
      System.out.println(">>>>>>>>>> Reading Stop Configuration Local File. <<<<<<<<<<<<<<<<");
      readStopConfigFromLocal();
    }
    catch(Exception e)
    {
      Log.e("WorkOrder activity", "Can not read stop configuration: " + e.getMessage());
      showAlertMessage("Error", "Cannot read stop configuration.");
    }
  }
  
  private void copyStopConfigFromDownload(File stopConfigDownload)
  {
    FileInputStream fin;
    try
    {
      fin = new FileInputStream(stopConfigDownload);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      reader.close();
      fin.close();    
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(stopConfigFileName, Context.MODE_PRIVATE));
      outputStreamWriter.write(sb.toString());
      outputStreamWriter.close();
    }
    catch (FileNotFoundException e)
    {
      Log.e("WorkOrder activity", "File not found: " + e.toString());
    }
    catch (IOException e)
    {
      Log.e("WorkOrder activity", "Can not read or write file: " + e.toString());
    }
     
  }
  
  private void copyStopConfigFromAsset()
  {
    try
    {
      AssetManager am = context.getAssets();
      InputStream is = am.open("stops-configurations.xml");
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
  
      int i;
      i = is.read();
      while (i != -1)
      {
        byteArrayOutputStream.write(i);
        i = is.read();
      }
      is.close();
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(stopConfigFileName, Context.MODE_PRIVATE));
      outputStreamWriter.write(byteArrayOutputStream.toString());
      outputStreamWriter.close();
    }
    catch (IOException e)
    {
      Log.e("WorkOrder activity", "Can not read or write file: " + e.toString());
    }
  }
  
  private void readStopConfigFromLocal()
  {
    try
    {
      InputStream inputStream = openFileInput(stopConfigFileName);

      if (inputStream != null)
      {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String receiveString = "";
        StringBuilder stringBuilder = new StringBuilder();
  
        while ((receiveString = bufferedReader.readLine()) != null)
        {
          stringBuilder.append(receiveString);
        }
  
        inputStream.close();
        JSONObject xmlJSONObj = XML.toJSONObject(stringBuilder.toString());
        configs = new StopsConfigurations(xmlJSONObj);
      }
    }
    catch(IOException e)
    {
      Log.e("WorkOrder activity", "Can not read file: " + e.toString());
    }
    catch (JSONException e)
    {
      Log.e("WorkOrder activity", "Can not parse XML to JSON: " + e.toString());
    }
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.mrm_stopmenu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle item selection
    switch (item.getItemId())
    {
      case R.id.addbreak:
        addBreak();
        return true;
      default:
        return true;
        // return super.onOptionsItemSelected(item);
    }
  }

  private void addBreak()
  {
    if(WorkOrderUtility.getWorkOrderListForBreaks(workOrders).size()==0)
    {
      new AlertDialog.Builder(this).setTitle("Warning").setMessage("Cannot add new break due to the lack of stops.").setPositiveButton("OK", null).show();
    }
    else
    {
      Class<?> cls = StopTypeActivity.class;
      String activity = "";
      activity = "StopTypeActivity";
      cls = StopTypeActivity.class;
  
      // Toast.makeText(this, message.getName(), Toast.LENGTH_SHORT).show();
      Intent intent = new Intent(getParent(), cls);
      Bundle b = new Bundle();
      b.putParcelableArray("WorkOrders", workOrders.toArray(new Parcelable[workOrders.size()]));
      b.putParcelable("stopsConfigurations", configs);
      intent.putExtras(b);
      TabGroupActivity parentActivity = (TabGroupActivity) getParent();
      parentActivity.startChildActivity(activity, intent);
    }
  }

  private void ShowItemClick(WorkOrderAdapter workorderAdapter, View v, int position)
  {
    WorkOrder workorder = workorderAdapter.getItem(position);

    Class<?> cls = WorkOrderDetailActivity.class;
    String activity = "";
    activity = "WorkOrderDetailActivity";
    cls = WorkOrderDetailActivity.class;

    // Toast.makeText(this, message.getName(), Toast.LENGTH_SHORT).show();
    Intent intent = new Intent(getParent(), cls);
    Bundle b = new Bundle();
    b.putParcelable("WorkOrder", workorder);
    b.putParcelableArray("WorkOrders", workOrders.toArray(new Parcelable[workOrders.size()]));
    b.putParcelable("stopsConfigurations", configs);
    intent.putExtras(b);
    TabGroupActivity parentActivity = (TabGroupActivity) getParent();
    parentActivity.startChildActivity(activity, intent);

  }

  public void SetWorkOrdersAdapter(List<WorkOrder> workOrders)
  {
    // ListView list = (ListView) findViewById(R.id.workorders);
    WorkOrderAdapter adapter = (WorkOrderAdapter) list.getAdapter();
    if (adapter == null)
    {
      adapter = new WorkOrderAdapter(this, workOrders, configs);

      list.setAdapter(adapter);
    }
    adapter.sort(new Comparator<WorkOrder>()
    {

      public int compare(WorkOrder lhs, WorkOrder rhs)
      {
        return lhs.getSequence() - rhs.getSequence();
      }
    });
    adapter.notifyDataSetChanged();
    // list.requestLayout();
  }

  

  private void showProgressDialog()
  {
    if (progressDialog != null)
    {
      progressDialog.dismiss();
      progressDialog = null;
    }
    progressDialog = new ProgressDialog(context);
    progressDialog.setTitle("Reloading Stops");
    progressDialog.setMessage("Please wait...");
    progressDialog.setCancelable(false);
    progressDialog.setIndeterminate(true);
    progressDialog.show();
    timerDelayRemoveDialog(10000, progressDialog);
  }

  private void loadPreferences()
  {
    reloadInterval = Integer.parseInt(sharedPrefs.getString("wo_update_interval", "10000"));
  }

  private void scheduleLoadStops()
  {
    refreshStops = true;
//    workOrders.clear();
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
        if (refreshStops)
          loadStops();
      }
    }, 0, reloadInterval / 1000, TimeUnit.SECONDS);

  }

  private void loadStops()
  {
    if (agsObjects.getStopsLayer() == null)
    {
      WorkOrdersActivity.this.runOnUiThread(new Runnable()
      {
        public void run()
        {
          SetWorkOrdersAdapter(workOrders);
        }
      });
      return;
    }
      
    stopLayer = agsObjects.getStopsLayer();

    WorkOrdersActivity.this.runOnUiThread(new Runnable()
    {
      public void run()
      {
        showProgressDialog();
      }
    });

    String routeFieldName = LayerUtility.getFieldNamebyAlias(stopLayer, getResources().getString(R.string.ALIAS_STOPSLAYER_ROUTE_NAME));
    whereClause = routeFieldName + "='" + agsObjects.getRouteId() + "'";

    Query query = new Query();
    query.setWhere(whereClause);
    query.setOutFields(new String[] { "*" });
    try
    {
      stopLayer.queryFeatures(query, new CallbackListener<FeatureSet>()
      {
  
        public void onCallback(FeatureSet queryResults)
        {
          workOrders.clear();
          if (queryResults.getGraphics().length > 0)
          {
            Map<String, Integer> fieldTypes = new HashMap<String, Integer>();
            Map<String, String> fieldAliases = new HashMap<String, String>();
            for (Field field : queryResults.getFields())
            {
              fieldTypes.put(field.getAlias(), field.getFieldType());
              fieldAliases.put(field.getName(), field.getAlias());
            }
            for (int i = 0; i < queryResults.getGraphics().length; i++)
            {
              Graphic graphic = queryResults.getGraphics()[i];
              WorkOrder workOrder = new WorkOrder(graphic, fieldTypes, fieldAliases, context);
              workOrders.add(workOrder);
            }
          }
          WorkOrdersActivity.this.runOnUiThread(new Runnable()
          {
            public void run()
            {
              SetWorkOrdersAdapter(workOrders);
            }
          });
          progressDialog.dismiss();
  
        }
  
        public void onError(Throwable e)
        {
          Log.d(TAG, "Select Features Error" + e.getLocalizedMessage());
          progressDialog.dismiss();
        }
      });
    }
    catch (Exception e)
    {
      progressDialog.dismiss();
    }
  }
  
  protected Context getContext()
  {
    return context;
  }

  protected void setContext(Context context)
  {
    this.context = context;
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
  protected void onPause()
  {
    super.onPause();
    refreshStops = false;
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    loadPreferences();
    scheduleLoadStops();
    refreshStops = true;
  }

  @Override
  protected void onStop()
  {
    super.onStop();
  }

  @Override
  protected void onStart()
  {
    super.onStart();
  }
  
  public void timerDelayRemoveDialog(long time, final Dialog d){
    Handler handler = new Handler(); 
    handler.postDelayed(new Runnable() {           
        public void run() {  
          if(d != null)
            d.dismiss();         
        }
    }, time); 
  }
  
  private void showAlertMessage(final String title, final String body)
  {
    WorkOrdersActivity.this.runOnUiThread(new Runnable()
    {

      public void run()
      {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder.setMessage(body).setCancelable(false).setPositiveButton(R.string.okButtonLabel, new DialogInterface.OnClickListener()
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

  public void resultCallback(boolean result)
  {
    refreshStops = true;
  }

}
