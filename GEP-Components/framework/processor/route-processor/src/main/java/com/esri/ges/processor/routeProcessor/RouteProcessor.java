package com.esri.ges.processor.routeProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldGroup;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.property.Property;
import com.esri.ges.manager.messages.Message;
import com.esri.ges.manager.messages.MessageType;
import com.esri.ges.manager.messages.MessagesManager;
import com.esri.ges.manager.routemonitor.util.PlanStatus;
import com.esri.ges.manager.routes.Route;
import com.esri.ges.manager.routes.RouteManager;
import com.esri.ges.manager.stops.DefaultStop;
import com.esri.ges.manager.stops.Stop;
import com.esri.ges.manager.stops.StopStatus;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.manager.vehicles.DefaultVehicle;
import com.esri.ges.manager.vehicles.Vehicle;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.messaging.EventDestination;
import com.esri.ges.messaging.EventProducer;
import com.esri.ges.messaging.EventUpdatable;
import com.esri.ges.messaging.GeoEventProducer;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class RouteProcessor extends GeoEventProcessorBase implements EventProducer, EventUpdatable
{
  private static final Log LOG            = LogFactory.getLog(RouteProcessor.class);

  private RouteManager routeManager;
  private VehiclesManager vehiclesManager;
  private StopsManager stopsManager;
  private  MessagesManager messagesManager;
  private String routeGEDName;
  private String routeUpdateGEDName;
  private String routeDispatchGEDName;
  private String agsConnectionName;
  private String routeSolverPath;
  private Messaging messaging;
  private GeoEventProducer geoEventProducer;
  private EventDestination destination;

  protected RouteProcessor(GeoEventProcessorDefinition definition, RouteManager routeManager, StopsManager stopsManager, VehiclesManager vehiclesManager, MessagesManager messagesManager, Messaging messaging) throws ComponentException
  {
    super(definition);
    this.routeManager = routeManager;
    this.stopsManager = stopsManager;
    this.vehiclesManager = vehiclesManager;
    this.messagesManager = messagesManager;
    this.routeGEDName = routeManager.getRoutesGeoEventDefinition().getName();
    this.routeUpdateGEDName = routeManager.getRouteUpdateGeoEventDefinition().getName();
    this.routeDispatchGEDName = routeManager.getRouteDispatchGeoEventDefinition().getName();
    this.messaging = messaging;
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
    Map<String, DefaultStop> stopsSnapshot = new HashMap<String, DefaultStop>();
    return processGeoEvent(geoEvent, stopsSnapshot);
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(definition.getName());
    sb.append("/");
    sb.append(definition.getVersion());
    sb.append("[");
    for (Property p : getProperties())
    {
      sb.append(p.getDefinition().getPropertyName());
      sb.append(":");
      sb.append(p.getValue());
      sb.append(" ");
    }
    sb.append("]");
    return sb.toString();
  }

  protected GeoEvent processGeoEvent(GeoEvent geoEvent, Map<String, DefaultStop> stopsSnapshot)
  {
    String requestId = "";
    if(geoEvent.getField("RequestId") != null)
      requestId = (String)geoEvent.getField("RequestId");
    
    if(geoEvent.getGeoEventDefinition().getName().equals(routeGEDName))
    {
      Route route = routeManager.convertGeoEventToRoute( geoEvent );
      String vehicleName = route.getVehicleName();
      Vehicle vehicle = vehiclesManager.getVehicleByName( vehicleName );
      if( vehicle == null )
      {
        DefaultVehicle defVehicle = new DefaultVehicle();
        defVehicle.setVehicleName( vehicleName );
        vehiclesManager.addOrReplaceVehicle( defVehicle );
      }
      routeManager.addOrReplaceRoute( route );
      return geoEvent;
    }
    else if (geoEvent.getGeoEventDefinition().getName().equals(routeUpdateGEDName))
    {
      try
      {
        if(geoEvent.getFieldGroups(0) == null)
          return null;
      }
      catch(FieldException e)
      {
        LOG.error("Unable to get field group from a route update GeoEvent", e);
        return null;
      }
      
      getStopsSnapshot(geoEvent, stopsSnapshot);
      
      GeoEvent planGeoEvent = routeManager.resequence(geoEvent, agsConnectionName, routeSolverPath);
      try
      {
        planGeoEvent.setField("RequestId", requestId);
        if(planGeoEvent.getField("Status").equals(PlanStatus.Failed.toString()))
        {
          sendErrorMessage(requestId, (String)planGeoEvent.getField("Message"));
        }
      }
      catch (FieldException e)
      {
        LOG.error("Unable to set request id in the output GeoEvent.  " + e.getMessage());
      }
      
      checkResults(stopsSnapshot, requestId);
      return planGeoEvent;
    }
    else if (geoEvent.getGeoEventDefinition().getName().equals(routeDispatchGEDName))
    {
      Object obj = geoEvent.getField(0);
      if(obj != null)
      {
        String routeName = (String)obj;
        if(routeName.length()>0)
        {
          Route route = routeManager.dispatchRoute(routeName);
          if(route != null)
          {
            try
            {
              send(routeManager.createGeoEvent(route, getId(), definition.getUri()));
              List<Stop> stops = stopsManager.getStopsByRouteName(route.getRouteName());
              List<Stop> stopsOut = new ArrayList<Stop>();
              
              if(stops != null)
              {
                for(Stop stop : stops)
                {
                  if(stop != null)
                  {
                    if(stop.getStatus() == StopStatus.Assigned)
                    {
                      stop.setStatus(StopStatus.Dispatched);
                      send(stopsManager.createGeoEvent(stop, getId(), definition.getUri()));
                      stopsOut.add(stop);
                    }
                  }
                }
              }
              if(stopsOut.size()>0)
              {
                send(stopsManager.createListGeoEvent(stopsOut, requestId, getId(), definition.getUri()));
              }
            }
            catch (MessagingException e)
            {
              LOG.error("Failed to send Route GeoEvent: ", e);
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public void afterPropertiesSet()
  {
    super.afterPropertiesSet();
    agsConnectionName = getProperty( RouteDefinition.NA_CONNECTION_PROPERTY ).getValueAsString();
    routeSolverPath = getProperty( RouteDefinition.NA_PATH_PROPERTY ).getValueAsString();
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
  
  private void sendErrorMessage(String messageTo, String errorMessage )
  {
    Message message = new Message();
    message.setType(MessageType.Notification.toString());
    message.setSubject("Route Update Error");
    message.setMessageFrom("RouteMonitor");
    message.setMessageTo(messageTo);
    message.setMessageBody(errorMessage);
    message.setCallback("");
    try
    {
      send(messagesManager.createGeoEvent(message, getId(), this.getDefinition().getUri()));
    }
    catch (MessagingException e)
    {
      LOG.error("Unable generate message geoevent.  Error message is: " + e.getMessage());
    }
    catch(Exception e)
    {
      LOG.error("Unable generate message geoevent.  Error message is: " + e.getMessage());
    }
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
  
  private void getStopsSnapshot(GeoEvent geoevent, Map<String, DefaultStop> stopsSnapshot)
  {
    try
    {
      List<FieldGroup> geRoutes = geoevent.getFieldGroups("route");
      for(FieldGroup fg : geRoutes)
      {
        List<String> geStops = (List<String>) fg.getFields("stops");
        for(String stopname : geStops)
        {
          Stop stop = stopsManager.getStopByName(stopname);
          if(stop != null)
          {
            DefaultStop dStop = new DefaultStop(stop);
            stopsSnapshot.put(stopname, dStop);
          }
        }
      }
    }
    catch (FieldException e)
    {
      LOG.error(e);
    }
    
  }
  
  private void checkResults(Map<String, DefaultStop> stopsSnapshot, String requestId)
  {
    List<Stop> stopsOut = new ArrayList<Stop>();
    // if new status is dispatched, old is assigned or unassigned, output the stop
    for (DefaultStop dStop : stopsSnapshot.values())
    {
      Stop cachedStop = stopsManager.getStopByName(dStop.getName());
      if(cachedStop != null)
      {
        if(dStop.getStatus() != cachedStop.getStatus() && cachedStop.getStatus() == StopStatus.Dispatched)
        {
          stopsOut.add(cachedStop);
        }
      }
    }
    if(stopsOut.size()>0)
    {
      try
      {
        send(stopsManager.createListGeoEvent(stopsOut, requestId, getId(), definition.getUri()));
      }
      catch (MessagingException e)
      {
        LOG.error("Failed to send Stops GeoEvent: ", e);
      }
    }
  }
}
