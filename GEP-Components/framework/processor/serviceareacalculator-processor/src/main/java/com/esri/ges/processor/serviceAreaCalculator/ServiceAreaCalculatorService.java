package com.esri.ges.processor.serviceAreaCalculator;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.aoi.AoiManager;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;
import com.esri.ges.spatial.Spatial;

public class ServiceAreaCalculatorService extends GeoEventProcessorServiceBase
{
  private StopsManager stopsManager;
  private ArcGISServerConnectionManager agsConnectionManager;
  private AoiManager aoiManager;
  private GeoEventCreator geoEventCreator;
  private Spatial spatial;
  private String geofenceGEDName;
  private String geofenceGEDOwner;
	
  public ServiceAreaCalculatorService()
	{
		definition = new ServiceAreaCalculatorDefinition();
	}

	@Override
	public GeoEventProcessor create() throws ComponentException
	{
		return new ServiceAreaCalculator(definition, stopsManager, agsConnectionManager, aoiManager, geoEventCreator, spatial, geofenceGEDName, geofenceGEDOwner);
	}
	
	public void setStopsManager(StopsManager stopsManager)
  {
    this.stopsManager = stopsManager;
  }

  public void setAgsConnectionManager(ArcGISServerConnectionManager agsConnectionManager)
  {
    this.agsConnectionManager = agsConnectionManager;
  }

  public void setAoiManager(AoiManager aoiManager)
  {
    this.aoiManager = aoiManager;
  }

  public void setMessaging( Messaging messaging )
  {
    this.geoEventCreator = messaging.createGeoEventCreator();
  }

  public void setGeofenceGEDName(String geofenceGEDName)
  {
    this.geofenceGEDName = geofenceGEDName;
  }

  public void setGeofenceGEDOwner(String geofenceGEDOwner)
  {
    this.geofenceGEDOwner = geofenceGEDOwner;
  }

  public void setSpatial(Spatial spatial)
  {
    this.spatial = spatial;
  }
	

}