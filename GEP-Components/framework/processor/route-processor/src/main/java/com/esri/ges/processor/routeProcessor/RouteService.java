package com.esri.ges.processor.routeProcessor;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.messages.MessagesManager;
import com.esri.ges.manager.routes.RouteManager;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;
import com.esri.ges.spatial.Spatial;

public class RouteService extends GeoEventProcessorServiceBase
{
  private RouteManager routeManager;
  private VehiclesManager vehiclesManager;
  private StopsManager stopsManager;
  private MessagesManager messagesManager;
  private Messaging messaging;
	
  public RouteService()
	{
		definition = new RouteDefinition();
	}

	@Override
	public GeoEventProcessor create() throws ComponentException
	{
		return new RouteProcessor(definition, routeManager, stopsManager, vehiclesManager, messagesManager, messaging);
	}

  public void setStopsManager(StopsManager stopsManager)
  {
    this.stopsManager = stopsManager;
  }

  public void setMessaging(Messaging messaging)
  {
    this.messaging = messaging;
  }

  public void setVehiclesManager(VehiclesManager vehiclesManager)
  {
    this.vehiclesManager = vehiclesManager;
  }

  public void setRouteManager(RouteManager routeManager)
  {
    this.routeManager = routeManager;
  }

  public void setMessagesManager(MessagesManager messagesManager)
  {
    this.messagesManager = messagesManager;
  }

}