package com.esri.ges.manager.plan;

import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;

public interface PlanManager
{
  public GeoEventDefinition getPlanCommandGeoEventDefinition();
  public String getPlanCommandActionField();
  public String getPlanCommandActionClear();
  public String getPlanCommandActionGet();
  public String getPlanCommandActionLoad();
  public String getPlanCommandActionReload();
  public GeoEvent clearPlan(GeoEvent geoEvent, String agsConnectionName, String path, String featureService, String stopLayer, String routeLayer, String vehicleLayer, String geofenceLayer, String alertLayer);
  public GeoEvent getPlan();
  public GeoEvent reloadPlan(String agsConnection, String Folder, String featureService, String stopLayer, String routeLayer, String vehicleLayer, String alertLayer);
  public GeoEvent loadPlan(GeoEvent geoEvent); 
}
