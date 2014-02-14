package esri.mrm.mobile.activity;

import android.content.Intent;
import android.os.Bundle;

public class NavigationTabGroupActivity  extends TabGroupActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("NavigationActivity", new Intent(this,NavigationActivity.class));
    }
}