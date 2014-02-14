package com.esri.ges.manager.stops.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;

import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.Uri;
import com.esri.ges.core.aoi.Aoi;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldCardinality;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldGroup;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.resource.Resource;
import com.esri.ges.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.datastore.agsconnection.ArcGISServerType;
import com.esri.ges.datastore.agsconnection.Layer;
import com.esri.ges.jaxb.resource.ResourceWrapper;
import com.esri.ges.manager.aoi.AoiManager;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.manager.resource.ResourceManager;
import com.esri.ges.manager.resource.ResourceManagerException;
import com.esri.ges.manager.routemonitor.util.FeatureUtil;
import com.esri.ges.manager.stops.DefaultStop;
import com.esri.ges.manager.stops.NonServiceStopType;
import com.esri.ges.manager.stops.Stop;
import com.esri.ges.manager.stops.StopResource;
import com.esri.ges.manager.stops.StopStatus;
import com.esri.ges.manager.stops.StopsConfiguration;
import com.esri.ges.manager.stops.StopsConfigurations;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.spatial.Spatial;
import com.esri.ges.util.Validator;

public class StopsManagerImpl implements StopsManager
{
  final static private Log log = LogFactory.getLog( StopsManagerImpl.class );
  final static private String stopsManagerAIOCategory = "AutoArrival";
  final static private String unassignedRouteName= "__Unassigned__";
  final static private String canceledRouteName= "__Canceled__";
  private Map<String, ArrayList<StopResource>> stopsByRouteName = new ConcurrentHashMap<String, ArrayList<StopResource>>();
  private Map<String, StopResource> stopsByStopName = new ConcurrentHashMap<String, StopResource>();
  private Map<String, String> stopToRouteNameMap = new ConcurrentHashMap<String,String>();
  private GeoEventCreator geoEventCreator;
  private ArcGISServerConnectionManager agsConnectionManager;
  private AoiManager aoiManager;
  private ResourceManager resourceManager;
  private Spatial spatial;
  private StopsConfigurations stopsConfigurations;
  private String stopGEDName;
  private String stopsListGEDName;
  private String stopGEDOwner;

  
  public void setStopsListGEDName(String stopsListGEDName)
  {
    this.stopsListGEDName = stopsListGEDName;
  }

  public void setStopGEDName(String stopGEDName)
  {
    this.stopGEDName = stopGEDName;
  }
  
  public void setStopGEDOwner(String stopGEDOwner)
  {
    this.stopGEDOwner = stopGEDOwner;
  }
  
  public void setResourceManager(ResourceManager resourceManager)
  {
    this.resourceManager = resourceManager;
  }
  
  public void setSpatial(Spatial spatial)
  {
    this.spatial = spatial;
  }
  
  public void setArcGISServerConnectionManager( ArcGISServerConnectionManager manager )
  {
    this.agsConnectionManager = manager;
  }  
  
  public void setMessaging( Messaging messaging )
  {
    this.geoEventCreator = messaging.createGeoEventCreator();
  }
  
  public void setAoiManager( AoiManager manager )
  {
    this.aoiManager = manager;
  }

  @Override
  public List<Stop> getStops()
  {
    List<Stop> allStops = new ArrayList<Stop>(); 
    allStops.addAll( stopsByStopName.values() );
    return allStops;
  }

  @Override
  public List<Stop> getStopsByRouteName(String key)
  {
    List<Stop> list = new ArrayList<Stop>();
    if (stopsByRouteName.containsKey(key))
    {
      List<StopResource> stopResources = stopsByRouteName.get(key);
      if( !Validator.isEmpty( stopResources ) )
      {
        list.addAll( stopResources );
      }
    }
    return list;
  }
  
  @Override
  public int getSequenceNumberForNextAssignedStop(String key)
  {
    List<Stop> list = getStopsByRouteName(key);
    int index = 0;
    for(Stop stop : list)
    {
      if(stop != null)
      {
        //if(stop.getType().equals("Service") && stop.getStatus()==StopStatus.Assigned)
        if( !stop.getType().equals(NonServiceStopType.Break.toString()) && !stop.getType().equals(NonServiceStopType.Lunch.toString()) 
            && (stop.getStatus()==StopStatus.Assigned || stop.getStatus()==StopStatus.Dispatched))
          break;
        index++;
      }
    }
    return index;
  }
  
  @Override
  public void batchAddOrReplaceStops( List<Stop> stops )
  {
    if( !Validator.isEmpty( stops ) )
    {
      Stop updatedStop;
      for( Stop stop : stops )
      {
        updatedStop = addOrReplaceInLocalMaps( stop );
      }
    }
  }

  @Override
  public Stop addOrReplaceStop(Stop stop)
  {
    if(stop.getType().equals(NonServiceStopType.Break.toString()) && stop.getStatus() != StopStatus.Canceled)
    {
      DefaultStop defaultStop = new DefaultStop(stop);
      com.esri.ges.spatial.Point location = findNextLocation(defaultStop);
      if(location==null)
        return null;
      else
      {
        defaultStop.setLocation(location);
        stop = defaultStop;
      }
    }
    return addOrReplaceInLocalMaps( stop );
  }
  
  private com.esri.ges.spatial.Point findNextLocation(Stop stop)
  {
    //Map<Integer, com.esri.ges.spatial.Point> locMap = new HashMap<Integer, com.esri.ges.spatial.Point>();
    List<com.esri.ges.spatial.Point> locMap = new ArrayList<com.esri.ges.spatial.Point>();
    List<Stop> stops = getStopsByRouteName(stop.getRouteName());
    int min = 0; 
    int max = 0;
    for(Stop s:stops)
    { 
      if(s != null)
      {
        if(s.getLocation() != null && !s.getName().equals(stop.getName()))
        {
          locMap.add(s.getLocation());
        }
      }
    }
    
    for(int i = stop.getSequenceNumber()-1; i>=min; i--)
    {
      if(locMap.get(i) != null)
      {
        return locMap.get(i);
      }
    }
    log.error("Cannot find a location to assign to the break.");
    return null;
  }

  private Stop addOrReplaceInLocalMaps( Stop stop )
  {
    StopResource stopToInsert;
    if(stop instanceof StopResource)
    {
      stopToInsert = (StopResource)stop;
    }
    else
    {
      stopToInsert = (StopResource)createStop(stop.getName());
      if(stop instanceof DefaultStop)
      {
        stopToInsert.update((DefaultStop)stop);
      }
    }

    if( stopToRouteNameMap.containsKey( stopToInsert.getName() ) )
    {
      removeStopFromRoute( stopToInsert );
    }
    String routeName = getUnassignedRouteName();
    if(stop.getStatus()!=StopStatus.Unassigned)
      routeName = stop.getRouteName();
    ArrayList<StopResource> stopsForRoute ;
    if (stopsByRouteName.containsKey(routeName))
    {
      stopsForRoute = stopsByRouteName.get(routeName);      
    }
    else
    {
      stopsForRoute = new ArrayList<StopResource>();
      stopsByRouteName.put(routeName, stopsForRoute);
    }
    if( stopToInsert.getRouteName().equalsIgnoreCase( getUnassignedRouteName() ) )
    {
      stopsForRoute.add(stopToInsert);
    }
    else
    {
      ensureSize( stopsForRoute, stopToInsert.getSequenceNumber() + 2 );
      int insertIndex = stopToInsert.getSequenceNumber();
      if( stopsForRoute.get( insertIndex ) == null )
      {
        stopsForRoute.set( insertIndex, stopToInsert );
      }
      else
      {
        stopsForRoute.add( insertIndex, stopToInsert );
      }
    }
    stopsByStopName.put( stopToInsert.getName(), stopToInsert );
    stopToRouteNameMap.put( stopToInsert.getName(), stopToInsert.getRouteName() );
    return stopToInsert;
  }
  
  private void ensureSize( ArrayList<StopResource> list, Integer minSize )
  {
    while( list.size() < minSize )
    {
      list.add( null );
    }
  }

  private void removeStopFromRoute( StopResource stopToInsert )
  {
    String oldRouteName = stopToRouteNameMap.get( stopToInsert.getName() );
    ArrayList<StopResource> oldRouteStops = stopsByRouteName.get( oldRouteName );
    int oldIndex = -1;
    Stop currStop;
    for( int i=0; i < oldRouteStops.size(); i++ )
    {
      currStop = oldRouteStops.get( i );
      if( currStop != null)
      {
        if( currStop.getName().equals( stopToInsert.getName() ) )
        {
          oldIndex = i;
          break;
        }
      }
    }
    if( oldIndex != -1 )
    {
      List<StopResource> stopsToPreserve = oldRouteStops.subList( oldIndex+1, oldRouteStops.size() );
      int newSequenceNumber = oldIndex;
      for( StopResource stopToPreserve : stopsToPreserve )
      {
        if(stopToPreserve != null)
        {
          stopToPreserve.setSequenceNumber( newSequenceNumber );
          newSequenceNumber++;
        }
      }
      oldRouteStops.remove( oldIndex );
    }
    else
    {
      // It is possible that it was in a virtual route (unassigned)
      log.error( "Didn't find stop "+stopToInsert.getName()+" in route "+oldRouteName );
    }
  }

  @Override
  public Stop getStopByName(String name)
  {
    return stopsByStopName.get( name );
  }


  @Override
  public void removeAllStops()
  {
    for(StopResource sr : stopsByStopName.values())
    {
      resourceManager.deleteResource(sr.getResource().getId());
    }
    stopsByRouteName.clear();
    stopsByStopName.clear();
    stopToRouteNameMap.clear();
    Map<String, Aoi> stopAois = aoiManager.searchAois( getStopsAoiCategory(), ".*" );
    for( Aoi aoi : stopAois.values() )
    {
      aoiManager.deleteAoi( aoi.getCategory(), aoi.getName() );
    }
  }
  
  @Override
  public GeoEvent createGeoEvent(List<Stop> stops, String ownerId, Uri uri)
  {
    GeoEventDefinition ged;
    GeoEvent geoEvent = null;
    try
    {
      ged = getGeoEventDefinition();
      geoEvent = geoEventCreator.create(ged.getName(), stopGEDOwner);
      List<FieldGroup> stopFieldGroups = new ArrayList<FieldGroup>();
      for (Stop stop : stops)
      {
        if(stop != null)
        {
          FieldGroup fieldGroup = geoEvent.createFieldGroup("Stops");
          GeoEvent stopGE = createGeoEvent(stop, stopGEDOwner, null);
          for(int i = 0; i<stopGE.getAllFields().length; i++)
          {
            fieldGroup.setField(i, stopGE.getField(i));
          }
          stopFieldGroups.add(fieldGroup);
        }
      }
      geoEvent.setField("Stops", stopFieldGroups);
    }
    catch (ConfigurationException e)
    {
      log.error(e);
      geoEvent = null;
    }
    catch (GeoEventDefinitionManagerException e)
    {
      log.error(e);
      geoEvent = null;
    }
    catch (MessagingException e)
    {
      log.error(e);
      geoEvent = null;
    }
    catch (FieldException e)
    {
      log.error(e);
      geoEvent = null;
    }
    return geoEvent;
  }

  @Override
  public GeoEvent createGeoEvent(Stop stop, String ownerId, Uri uri)
  {
    try
    {
      GeoEvent geoEvent = geoEventCreator.create(stopGEDName, stopGEDOwner);
      
      Set<String> predefinedTags = stop.getPredefinedKeys();

      for (FieldDefinition fd : geoEvent.getGeoEventDefinition().getFieldDefinitions())
      {
        String name = fd.getName();
        String value = stop.getAttribute(getResourceName(fd, predefinedTags));

        if (value == null)
        {
          geoEvent.setField(name, null);
        }
        else
        {
          switch (fd.getType())
          {
            case Boolean:
              geoEvent.setField(name,  value.equals("true")?true:false );
              break;
            case Date:  
              geoEvent.setField(name,  new Date( Long.parseLong( value ) ) );
              break;
            case Double:
              Double doubleValue = null;
              if( !Validator.isEmpty( value ) )
              {
                doubleValue = Double.parseDouble( value );
              }
              geoEvent.setField(name, doubleValue);
              break;
            case Float:
              Float floatValue = null;
              if( !Validator.isEmpty( value ) )
              {
                floatValue = Float.parseFloat( value );
              }
              geoEvent.setField(name, floatValue);
              break;
            case Integer:
              Integer intValue = null;
              if( !Validator.isEmpty( value ) )
              {
                intValue = Integer.parseInt( value );
              }
              geoEvent.setField(name, intValue);
              break;
            case Long:
              Long longValue = null;
              if( !Validator.isEmpty( value ) )
              {
                longValue = Long.parseLong( value );
              }
              geoEvent.setField(name, longValue);
              break;
            case Geometry:
              geoEvent.setGeometry(stop.getLocation());
              break;
            default:
              geoEvent.setField(name, value);
              break;
          }
        }
      }
      geoEvent.setProperty(GeoEventPropertyName.TYPE, "event");
      geoEvent.setProperty(GeoEventPropertyName.OWNER_ID, ownerId);
      geoEvent.setProperty(GeoEventPropertyName.OWNER_URI, uri);
      return geoEvent;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }

  }
  
  private GeoEventDefinition getGeoEventDefinition() throws ConfigurationException, GeoEventDefinitionManagerException
  {
    GeoEventDefinition geoEventDefinition = geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition("route-stops", stopGEDOwner);
    if(geoEventDefinition != null)
      return geoEventDefinition;
    
    geoEventDefinition = new DefaultGeoEventDefinition();
    geoEventDefinition.setName("route-stops");
    geoEventDefinition.setOwner(stopGEDOwner);
    
    List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
    
    FieldDefinition stopFD = new DefaultFieldDefinition("Stops", FieldType.Group);
    List<FieldDefinition> stopFieldDefinitions = getStopsGeoEventDefinition().getFieldDefinitions();
    for( FieldDefinition child : stopFieldDefinitions )
      stopFD.addChild(child);
    stopFD.setCardinality(FieldCardinality.Many);
    fieldDefinitions.add(stopFD);

    geoEventDefinition.setFieldDefinitions(fieldDefinitions);
    geoEventCreator.getGeoEventDefinitionManager().addGeoEventDefinition(geoEventDefinition);
    return geoEventDefinition;
  }
  
  @Override
  public GeoEvent createListGeoEvent(List<Stop> stops, String requestId, String ownerId, Uri uri)
  {
    GeoEventDefinition ged;
    GeoEvent geoEvent = null;
    try
    {
      ged = getGeoEventDefinition2();
      geoEvent = geoEventCreator.create(ged.getName(), stopGEDOwner);
      List<FieldGroup> stopFieldGroups = new ArrayList<FieldGroup>();
      for (Stop stop : stops)
      {
        if(stop != null)
        {
          FieldGroup fieldGroup = geoEvent.createFieldGroup(stopGEDName);
          GeoEvent stopGE = createGeoEvent(stop, stopGEDOwner, null);
          for(int i = 0; i<stopGE.getAllFields().length; i++)
          {
            fieldGroup.setField(i, stopGE.getField(i));
          }
          stopFieldGroups.add(fieldGroup);
        }
      }
      geoEvent.setField(stopGEDName, stopFieldGroups);
      geoEvent.setField("RequestId", requestId);
      geoEvent.setProperty(GeoEventPropertyName.TYPE, "event");
      geoEvent.setProperty(GeoEventPropertyName.OWNER_ID, ownerId);
      geoEvent.setProperty(GeoEventPropertyName.OWNER_URI, uri);
    }
    catch (ConfigurationException e)
    {
      log.error(e);
      geoEvent = null;
    }
    catch (GeoEventDefinitionManagerException e)
    {
      log.error(e);
      geoEvent = null;
    }
    catch (MessagingException e)
    {
      log.error(e);
      geoEvent = null;
    }
    catch (FieldException e)
    {
      log.error(e);
      geoEvent = null;
    }
    return geoEvent;
  }
  
  private GeoEventDefinition getGeoEventDefinition2() throws ConfigurationException, GeoEventDefinitionManagerException
  {
    GeoEventDefinition geoEventDefinition = geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(stopsListGEDName, stopGEDOwner);
    if(geoEventDefinition != null)
      return geoEventDefinition;
    
    geoEventDefinition = new DefaultGeoEventDefinition();
    geoEventDefinition.setName(stopsListGEDName);
    geoEventDefinition.setOwner(stopGEDOwner);
    
    List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
    
    String stopGEDName = getStopsGeoEventDefinition().getName();
    FieldDefinition stopFD = new DefaultFieldDefinition(stopGEDName, FieldType.Group);
    List<FieldDefinition> stopFieldDefinitions = getStopsGeoEventDefinition().getFieldDefinitions();
    for( FieldDefinition child : stopFieldDefinitions )
      stopFD.addChild(child);
    stopFD.setCardinality(FieldCardinality.Many);
    fieldDefinitions.add(stopFD);

    FieldDefinition fd1 = new DefaultFieldDefinition("RequestId", FieldType.String);
    fieldDefinitions.add(fd1);

    geoEventDefinition.setFieldDefinitions(fieldDefinitions);
    geoEventCreator.getGeoEventDefinitionManager().addGeoEventDefinition(geoEventDefinition);
    return geoEventDefinition;
  }

  public GeoEvent createGeoEvent2(Stop stop, String ownerId, Uri uri)
  {
    try
    {
      GeoEvent geoEvent = geoEventCreator.create(stopGEDName, stopGEDOwner);
      geoEvent.setGeometry( stop.getLocation() );
      geoEvent.setField(StopResource.LAST_UPDATED_KEY, stop.getLastUpdated());
      geoEvent.setField(StopResource.SHAPE_KEY, stop.getLocation().toJson());
      geoEvent.setField(StopResource.STOP_NAME_KEY, stop.getName());
      geoEvent.setField(StopResource.ROUTE_NAME_KEY, stop.getRouteName());
      geoEvent.setField(StopResource.SEQUENCE_NUMBER_KEY, stop.getSequenceNumber());
      geoEvent.setField(StopResource.STATUS_KEY, stop.getStatus().toString() );
      geoEvent.setField(StopResource.TYPE_KEY, stop.getType());
      geoEvent.setField(StopResource.DESCRIPTION_KEY, stop.getDescription());
      geoEvent.setField(StopResource.PICKUP_CAPACITY_KEY, stop.getPickupCapacity());
      geoEvent.setField(StopResource.DELIVERY_CAPACITY_KEY, stop.getDeliveryCapacity());
      geoEvent.setField(StopResource.ADDRESS_KEY, stop.getAddress());
      geoEvent.setField(StopResource.CURB_APPROACH_KEY, stop.getCurbApproach());
      geoEvent.setField(StopResource.CUSTOM_STOP_PROPERTIES_KEY, stop.getCustomProperties());
      geoEvent.setField(StopResource.TIME_WINDOW_START_1_KEY, stop.getTimeWindowStart1());
      geoEvent.setField(StopResource.TIME_WINDOW_END_1_KEY, stop.getTimeWindowEnd1());
      geoEvent.setField(StopResource.TIME_WINDOW_START_2_KEY, stop.getTimeWindowStart2());
      geoEvent.setField(StopResource.TIME_WINDOW_END_2_KEY, stop.getTimeWindowEnd2());
      geoEvent.setField(StopResource.MAX_VIOLATION_KEY, stop.getMaxViolationTime());
      geoEvent.setField(StopResource.SCHEDULED_ARRIVAL_KEY, stop.getScheduledArrival());
      geoEvent.setField(StopResource.PROJECTED_ARRIVAL_KEY, stop.getProjectedArrival());
      geoEvent.setField(StopResource.ACTUAL_ARRIVAL_KEY, stop.getActualArrival());
      geoEvent.setField(StopResource.SCHEDULED_SERVICE_DURATION_KEY, stop.getScheduledServiceDuration());
      geoEvent.setField(StopResource.ACTUAL_SERVICE_DURATION_KEY, stop.getActualServiceDuration());
      geoEvent.setField(StopResource.SCHEDULED_DEPARTURE_KEY, stop.getScheduledDeparture());
      geoEvent.setField(StopResource.PROJECTED_DEPARTURE_KEY, stop.getProjectedDeparture());
      geoEvent.setField(StopResource.ACTUAL_DEPARTURE_KEY, stop.getActualDeparture());
      geoEvent.setField(StopResource.NOTE_KEY, stop.getNote());
      geoEvent.setProperty(GeoEventPropertyName.TYPE, "event");
      geoEvent.setProperty(GeoEventPropertyName.OWNER_ID, ownerId);
      geoEvent.setProperty(GeoEventPropertyName.OWNER_URI, uri);
      return geoEvent;
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
    
  }

  @Override
  public String getStopsAoiCategory()
  {
    return stopsManagerAIOCategory;
  }
  
  @Override
  public List<Stop> unassign( List<String> stopNames )
  {
    List<Stop> newStops = null;
    
    if( !Validator.isEmpty( stopNames ) )
    {
      Stop stop;
      DefaultStop newStop;
      newStops = new ArrayList<Stop>();
      for( String stopName : stopNames )
      {
        stop = stopsByStopName.get( stopName );
        if( stop != null )
        {
          newStop = new DefaultStop( stop );
          newStop.setRouteName( getUnassignedRouteName() );
          newStop.setStatus( StopStatus.Unassigned );
          newStop.setSequenceNumber(0);
          newStops.add( newStop );
        }
      }
    }
    return newStops;
  }

  @Override
  public String getUnassignedRouteName()
  {
    return unassignedRouteName;
  }

  @Override
  public String getCanceledRouteName()
  {
    return canceledRouteName;
  }
  
  @Override
  public Stop createStop(String stopName)
  {
    ResourceWrapper wrapper = new ResourceWrapper();
    wrapper.setFirstName( stopName );
    wrapper.setLastName( "Stop" );
    wrapper.setId(stopName);
    wrapper.setType("Stop");
    wrapper.setDescription( "Stop "+stopName );
    try
    {
      Resource resource = resourceManager.getResource(wrapper);
      if (resource == null)
      {
        resource = resourceManager.createNewResource(wrapper);
      }
      return new StopResource(resource, spatial);
    }
    catch (ResourceManagerException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void loadStopsConfigurations(String filePath)
  {

  }

  @Override
  public StopsConfiguration getStopsConfiguration(String stopType)
  {
    return null;
  }

  @Override
  public Map<String, String> getDefaultStopFields()
  {
    Map<String, String> cmap = new HashMap<String, String>();
    cmap.put("ActualArrival", "");
    cmap.put("ActualDeparture", "");
    cmap.put("ActualServiceDuration", "");
    cmap.put("Address", "");
    cmap.put("CurbApproach", "");
    cmap.put("CustomStopProperties", "");
    cmap.put("DeliveryCapacity", "");
    cmap.put("Description", "");
    cmap.put("LastUpdated", "");
    cmap.put("Location", "");
    cmap.put("MaxViolationTime", "");
    cmap.put("StopName", "");
    cmap.put("Note", "");
    cmap.put("PickupCapacity", "");
    cmap.put("ProjectedArrival", "");
    cmap.put("ProjectedDeparture", "");
    cmap.put("RouteName", "");
    cmap.put("ScheduledArrival", "");
    cmap.put("ScheduledDeparture", "");
    cmap.put("ScheduledServiceDuration", "");
    cmap.put("SequenceNumber", "");
    cmap.put("Status", "");
    cmap.put("TimeWindowEnd1", "");
    cmap.put("TimeWindowEnd2", "");
    cmap.put("TimeWindowStart1", "");
    cmap.put("TimeWindowStart2", "");
    cmap.put("Type", "");
    cmap.put("CumulativeMinutes", "");
    
    return cmap;
  }

  @Override
  public StopsConfigurations getStopsConfigurations()
  {
    return stopsConfigurations;
  }
  
  @Override
  public void clearAllStops(String agsConnectionName, String path, String featureService, String stopLayer, String geofenceLayer)
  {
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection(agsConnectionName);
    Layer stoplyr =  agsConnection.getLayer(path, featureService, stopLayer, ArcGISServerType.FeatureServer);
    agsConnection.deleteAllRecordsFromLayer(path , featureService, stoplyr.getId());
    removeAllStops();
    
    if(!Validator.isEmpty(geofenceLayer))
    {
      Layer geofencelyr = agsConnection.getLayer(path, featureService, geofenceLayer, ArcGISServerType.FeatureServer);
      agsConnection.deleteAllRecordsFromLayer(path , featureService, geofencelyr.getId());
    }
  }
  @Override
  public List<Stop> reloadStops(String agsConnectionName, String path, String featureService, String layer)
  {
    removeAllStops();
    
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection(agsConnectionName);
    Layer lyr =  agsConnection.getLayer(path, featureService, layer, ArcGISServerType.FeatureServer);
    //getAllFeatures(String folder, String serviceName, int layerIndex, String queryDefinition, String outFields, boolean includeGeometry, ArcGISServerType serverType, long lastOid)
    try
    {
      List<JsonNode> nodes = agsConnection.getAllFeatures(path, featureService, lyr.getId(), "1=1", "*", true, ArcGISServerType.FeatureServer, 0);
      List<Stop> stops = parseFeaturesToStops(nodes);
      for(Stop stop : stops)
      {
        addOrReplaceStop(stop);
      }
    }
    catch (IOException e)
    {
      log.error(e);
    }
    return getStops();
  }
  
  private List<Stop> parseFeaturesToStops(List<JsonNode> nodes)
  {
    GeoEvent geoEvent;
    List <Stop> stops = new ArrayList<Stop>();
    try
    {
      geoEvent = geoEventCreator.create(stopGEDName, stopGEDOwner);
      List<GeoEvent> geoEvents = FeatureUtil.convertFeaturesToGeoEvents(nodes, spatial, geoEventCreator, geoEvent.getGeoEventDefinition());
      for(GeoEvent ge : geoEvents)
      {
        Stop stop = createStop((String)ge.getField( StopResource.STOP_NAME_KEY ));
        convertGeoEventToStop(ge, stop);
        stops.add(stop);
      }
    }
    catch (MessagingException e)
    {
      log.error(e);
    }
    
    return stops;
  }
  
  public void convertGeoEventToStop( GeoEvent message, Stop stop )
  {
    Set<String> predefinedTags = stop.getPredefinedKeys();
    for (FieldDefinition fd :message.getGeoEventDefinition().getFieldDefinitions())
    {
      String name = fd.getName();
      Object value = message.getField(name);
      String resrouceName = getResourceName(fd, predefinedTags);
      
      if(value == null)
      {
        continue;
      }
      String valueToInsert;
      switch(fd.getType())
      {
        case Boolean:
        case Double:
        case Integer:
        case Long:
        case Short:
        case String:
        case Geometry:
          valueToInsert = value.toString();
          break;
        case Date:
          valueToInsert = Long.toString( ((Date)value).getTime() );
          break;
        default:
          valueToInsert = null;
          break; 
        
      }
      stop.setAttribute( resrouceName, valueToInsert );
    }
  }
  
  private String getResourceName(FieldDefinition fd, Set<String> predefinedTags)
  {
    List<String> tags = fd.getTags();
    if(tags != null)
    {
      for(String tag: tags)
      {
        if(predefinedTags.contains(tag))
          return tag;
      }
    }
    return fd.getName();
  }
  
  @Override
  public GeoEventDefinition getStopsGeoEventDefinition()
  {
    return geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(stopGEDName, stopGEDOwner);
  }

}
