package com.esri.ges.processor.stopProcessor;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.messages.MessagesManager;
import com.esri.ges.manager.routes.RouteManager;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class StopService extends GeoEventProcessorServiceBase
{
  private StopsManager stopsManager;
  private RouteManager routeManager;
  private MessagesManager messagesManager;
  private Messaging messaging;
	
  public StopService()
	{
		definition = new StopDefinition();
	}

	@Override
	public GeoEventProcessor create() throws ComponentException
	{
		return new StopProcessor(definition, stopsManager, routeManager, messagesManager, messaging);
	}
	
	public void setStopsManager(StopsManager stopsManager)
  {
    this.stopsManager = stopsManager;
  }

  public void setRouteManager(RouteManager routeManager)
  {
    this.routeManager = routeManager;
  }

  public void setMessaging(Messaging messaging)
  {
    this.messaging = messaging;
  }

  public void setMessagesManager(MessagesManager messagesManager)
  {
    this.messagesManager = messagesManager;
  }
  
}