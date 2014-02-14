package com.esri.ges.processor.etacalculator;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;
import com.esri.ges.processor.util.XmlGeoEventProcessorDefinition;

public class EtaCalculatorService extends GeoEventProcessorServiceBase
{
  private Messaging messaging;
  private StopsManager stopsManager;
  private ArcGISServerConnectionManager agsConnectionManager;
  private VehiclesManager vehiclesManager;

  public EtaCalculatorService()
  {
    definition = new XmlGeoEventProcessorDefinition( getResourceAsStream("etacalculator-definition.xml") );
  }

  @Override
  public GeoEventProcessor create() throws ComponentException
  {
    return new EtaCalculator(definition, messaging, stopsManager, agsConnectionManager, vehiclesManager);
  }

  public void setMessaging(Messaging messaging)
  {
    this.messaging = messaging;
  }

  public void setStopsManager(StopsManager stopsManager)
  {
    this.stopsManager = stopsManager;
  }

  public void setAgsConnectionManager(ArcGISServerConnectionManager agsConnectionManager)
  {
    this.agsConnectionManager = agsConnectionManager;
  }
  
  public void setVehiclesManager( VehiclesManager manager )
  {
    this.vehiclesManager = manager;
  }
}