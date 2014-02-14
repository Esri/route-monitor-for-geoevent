package com.esri.ges.processor.routeProcessor;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class RouteDefinition extends GeoEventProcessorDefinitionBase
{
  
  public final static String NA_CONNECTION_PROPERTY = "naConnectionName";
  public final static String NA_PATH_PROPERTY = "routeSolverPath";
  
	public RouteDefinition()
	{
	  try
    {
      
	    propertyDefinitions.put(NA_CONNECTION_PROPERTY, new PropertyDefinition(NA_CONNECTION_PROPERTY, PropertyType.ArcGISConnection, null, "ArcGIS ServerConnection Name", "The ArcGIS Server Connection to use when contacing Network Analyst", "", false, false));
      propertyDefinitions.put(NA_PATH_PROPERTY, new PropertyDefinition(NA_PATH_PROPERTY, PropertyType.String, "rest/services/Network/USA/NAServer/Route/solve", "Path to Network Analyst Route solver", "Path to Network Analyst Route solver relative to AGS Connection", "", false, false));
    }
    catch(Exception e)
    {
      ;
    }
	}
	
	@Override
	public String getName()
	{
		return "RouteUpdater";
	}
	
	@Override
  public String getLabel()
  {
    return "Route Updater";
  }
}