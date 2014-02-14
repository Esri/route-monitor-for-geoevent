package com.esri.ges.manager.stops;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.esri.ges.core.Uri;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;

public interface StopsManager
{
  public List<Stop> getStops();
  public List<Stop> getStopsByRouteName(String key);
  public int getSequenceNumberForNextAssignedStop(String key);
  public Stop getStopByName(String name);
  public Stop addOrReplaceStop(Stop stop);
  public void batchAddOrReplaceStops( List<Stop> stops );
  public void removeAllStops();
  public GeoEvent createGeoEvent(Stop stop, String ownerId, Uri uri);
  public GeoEvent createGeoEvent(List<Stop> stops, String ownerId, Uri uri);
  public GeoEvent createListGeoEvent(List<Stop> stops, String requestId, String ownerId, Uri uri);
  public String getStopsAoiCategory();
  public List<Stop> unassign( List<String> stopNames );
  public String getUnassignedRouteName();
  public String getCanceledRouteName();
  public Stop createStop(String stopName);
  public void loadStopsConfigurations(String path);
  public StopsConfiguration getStopsConfiguration(String stopType);
  public StopsConfigurations getStopsConfigurations();
  public Map<String, String> getDefaultStopFields();
//  public void updateStop(String stopName, Map<String, String> updateParams);
//  public GeoEvent createAoiForStop(String naConnection, String path, int driveTime, Stop stop);
//  public GeoEvent createBufferForStop(double bufferDistance, Stop stop);
  public void clearAllStops(String agsConnectionName, String path, String featureService, String stopLayer, String geofenceLayer);
  public List<Stop> reloadStops(String agsConnectionName, String path, String featureService, String layer);
  public void convertGeoEventToStop( GeoEvent message, Stop stop );
  public GeoEventDefinition getStopsGeoEventDefinition();
}
