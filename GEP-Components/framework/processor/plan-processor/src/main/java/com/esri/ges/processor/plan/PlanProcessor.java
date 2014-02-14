package com.esri.ges.processor.plan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.manager.plan.PlanManager;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class PlanProcessor extends GeoEventProcessorBase
{
  private static final Log log            = LogFactory.getLog(PlanProcessor.class);
  private static final String REQUEST_ID_FIELD = "RequestId";
  
  private PlanManager planManager;
  
  private String agsConnectionName;
  private String path;
  private String featureService;
  private String stopLayer;
  private String routeLayer;
  private String vehicleLayer;
  private String geofenceLayer;
  private String alertLayer;
  
  public PlanProcessor(GeoEventProcessorDefinition definition, PlanManager planManager) throws ComponentException
  {
    super(definition);
    this.planManager = planManager;
  }

  @Override
  public GeoEvent process(GeoEvent geoEvent) throws Exception
  {
    GeoEvent newGeoEvent = null;
    if(geoEvent.getField(REQUEST_ID_FIELD) == null )
    {
      log.error("Request ID is not available in the GeoEvent.");
      return null;
    }
    String requestId = (String)geoEvent.getField(REQUEST_ID_FIELD);
    
    if(!geoEvent.getGeoEventDefinition().getName().equals(planManager.getPlanCommandGeoEventDefinition().getName()))
      return null;
    if(geoEvent.getField(planManager.getPlanCommandActionField()).equals(planManager.getPlanCommandActionClear()))
      newGeoEvent = planManager.clearPlan(geoEvent, agsConnectionName, path, featureService, stopLayer, routeLayer, vehicleLayer, geofenceLayer, alertLayer);
    else if(geoEvent.getField(planManager.getPlanCommandActionField()).equals(planManager.getPlanCommandActionGet()))
      newGeoEvent = planManager.getPlan();
    else if(geoEvent.getField(planManager.getPlanCommandActionField()).equals(planManager.getPlanCommandActionReload()))
      newGeoEvent = planManager.reloadPlan(agsConnectionName, path, featureService, stopLayer, routeLayer, vehicleLayer, alertLayer);
    else if(geoEvent.getField(planManager.getPlanCommandActionField()).equals(planManager.getPlanCommandActionLoad()))
    {
      //planManager.clearPlan(geoEvent, agsConnectionName, path, featureService, stopLayer, routeLayer, vehicleLayer, geofenceLayer, alertLayer);
      newGeoEvent = planManager.loadPlan(geoEvent);
    }
    
    if(newGeoEvent.getGeoEventDefinition().getFieldDefinition(REQUEST_ID_FIELD) != null)
      newGeoEvent.setField(REQUEST_ID_FIELD, requestId);
    
    return newGeoEvent;
  }
  
  @Override
  public void afterPropertiesSet()
  {
    super.afterPropertiesSet();
    if (hasProperty(PlanDefinition.AGS_CONNECTION_PROPERTY))
      agsConnectionName = getProperty(PlanDefinition.AGS_CONNECTION_PROPERTY).getValueAsString();
    if (hasProperty(PlanDefinition.AGS_PATH_PROPERTY))
      path = getProperty(PlanDefinition.AGS_PATH_PROPERTY).getValueAsString();
    if (hasProperty(PlanDefinition.FEATURE_SERVICE_PROPERTY))
      featureService = getProperty(PlanDefinition.FEATURE_SERVICE_PROPERTY).getValueAsString();
    if (hasProperty(PlanDefinition.STOP_LAYER_INDEX_PROPERTY))
      stopLayer = getProperty(PlanDefinition.STOP_LAYER_INDEX_PROPERTY).getValueAsString();
    if (hasProperty(PlanDefinition.ROUTE_LAYER_INDEX_PROPERTY))
      routeLayer = getProperty(PlanDefinition.ROUTE_LAYER_INDEX_PROPERTY).getValueAsString();
    if (hasProperty(PlanDefinition.VEHICLE_LAYER_INDEX_PROPERTY))
      vehicleLayer = getProperty(PlanDefinition.VEHICLE_LAYER_INDEX_PROPERTY).getValueAsString();
    if (hasProperty(PlanDefinition.GEOFENCE_LAYER_INDEX_PROPERTY))
      geofenceLayer = getProperty(PlanDefinition.GEOFENCE_LAYER_INDEX_PROPERTY).getValueAsString();
    if (hasProperty(PlanDefinition.ALERT_LAYER_INDEX_PROPERTY))
      alertLayer = getProperty(PlanDefinition.ALERT_LAYER_INDEX_PROPERTY).getValueAsString();
  }

}
