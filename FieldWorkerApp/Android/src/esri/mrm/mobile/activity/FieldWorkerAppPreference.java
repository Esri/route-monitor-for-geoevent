package esri.mrm.mobile.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;

public class FieldWorkerAppPreference extends PreferenceActivity
{
  @Override
  public void onCreate(Bundle savedInstanceState) {        
      super.onCreate(savedInstanceState);    
      int settingsId = this.getIntent().getIntExtra("settingsId", -1);
      addPreferencesFromResource(settingsId);
      
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(Menu.NONE, 0, 0, "Show current settings");
      return super.onCreateOptionsMenu(menu);
  }


}
