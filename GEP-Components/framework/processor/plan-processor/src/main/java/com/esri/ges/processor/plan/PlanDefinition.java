package com.esri.ges.processor.plan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class PlanDefinition extends GeoEventProcessorDefinitionBase
{
  final private static Log LOG = LogFactory.getLog(PlanDefinition.class);
  
  public final static String AGS_CONNECTION_PROPERTY = "agsConnection";
  public final static String AGS_PATH_PROPERTY = "path";
  public final static String FEATURE_SERVICE_PROPERTY = "featureService";
  public final static String STOP_LAYER_INDEX_PROPERTY = "stopLayer";
  public final static String ROUTE_LAYER_INDEX_PROPERTY = "routeLayer";
  public final static String VEHICLE_LAYER_INDEX_PROPERTY = "vehicleLayer";
  public final static String ALERT_LAYER_INDEX_PROPERTY = "alertLayer";
  public final static String GEOFENCE_LAYER_INDEX_PROPERTY = "geofenceLayer";

  public PlanDefinition()
  {
    try
    {
      propertyDefinitions.put(AGS_CONNECTION_PROPERTY, new PropertyDefinition(AGS_CONNECTION_PROPERTY, PropertyType.ArcGISConnection, null, "ArcGIS Server Connection", "ArcGIS Server Connection.", true, false));
      propertyDefinitions.put(AGS_PATH_PROPERTY, new PropertyDefinition(AGS_PATH_PROPERTY, PropertyType.ArcGISFolder, "/", "Folder", "Path to the feature service.", true, false));
      propertyDefinitions.put(FEATURE_SERVICE_PROPERTY, new PropertyDefinition(FEATURE_SERVICE_PROPERTY, PropertyType.ArcGISFeatureService, null, "Feature Service", "Feature Service.", true, false));
      propertyDefinitions.put(STOP_LAYER_INDEX_PROPERTY, new PropertyDefinition(STOP_LAYER_INDEX_PROPERTY, PropertyType.ArcGISLayer, null, "Stop Layer", "Stop Layer.", true, false));
      propertyDefinitions.put(ROUTE_LAYER_INDEX_PROPERTY, new PropertyDefinition(ROUTE_LAYER_INDEX_PROPERTY, PropertyType.ArcGISLayer, null, "Route Layer", "Route Layer.", true, false));
      propertyDefinitions.put(VEHICLE_LAYER_INDEX_PROPERTY, new PropertyDefinition(VEHICLE_LAYER_INDEX_PROPERTY, PropertyType.ArcGISLayer, null, "Vehicle Layer", "Vehicle Layer.", true, false));
      propertyDefinitions.put(ALERT_LAYER_INDEX_PROPERTY, new PropertyDefinition(ALERT_LAYER_INDEX_PROPERTY, PropertyType.ArcGISLayer, null, "Alert Layer", "Alert Layer.", true, false));
      propertyDefinitions.put(GEOFENCE_LAYER_INDEX_PROPERTY, new PropertyDefinition(GEOFENCE_LAYER_INDEX_PROPERTY, PropertyType.ArcGISLayer, null, "GeoFence Layer", "GeoFence Layer.", true, false));
    }
    catch (Exception e)
    {
      LOG.error("Error setting up Plan Definition.", e);
    }
  }
  
  @Override
  public String getName()
  {
    return "PlanProcessor";
  }
  
  @Override
  public String getLabel()
  {
    return "PlanProcessor";
  }

  @Override
  public String getDescription()
  {
    return "Performs operations on Route Plan.";
  }
}
