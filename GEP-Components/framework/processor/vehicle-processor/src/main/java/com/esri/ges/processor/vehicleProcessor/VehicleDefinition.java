package com.esri.ges.processor.vehicleProcessor;

import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class VehicleDefinition extends GeoEventProcessorDefinitionBase
{
	public VehicleDefinition()
	{
		;
	}
	
	@Override
	public String getName()
	{
		return "VehicleUpdater";
	}
	
	@Override
  public String getLabel()
  {
    return "Vehicle Updater";
  }
}