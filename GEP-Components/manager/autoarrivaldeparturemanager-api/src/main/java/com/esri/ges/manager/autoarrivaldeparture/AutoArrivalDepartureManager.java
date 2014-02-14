package com.esri.ges.manager.autoarrivaldeparture;

import java.util.Collection;
import java.util.List;

import com.esri.ges.core.Uri;
import com.esri.ges.core.condition.Condition;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.incident.AlertType;
import com.esri.ges.core.incident.GeometryType;
import com.esri.ges.core.incident.Incident;
import com.esri.ges.core.incident.IncidentType;
import com.esri.ges.core.resource.Resource;

public interface AutoArrivalDepartureManager
{
  public String buildIncidentKey(GeoEvent geoEvent);
  public String getIncidentId(String incidentKey);
  public boolean hasIncident(String incidentId);
  public Incident openIncident(String arg0, IncidentType arg1, AlertType arg2, GeometryType arg3, Condition arg4, Condition arg5, String arg6, Uri arg7, long arg8, GeoEvent arg9, String incidentCacheKey);
  public Incident updateIncident(String incidentId, GeoEvent geoEvent);
  public Incident getIncidentById(String incidentId);
  public Incident getIncidentByKey(String incidentKey);
  public void closeIncident(String incidentCacheKey, GeoEvent geoEvent);
  public void clearIncidents();
}
