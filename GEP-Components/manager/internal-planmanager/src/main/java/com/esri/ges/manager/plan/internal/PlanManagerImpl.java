package com.esri.ges.manager.plan.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.RunningException;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.alerts.AlertsManager;
import com.esri.ges.manager.autoarrivaldeparture.AutoArrivalDepartureManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.plan.PlanManager;
import com.esri.ges.manager.routemonitor.util.PlanStatus;
import com.esri.ges.manager.routes.Plan;
import com.esri.ges.manager.routes.Route;
import com.esri.ges.manager.routes.RouteManager;
import com.esri.ges.manager.stops.Stop;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.manager.stream.StreamManager;
import com.esri.ges.manager.vehicles.Vehicle;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.registry.transport.TransportProxy;
import com.esri.ges.stream.Stream;
import com.esri.ges.transport.Transport;

public class PlanManagerImpl implements PlanManager
{
  final private static Log log = LogFactory.getLog( PlanManagerImpl.class );
  private final String ACTION_FIELD = "Action";
  private final String STATUS_FIELD = "Status";
  private final String PLANFOLDER_FIELD = "PlanFolder";
  private final String ACTION_CLEAR = "Clear";
  private final String ACTION_GET = "Get";
  private final String ACTION_RELOAD = "Reload";
  private final String ACTION_LOAD = "Load";
  
  private AutoArrivalDepartureManager autoArrivalDepartureManager;
  private StopsManager stopsManager;
  private RouteManager routeManager;
  private VehiclesManager vehiclesManager;
  private AlertsManager alertsManager;
  private GeoEventDefinitionManager geoEventDefinitionManager;
  private GeoEventCreator geoEventCreator;
  private String planGEDOwner;
  private String planCommandGEDName;
  private StreamManager streamManager;
  private String planInputName;

  @Override
  public GeoEventDefinition getPlanCommandGeoEventDefinition()
  {
    return geoEventDefinitionManager.searchGeoEventDefinition(planCommandGEDName, planGEDOwner);
  }

  @Override
  public GeoEvent clearPlan(GeoEvent geoEvent, String agsConnectionName, String path, String featureService, String stopLayer, String routeLayer, String vehicleLayer, String geofenceLayer, String alertLayer)
  {
    stopsManager.clearAllStops(agsConnectionName, path, featureService, stopLayer, geofenceLayer);
    routeManager.clearAllRouteFeatures(agsConnectionName, path, featureService, routeLayer);
    vehiclesManager.clearAllVehicleFeatures(agsConnectionName, path, featureService, vehicleLayer);
    alertsManager.clearAllAlertFeatures(agsConnectionName, path, featureService, alertLayer);
    autoArrivalDepartureManager.clearIncidents();

    GeoEvent newGeoEvent = (GeoEvent) geoEvent.clone(null);
    try
    {
      newGeoEvent.setField(STATUS_FIELD, PlanStatus.Successful.toString());
    }
    catch (FieldException e)
    {
      log.error(e.getStackTrace());
      try
      {
        newGeoEvent.setField(STATUS_FIELD, PlanStatus.Failed.toString());
      }
      catch (FieldException e1)
      {
        log.error(e1.getStackTrace());
      }
    }
    return newGeoEvent;
  }

  @Override
  public GeoEvent getPlan()
  {
    //List<Vehicle> vehicles = vehiclesManager.getVehicles();
    List<Stop> stops = stopsManager.getStops();
    List<Route> routes = new ArrayList<Route>(routeManager.getRoutes());
    Plan plan = new Plan();
    plan.setRoutes(routes);
    plan.setStops(stops);
    GeoEvent newGeoEvent = null;
    try
    {
      newGeoEvent = routeManager.createPlanGeoEvent(plan, false, PlanStatus.Successful, "");
    }
    catch(Exception ex)
    {
      log.error(ex);
      newGeoEvent = routeManager.createPlanGeoEvent(null, false, PlanStatus.Failed, ex.getMessage());
    }
    return newGeoEvent;
  }

  @Override
  public GeoEvent reloadPlan(String agsConnectionName, String path, String featureService, String stopLayer, String routeLayer, String vehicleLayer, String alertLayer)
  {
    vehiclesManager.reloadVehicles(agsConnectionName, path, featureService, vehicleLayer);
    List<Stop> stops = stopsManager.reloadStops(agsConnectionName, path, featureService, stopLayer);
    List<Route> routes = routeManager.reloadRoutes(agsConnectionName, path, featureService, routeLayer);
    Plan plan = new Plan();
    plan.setRoutes(routes);
    plan.setStops(stops);
    GeoEvent planGE = null;
    try
    {
      if(stops != null || routes != null)
        planGE = routeManager.createPlanGeoEvent(plan, false, PlanStatus.Successful, "");
    }
    catch (Exception e)
    {
      log.error(e);
      planGE = routeManager.createPlanGeoEvent(null, false, PlanStatus.Failed, e.getMessage());
    }
    resetVehicleNextSequenceNumber();
    return planGE;
  }

  @Override
  public GeoEvent loadPlan(GeoEvent geoEvent)
  {
    String planFolder = geoEvent.getField(PLANFOLDER_FIELD).toString();
    GeoEvent newGeoEvent = (GeoEvent) geoEvent.clone(null);
    Stream input;

    try
    {
      input = findInput(planInputName);
      notifyInputs(input, planFolder);
      newGeoEvent.setField(STATUS_FIELD, PlanStatus.Successful.toString());
    }
    catch (PropertyException e)
    {
      log.error(e.getStackTrace());
      try
      {
        newGeoEvent.setField(STATUS_FIELD, PlanStatus.Failed.toString());
      }
      catch (FieldException e1)
      {
        log.error(e1.getStackTrace());
      }
    }
    catch (RunningException e)
    {
      log.error(e.getStackTrace());
      try
      {
        newGeoEvent.setField(STATUS_FIELD, PlanStatus.Failed.toString());
      }
      catch (FieldException e1)
      {
        log.error(e1.getStackTrace());
      }
    }
    catch (FieldException e)
    {
      log.error(e.getStackTrace());
    }
    
    resetVehicleNextSequenceNumber();
    return newGeoEvent;
  }
  
  private void resetVehicleNextSequenceNumber()
  {
    List<Vehicle> vehicles = vehiclesManager.getVehicles();
    for(Vehicle v:vehicles)
    {
      v.setNextStopSequenceNumber(stopsManager.getSequenceNumberForNextAssignedStop(v.getVehicleName()));
    }
  }
  
  private void notifyInputs(Stream input, String planFolder) throws PropertyException, RunningException
  {
    TransportProxy tp = input.getTransportProxy();
    Transport t = tp.getProxiedTransport();
    
    input.stop();
    t.setProperty("inputDirectory", planFolder);
    input.start();

  }
  
  private Stream findInput(String name)
  {
    Stream input = null;
    Collection<Stream> inputs = streamManager.getInboundStreams();
    for(Stream stream : inputs)
    {
      if(stream.getName().equals(name))
      {
        input = stream;
        input.getAdapterProxy();
      }
    }
    return input;
  }

  @Override
  public String getPlanCommandActionField()
  {
    return ACTION_FIELD;
  }

  @Override
  public String getPlanCommandActionClear()
  {
    return ACTION_CLEAR;
  }

  @Override
  public String getPlanCommandActionGet()
  {
    return ACTION_GET;
  }

  @Override
  public String getPlanCommandActionLoad()
  {
    return ACTION_LOAD;
  }

  @Override
  public String getPlanCommandActionReload()
  {
    return ACTION_RELOAD;
  }

  public void setStopsManager(StopsManager stopsManager)
  {
    this.stopsManager = stopsManager;
  }

  public void setRouteManager(RouteManager routeManager)
  {
    this.routeManager = routeManager;
  }

  public void setVehiclesManager(VehiclesManager vehiclesManager)
  {
    this.vehiclesManager = vehiclesManager;
  }

  public void setPlanGEDOwner(String planGEDOwner)
  {
    this.planGEDOwner = planGEDOwner;
  }

  public void setPlanCommandGEDName(String planCommandGEDName)
  {
    this.planCommandGEDName = planCommandGEDName;
  }
  
  public void setMessaging( Messaging messaging )
  {
    this.geoEventCreator = messaging.createGeoEventCreator();
    this.geoEventDefinitionManager = geoEventCreator.getGeoEventDefinitionManager();
  }
  
  public void setStreamManager(StreamManager streamManager)
  {
    this.streamManager = streamManager;
  }
  
  public void setPlanInputName(String planInputName)
  {
    this.planInputName = planInputName;
  }
  
  public void setAlertsManager(AlertsManager alertsManager)
  {
    this.alertsManager = alertsManager;
  }

  public void setAutoArrivalDepartureManager(AutoArrivalDepartureManager autoArrivalDepartureManager)
  {
    this.autoArrivalDepartureManager = autoArrivalDepartureManager;
  }
  
  
}
