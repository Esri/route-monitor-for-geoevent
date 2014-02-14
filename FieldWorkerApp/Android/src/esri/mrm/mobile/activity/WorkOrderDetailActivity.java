package esri.mrm.mobile.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;

import esri.mrm.mobile.AGSObjects;
import esri.mrm.mobile.NonServiceWorkOrderType;
import esri.mrm.mobile.PropertyDefinition;
import esri.mrm.mobile.R;
import esri.mrm.mobile.StopsConfiguration;
import esri.mrm.mobile.StopsConfigurations;
import esri.mrm.mobile.WorkOrder;
import esri.mrm.mobile.WorkOrderStatus;

public class WorkOrderDetailActivity extends Activity implements OnSharedPreferenceChangeListener
{

  private String             baseURL;

  Map<String, View>          updates;
  StopsConfigurations        stopsConfigurations;
  StopsConfiguration         stopsConfiguration;

  private String             updateUrlPrefix;
  private Context            context;
  private WorkOrder          workOrder;
  private ArcGISFeatureLayer stopLayer;
  private ArcGISFeatureLayer stopPendingLayer;

  private AGSObjects         agsObjects;
  private ProgressDialog     progressDialog;
  private SharedPreferences  sharedPrefs;
  private boolean            needToClose;
  private List<WorkOrder>    workOrders;
  private View               viewToLoad;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    // setContentView(R.layout.workorder_detail);

    needToClose = false;

    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    sharedPrefs.registerOnSharedPreferenceChangeListener(this);

    viewToLoad = LayoutInflater.from(this.getParent()).inflate(R.layout.workorder_detail, null);
    this.setContentView(viewToLoad);
    setContext(viewToLoad.getContext());

    WorkOrder workorder;

    Bundle b = this.getIntent().getExtras();
    if (b != null)
    {
      Parcelable[] parcelable = b.getParcelableArray("WorkOrders");
      if (parcelable != null)
      {
        workOrders = new ArrayList<WorkOrder>();
        for (int i = 0; i < parcelable.length; i++)
        {
          workOrders.add((WorkOrder) parcelable[i]);
        }
      }
      workorder = b.getParcelable("WorkOrder");
      stopsConfigurations = b.getParcelable("stopsConfigurations");
      stopsConfiguration = stopsConfigurations.getStopsConfiguration(workorder.getType());
      baseURL = stopsConfigurations.getGepUrl();
      if (workorder != null)
      {
        setWorkOrder(workorder);
        fillView(workorder, stopsConfiguration, viewToLoad);
        setUpdateUrlPrefix(baseURL + stopsConfigurations.getCallback() + "/" + workorder.getStopName() + "?");
      }
    }
  }

  public boolean isNeedToClose()
  {
    return needToClose;
  }

  public void fillView(WorkOrder workorder, StopsConfiguration stopsConfiguration, View view)
  {

    TextView textViewName = (TextView) findViewById(R.id.workorder_name);

    new GetPictureTask().execute(baseURL + workorder.getPictureSubUrl());
    textViewName.setText(workorder.getStopName());

    TextView textViewAddress = (TextView) findViewById(R.id.workorder_address);
    if (workorder.getType().equals(NonServiceWorkOrderType.Break.toString()))
      textViewAddress.setText("");
    else
      textViewAddress.setText(workorder.getAddress());

    Button allAttributesButton = (Button) findViewById(R.id.allAttributesButton);
    allAttributesButton.setOnClickListener(new View.OnClickListener()
    {

      public void onClick(View v)
      {
        View allAttrView = LayoutInflater.from(context).inflate(R.layout.allattributes, null);
        TableLayout tv = (TableLayout) allAttrView.findViewById(R.id.tableLayout);
        tv.removeAllViewsInLayout();
        tv.setShrinkAllColumns(true);
        TableRow trHeader = new TableRow(tv.getContext());
        trHeader.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        TextView name = new TextView(trHeader.getContext());
        name.setTextSize(14);
        name.setTextColor(Color.WHITE);
        name.setText("Field");
        name.setBackgroundDrawable(getResources().getDrawable(R.drawable.header_cell_shape));
        trHeader.addView(name);
        TextView value = new TextView(trHeader.getContext());
        value.setTextSize(14);
        value.setTextColor(Color.WHITE);
        value.setText("Value");
        value.setBackgroundDrawable(getResources().getDrawable(R.drawable.header_cell_shape));
        trHeader.addView(value);
        tv.addView(trHeader);

        Map<String, String> aliases = getWorkOrder().getFieldAliases();
        Map<String, Integer> fieldTypes = getWorkOrder().getFieldTypes();
        for (Map.Entry<String, String> entry : aliases.entrySet())
        {
          TableRow tr = new TableRow(tv.getContext());
          tr.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
          String workOrderResourceName = entry.getValue();
          String alias = entry.getValue();
          TextView fieldNameText = new TextView(tr.getContext());
          fieldNameText.setTextSize(14);
          fieldNameText.setTextColor(Color.WHITE);
          fieldNameText.setText(alias);
          fieldNameText.setBackgroundDrawable(getResources().getDrawable(R.drawable.fieldname_cell_shape));
          tr.addView(fieldNameText);
          TextView fieldValueText = new TextView(tr.getContext());

          fieldValueText.setTextSize(14);
          fieldValueText.setTextColor(Color.BLACK);
          if(fieldTypes.get(workOrderResourceName) == Field.esriFieldTypeDate)
          {
            if(getWorkOrder().getAttributes(workOrderResourceName)==null)
              fieldValueText.setText("");
            else
            {
              Date d = new Date((Long)getWorkOrder().getAttributes(workOrderResourceName));
              fieldValueText.setText(d.toString());
            }
          }
          else
            fieldValueText.setText(getWorkOrder().getAttributes(workOrderResourceName)==null?"":getWorkOrder().getAttributes(workOrderResourceName).toString());
          fieldValueText.setBackgroundDrawable(getResources().getDrawable(R.drawable.cell_shape));
          
          tr.addView(fieldValueText);
          tv.addView(tr);
        }

        Button cancelButton = (Button) allAttrView.findViewById(R.id.cancel_button);
        final PopupWindow popUp = new PopupWindow(allAttrView, viewToLoad.getWidth(), viewToLoad.getHeight(), true); // new
                                                                                                                     // PopupWindow(allAttrView);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {

          public void onClick(View v)
          {
            popUp.dismiss();

          }
        });

        popUp.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.simple_popup_window_drop_shadow));
        popUp.showAtLocation(viewToLoad, Gravity.CENTER, 0, 0);
      }
    });

    TextView textViewWorkOrderId = (TextView) findViewById(R.id.workorder_id);
    textViewWorkOrderId.setText("#" + workorder.getId());
    TextView textViewTask = (TextView) findViewById(R.id.workorder_task);
    ImageView imageViewWorkOrder = (ImageView) findViewById(R.id.iconWorkOrder);

    String type = workorder.getType();
    if (type.equals("Turn On"))
    {
      imageViewWorkOrder.setImageResource(R.drawable.ic_turnon_w);
      textViewTask.setText("Turn On");
    }
    else if (type.equals("DoorTag"))
    {
      imageViewWorkOrder.setImageResource(R.drawable.ic_doortag_w);
      textViewTask.setText("Door Tag");
    }
    else if (type.equals("Inspection"))
    {
      imageViewWorkOrder.setImageResource(R.drawable.ic_inspection_w);
      textViewTask.setText("Inspection");
      // To Do Show text input
    }
    else if (type.equals("Break"))
    {
      imageViewWorkOrder.setImageResource(R.drawable.ic_breaktime_w);
      textViewTask.setText("Break");
    }
    else
    {
      imageViewWorkOrder.setImageResource(R.drawable.ic_no);
      textViewTask.setText("N/A");
    }

    TextView textViewArrivalTime = (TextView) findViewById(R.id.arrivalTime);
    TextView textViewDepartureTime = (TextView) findViewById(R.id.departureTime);
    if (workorder.getType().equals(NonServiceWorkOrderType.Break.toString()))
    {
      textViewArrivalTime.setText(workorder.getProjectedArrival() + " Arrival");
      textViewDepartureTime.setText(workorder.getProjectedDeparture() + " Departure");
    }
    else
    {
      textViewArrivalTime.setText(workorder.getEtaTime() + " Arrival");
      textViewDepartureTime.setText(workorder.getEtdTime() + " Departure");
    }

    if (stopsConfiguration == null)
    {
      LinearLayout updatesLayout = (LinearLayout) findViewById(R.id.updatesLayout);
      updatesLayout.setVisibility(View.INVISIBLE);
      return;
    }

    // Special handling for status
    PropertyDefinition statusPD = stopsConfiguration.getPropertyDefinition(getResources().getString(R.string.ALIAS_STOPSLAYER_STATUS));
    LinearLayout layout = (LinearLayout) findViewById(R.id.statusButtonsLayout);
    if (statusPD.getAllowedValues().size() > 0)
    {
      // Create a vertical list of buttons
      for (Object statusObj : statusPD.getAllowedValues())
      {
        ToggleButton button = new ToggleButton(this);
        button.setTag(statusPD.getPropertyName());
        button.setChecked(false);
        button.setTextOff((String) statusObj);
        button.setText((String) statusObj);
        button.setTextOn((String) statusObj);
        button.setWidth(getPixelFromDP(90));
        button.setHeight(getPixelFromDP(50));
        button.setPadding(0, 0, 0, 8);
        if (workorder.getStatus().equals(statusObj))
        {
          button.setChecked(true);
        }

        button.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View v)
          {
            // Exception
            LinearLayout parent = (LinearLayout) v.getParent();
            ToggleButton thisButton = (ToggleButton) v;

            if (!thisButton.isChecked())
            {
              // toggle button changes checked state as soon as it is clicked
              thisButton.setChecked(true);
              return;
            }
            String newStatus = ((Button) v).getText().toString();
            //getWorkOrder().setAttributes(((Button) v).getTag().toString(), ((Button) v).getText().toString());
            updateWorkOrderTimesAndStatus(newStatus);
            selectRemarks(parent, thisButton);

            // unselect other buttons
            for (int i = 0; i < parent.getChildCount(); i++)
            {
              View child = parent.getChildAt(i);
              if (child instanceof ToggleButton && child != v)
              {
                ((ToggleButton) child).setChecked(false);
              }
            }

          }
        });
        layout.addView(button);

      }
    }
    else
    {
      // Create a label and a input box
    }

    // Handle additional update fields
    TextView updatesSeparator = (TextView) findViewById(R.id.updatesSeparator);
    LinearLayout updatesLayout = (LinearLayout) findViewById(R.id.updatesLayout);
    updatesLayout.setVisibility(View.INVISIBLE);
    updatesSeparator.setVisibility(View.INVISIBLE);
    updates = new HashMap<String, View>();

    for (PropertyDefinition pd : stopsConfiguration.getPropertyDefinitions())
    {
      if (!pd.isReadOnly() && pd != statusPD && !pd.getPropertyName().equals(getResources().getString(R.string.ALIAS_STOPSLAYER_STATUS_REMARK)))
      {

        if (pd.hasAllowedValues())
        {
          String[] values = pd.getAllowedValues().toArray(new String[pd.getAllowedValues().size()]);
          Spinner spinner = new Spinner(view.getContext());
          ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, values);
          spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          spinner.setAdapter(spinnerArrayAdapter);

          Object obj = workorder.getAttributes(pd.getPropertyName());
          String matchString = null;
          if (obj != null)
          {
            matchString = obj.toString();
          }
          else
          {
            if (pd.getDefaultValue() != null)
              matchString = pd.getDefaultValue().toString();
          }
          if (matchString != null)
          {
            ArrayAdapter myAdap = (ArrayAdapter) spinner.getAdapter();
            int spinnerPosition = myAdap.getPosition(matchString);
            spinner.setSelection(spinnerPosition);
          }

          updates.put(pd.getPropertyName(), spinner);
          updatesLayout.addView(spinner, 0);
        }
        else
        {
          EditText editText = new EditText(view.getContext());
          editText.setPadding(0, 0, 0, getPixelFromDP(5));
          editText.setTextSize(getPixelFromDP(16));
          Object obj = workorder.getAttributes(pd.getPropertyName());
          if (obj != null)
          {
            editText.setText(obj.toString());
          }
          else
          {
            if (pd.getDefaultValue() != null)
              editText.setHint(pd.getDefaultValue().toString());
          }
          updates.put(pd.getPropertyName(), editText);
          updatesLayout.addView(editText, 0);
        }

        TextView label = new TextView(this);
        label.setTextSize(getPixelFromDP(16));
        label.setText(pd.getLabel());
        updatesLayout.addView(label, 0);
      }
    }
    if (updatesLayout.getChildCount() > 1)
    {
      updatesLayout.setVisibility(View.VISIBLE);
      updatesSeparator.setVisibility(View.VISIBLE);
      Button updateButton = (Button) findViewById(R.id.updateButton);
      updateButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View v)
        {
          Iterator it = updates.entrySet().iterator();
          while (it.hasNext())
          {
            Map.Entry pairs = (Map.Entry) it.next();
            if (pairs.getValue() instanceof EditText)
            {
              try
              {
                getWorkOrder().setAttributes(pairs.getKey().toString(), ((EditText) pairs.getValue()).getText().toString());
              }
              catch (Exception e)
              {
                e.printStackTrace();
              }
            }
            else if (pairs.getValue() instanceof Spinner)
            {
              try
              {
                getWorkOrder().setAttributes(pairs.getKey().toString(), (String) ((Spinner) pairs.getValue()).getSelectedItem());
              }
              catch (Exception e)
              {
                e.printStackTrace();
              }
            }
          }
          updateStopsLayer(false);
        }
      });
    }
    else if (getWorkOrder().getType().equals(NonServiceWorkOrderType.Break.toString()))
    {
      updatesLayout.setVisibility(View.VISIBLE);
      updatesSeparator.setVisibility(View.VISIBLE);
      Button updateButton = (Button) findViewById(R.id.updateButton);
      updateButton.setText("Change Break...");
      updateButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View v)
        {
          Intent intent = new Intent(getParent(), StopTypeActivity.class);
          Bundle b = new Bundle();
          b.putParcelable("WorkOrder", getWorkOrder());
          b.putParcelableArray("WorkOrders", workOrders.toArray(new Parcelable[workOrders.size()]));
          intent.putExtras(b);
          TabGroupActivity parentActivity = (TabGroupActivity) getParent();
          parentActivity.startChildActivity("StopTypeActivity", intent);
        }
      });

      if (getWorkOrder().getStatus().equals(WorkOrderStatus.Completed.toString()) || getWorkOrder().getStatus().equals(WorkOrderStatus.AtStop.toString()))
        updateButton.setEnabled(false);
    }

  }
  
  private void updateWorkOrderTimesAndStatus(String newStatus)
  {
    Date now = new Date();
    getWorkOrder().setLastUpdated(now.getTime());
    getWorkOrder().setStatus(newStatus);
    if(newStatus.equals(WorkOrderStatus.AtStop.toString()))
      getWorkOrder().setAttributes(getResources().getString(R.string.ALIAS_STOPSLAYER_ACTUAL_ARRIVAL), Long.toString(now.getTime()));
    
    if(newStatus.equals(WorkOrderStatus.Completed.toString()) || newStatus.equals(WorkOrderStatus.Exception.toString()))
    {
      getWorkOrder().setAttributes(getResources().getString(R.string.ALIAS_STOPSLAYER_ACTUAL_DEPARTURE), Long.toString(now.getTime()));
      if(getWorkOrder().getActualArrivalAsLong() != null)
      {
        // TODO:  Revisit duration strategy
        long minutes =  (now.getTime()-getWorkOrder().getActualArrivalAsLong())/60000 > 32767 ? 32767 : (now.getTime()-getWorkOrder().getActualArrivalAsLong())/60000;
        getWorkOrder().setAttributes(getResources().getString(R.string.ALIAS_STOPSLAYER_ACTUAL_DURATION), Long.toString(minutes));
      }
    }
  }

  private void selectRemarks(View parent, ToggleButton thisButton)
  {
    PropertyDefinition pd = getPropertyDefinitionBasedOnDependency(getResources().getString(R.string.ALIAS_STOPSLAYER_STATUS_REMARK));
    if(pd==null)
    {
      updateStopsLayer(true);
    }
    else
    {
      final String[] values = pd.getAllowedValues().toArray(new String[pd.getAllowedValues().size()]);
      final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, values);
      new AlertDialog.Builder(getContext()).setTitle(R.string.exceptionReasonPrompt).setAdapter(arrayAdapter, new DialogInterface.OnClickListener()
      {
  
        public void onClick(DialogInterface dialog, int which)
        {
          String selection = "";
          try
          {
            ListView lw = ((AlertDialog) dialog).getListView();
            selection = (String) lw.getAdapter().getItem(which);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          getWorkOrder().setAttributes(getResources().getString(R.string.ALIAS_STOPSLAYER_STATUS_REMARK), selection);
          updateStopsLayer(true);
          dialog.dismiss();
        }
      }).create().show();
    }
  }
  
  private PropertyDefinition getPropertyDefinitionBasedOnDependency(String name)
  {
    List<PropertyDefinition> pds = stopsConfiguration.getPropertyDefinitions(name);
    if(pds != null)
    {
      for(PropertyDefinition p:pds)
      {
        String dependsOn = p.getDependsOn();
        if(dependsOn != null)
        {
          if(dependsOn.contains("="))
          {
            String fieldAlias = dependsOn.split("=")[0].trim();
            String value = dependsOn.split("=")[1].trim();
            if(((String)getWorkOrder().getAttributes(fieldAlias)).equals(value))
            {
              return p;
            }
          }
        }
      }
    }
    
    return null;
  }

  private class GetPictureTask extends AsyncTask<String, Void, Bitmap>
  {

    @Override
    protected Bitmap doInBackground(String... uri)
    {

      URL url;
      Bitmap bmp = null;
      try
      {
        url = new URL(uri[0]);
        bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
      }
      catch (MalformedURLException e)
      {

      }
      catch (IOException e)
      {

      }
      return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bmp)
    {
      ImageView imageViewWorkOrderLocation = (ImageView) findViewById(R.id.icon_workorderLocation);
      try
      {
        if (bmp != null)
        {
          imageViewWorkOrderLocation.setImageBitmap(bmp);
        }
        else
        {
          imageViewWorkOrderLocation.setImageResource(R.drawable.image_missing);
        }
      }
      catch (Exception e)
      {
        imageViewWorkOrderLocation.setImageResource(R.drawable.image_missing);
      }
    }
  }

  private void showProgressDialog()
  {
    progressDialog = new ProgressDialog(context);
    progressDialog.setTitle("Processing...");
    progressDialog.setMessage("Please wait.");
    progressDialog.setCancelable(false);
    progressDialog.setIndeterminate(true);
    progressDialog.show();
  }

  private void updateStopsLayer(final boolean notifyGEP)
  {
    showProgressDialog();

    agsObjects = ((AGSObjects) getApplicationContext());
    stopLayer = agsObjects.getStopsLayer();
    Map<String, Object> attrs = getWorkOrder().getAllAttributes();

    final Graphic newGraphic = new Graphic(getWorkOrder().getGraphic().getGeometry(), null, attrs);
    try
    {
      stopLayer.applyEdits(null, null, new Graphic[] { newGraphic }, new CallbackListener<FeatureEditResult[][]>()
      {
        public void onCallback(FeatureEditResult[][] result)
        {

          if (notifyGEP)
          {
            stopPendingLayer = agsObjects.getStopsPendingLayer();
            try
            {
              stopPendingLayer.applyEdits(new Graphic[] { newGraphic }, null, null, new CallbackListener<FeatureEditResult[][]>()
              {

                public void onError(Throwable arg0)
                {
                  progressDialog.dismiss();
                  showUpdateResult(false);
                }

                public void onCallback(FeatureEditResult[][] arg0)
                {
                  progressDialog.dismiss();
                  showUpdateResult(true);
                  // fillView(getWorkOrder(), stopsConfiguration, viewToLoad);
                  if (getWorkOrder().getType().equals(NonServiceWorkOrderType.Break.toString()))
                  {
                    Button updateButton = (Button) findViewById(R.id.updateButton);
                    if (getWorkOrder().getStatus().equals(WorkOrderStatus.AtStop.toString()) || getWorkOrder().getStatus().equals(WorkOrderStatus.Completed.toString()))
                      updateButton.setEnabled(false);
                    else
                      updateButton.setEnabled(true);
                  }
                }
              });
            }
            catch (Exception e)
            {
              progressDialog.dismiss();
              showUpdateResult(false);
            }
          }
          else
          {
            progressDialog.dismiss();
            showUpdateResult(true);
          }
        }

        public void onError(Throwable e)
        {
          progressDialog.dismiss();
          showUpdateResult(false);
        }
      });
    }
    catch (Exception e)
    {
      progressDialog.dismiss();
      showUpdateResult(false);
    }
  }

  private void showUpdateResult(final boolean successful)
  {
    WorkOrderDetailActivity.this.runOnUiThread(new Runnable()
    {

      public void run()
      {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

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

  private int getPixelFromDP(int dp)
  {
    final float scale = this.getResources().getDisplayMetrics().density;
    return (int) (dp * scale + 0.5f);
  }

  protected String getUpdateUrlPrefix()
  {
    return updateUrlPrefix;
  }

  protected void setUpdateUrlPrefix(String updateUrlPrefix)
  {
    this.updateUrlPrefix = updateUrlPrefix;
  }

  protected Context getContext()
  {
    return context;
  }

  protected void setContext(Context context)
  {
    this.context = context;
  }

  protected WorkOrder getWorkOrder()
  {
    return workOrder;
  }

  protected void setWorkOrder(WorkOrder workOrder)
  {
    this.workOrder = workOrder;
  }

  @Override
  protected void onDestroy()
  {
    // TODO Auto-generated method stub
    super.onDestroy();
  }

  @Override
  protected void onPause()
  {
    // TODO Auto-generated method stub
    super.onPause();
  }

  @Override
  protected void onRestart()
  {
    // TODO Auto-generated method stub
    super.onRestart();
  }

  @Override
  protected void onResume()
  {
    // TODO Auto-generated method stub
    super.onResume();
  }

  @Override
  protected void onStart()
  {
    // TODO Auto-generated method stub
    super.onStart();
  }

  @Override
  protected void onStop()
  {
    // TODO Auto-generated method stub
    super.onStop();
  }

  public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
  {
    needToClose = true;
  }

}
