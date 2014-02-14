package esri.mrm.mobile.activity;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;

public class WorkOrdersTabGroupActivity extends TabGroupActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("WorkOrdersActivity", new Intent(this,WorkOrdersActivity.class));
    }
    
    public void returnToDefaultActivity()
    {
      LocalActivityManager manager = getLocalActivityManager();
      Activity activity = manager.getCurrentActivity();
      if(activity instanceof WorkOrderDetailActivity)
      {
        if(((WorkOrderDetailActivity)activity).isNeedToClose())
        {
          String id = manager.getCurrentId();
          manager.destroyActivity(id, false);
          startChildActivity("WorkOrdersActivity", new Intent(this,WorkOrdersActivity.class));
        }
      }
    }

    @Override
    protected void onResume()
    {
      returnToDefaultActivity();
      super.onResume();
    }
    
}
