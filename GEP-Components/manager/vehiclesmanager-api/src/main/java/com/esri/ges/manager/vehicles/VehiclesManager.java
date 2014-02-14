package com.esri.ges.manager.vehicles;

import java.util.Collection;
import java.util.List;

import com.esri.ges.core.Uri;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.resource.Resource;

public interface VehiclesManager
{
  public List<Vehicle> getVehicles();
  public Vehicle getVehicleByName(String name);
  public void addOrReplaceVehicle(Vehicle vehicle);
  public void removeAllVehicles();
  public GeoEvent createGeoEvent(Vehicle vehicle, String ownerId, Uri uri);
  public Resource getResourceForVehicle(String name);
  public void clearAllVehicleFeatures(String agsConnectionName, String path, String featureService, String layer);
  public List<Vehicle> reloadVehicles(String agsConnectionName, String path, String featureService, String layer);
  public Vehicle createVehicleFromGeoEvent(GeoEvent geoEvent);
}
