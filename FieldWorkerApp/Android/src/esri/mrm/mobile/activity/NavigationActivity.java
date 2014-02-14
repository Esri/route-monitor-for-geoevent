package esri.mrm.mobile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.esri.core.portal.Portal;
import com.esri.core.portal.WebMap;
import com.esri.core.tasks.ags.query.Query;

import esri.mrm.mobile.AGSObjects;
import esri.mrm.mobile.R;
import esri.mrm.mobile.WorkOrderStatus;

public class NavigationActivity extends Activity implements OnSharedPreferenceChangeListener
{
  MapView                    map               = null;
  ArcGISTiledMapServiceLayer tiledMapService;
  private Toast              toast;
  private long               lastBackPressTime = 0;

  private AGSObjects         agsObjects;
  private ProgressDialog     progressDialog;

  private String             webmapItemId;
  private String             layernameStops;
  private String             layernameStopsPending;
  private String             layernameVehicles;
  private String             layernameRoutes;
  private String             layernameMessages;
  private String             layernameMessagesPending;
  private String             layernameRouteAssignment;
  private int                refreshInterval;
  private SharedPreferences  sharedPrefs;
  private Portal portal;

  /** Called when the activity is first created. */
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    agsObjects = ((AGSObjects) getApplicationContext());
    
    loadPreferences();
    loadMap();
  }

  private void loadPreferences()
  {
    webmapItemId = sharedPrefs.getString("webmap_item_id", null);
    layernameStops = sharedPrefs.getString("layername_stops", null);
    layernameStopsPending = sharedPrefs.getString("layername_stops_pending", null);
    layernameVehicles = sharedPrefs.getString("layername_vehicles", null);
    layernameRoutes = sharedPrefs.getString("layername_routes", null);
    layernameMessages = sharedPrefs.getString("layername_messages", null);
    layernameMessagesPending = sharedPrefs.getString("layername_messages_pending", null);
    layernameRouteAssignment = sharedPrefs.getString("layername_route_assignment", null);
    refreshInterval = Integer.parseInt(sharedPrefs.getString("layer_refresh_interval", "10000"));

    agsObjects.setStopsLayer(null);
    agsObjects.setRoutesLayer(null);
    agsObjects.setStopsPendingLayer(null);
    agsObjects.setMessagesLayer(null);
    agsObjects.setMessagesPendingLayer(null);
    portal = agsObjects.getPortal();
  }

  private void loadMap()
  {
    
    if(webmapItemId == null || webmapItemId.length()==0)
    {
      return;
    }
    
    showProgressDialog();
    
    try
    {
      WebMap.newInstance(webmapItemId, portal,
        new CallbackListener<WebMap>() {

          public void onError(Throwable e) {
            if (progressDialog != null)
              progressDialog.dismiss();
            final String error = e.getMessage();
            NavigationActivity.this.runOnUiThread(new Runnable() {
              public void run() {
                Toast.makeText(NavigationActivity.this,
                    "Webmap failed to load: " + error,
                    Toast.LENGTH_LONG).show();
              }
            });
            
          }

          public void onCallback(final WebMap webmap) {
            
            // Add the mapview in the ui thread.
            NavigationActivity.this.runOnUiThread(new Runnable() {

              public void run() {

                if (webmap != null){
                  map = new MapView(NavigationActivity.this, webmap, "", null);
                  
                  map.setOnStatusChangedListener(new OnStatusChangedListener() {
    
                    private static final long serialVersionUID = 1L;

                    public void onStatusChanged(Object source, STATUS status) {
                      if(status.getValue() == EsriStatusException.INIT_FAILED_WEBMAP_UNSUPPORTED_LAYER)
                      {
                        
                        Toast.makeText(NavigationActivity.this,
                            "Webmap failed to load",
                            Toast.LENGTH_LONG).show();
                      }
                      else if (source == map && status == STATUS.INITIALIZED)
                      {
                        Layer[] layers = map.getLayers();
                        for (Layer layer : layers)
                        {
                          // layer.setVisible(false);
                          if (layer.isInitialized())
                          {
                            if (layer instanceof ArcGISFeatureLayer)
                            {
                              ArcGISFeatureLayer agsLayer = (ArcGISFeatureLayer) layer;
                              drawLayers(agsLayer);
                            }
                          }
                          else
                          {
                            layer.setOnStatusChangedListener(new OnStatusChangedListener()
                            {

                              /**
                               * 
                               */
                              private static final long serialVersionUID = 1L;

                              public void onStatusChanged(Object arg0, STATUS arg1)
                              {
                                if (arg0 instanceof ArcGISFeatureLayer && arg1 == STATUS.INITIALIZED)
                                {
                                  ArcGISFeatureLayer agsLayer = (ArcGISFeatureLayer) arg0;
                                  drawLayers(agsLayer);

                                }

                              }
                            });

                          }
                        }
                      }
                      else if (source == map && status == STATUS.INITIALIZATION_FAILED)
                      {
                        if (progressDialog != null)
                          progressDialog.dismiss();
                      }
                      
                    }
                  });
                  setContentView(map);
                } 

              }
            });

          }
        });
    }
    catch(Exception e)
    {
      Toast.makeText(NavigationActivity.this,
          "Webmap failed to load: " + e.getMessage(),
          Toast.LENGTH_LONG).show();
    }
  }

  private void drawLayers(ArcGISFeatureLayer agsLayer)
  {
    if (agsLayer.getName().equals(layernameStops))
      drawStopLayer(agsLayer);
    else if (agsLayer.getName().equals(layernameVehicles))
      drawVehicleLayer(agsLayer);
    else if (agsLayer.getName().equals(layernameRoutes))
      drawRouteLayer(agsLayer);
    else if (agsLayer.getName().equals(layernameRouteAssignment))
      queryRouteAssignmentLayer(agsLayer);
    else if (agsLayer.getName().equals(layernameMessages))
      drawMessageLayer(agsLayer);
    else if (agsLayer.getName().equals(layernameMessagesPending))
    {
      agsLayer.setVisible(false);
      agsObjects.setMessagesPendingLayer(agsLayer);
    }
    else if (agsLayer.getName().equals(layernameStopsPending))
    {
      agsLayer.setVisible(false);
      agsObjects.setStopsPendingLayer(agsLayer);
    }
    else
    {
      agsLayer.setVisible(false);
    }
  }
  
  private void queryRouteLayer(ArcGISFeatureLayer layer)
  {
    System.out.println("   ---   " + "Called queryRouteLayer." + "   ---   ");
    //String queryString = getResources().getString(R.string.KEY_ROUTE_NAME) + "='" + agsObjects.getRouteId() + "'";
    String routeFieldName = getFieldNamebyAlias(layer, getResources().getString(R.string.ALIAS_ROUTESLAYER_ROUTE));
    final String vehicleFieldName = getFieldNamebyAlias(layer, getResources().getString(R.string.ALIAS_ROUTESLAYER_Vehicle));
    
    String queryString = routeFieldName + "='" + agsObjects.getRouteId() + "'";
    System.out.println("   ---   " + "query string is " + queryString + "   ---   ");
    Query query = new Query();
    query.setWhere(queryString); 
    query.setOutFields(new String[] { "*" });
    layer.queryFeatures(query, new CallbackListener<FeatureSet>()
    {
        public void onError(Throwable arg0)
        {
          System.out.println("   ---   " + "queryRouteLayer error." + "   ---   ");
          progressDialog.dismiss();
        }

        public void onCallback(FeatureSet queryResults)
        {
          if (queryResults.getGraphics().length > 0)
          {
            Graphic graphic = queryResults.getGraphics()[0];
            if(graphic.getGeometry() != null)
              map.setExtent(graphic.getGeometry(), 60);
            String vehicleId = (String) graphic.getAttributeValue(vehicleFieldName);
            System.out.println("   ---   " + "Got Veh ID " + vehicleId + "   ---   ");
            agsObjects.setVehicleId(vehicleId);
            if (agsObjects.getVehiclesLayer() != null)
            {
              drawVehicleLayer(agsObjects.getVehiclesLayer());
            }
          }
          else
          {
            System.out.println("   ---   " + "queryRouteLayer got nothing." + "   ---   ");
          }
          progressDialog.dismiss();
        }
      });
  }
  
  private void queryRouteAssignmentLayer(ArcGISFeatureLayer layer)
  {
    System.out.println("   ---   " + "Called queryRouteAssignmentLayer." + "   ---   ");
    final String accountName = agsObjects.getUsername();
    String accountNameFieldName = getFieldNamebyAlias(layer, getResources().getString(R.string.KEY_ACCOUNT_NAME_RA));
    final String routeNameFieldName = getFieldNamebyAlias(layer, getResources().getString(R.string.KEY_ROUTE_NAME_RA));
    String queryString = accountNameFieldName + "='" + accountName + "'";
    System.out.println("   ---   " + "query string is " + queryString + "   ---   ");
    Query query = new Query();
    query.setWhere(queryString); 
    query.setOutFields(new String[] { "*" });
    layer.queryFeatures(query, new CallbackListener<FeatureSet>()
    {
        public void onError(Throwable arg0)
        {
          System.out.println("   ---   " + "queryRouteAssignmentLayer error." + "   ---   ");
          progressDialog.dismiss();
        }

        public void onCallback(FeatureSet queryResults)
        {
          if (queryResults.getGraphics().length > 0)
          {
            Graphic graphic = queryResults.getGraphics()[0];
            String routeId = (String) graphic.getAttributeValue(routeNameFieldName);
            System.out.println("   ---   " + "Got Route ID " + routeId + "   ---   ");
            agsObjects.setRouteId(routeId);
            if (agsObjects.getRoutesLayer() != null)
              drawRouteLayer(agsObjects.getRoutesLayer());
            if (agsObjects.getStopsLayer() != null)
              drawStopLayer(agsObjects.getStopsLayer());
            if(agsObjects.getMessagesLayer() != null)
              drawMessageLayer(agsObjects.getMessagesLayer());
          }
          else
          {
            System.out.println("   ---   " + "queryRouteAssignmentLayer got nothing." + "   ---   ");
            progressDialog.dismiss();
            showAlertMessage("Warning", "No route is assigned to " + accountName);
          }
        }
      });
  }
  
  private void drawStopLayer(ArcGISFeatureLayer agsLayer)
  {
    System.out.println("   ---   " + "Called drawStopLayer." + "   ---   ");
    if (agsObjects.getRouteId() != null)
    {
      String routeFieldName = getFieldNamebyAlias(agsLayer, getResources().getString(R.string.ALIAS_STOPSLAYER_ROUTE_NAME));
      String statusFieldName = getFieldNamebyAlias(agsLayer, getResources().getString(R.string.ALIAS_STOPSLAYER_STATUS));
      final String whereClause = routeFieldName + "='" + agsObjects.getRouteId() + "' and " 
          + statusFieldName + " in ('" + WorkOrderStatus.Dispatched.toString() + "','" + WorkOrderStatus.AtStop.toString() + "','" 
          + WorkOrderStatus.Completed.toString() + "','" + WorkOrderStatus.Exception.toString() + "')";
      System.out.println("   ---   " + "Where clause is " + whereClause + "   ---   ");
      agsLayer.setDefinitionExpression(whereClause);
      agsLayer.setVisible(true);
      agsLayer.refresh();
    }
    else
    {
      System.out.println("   ---   " + "Route ID is null." + "   ---   ");
      agsLayer.setVisible(false);
    }
    agsLayer.setAutoRefreshOnExpiration(true);
    agsLayer.setExpirationInterval(refreshInterval/1000);
    
    agsObjects.setStopsLayer(agsLayer);
  }
  
  private void drawMessageLayer(ArcGISFeatureLayer agsLayer)
  {
    System.out.println("   ---   " + "Called drawMsgLayer." + "   ---   ");
    if (agsObjects.getRouteId() != null)
    {
      String msgTo = getFieldNamebyAlias(agsLayer, getResources().getString(R.string.ALIAS_MESSAGESLAYER_MESSAGE_TO));
      final String whereClause = msgTo + "='" + agsObjects.getRouteId() + "'";
      System.out.println("   ---   " + "Where clause is " + whereClause + "   ---   ");
      agsLayer.setDefinitionExpression(whereClause);
      agsLayer.refresh();
    }
    else
      System.out.println("   ---   " + "Route ID is null." + "   ---   ");
    agsLayer.setVisible(false);
    agsLayer.setAutoRefreshOnExpiration(true);
    agsLayer.setExpirationInterval(refreshInterval/1000);
    agsObjects.setMessagesLayer(agsLayer);
  }
  
  private void drawVehicleLayer(ArcGISFeatureLayer agsLayer)
  {
    System.out.println("   ---   " + "Called drawVehLayer." + "   ---   ");
    if (agsObjects.getVehicleId() != null)
    {
      String vehicleFieldName = getFieldNamebyAlias(agsLayer, getResources().getString(R.string.ALIAS_VEHICLESSLAYER_Vehicle));
      final String whereClause = vehicleFieldName + "='" + agsObjects.getVehicleId() + "'";
      System.out.println("   ---   " + "Where clause is " + whereClause + "   ---   ");
      agsLayer.setDefinitionExpression(whereClause);
      agsLayer.setVisible(true);
      agsLayer.refresh();
    }
    else
    {
      agsLayer.setVisible(false);
      System.out.println("   ---   " + "Veh ID is null." + "   ---   ");
    }
    agsLayer.setAutoRefreshOnExpiration(true);
    agsLayer.setExpirationInterval(refreshInterval/1000);
    agsObjects.setVehiclesLayer(agsLayer);
  }
  
  private void drawRouteLayer(ArcGISFeatureLayer agsLayer)
  {
    System.out.println("   ---   " + "Called drawRouteLayer." + "   ---   ");
    if (agsObjects.getRouteId() != null)
    {
      queryRouteLayer(agsLayer);
      String routeFieldName = getFieldNamebyAlias(agsLayer, getResources().getString(R.string.ALIAS_ROUTESLAYER_ROUTE));
      String statusFieldName = getFieldNamebyAlias(agsLayer, getResources().getString(R.string.ALIAS_ROUTESLAYER_Status));
      final String whereClause = routeFieldName + "='" + agsObjects.getRouteId() + "' and " + statusFieldName + "='Dispatched'";
      System.out.println("   ---   " + "Where clause is " + whereClause + "   ---   ");
      agsLayer.setDefinitionExpression(whereClause);
      agsLayer.setVisible(true);
      agsLayer.refresh();
    }
    else
    {
      agsLayer.setVisible(false);
      System.out.println("   ---   " + "Route ID is null." + "   ---   ");
    }
    agsLayer.setAutoRefreshOnExpiration(true);
    agsLayer.setExpirationInterval(refreshInterval/1000);
    agsObjects.setRoutesLayer(agsLayer);
  }
  
  private String getFieldNamebyAlias(ArcGISFeatureLayer layer, String alias)
  {
    String out = "";
    Field[] fields = layer.getFields();
    for(int i=0; i<fields.length; i++)
    {
      if(fields[i].getAlias().equals(alias))
        out = fields[i].getName();
    }
    return out;
  }

  private void showProgressDialog()
  {
    progressDialog = new ProgressDialog(this);
    progressDialog.setTitle("Initializing Map");
    progressDialog.setMessage("Please wait...");
    progressDialog.setCancelable(false);
    progressDialog.setIndeterminate(true);
    progressDialog.show();
    timerDelayRemoveDialog(30000, progressDialog);
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
    inflater.inflate(R.menu.mrm_menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle item selection
    switch (item.getItemId())
    {
      case R.id.settings:
        Intent intent = new Intent(this, FieldWorkerAppPreference.class);
        intent.putExtra("settingsId", R.xml.preference);
        startActivity(intent);
        return true;
      default:
        return true;
    }
  }

  private void cleanup()
  {
    if (map != null)
    {
      Layer[] layers = map.getLayers();
      for (Layer layer : layers)
      {
        if (layer instanceof ArcGISFeatureLayer)
        {
          ((ArcGISFeatureLayer) layer).setAutoRefreshOnExpiration(false);
        }
      }
    }
    if(agsObjects != null)
      agsObjects.clear();
    map = null;
  }

  protected void onPause()
  {
    super.onPause();
    if(map != null)
      map.pause();
//    sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
  }

  protected void onResume()
  {
    super.onResume();
    if(map != null)
      map.unpause();
//    sharedPrefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    cleanup();
  }

  @Override
  protected void onStop()
  {
    super.onStop();
  }

  public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1)
  {
    loadPreferences();
    loadMap();
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
    NavigationActivity.this.runOnUiThread(new Runnable()
    {

      public void run()
      {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NavigationActivity.this);

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

}
