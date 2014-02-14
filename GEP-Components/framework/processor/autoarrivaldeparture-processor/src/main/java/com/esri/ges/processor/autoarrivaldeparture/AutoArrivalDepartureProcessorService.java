package com.esri.ges.processor.autoarrivaldeparture;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.autoarrivaldeparture.AutoArrivalDepartureManager;
import com.esri.ges.manager.incident.IncidentManager;
import com.esri.ges.manager.routes.RouteManager;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;
import com.esri.ges.registry.condition.ConditionRegistry;

public class AutoArrivalDepartureProcessorService extends GeoEventProcessorServiceBase
{
  private StopsManager stopsManager;
  private VehiclesManager vehiclesManager;
  private RouteManager routeManager;
  private ConditionRegistry conditionRegistry;
  private AutoArrivalDepartureManager autoArrivalDepartureManager;
 
  public AutoArrivalDepartureProcessorService()
  {
    definition = new AutoArrivalDepartureProcessorDefinition();
  }

  @Override
  public GeoEventProcessor create() throws ComponentException
  {
    return new AutoArrivalDepartureProcessor(definition, stopsManager, routeManager, vehiclesManager, conditionRegistry, autoArrivalDepartureManager);
  }

  public void setStopsManager(StopsManager stopsManager)
  {
    this.stopsManager = stopsManager;
  }
  
  public void setVehiclesManager(VehiclesManager vehiclesManager)
  {
    this.vehiclesManager = vehiclesManager;
  }

  public void setRouteManager(RouteManager routeManager)
  {
    this.routeManager = routeManager;
  }

  public void setConditionRegistry(ConditionRegistry conditionRegistry)
  {
    this.conditionRegistry = conditionRegistry;
  }

  public void setAutoArrivalDepartureManager(AutoArrivalDepartureManager autoArrivalDepartureManager)
  {
    this.autoArrivalDepartureManager = autoArrivalDepartureManager;
  }

  
}
