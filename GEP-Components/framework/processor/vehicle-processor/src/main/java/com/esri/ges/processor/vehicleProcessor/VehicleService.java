package com.esri.ges.processor.vehicleProcessor;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;
import com.esri.ges.spatial.Spatial;

public class VehicleService extends GeoEventProcessorServiceBase
{
  private VehiclesManager vehiclesManager;
  private Spatial spatial;
	
  public VehicleService()
	{
		definition = new VehicleDefinition();
	}

	@Override
	public GeoEventProcessor create() throws ComponentException
	{
		return new VehicleProcessor(definition, vehiclesManager, spatial);
	}
	
	public void setSpatial(Spatial spatial)
  {
    this.spatial = spatial;
  }

  public void setVehiclesManager(VehiclesManager vehiclesManager)
  {
    this.vehiclesManager = vehiclesManager;
  }

}