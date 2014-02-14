package esri.mrm.mobile.activity;

import esri.mrm.mobile.adapter.MessageAdapter;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;

public class MessagesTabGroupActivity  extends TabGroupActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("MessagesActivity", new Intent(this,MessagesActivity.class));
    }
    
    public void returnToDefaultActivity()
    {
      LocalActivityManager manager = getLocalActivityManager();
      Activity activity = manager.getCurrentActivity();
      if(activity instanceof DispatchActivity)
      {
        if(((DispatchActivity)activity).isNeedToClose())
        {
          String id = manager.getCurrentId();
          manager.destroyActivity(id, false);
          startChildActivity("MessagesActivity", new Intent(this,MessagesActivity.class));
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
