package esri.mrm.mobile.activity;

import esri.mrm.mobile.R;
import esri.mrm.mobile.R.drawable;
import esri.mrm.mobile.R.id;
import esri.mrm.mobile.R.layout;
import esri.mrm.mobile.R.string;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class EsriMrmActivity extends TabActivity
{
  private TabHost tabHost;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    // This line must be here or crash on start
//    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

    TabHost tabHost = getTabHost(); // The activity TabHost

    setTabs();
    tabHost.setCurrentTab(2);

    // This line must be here or no custom title
//    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
  }

  private void setTabs()
  {
    tabHost = getTabHost();
    addTab(R.string.tab_3, R.drawable.ic_tab_messages, MessagesTabGroupActivity.class);
    addTab(R.string.tab_1, R.drawable.ic_tab_workorders, WorkOrdersTabGroupActivity.class);
    addTab(R.string.tab_2, R.drawable.ic_tab_navigation, NavigationActivity.class);
    
//    addTab(R.string.tab_4, R.drawable.ic_tab_history, HistoryActivity.class);
  }

  private void addTab(int labelId, int drawableId, Class<?> cls)
  {
    Intent intent = new Intent(this, cls);
    TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);

    View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);

    TextView title = (TextView) tabIndicator.findViewById(R.id.title);
    title.setText(labelId);
    ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
    icon.setImageResource(drawableId);

    spec.setIndicator(tabIndicator);
    spec.setContent(intent);
    tabHost.addTab(spec);
  }

}
