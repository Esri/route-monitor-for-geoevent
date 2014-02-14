package esri.mrm.mobile.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import esri.mrm.mobile.NonServiceWorkOrderType;
import esri.mrm.mobile.NumberPicker;
import esri.mrm.mobile.R;
import esri.mrm.mobile.StopsConfigurations;
import esri.mrm.mobile.WorkOrder;
import esri.mrm.mobile.WorkOrderStatus;
import esri.mrm.mobile.WorkOrderUtility;
import esri.mrm.mobile.adapter.WorkOrderSpinnerAdapter;
import esri.mrm.mobile.task.SaveStopTask;

public class StopTypeActivity extends Activity
{

  private SharedPreferences        sharedPrefs;
  private WorkOrder woBefore;
  private Integer duration;
  private WorkOrder workOrder;
  private ProgressDialog     progressDialog;
  private Context            context;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    View view = LayoutInflater.from(getParent()).inflate(R.layout.add_stop, null);
    setContentView(view);
    context = view.getContext();
    // Stop stop;
    Bundle b = this.getIntent().getExtras();
    List<WorkOrder> workOrders = null;
    StopsConfigurations stopsConfigurations;
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
      stopsConfigurations = b.getParcelable("stopsConfigurations");
      workOrder = b.getParcelable("WorkOrder");
      fillView(workOrders);
    }
  }

  public void fillView(List<WorkOrder> workOrders)
  {
    ImageView imageView = (ImageView) findViewById(R.id.icon);
    imageView.setImageResource(R.drawable.ic_breaktime_w);
    

    TextView textViewStopTypeLabel = (TextView) findViewById(R.id.break_action_label);
    if(workOrder == null)
      textViewStopTypeLabel.setText("Add A Break");
    else
      textViewStopTypeLabel.setText("Change A Break");

    esri.mrm.mobile.NumberPicker durationPicker = (esri.mrm.mobile.NumberPicker) findViewById(R.id.timePicker);
    durationPicker.setRange(0, 60);
    durationPicker.setStep(5);
    if(workOrder == null)
      durationPicker.setCurrent(30);
    else
      durationPicker.setCurrent(workOrder.getScheduledDuration());
    durationPicker.setOnChangeListener(new esri.mrm.mobile.NumberPicker.OnChangedListener()
    {
      public void onChanged(NumberPicker picker, int oldVal, int newVal)
      {
        TextView textViewDuration = (TextView) findViewById(R.id.duration);
        duration = newVal;
        textViewDuration.setText(duration.toString() + " minutes");
        textViewDuration.invalidate();
      }
    });

    TextView textViewDurationLabel = (TextView) findViewById(R.id.duration_label);
    textViewDurationLabel.setText("Duration");

    TextView textViewDuration = (TextView) findViewById(R.id.duration);
    duration = durationPicker.getCurrent();
    textViewDuration.setText(duration.toString() + " minutes");
    
    TextView textViewInsertAfter = (TextView) findViewById(R.id.add_stop_after_label);
    textViewInsertAfter.setText("Insert the break after this stop:");

    List<WorkOrder> newList = WorkOrderUtility.getWorkOrderListForBreaks(workOrders);
    WorkOrderSpinnerAdapter adapterWorkOrders = new WorkOrderSpinnerAdapter(this, newList);
    Spinner spinnerWorkOrders = (Spinner) findViewById(R.id.spinnerAddStopAfter);

    // An anonymous listener to handle the event when is selected an spinner
    // item
    spinnerWorkOrders.setOnItemSelectedListener(new OnItemSelectedListener()
    {
      public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
      {
        WorkOrderSpinnerAdapter adaptor = (WorkOrderSpinnerAdapter) adapterView.getAdapter();
        woBefore = adaptor.getItem(position);
      }

      public void onNothingSelected(AdapterView<?> adapterView)
      {
        WorkOrderSpinnerAdapter adaptor = (WorkOrderSpinnerAdapter) adapterView.getAdapter();
      }
    });
    spinnerWorkOrders.setAdapter(adapterWorkOrders);
    
    if(workOrder != null)
      spinnerWorkOrders.setSelection(findPositionOfWorkOrderBeforeBreak( newList));
    
    // Save
    Button saveButton = (Button) findViewById(R.id.buttonSave);
    saveButton.setOnClickListener(new View.OnClickListener()
    {
      
      public void onClick(View v)
      {
        applyChanges(duration, woBefore);
      }
    });
  }
  
  private int findPositionOfWorkOrderBeforeBreak(List<WorkOrder> workOrders)
  {
    int breakSeq = workOrder.getSequence();
    int position = 0;
    for(WorkOrder wo : workOrders)
    {
      if(wo.getSequence()==(breakSeq-1))
        break;
      position++;
    }
    return position;
  }
  
  private String generateBreakName(WorkOrder workOrder)
  {
    Calendar now = Calendar.getInstance();
    return "Break-" + workOrder.getRouteName()+"-"+now.get(Calendar.HOUR_OF_DAY)+now.get(Calendar.MINUTE)+now.get(Calendar.SECOND);
  }
  
  private void applyChanges(int duration, WorkOrder woBefore)
  {
    WorkOrder brk = null;
    if(workOrder==null)
    {
      // new
      brk = new WorkOrder(woBefore.getGraphic(), woBefore.getFieldTypes(), woBefore.getFieldAliases(), woBefore.getContext());
      brk.setStopName(generateBreakName(woBefore));
      brk.setType(NonServiceWorkOrderType.Break.toString());
      brk.setStatus(WorkOrderStatus.Dispatched.toString());
      brk.setSequence(woBefore.getSequence()+1);
    }
    else
    {
      // update
      brk = workOrder;
      if(brk.getSequence()<woBefore.getSequence())
        brk.setSequence(woBefore.getSequence());
      else
        brk.setSequence(woBefore.getSequence()+1);
    }
    brk.setScheduledDuration(duration);
    brk.setLastUpdated(new Date().getTime());
    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    String url = sharedPrefs.getString("gep_url_port", "") + sharedPrefs.getString("gep_update_stop_path", "");
    new SaveStopTask(brk, StopTypeActivity.this, context, null).execute(url);
  }
}
