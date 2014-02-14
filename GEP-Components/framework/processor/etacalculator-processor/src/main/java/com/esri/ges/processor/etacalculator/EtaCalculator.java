package com.esri.ges.processor.etacalculator;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.stops.Stop;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.manager.vehicles.Vehicle;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.messaging.EventDestination;
import com.esri.ges.messaging.EventProducer;
import com.esri.ges.messaging.EventUpdatable;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.GeoEventProducer;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.Point;
import com.esri.ges.util.DateUtil;
import com.esri.ges.util.Validator;

public class EtaCalculator extends GeoEventProcessorBase implements EventProducer, EventUpdatable
{
  private static final Log LOG = LogFactory.getLog( EtaCalculator.class );
  private Messaging messaging;
  private GeoEventCreator geoEventCreator;
  private StopsManager stopsManager;
  private ArcGISServerConnectionManager agsConnectionManager;
  private String agsConnectionName;
  private String routeSolverPath;
  private VehiclesManager vehiclesManager;
  private GeoEventProducer geoEventProducer;
  private EventDestination destination;
  private String updateMode;
  
  private final String UPDATEMODE_ALL = "All Future Stops";
  private final String UPDATEMODE_NEXT = "Next Stop Only";

  protected EtaCalculator( GeoEventProcessorDefinition definition,
                           Messaging messaging,
                           StopsManager stopsManager,
                           ArcGISServerConnectionManager agsConnectionManager,
                           VehiclesManager vehiclesManager) throws ComponentException
  {
    super(definition);
    this.messaging = messaging;
    this.stopsManager = stopsManager;
    this.agsConnectionManager = agsConnectionManager;
    this.vehiclesManager = vehiclesManager;
  }

  @Override
  public void afterPropertiesSet()
  {
    super.afterPropertiesSet();
    agsConnectionName = getProperty( "dataStoreName" ).getValueAsString();
    routeSolverPath = getProperty( "routeSolverPath" ).getValueAsString();
    updateMode = getProperty("updateMode").getValueAsString();
  }

  @Override
  public void setId(String id)
  {
    super.setId(id);
    destination = new EventDestination(getId() + ":event");
    geoEventProducer = messaging.createGeoEventProducer(destination.getName());
  }
  
  @Override
  public GeoEvent process(GeoEvent geoEvent) throws Exception
  {
    if(updateMode.equals(UPDATEMODE_ALL))
      updateETAForAllStops(geoEvent);
    else if (updateMode.equals(UPDATEMODE_NEXT))
      updateETAForOneStop(geoEvent);
    return null;
  }
  
  private void updateETAForAllStops(GeoEvent geoEvent) throws Exception
  {
    if (geoEventCreator == null && (geoEventCreator = messaging.createGeoEventCreator()) == null)
      return;

    String trackId = (String) geoEvent.getField("TRACK_ID");
    List<Stop> stops = getNextStops(trackId);
    Stop stop = stops.get(0);
    
    if (stop == null)
    {
      LOG.error( "Couldn't update ETA for track "+trackId+" because the next stop was not found." );
      return;
    }
    calculateETAForNextStop(stop, geoEvent);
    
    if(stop != null)
    {
      long diff = DateUtil.millisecondsBetween(stop.getProjectedArrival(), stop.getScheduledArrival());
      
      for(int i=1; i<stops.size(); i++)
      {
        if(stops.get(i) != null)
        {
          if(stops.get(i).getScheduledArrival() != null)
            stops.get(i).setProjectedArrival(new Date(stops.get(i).getScheduledArrival().getTime() + diff));
          if(stops.get(i).getScheduledDeparture() != null)
            stops.get(i).setProjectedDeparture(new Date(stops.get(i).getScheduledDeparture().getTime() + diff));
          send(stopsManager.createGeoEvent(stops.get(i), getId(), definition.getUri()));
        }
      }
    }
  }
  
  private void updateETAForOneStop(GeoEvent geoEvent) throws Exception
  {
    if (geoEventCreator == null && (geoEventCreator = messaging.createGeoEventCreator()) == null)
      return;

    String trackId = (String) geoEvent.getField("TRACK_ID");
    Stop stop = getNextStop(trackId);
    if (stop == null)
    {
      LOG.error( "Couldn't update ETA for track "+trackId+" because the next stop was not found." );
      return;
    }
    calculateETAForNextStop(stop, geoEvent);
    if(stop != null)
      send(stopsManager.createGeoEvent(stop, getId(), definition.getUri()));
  }
  
  private void calculateETAForNextStop(Stop stop, GeoEvent geoEvent)
  {  
    Geometry geom = geoEvent.getGeometry();
    Point pt = null;
    if( geom != null && geom instanceof Point )
    {
      pt = (Point)geom;
    }
    if( pt == null )
    {
      LOG.error( "GeoEvent did not have a Point element set as its Geometry "+geoEvent.toString() );
      stop = null;
    }
    Double minutesToNextStop = getMinutesToNextStop( stop, pt );
    if( minutesToNextStop == null )
    {
      LOG.error( "Unable to update ETA for next stop.");
      stop = null;
    }
    Date eventTime = (Date) geoEvent.getField("TIME_START");
    if( eventTime == null )
    {
      LOG.error( "GeoEvent had not TIME_START field, so we can't set the Project ETA.");
      stop = null;
    }
    if(stop != null)
    {
      Date peta = DateUtil.addMins(eventTime, minutesToNextStop.intValue());
      if (stop.getActualArrival() == null)
      {
        stop.setProjectedArrival(peta);
        stop.setProjectedDeparture(DateUtil.addMins(peta, stop.getScheduledServiceDuration().intValue()));
      }
    }
  }

  private Stop getNextStop(String trackId)
  {
    Vehicle vehicle = vehiclesManager.getVehicleByName( trackId );
    List<Stop> stopsForVehicle = stopsManager.getStopsByRouteName( trackId );
    if( vehicle == null || Validator.isEmpty( stopsForVehicle ) )
    {
      return null;
    }
    Integer nextStopSequenceNumber = vehicle.getNextStopSequenceNumber();
    if( nextStopSequenceNumber > stopsForVehicle.size() )
    {
      return null;
    }
    return stopsForVehicle.get( nextStopSequenceNumber );
  }
  
  private List<Stop> getNextStops(String trackId)
  {
    Vehicle vehicle = vehiclesManager.getVehicleByName( trackId );
    List<Stop> stopsForVehicle = stopsManager.getStopsByRouteName( trackId );
    if( vehicle == null || Validator.isEmpty( stopsForVehicle ) )
    {
      return null;
    }
    Integer nextStopSequenceNumber = vehicle.getNextStopSequenceNumber();
    if( nextStopSequenceNumber > stopsForVehicle.size() )
    {
      return null;
    }
    return stopsForVehicle.subList(nextStopSequenceNumber, stopsForVehicle.size());
  }

  private Double getMinutesToNextStop( Stop stop, Point currentLocation )
  {
    Double retVal = null;
    ArcGISServerConnection agsConn = agsConnectionManager.getArcGISServerConnection( agsConnectionName );
    if( agsConn != null )
    {
      retVal = agsConn.getTimeInMinutesBetween(routeSolverPath, currentLocation, stop.getLocation() );
    }
    else
    {
      LOG.error( "Could not find ArcGISServer Data Store "+agsConnectionName );
    }

    return retVal;
  }

  @Override
  public void send(GeoEvent msg) throws MessagingException
  {
    if(geoEventProducer == null)
    {
      if(messaging == null)
      {
        LOG.error("Messaging is null.  Unable to create geoEventProducer.");
        return;
      }
      destination = new EventDestination(getId() + ":event");
      geoEventProducer = messaging.createGeoEventProducer(destination.getName());
      if(geoEventProducer == null)
      {
        LOG.error("Unable to create geoEventProducer.");
        return;
      }
    }
    geoEventProducer.send(msg);
  }

  @Override
  public List<EventDestination> getEventDestinations()
  {
    return Arrays.asList(destination);
  }

  @Override
  public EventDestination getEventDestination()
  {
    return destination;
  }

}