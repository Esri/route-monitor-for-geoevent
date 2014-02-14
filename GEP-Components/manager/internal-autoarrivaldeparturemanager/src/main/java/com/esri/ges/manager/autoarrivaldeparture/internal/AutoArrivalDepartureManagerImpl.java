package com.esri.ges.manager.autoarrivaldeparture.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.Uri;
import com.esri.ges.core.condition.Condition;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.incident.AlertType;
import com.esri.ges.core.incident.GeometryType;
import com.esri.ges.core.incident.Incident;
import com.esri.ges.core.incident.IncidentType;
import com.esri.ges.manager.autoarrivaldeparture.AutoArrivalDepartureManager;
import com.esri.ges.manager.incident.IncidentManager;

public class AutoArrivalDepartureManagerImpl implements AutoArrivalDepartureManager
{
  final private static Log log = LogFactory.getLog( AutoArrivalDepartureManagerImpl.class );
  private IncidentManager incidentManager;
  private final Map<String, String> incidentIDMapper = new ConcurrentHashMap<String, String>();

  @Override
  public Incident openIncident(String arg0, IncidentType arg1, AlertType arg2, GeometryType arg3, Condition arg4, Condition arg5, String arg6, Uri arg7, long arg8, GeoEvent arg9, String incidentCacheKey)
  {
    Incident incident = incidentManager.openIncident( arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
    incidentIDMapper.put( incidentCacheKey, incident.getId() );
    return incident;
  }


  public void setIncidentManager(IncidentManager incidentManager)
  {
    this.incidentManager = incidentManager;
  }


  @Override
  public void closeIncident(String incidentCacheKey, GeoEvent geoEvent)
  {
    String guid = incidentIDMapper.get(incidentCacheKey);
    incidentIDMapper.remove( incidentCacheKey );
    if(guid != null && geoEvent != null)
      incidentManager.closeIncident( guid, geoEvent );
  }


  @Override
  public String buildIncidentKey(GeoEvent geoEvent)
  {
    if (geoEvent != null && geoEvent.getTrackId() != null && geoEvent.getStartTime() != null && geoEvent.getGeometry() != null)
    {
      GeoEventDefinition definition = geoEvent.getGeoEventDefinition();
      return definition.getOwner() + "/" + definition.getName() + "/" + geoEvent.getTrackId();
    }
    return null;
  }


  @Override
  public String getIncidentId(String incidentKey)
  {
    return incidentIDMapper.get(incidentKey);
  }


  @Override
  public boolean hasIncident(String incidentId)
  {
    return incidentManager.hasIncident(incidentId);
  }


  @Override
  public Incident updateIncident(String incidentId, GeoEvent geoEvent)
  {
    return incidentManager.updateIncident(incidentId, geoEvent);
  }


  @Override
  public Incident getIncidentById(String incidentId)
  {
    return incidentManager.getIncident(incidentId);
  }


  @Override
  public Incident getIncidentByKey(String incidentKey)
  {
    String id = incidentIDMapper.get(incidentKey);
    return incidentManager.getIncident(id);
  }


  @Override
  public void clearIncidents()
  {
    incidentIDMapper.clear();
  }

}
