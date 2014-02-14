package com.esri.ges.processor.autoarrivaldeparture;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.condition.ConditionService;
import com.esri.ges.condition.spatial.SpatialCondition;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.incident.AlertType;
import com.esri.ges.core.incident.Incident;
import com.esri.ges.core.incident.IncidentType;
import com.esri.ges.core.operator.SpatialOperator;
import com.esri.ges.core.resource.Resource;
import com.esri.ges.manager.autoarrivaldeparture.AutoArrivalDepartureManager;
import com.esri.ges.manager.incident.IncidentManager;
import com.esri.ges.manager.routes.Route;
import com.esri.ges.manager.routes.RouteManager;
import com.esri.ges.manager.stops.Stop;
import com.esri.ges.manager.stops.StopStatus;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.manager.vehicles.Vehicle;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.processor.CacheEnabledGeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.ges.registry.condition.ConditionRegistry;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryType;
import com.esri.ges.spatial.Point;
import com.esri.ges.util.DateUtil;
import com.esri.ges.util.Validator;

public class AutoArrivalDepartureProcessor extends CacheEnabledGeoEventProcessor
{
  private static final Log log = LogFactory.getLog( AutoArrivalDepartureProcessor.class );
  private StopsManager stopsManager;
  private RouteManager routeManager;
  private ConditionRegistry conditionRegistry;
  private Map<String, StopIncidentConditions> stopConditions = new ConcurrentHashMap<String,StopIncidentConditions>();
//  private IncidentManager incidentManager;
  private AutoArrivalDepartureManager autoArrivalDepartureManager;
//  private final Map<String, String> incidentIDMapper = new ConcurrentHashMap<String, String>();
  private VehiclesManager vehiclesManager;
  
  private class StopIncidentConditions
  {
    SpatialCondition open;
    SpatialCondition close;
  }

  protected AutoArrivalDepartureProcessor( GeoEventProcessorDefinition definition,
                           StopsManager stopsManager,
                           RouteManager routeManager,
                           VehiclesManager vehiclesManager,
                           ConditionRegistry conditionRegistry,
                           AutoArrivalDepartureManager autoArrivalDepartureManager) throws ComponentException
  {
    super(definition);
    this.stopsManager = stopsManager;
    this.vehiclesManager = vehiclesManager;
    this.routeManager = routeManager;
    this.conditionRegistry = conditionRegistry;
    this.autoArrivalDepartureManager = autoArrivalDepartureManager;
  }

  @Override
  public void afterPropertiesSet()
  {
    super.afterPropertiesSet();
  }
  
  private String buildIncidentCacheKey(GeoEvent geoEvent)
  {
    if (geoEvent != null && geoEvent.getTrackId() != null && geoEvent.getStartTime() != null && geoEvent.getGeometry() != null)
    {
      GeoEventDefinition definition = geoEvent.getGeoEventDefinition();
      return definition.getOwner() + "/" + definition.getName() + "/" + geoEvent.getTrackId();
    }
    return null;
  }

  @Override
  public GeoEvent process(GeoEvent geoEvent) throws Exception
  {
    Date eventTime = (Date) geoEvent.getField("TIME_START");
    String trackId = (String) geoEvent.getField("TRACK_ID");
    Stop stop = getNextStop(trackId);
    if (stop == null)
    {
      log.error( "Couldn't update ETA for track "+trackId+" because the next stop was not found." );
      return null;
    }
    
    String incidentCacheKey = buildIncidentCacheKey(geoEvent);
    if( incidentCacheKey != null )
    {
//      String guid = incidentIDMapper.get( incidentCacheKey );
      String guid = autoArrivalDepartureManager.getIncidentId(incidentCacheKey);
      Incident incident = null;
      StopIncidentConditions conditions = stopConditions.get( stop.getName() );
      if( guid != null && autoArrivalDepartureManager.hasIncident( guid ) )
      {
        autoArrivalDepartureManager.updateIncident( guid, geoEvent );
        incident = autoArrivalDepartureManager.getIncidentById(guid);
      }
      else
      {
        if( conditions == null )
        {
          conditions = createOpenSpatialConditionForStop( stop );
          stopConditions.put( stop.getName(), conditions );
        }
        if( conditions.open.evaluate( geoEvent ) )
        {
          incident = autoArrivalDepartureManager.openIncident( "Arrive-Depart for Stop "+stop.getName(),
                                                   IncidentType.Cumulative, AlertType.Notification, 
                                                   com.esri.ges.core.incident.GeometryType.Point, conditions.open, 
                                                   conditions.close, geoEvent.getGeoEventDefinition().getOwner(), definition.getUri(), 3600, geoEvent, incidentCacheKey);
//          incidentIDMapper.put( incidentCacheKey, incident.getId() );
          if(stop.getStatus()==StopStatus.Dispatched || stop.getStatus()==StopStatus.Assigned)
          {
            stop.setActualArrival( eventTime );
            stop.setStatus( StopStatus.AtStop );
          }
        }
      }
      if (incident != null)
      {
        if( conditions.close.evaluate(geoEvent) || stop.getStatus()==StopStatus.Completed || stop.getStatus()==StopStatus.Exception)
        {
//          incidentIDMapper.remove( incidentCacheKey );
//          incidentManager.closeIncident( guid, geoEvent );
          autoArrivalDepartureManager.closeIncident(incidentCacheKey, geoEvent);
          stopConditions.remove(stop.getName());
          Vehicle vehicle = vehiclesManager.getVehicleByName( stop.getRouteName() );
          vehicle.setNextStopSequenceNumber( stop.getSequenceNumber()+1 );

          if(stop.getStatus()==StopStatus.AtStop)
          {
            stop.setActualDeparture( eventTime );
            stop.setActualServiceDuration( (int)DateUtil.minutesBetween( stop.getActualArrival(), eventTime ) );
            stop.setStatus( StopStatus.Completed );
            
          }
          
        }
      }
    }
    stop.setLastUpdated(eventTime);
    cacheGeoEventWithVehichleResource( geoEvent, trackId );
    return createGeoEvent(stop);
  }

  private void cacheGeoEventWithVehichleResource(GeoEvent geoEvent, String trackId)
  {
    Route route = routeManager.getRouteByName( trackId );
    if( route == null )
    {
      log.error( "Couldn't find route "+trackId+" when trying to cache last GeoEvent." );
    }
    else
    {
      Resource resource = vehiclesManager.getResourceForVehicle( trackId );
      if( resource != null )
      {
        resource.cache( geoEvent );
        Geometry geom = geoEvent.getGeometry();
        if( geom.getType() == GeometryType.Point )
        {
          vehiclesManager.getVehicleByName( trackId ).setLocation( (Point)geom );
        }
      }
      else
      {
        log.info( "Didn't find resource for vehicle "+trackId );
      }
    }
  }

  private GeoEvent createGeoEvent(Stop stop) throws Exception
  {
    return stopsManager.createGeoEvent(stop, "arcgis", definition.getUri() );
  }

  private Stop getNextStop(String trackId)
  {
    Vehicle vehicle = vehiclesManager.getVehicleByName( trackId );
    List<Stop> stopsForVehicle = stopsManager.getStopsByRouteName( trackId );
    if( vehicle == null || Validator.isEmpty( stopsForVehicle ) )
    {
      return null;
    }
    if(vehicle.getNextStopSequenceNumber()==null)
      resetVehicleNextSequenceNumber();
    Integer nextStopSequenceNumber = vehicle.getNextStopSequenceNumber();
    if( nextStopSequenceNumber > stopsForVehicle.size() )
    {
      return null;
    }
    return stopsForVehicle.get( nextStopSequenceNumber );
  }
  
  private void resetVehicleNextSequenceNumber()
  {
    List<Vehicle> vehicles = vehiclesManager.getVehicles();
    for(Vehicle v:vehicles)
    {
      v.setNextStopSequenceNumber(stopsManager.getSequenceNumberForNextAssignedStop(v.getVehicleName()));
    }
  }

  private StopIncidentConditions createOpenSpatialConditionForStop( Stop stop )
  {
    StopIncidentConditions retConditions = new StopIncidentConditions();
    SpatialCondition condition = null;
    ConditionService service = conditionRegistry.getCondition("spatialCondition");
    if (service == null)
      throw new RuntimeException("Spatial conditions are not currently supported: bundle 'Esri :: AGES :: Condition :: Spatial' is not registered.");
    try
    {
//      String geoFenceString = stopsManager.getStopsAoiCategory()+"/"+stop.getName();
      String geoFenceString = stopsManager.getStopsAoiCategory()+"/"+Validator.normalizeName(stop.getName());
      condition = (SpatialCondition)service.create();
      condition.setGeofence( geoFenceString );
      condition.setOperator( SpatialOperator.INSIDE );
      condition.setOperand("GEOMETRY");
      condition.setGeoEventCache( geoEventCache );
      retConditions.open = condition;
      condition = (SpatialCondition)service.create();
      condition.setGeofence( geoFenceString );
      condition.setOperator( SpatialOperator.OUTSIDE );
      condition.setOperand("GEOMETRY");
      condition.setGeoEventCache( geoEventCache );
      retConditions.close = condition;
    }
    catch( Exception e)
    {
      throw new RuntimeException(e.getMessage());
    }
    return retConditions;
  }
  
  @Override
  public boolean isCacheRequired()
  {
    return false;
  }
}