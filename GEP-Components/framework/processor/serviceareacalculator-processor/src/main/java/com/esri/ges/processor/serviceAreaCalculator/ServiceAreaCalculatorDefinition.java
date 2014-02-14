package com.esri.ges.processor.serviceAreaCalculator;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class ServiceAreaCalculatorDefinition extends GeoEventProcessorDefinitionBase
{
  public final static String SERVICE_AREA_TYPE_PROPERTY = "serviceAreaType";
  public final static String SERVICE_AREA_TYPE_BUFFER = "Buffer";
  public final static String SERVICE_AREA_TYPE_DRIVE_TIME = "Drive Time";
  public final static String BUFFER_DISTANCE_PROPERTY = "bufferDistance";
  public final static String NA_CONNECTION_PROPERTY = "naConnectionName";
  public final static String NA_PATH_PROPERTY = "routeSolverPath";
  public final static String DRIVE_TIME_PROPERTY = "driveTimeMinutes";
  
	public ServiceAreaCalculatorDefinition()
	{
    try
    {
      
      propertyDefinitions.put(SERVICE_AREA_TYPE_PROPERTY, new PropertyDefinition(SERVICE_AREA_TYPE_PROPERTY, PropertyType.String, SERVICE_AREA_TYPE_BUFFER, "Service Area Type", "Create service area either by buffer with a distance or by drive time.", true, false, SERVICE_AREA_TYPE_BUFFER, SERVICE_AREA_TYPE_DRIVE_TIME));
      propertyDefinitions.put(BUFFER_DISTANCE_PROPERTY, new PropertyDefinition(BUFFER_DISTANCE_PROPERTY, PropertyType.Double, 500, "Buffer Distance (feet)", "Radius of a buffer around a point.", SERVICE_AREA_TYPE_PROPERTY + "=" + SERVICE_AREA_TYPE_BUFFER, false, false));
      propertyDefinitions.put(NA_CONNECTION_PROPERTY, new PropertyDefinition(NA_CONNECTION_PROPERTY, PropertyType.ArcGISConnection, null, "ArcGIS ServerConnection Name", "The ArcGIS Server Connection to use when contacing Network Analyst", SERVICE_AREA_TYPE_PROPERTY + "=" + SERVICE_AREA_TYPE_DRIVE_TIME, false, false));
      propertyDefinitions.put(NA_PATH_PROPERTY, new PropertyDefinition(NA_PATH_PROPERTY, PropertyType.String, "rest/services/Network/USA/NAServer/Route/solve", "Path to Network Analyst Route solver", "Path to Network Analyst Route solver relative to AGS Connection", SERVICE_AREA_TYPE_PROPERTY + "=" + SERVICE_AREA_TYPE_DRIVE_TIME, false, false));
      propertyDefinitions.put(DRIVE_TIME_PROPERTY, new PropertyDefinition(DRIVE_TIME_PROPERTY, PropertyType.Integer, 2, "Drive Time Limit (minutes)", "Drive time limit of the service area", SERVICE_AREA_TYPE_PROPERTY + "=" + SERVICE_AREA_TYPE_DRIVE_TIME, false, false));
    }
    catch(Exception e)
    {
      ;
    }
	}
	
	@Override
	public String getName()
	{
		return "ServiceAreaCreator";
	}
	
	@Override
  public String getLabel()
  {
    return "Service Area Creator";
  }
}