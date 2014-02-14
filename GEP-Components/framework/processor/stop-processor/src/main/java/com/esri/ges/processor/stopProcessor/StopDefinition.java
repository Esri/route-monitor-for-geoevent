package com.esri.ges.processor.stopProcessor;

import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class StopDefinition extends GeoEventProcessorDefinitionBase
{
	public StopDefinition()
	{
		;
	}
	
	@Override
	public String getName()
	{
		return "StopUpdater";
	}
	
	@Override
  public String getLabel()
  {
    return "Stop Updater";
  }
}