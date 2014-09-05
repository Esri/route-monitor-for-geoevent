package com.esri.ges.manager.routes.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;

import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.Uri;
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
import com.esri.ges.core.http.GeoEventHttpClientService;
import com.esri.ges.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.datastore.agsconnection.ArcGISServerType;
import com.esri.ges.datastore.agsconnection.Layer;
import com.esri.ges.datastore.agsconnection.Location;
import com.esri.ges.datastore.agsconnection.NamedGeometry;
import com.esri.ges.datastore.agsconnection.SolvedRoute;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.manager.routemonitor.util.FeatureUtil;
import com.esri.ges.manager.routemonitor.util.PlanStatus;
import com.esri.ges.manager.routes.DefaultRoute;
import com.esri.ges.manager.routes.DefaultRouteWithStops;
import com.esri.ges.manager.routes.Plan;
import com.esri.ges.manager.routes.Route;
import com.esri.ges.manager.routes.RouteDispatchStatus;
import com.esri.ges.manager.routes.RouteManager;
import com.esri.ges.manager.routes.RouteResource;
import com.esri.ges.manager.routes.RouteStatus;
import com.esri.ges.manager.routes.RouteWithStops;
import com.esri.ges.manager.stops.DefaultStop;
import com.esri.ges.manager.stops.NonServiceStopType;
import com.esri.ges.manager.stops.Stop;
import com.esri.ges.manager.stops.StopResource;
import com.esri.ges.manager.stops.StopStatus;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.manager.vehicles.Vehicle;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.Spatial;
import com.esri.ges.util.DateUtil;
import com.esri.ges.util.Validator;


public class RouteManagerImpl implements RouteManager
{
  final private static Log log = LogFactory.getLog( RouteManagerImpl.class );
  final private static String currentLocationName = "CurrentLocation";
  final private static String routeEndPointLocationName = "RouteEndPointLocation";
  private Map<String, Route> routes = new ConcurrentHashMap<String,Route>();
  private StopsManager stopsManager;
  private ArcGISServerConnectionManager agsConnectionManager;
  private GeoEventCreator geoEventCreator;
  private VehiclesManager vehiclesManager;
  private Spatial spatial;
  private String routeGEDName;
  private String routeGEDOwner;
  private String planGEDName;
  private String planGEDOwner;
  private String routeUpdateGEDName;
  private String routeDispatchGEDName;
  private GeoEventHttpClientService httpClientService;
  
  public void setHttpClientService( GeoEventHttpClientService service )
  {
    this.httpClientService = service;
  }
  
  public void setRouteDispatchGEDName(String routeDispatchGEDName)
  {
    this.routeDispatchGEDName = routeDispatchGEDName;
  }

  public void setRouteUpdateGEDName(String routeUpdateGEDName)
  {
    this.routeUpdateGEDName = routeUpdateGEDName;
  }

  public void setRouteGEDName(String routeGEDName)
  {
    this.routeGEDName = routeGEDName;
  }

  public void setRouteGEDOwner(String routeGEDOwner)
  {
    this.routeGEDOwner = routeGEDOwner;
  }

  public void setSpatial(Spatial spatial)
  {
    this.spatial = spatial;
  }

  public void setVehiclesManager( VehiclesManager manager )
  {
    this.vehiclesManager = manager;
  }
  
  public void setPlanGEDName(String planGEDName)
  {
    this.planGEDName = planGEDName;
  }

  public void setPlanGEDOwner(String planGEDOwner)
  {
    this.planGEDOwner = planGEDOwner;
  }
  
  public void setArcGISServerConnectionManager( ArcGISServerConnectionManager manager )
  {
    this.agsConnectionManager = manager;
  }
  
  public void setStopsManager( StopsManager stopsManager )
  {
    this.stopsManager = stopsManager;
  }
  
  public void setMessaging( Messaging messaging )
  {
    geoEventCreator = messaging.createGeoEventCreator();
  }

  @Override
  public Collection<Route> getRoutes()
  {
    return routes.values();
  }

  @Override
  public Route getRouteByName(String name)
  {
    return routes.get( name );
  }
  
  private void addToLocalMap( Route route )
  {
    routes.put( route.getRouteName(), route );
  }
  
  @Override
  public void addOrReplaceRoute(Route route)
  {
    addToLocalMap( route );
  }

  @Override
  public void removeRoute(String name)
  {
    if( routes.containsKey( name ) )
    {
      routes.remove( name );
    }
  }

  @Override
  public void removeAllRoutes()
  {
    routes.clear();
  }
  
  private Map<String,Object> createAttributesForLocation( Integer sequence, String stopName, String routeName )
  {
    Map<String,Object> attributes = new HashMap<String,Object>();
    attributes.put( "Sequence", sequence );
    attributes.put( "Name", stopName );
    attributes.put( "RouteName", routeName );
    return attributes;
  }

  @Override
  public Plan solveRoute(List<RouteWithStops> routesWithStops, String naConnection, String routeSolverPath)
  {
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection( naConnection );
    if( agsConnection == null )
    {
      throw new RuntimeException( "Could not find ArcGIS Server Connection "+naConnection );
    }
    
    NetworkAnalystServerConnection networkAnalystServerConnection = new NetworkAnalystServerConnection(spatial, agsConnection.getUrl(), httpClientService.createNewClient());
    Plan plan = new Plan();
    plan.setRoutes( new ArrayList<Route>() );
    plan.setStops( new ArrayList<Stop>() );
    if( Validator.isEmpty( routesWithStops ) )
    {
      return plan;
    }
    
    SolvedRoute solvedRoute;
    Date startTime;
    for( RouteWithStops routeWithStops : routesWithStops )
    {
      ArrayList<Location> locations = new ArrayList<Location>();
      ArrayList<Location> notServicedLocations = new ArrayList<Location>();
      
      startTime = routeWithStops.getCurrentTimeStamp();
      List<Stop> servicedStops = new ArrayList<Stop>();
      for( Stop stop : routeWithStops.getStops() )
      {
        if( !stop.isServiced() )
        {
          notServicedLocations.add( convertStopToLocation( stop ) );
        }
        else
        {
          log.info( "Stop "+stop.getName()+" has already been serviced.  Will not include it in updated route." );
          DefaultStop stopCopy = new DefaultStop( stop );     
          stopCopy.setSequenceNumber( servicedStops.size());
          servicedStops.add( stopCopy );
        }
      }
      
      // put two lists together
      if(routeWithStops.getStops().get(0).isServiced() || (!routeWithStops.getStops().get(0).isServiced() && !routeWithStops.getStops().get(0).getType().equals(NonServiceStopType.Base)))
      {
        //If vehicle started moving, current location may be needed.  If vehicle has not completed the start/base, current location is not needed.
        addCurrentLocation(notServicedLocations, routeWithStops, servicedStops);
      }
      locations.addAll(notServicedLocations);
      
      updateSequenceNumbers( locations );
      startTime = constructStartTime(routeWithStops, servicedStops);
      solvedRoute = networkAnalystServerConnection.solveRoute(routeSolverPath, locations, routeWithStops.isOptimize(), startTime );
      addSolvedRouteToPlan( plan, solvedRoute, servicedStops.size(), startTime );
      //Add previously serviced Stops
      plan.getStops().addAll( servicedStops );
    }
    
    return plan;
  }
  
  private void addCurrentLocation(ArrayList<Location> locations, RouteWithStops routeWithStops, List<Stop> servicedStops)
  {
    if(routeWithStops.getCurrentLocation() != null)
    {
      locations.add( createLocationForPoint( routeWithStops.getCurrentLocation(), currentLocationName, routeWithStops.getRouteName(), 1 ) );
    }
    else
    {
      if (servicedStops.size()==0)
      {
        // Use 1st stop location
        locations.add( createLocationForPoint( routeWithStops.getStops().get(0).getLocation(), currentLocationName, routeWithStops.getRouteName(), 1 ) );
      }
      else
      {
        // Use last serviced stop location
        // Enhancement opportunity:  Sort stops by actual departure
        locations.add( createLocationForPoint( servicedStops.get(servicedStops.size()-1).getLocation(), currentLocationName, routeWithStops.getRouteName(), 1 ) );
      }
    }
  }
  
  private Date constructStartTime(RouteWithStops routeWithStops, List<Stop> servicedStops)
  {
    if(routeWithStops.getCurrentTimeStamp() != null)
      return routeWithStops.getCurrentTimeStamp();
    else
    {
      if (servicedStops.size()==0)
      {
        return routeWithStops.getStops().get(0).getProjectedArrival();
      }
      else
      {
        return servicedStops.get(servicedStops.size()-1).getActualDeparture();
      }
    }
  }

  private StopStatus getNewStopStatus(String routeName)
  {
    Route route = getRouteByName(routeName);
    if(route != null)
    {
      if(route.getRouteName().equals(stopsManager.getUnassignedRouteName()))
      {
        return StopStatus.Unassigned;
      }
      else
      {
        if(route.getDispatchAck() != null && (route.getDispatchAck().equals(RouteStatus.Acknowledged.toString()) || route.getDispatchAck().equals(RouteStatus.Dispatched.toString())))
        {
          return StopStatus.Dispatched;
        }
        else
        {
          return StopStatus.Assigned;
        }
      }
    }
    return null;
  }
 
  private void updateSequenceNumbers(ArrayList<Location> locations )
  {
    Integer sequence = 1;
    for( Location location : locations )
    {
      location.getAttributes().put( "Sequence", sequence );
      sequence++;
    }
  }

  private Location createLocationForPoint(Point currentLocation, String locationName, String routeName, Integer sequence )
  {
    Location location = new Location();
    location.setPoint( currentLocation );
    location.setAttributes( createAttributesForLocation( sequence, locationName, routeName) );
    return location;
  }
  
  private void addSolvedRouteToPlan( Plan plan, SolvedRoute solvedRoute, int startingSequenceNumber, Date startTime )
  {
    List<Location> locations = solvedRoute.getLocations();
    boolean firstInRoutesLeft = true;
    if( locations != null && locations.size() > 0 )
    {
      List<Stop> stopsFromPlan = plan.getStops();
      Stop newStop;
      int prevTotalServiceTime = 0;
      for( Location location : locations )
      {
        newStop = mergeWithStopInfo( location, startingSequenceNumber, firstInRoutesLeft, startTime, prevTotalServiceTime );
        if( newStop != null )
        {
          stopsFromPlan.add( newStop );
          startingSequenceNumber++;
          firstInRoutesLeft = false;
          prevTotalServiceTime = prevTotalServiceTime + newStop.getScheduledServiceDuration();
        }
      }
    }
    List<NamedGeometry> routes = solvedRoute.getRoutes();
    if( routes != null && routes.size() > 0 )
    {
      List<Route> routesFromPlan = plan.getRoutes();
      Route newRoute;
      for( NamedGeometry namedGeometry : routes )
      {
        newRoute = mergeWithRouteInfo( namedGeometry );
        if( newRoute != null )
        {
          routesFromPlan.add( newRoute );
        }
      }
    }
  }

  private Route mergeWithRouteInfo(NamedGeometry namedGeometry)
  {
    Route route = getRouteByName( namedGeometry.getName() );
    if( route == null )
    {
      throw new RuntimeException( "Could not find route "+namedGeometry.getName() );
    }
    DefaultRoute routeCopy = new DefaultRoute( route );
    routeCopy.setShape( namedGeometry.getGeometry() );
    return routeCopy;
  }

  private Stop mergeWithStopInfo(Location location, int sequenceNumber, boolean firstInUpdateSequence, Date startTime, int prevTotalServiceTime)
  {
    Map<String, Object> attributes = location.getAttributes();
    String stopName = (String) attributes.get("Name");
    if (stopName.equals(currentLocationName) || stopName.equals(routeEndPointLocationName))
    {
      return null;
    }

    Stop stop = stopsManager.getStopByName(stopName);
    if (stop == null)
    {
      throw new RuntimeException("Couldn't find stop " + stopName);
    }
    DefaultStop stopCopy = new DefaultStop(stop);
    stopCopy.setRouteName((String) attributes.get("RouteName"));
    stopCopy.setSequenceNumber(sequenceNumber);
    stopCopy.setScheduledDeparture(null);
    stopCopy.setStatus(getNewStopStatus(stopCopy.getRouteName()));
    Number cumulativeTime = (Number) attributes.get("Cumul_Time");
    Date peta = DateUtil.addMins(startTime, (cumulativeTime.intValue() + prevTotalServiceTime));
    stopCopy.setProjectedArrival(peta);
    Integer scheduledDuration = stopCopy.getScheduledServiceDuration();
    if (scheduledDuration != null)
    {
      stopCopy.setProjectedDeparture(DateUtil.addMins(peta, scheduledDuration.intValue()));
    }
    return stopCopy;
  }

  private Location convertStopToLocation(Stop stop)
  {
    return createLocationForPoint( stop.getLocation(), stop.getName(), stop.getRouteName(), stop.getSequenceNumber() );    
  }

  @Override
  public Plan solveRouteAndCommit(List<RouteWithStops> routesWithStops, String naConnection, String routeSolverPath)
  {
    Plan newPlan = solveRoute( routesWithStops, naConnection, routeSolverPath );
    
    for( Route route : newPlan.getRoutes() )
    {
      addToLocalMap( route );
    }
    stopsManager.batchAddOrReplaceStops( newPlan.getStops() );

    return newPlan;
  }

  @Override
  public void clearAllRouteFeatures(String agsConnectionName, String path, String featureService, String layer)
  {
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection(agsConnectionName);
    Layer lyr =  agsConnection.getLayer(path, featureService, layer, ArcGISServerType.FeatureServer);
    agsConnection.deleteAllRecordsFromLayer(path , featureService, lyr.getId());
    removeAllRoutes();
  }

  @Override
  public List<Route> reloadRoutes(String agsConnectionName, String path, String featureService, String layer)
  {
    removeAllRoutes();
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection(agsConnectionName);
    Layer lyr =  agsConnection.getLayer(path, featureService, layer, ArcGISServerType.FeatureServer);
    try
    {
      List<JsonNode> nodes = agsConnection.getAllFeatures(path, featureService, lyr.getId(), "1=1", "*", true, ArcGISServerType.FeatureServer, 0);
      List<Route> routes = parseFeaturesToRoutes(nodes);
      for(Route route:routes)
      {
        addOrReplaceRoute(route);
      }
    }
    catch (IOException e)
    {
      log.error(e);
    }
    return new ArrayList<Route>(getRoutes());
  }
  
  private List<Route> parseFeaturesToRoutes(List<JsonNode> nodes)
  {
    GeoEvent geoEvent;
    List <Route> routes = new ArrayList<Route>();
    try
    {
      geoEvent = geoEventCreator.create(routeGEDName, routeGEDOwner);
      List<GeoEvent> geoEvents = FeatureUtil.convertFeaturesToGeoEvents(nodes, spatial, geoEventCreator, geoEvent.getGeoEventDefinition());
      for(GeoEvent ge : geoEvents)
      {
        Route route = convertGeoEventToRoute( ge );
        routes.add(route);
      }
    }
    catch (MessagingException e)
    {
      log.error(e);
    }
    
    return routes;
  }
  
  public Route convertGeoEventToRoute( GeoEvent geoEvent )
  {
    DefaultRoute route = new DefaultRoute();
    route.setDriverName( (String)geoEvent.getField( RouteResource.DRIVER_NAME_KEY ) );
    route.setLastUpdated( (Date)geoEvent.getField( RouteResource.LAST_UPDATED_KEY ) ) ;
    route.setPassengerName( (String)geoEvent.getField( RouteResource.PASSENGER_NAME_KEY ) );
    route.setRouteEnd( (Date)geoEvent.getField( RouteResource.ROUTE_END_KEY ) );
    route.setRouteName( (String)geoEvent.getField( RouteResource.ROUTE_NAME_KEY ) );
    route.setRouteStart( (Date)geoEvent.getField( RouteResource.ROUTE_START_KEY ) );
    route.setShape( geometryFromAttribute( geoEvent.getField( RouteResource.SHAPE_KEY ) ) );
    route.setVehicleName( (String)geoEvent.getField( RouteResource.VEHICLE_NAME_KEY ) );
    route.setDispatchAck( RouteStatus.AtBase.toString() );
    route.setRouteStartPoint( getPointFromField( geoEvent, RouteResource.ROUTE_START_POINT_KEY ) );
    route.setRouteEndPoint( getPointFromField( geoEvent, RouteResource.ROUTE_END_POINT_KEY ) );
    
    return route;
  }
  
  private Point getPointFromField( GeoEvent geoEvent, String fieldName )
  {
    Object routePointObject = geoEvent.getField( fieldName );
    if( routePointObject != null )
    {
      Geometry routeEndPoint = geometryFromAttribute( routePointObject );
      if( routeEndPoint != null && routeEndPoint instanceof Point )
      {
        return (Point)routeEndPoint;
      }
    }
    return null;
  }
  
  protected Geometry geometryFromAttribute( Object locationAttribute )
  {
    Geometry retGeom = null;
    if( locationAttribute instanceof String )
    {
      try
      {
        retGeom = spatial.fromJson( (String)locationAttribute );
      }
      catch (GeometryException e)
      {
        log.info("Failed to convert string to Point.", e);
      }
    }
    else if( locationAttribute instanceof Geometry )
    {
      retGeom = (Geometry)locationAttribute;
    }
    return retGeom;
  }

  @Override
  public GeoEventDefinition getRoutesGeoEventDefinition()
  {
    return geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(routeGEDName, routeGEDOwner);
  }
  
  @Override
  public GeoEventDefinition getRouteUpdateGeoEventDefinition()
  {
    return geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(routeUpdateGEDName, routeGEDOwner);
  }
  
  @Override
  public GeoEventDefinition getRouteDispatchGeoEventDefinition()
  {
    return geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(routeDispatchGEDName, routeGEDOwner);
  }

  @Override
  public GeoEvent createGeoEvent(Route route, String ownerId, Uri uri)
  {
    try
    {
      GeoEvent geoEvent = geoEventCreator.create( routeGEDName, routeGEDOwner );
      geoEvent.setGeometry( route.getShape() );
      geoEvent.setField( RouteResource.ROUTE_NAME_KEY, route.getRouteName() );
      geoEvent.setField( RouteResource.VEHICLE_NAME_KEY, route.getVehicleName() );
      geoEvent.setField( RouteResource.DRIVER_NAME_KEY, route.getDriverName() );
      geoEvent.setField( RouteResource.PASSENGER_NAME_KEY, route.getPassengerName() );
      geoEvent.setField( RouteResource.LAST_UPDATED_KEY, route.getLastUpdated() );
      geoEvent.setField( RouteResource.ROUTE_START_KEY, route.getRouteStart() );
      geoEvent.setField( RouteResource.ROUTE_END_KEY, route.getRouteEnd() );
      geoEvent.setField( RouteResource.DISPATCH_ACK_KEY, route.getDispatchAck() );
      geoEvent.setField( RouteResource.SHAPE_KEY, route.getShape() );
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
  
  private boolean isStopTypeService(String type)
  {
    for (NonServiceStopType stopType : NonServiceStopType.values()) {
      if(type.equals(stopType.toString()))
      {
        return false;
      }
    }
    return true;
  }
  
  private Plan calculate( Collection<CalculateParamsWrapper> params, String naConnection, String routeSolverPath, boolean commitResults )
  {
    List<String> errorMessages = new ArrayList<String>();
    List<RouteWithStops> routesWithStops = new ArrayList<RouteWithStops>();
    Stop stop;
    Route route;
    Plan plan = null;
    if( !Validator.isEmpty( params ) )
    {
      String routeName;
      List<String> stopNames;
      int sequenceNumber;
      Vehicle vehicle;
      List<Stop> stopsForRoute = null;
      Point location;
      Date locationTimestamp;
      for( CalculateParamsWrapper param : params )
      {
        routeName = param.getRouteName();
        if(routeName.equals(stopsManager.getUnassignedRouteName()))
        {
          // make sure we are not unassigning bases, lunch and breaks
          for(String stopName : param.getStops())
          {
            if(!isStopTypeService(stopsManager.getStopByName(stopName).getType()))
              errorMessages.add("Unassigning non-service stops is not allowed.");
          }
          continue;
        }
        route = getRouteByName( routeName );
        if( route == null )
        {
            errorMessages.add("Could not find route "+routeName );
            continue;
        }
        
        vehicle = vehiclesManager.getVehicleByName( route.getVehicleName() );
        if( vehicle == null  )
        {
          errorMessages.add( "Could not find vehicle with name "+route.getVehicleName() );
          continue;
        }
        location = vehicle.getLocation();
        locationTimestamp = vehicle.getLastUpdated();
        if( location == null )
        {
          errorMessages.add( "No location for vehicle "+route.getVehicleName()+".  Don't know where to start route from." );
          continue;
        }
        stopNames = param.getStops();
        if( !Validator.isEmpty( stopNames )  )
        {
          sequenceNumber = 1;
          stopsForRoute = new ArrayList<Stop>( stopNames.size() );
          List<Stop> newStops = new ArrayList<Stop>();
          List<Stop> existingStops = new ArrayList<Stop>();
          DefaultStop stopCopy;
          DefaultStop lastStop = null;
          for( String stopName : stopNames )
          {
            stop = stopsManager.getStopByName( stopName );
            if( stop == null )
            {
              errorMessages.add( "Could not find Stop "+stopName );
              continue;
            }
            
            if( param.isOptimize() && (stop.getType().equals(NonServiceStopType.Break.toString()) || stop.getType().equals(NonServiceStopType.Lunch.toString())) )
            {
              errorMessages.add( "Breaks are not supported when a route needs to be optimized." );
              continue;
            }
            
            if( lastStop != null )
            {
              if( stop.getType().equals(NonServiceStopType.Break.toString()) && lastStop.getType().equals(NonServiceStopType.Break.toString()))
              {
                errorMessages.add( "Two breaks cannot be next to each other." );
                continue;
              }
            }
            // build a new list of stops
            if(param.isOptimize())
            {
              if(!stop.getRouteName().equals(param.getRouteName()))
                newStops.add(stop);
              else
                existingStops.add(stop);
            }
            else
            {
              if( errorMessages.size() == 0 )
              {
                stopCopy = new DefaultStop( stop );
                stopCopy.setSequenceNumber( sequenceNumber );
                sequenceNumber++;
                stopCopy.setRouteName( route.getRouteName() );
                if((stopCopy.getType().equals(NonServiceStopType.Break.toString()) || stopCopy.getType().equals(NonServiceStopType.Lunch.toString())) && lastStop != null)
                {
                  stopCopy.setLocation(lastStop.getLocation());
                  stop.setAttribute(StopResource.SHAPE_KEY, lastStop.getLocation().toJson());
                  stop.setAttribute(StopResource.ADDRESS_KEY, lastStop.getAddress());
                }
                if(stopCopy.getLocation() != null)
                {
                  stopsForRoute.add( stopCopy );
                  lastStop = stopCopy;
                }
              }
            }
          }
          if(param.isOptimize())
          {
            sequenceNumber = 1;
            for(Stop existingStop:existingStops)
            {
              if( !(existingStop.getSequenceNumber()==0 && existingStop.getType().equals(NonServiceStopType.Base.toString())) && (existingStop.getStatus()==StopStatus.Assigned || existingStop.getStatus() == StopStatus.Dispatched) && newStops.size()>0 )
              {
                for(Stop newStop:newStops)
                {
                  DefaultStop newStopCopy = new DefaultStop( newStop );
                  newStopCopy.setSequenceNumber(sequenceNumber);
                  sequenceNumber ++;
                  newStopCopy.setRouteName(route.getRouteName());
                  stopsForRoute.add( newStopCopy );
                }
                newStops.clear();
              }

              DefaultStop existingStopCopy = new DefaultStop( existingStop );
              existingStopCopy.setSequenceNumber(sequenceNumber);
              sequenceNumber ++;
              existingStopCopy.setRouteName(route.getRouteName());
              stopsForRoute.add( existingStopCopy );
            }
          }

          if(newStops.size()>0)
          {
            errorMessages.add( "Unable to insert stop(s) to route " + route.getRouteName() );
          }
          routesWithStops.add( new DefaultRouteWithStops( route,stopsForRoute, location, locationTimestamp, param.isOptimize() ) );

        }
        
        // Make sure bases are not moved.
        if(stopsForRoute != null && !param.isOptimize())
          for(int i = 0; i<stopsForRoute.size(); i++)
          {
            if(stopsForRoute.get(i) != null)
              if(stopsForRoute.get(i).getType().equals(NonServiceStopType.Base.toString()) && i>0 && i<stopsForRoute.size()-1)
                errorMessages.add( "Base stops cannot be moved away from start or end." );
          }
      }
    }
    
    if( errorMessages.size() > 0) 
    {
      StringBuffer sb = new StringBuffer();
      for( String message : errorMessages )
      {
        if( sb.length() > 0 )
        {
          sb.append( ';' );
        }
        sb.append( message );
      }
      throw new RuntimeException( sb.toString() );
    }
    if (commitResults)
      plan = solveRouteAndCommit( routesWithStops, naConnection, routeSolverPath );
    else
      plan = solveRoute( routesWithStops, naConnection, routeSolverPath ) ;
    
    // Unassign based on "__Unassigned__" routes in the input GeoEvent.
    unassign(params, plan, commitResults);
    
    return plan;
  }

  @Override
  public GeoEvent resequence(GeoEvent geoEvent, String naConnection, String routeSolverPath)
  {
    Collection<CalculateParamsWrapper> routes;
    Plan plan = null;
    boolean commit = false;
    try
    {
      routes = convertGeoEventToCalculateParameters(geoEvent);
      //commit = (Boolean)geoEvent.getField("commit");
      
      // TODO: workaround.  json adapter writes Boolean as string.  As a result, the input which uses the same GED could not read Boolean fields.
      // If commit is null, set it to true.
      commit = true;
      if(geoEvent.getField("commit") != null)
        commit = (Boolean)geoEvent.getField("commit");
      
      plan = calculate(routes, naConnection, routeSolverPath, commit);
    }
    catch (Exception e)
    {
      log.error(e);
      return createPlanGeoEvent(null, false, PlanStatus.Failed, e.getMessage());
    }
    
    return createPlanGeoEvent(plan, commit, PlanStatus.Successful, "");
  }
  
  private void unassign(Collection<CalculateParamsWrapper> params, Plan plan, boolean commit)
  {
    List<Stop> unassignedStops = new ArrayList<Stop>();
    if( !Validator.isEmpty( params ) )
    {
      String routeName;
      List<String> stopNames;
      for( CalculateParamsWrapper param : params )
      {
        routeName = param.getRouteName();
        if(routeName.equals(stopsManager.getUnassignedRouteName()))
        {
         stopNames = param.getStops();
          if( !Validator.isEmpty( stopNames )  )
          {
            unassignedStops = stopsManager.unassign(stopNames);
            if( !Validator.isEmpty( unassignedStops ) )
            {
              plan.getStops().addAll(unassignedStops);
              if(commit)
                stopsManager.batchAddOrReplaceStops(unassignedStops);
            }
          }
        }
      }
    }
  }
  
  private Collection<CalculateParamsWrapper> convertGeoEventToCalculateParameters(GeoEvent geoEvent) throws FieldException
  {
    Collection<CalculateParamsWrapper> wrappers = new ArrayList<CalculateParamsWrapper>();
    List<FieldGroup> fieldGroups = geoEvent.getFieldGroups(0);
    for(FieldGroup fg : fieldGroups)
    {
      // TODO: workaround.  json adapter writes Boolean as string.  As a result, the input which uses the same GED could not read Boolean fields.
      // If optimize is null, set it to false.
      boolean optimize = false;
      if(fg.getField("optimize") != null)
        optimize = (Boolean)fg.getField("optimize");
      CalculateParamsWrapper wrapper = new CalculateParamsWrapper();
      wrapper.setRouteName((String)fg.getField("routeName"));
      wrapper.setOptimize(optimize);
      wrapper.setStops((List<String>)fg.getFields("stops"));
      wrappers.add(wrapper);
    }   
    return wrappers;
  }

  @Override
  public GeoEvent createPlanGeoEvent(Plan plan, boolean updateFeatures, PlanStatus status, String message)
  {
    GeoEvent geoEvent = null;
    try
    {
      getGeoEventDefinition();
      geoEvent = geoEventCreator.create(planGEDName, planGEDOwner);
      if(status == PlanStatus.Successful)
        populatePlanGeoEvent(geoEvent, plan.getStops(), plan.getRoutes(), updateFeatures);
      else
        populatePlanGeoEventWithError(geoEvent, message);
    }
    catch (MessagingException e)
    {
      log.error(e);
    }
    catch (FieldException e)
    {
      log.error(e);
    }
    catch (GeoEventDefinitionManagerException e)
    {
      log.error(e);
    }
    catch (ConfigurationException e)
    {
      log.error(e);
    }
    return geoEvent;
  }
  
  private void populatePlanGeoEventWithError(GeoEvent geoEvent, String message) throws FieldException
  {
    geoEvent.setField("Status", PlanStatus.Failed.toString());
    geoEvent.setField("Message", message);
  }

  private void populatePlanGeoEvent(GeoEvent geoEvent, List<Stop> stops, List<Route> routes, boolean updateFeatures) throws FieldException
  {
    List<FieldGroup> stopFieldGroups = new ArrayList<FieldGroup>();
    List<FieldGroup> routeFieldGroups = new ArrayList<FieldGroup>();
    String stopGEDName = stopsManager.getStopsGeoEventDefinition().getName();
    for (Stop stop : stops)
    {
      FieldGroup fieldGroup = geoEvent.createFieldGroup(stopGEDName);
      GeoEvent stopGE = stopsManager.createGeoEvent(stop, planGEDOwner, null);
      for(int i = 0; i<stopGE.getAllFields().length; i++)
      {
        fieldGroup.setField(i, stopGE.getField(i));
      }
      stopFieldGroups.add(fieldGroup);
    }
    geoEvent.setField(stopGEDName, stopFieldGroups);
    
    String routeGEDName = getRoutesGeoEventDefinition().getName();
    for(Route route: routes)
    {
      FieldGroup fieldGroup = geoEvent.createFieldGroup(routeGEDName);
      GeoEvent routeGE = createGeoEvent(route, planGEDOwner, null);
      for(int i=0; i<routeGE.getAllFields().length; i++)
      {
        fieldGroup.setField(i, routeGE.getField(i));
      }
      routeFieldGroups.add(fieldGroup);
    }
    geoEvent.setField(routeGEDName, routeFieldGroups);
    
    geoEvent.setField("UpdateFeatures", updateFeatures);
    geoEvent.setField("Status", PlanStatus.Successful.toString());
    geoEvent.setField("Message", "");
  }
  
  private GeoEventDefinition getGeoEventDefinition() throws ConfigurationException, GeoEventDefinitionManagerException
  {
    GeoEventDefinition geoEventDefinition = geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(planGEDName, planGEDOwner);
    if(geoEventDefinition != null)
      return geoEventDefinition;
    
    geoEventDefinition = new DefaultGeoEventDefinition();
    geoEventDefinition.setName(planGEDName);
    geoEventDefinition.setOwner(planGEDOwner);
    
    List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
    
    String stopGEDName = stopsManager.getStopsGeoEventDefinition().getName();
    FieldDefinition stopFD = new DefaultFieldDefinition(stopGEDName, FieldType.Group);
    List<FieldDefinition> stopFieldDefinitions = stopsManager.getStopsGeoEventDefinition().getFieldDefinitions();
    for( FieldDefinition child : stopFieldDefinitions )
      stopFD.addChild(child);
    stopFD.setCardinality(FieldCardinality.Many);
    fieldDefinitions.add(stopFD);

    String routeGEDName = getRoutesGeoEventDefinition().getName();
    FieldDefinition routeFD = new DefaultFieldDefinition(routeGEDName, FieldType.Group);
    List<FieldDefinition> routeFieldDefinitions = getRoutesGeoEventDefinition().getFieldDefinitions();
    for( FieldDefinition child : routeFieldDefinitions )
      routeFD.addChild(child);
    routeFD.setCardinality(FieldCardinality.Many);
    fieldDefinitions.add(routeFD);
    
    FieldDefinition fd = new DefaultFieldDefinition("UpdateFeatures", FieldType.Boolean);
    fieldDefinitions.add(fd);
    
    FieldDefinition fd1 = new DefaultFieldDefinition("RequestId", FieldType.String);
    fieldDefinitions.add(fd1);
    
    FieldDefinition fd2 = new DefaultFieldDefinition("Status", FieldType.String);
    fieldDefinitions.add(fd2);
    
    FieldDefinition fd3 = new DefaultFieldDefinition("Message", FieldType.String);
    fieldDefinitions.add(fd3);

    geoEventDefinition.setFieldDefinitions(fieldDefinitions);
    geoEventCreator.getGeoEventDefinitionManager().addGeoEventDefinition(geoEventDefinition);
    return geoEventDefinition;
  }

  @Override
  public GeoEvent createUpdateRouteGeoEvent(String routeName, boolean optimize, boolean commit, String requestId, String ownerId, Uri ownerUri)
  {
    GeoEvent geoEvent = null;
    Route route = getRouteByName(routeName);
    if(route == null)
      return null;
    
    List<Stop> stops = stopsManager.getStopsByRouteName(routeName);
    try
    {
      geoEvent = geoEventCreator.create( routeUpdateGEDName, routeGEDOwner );
      List<FieldGroup> routeFieldGroups = new ArrayList<FieldGroup>();
      
      List<String> stopNames = new ArrayList<String>();
      for(int i=0; i<stops.size(); i++)
      {
        if(stops.get(i) != null)
          stopNames.add(stops.get(i).getName());
      }

      FieldGroup fieldGroup = geoEvent.createFieldGroup("route");
      fieldGroup.setField("routeName", routeName);
      fieldGroup.setField("stops", stopNames);
      fieldGroup.setField("optimize", optimize);
      routeFieldGroups.add(fieldGroup);
      
      geoEvent.setField("route", routeFieldGroups);
      geoEvent.setField("commit", commit);
      if(requestId==null)
        requestId = "";
      geoEvent.setField("RequestId", requestId);
      geoEvent.setProperty(GeoEventPropertyName.TYPE, "event");
      geoEvent.setProperty(GeoEventPropertyName.OWNER_ID, ownerId);
      geoEvent.setProperty(GeoEventPropertyName.OWNER_URI, ownerUri);
      
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
    
    return geoEvent;
  }

  @Override
  public Route dispatchRoute(String name)
  {
    Route route = getRouteByName( name );
    if( route == null )
    {
      throw new RuntimeException( "Could not find route "+name );
    }
    DefaultRoute routeCopy = new DefaultRoute( route );
    routeCopy.setDispatchAck(RouteDispatchStatus.Dispatched.toString());
    route = routeCopy;
    addOrReplaceRoute(route);
    return routeCopy;
  }

}
