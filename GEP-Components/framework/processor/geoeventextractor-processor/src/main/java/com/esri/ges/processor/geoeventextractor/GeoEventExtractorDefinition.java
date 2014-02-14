package com.esri.ges.processor.geoeventextractor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class GeoEventExtractorDefinition extends GeoEventProcessorDefinitionBase
{
  final private static Log LOG = LogFactory.getLog(GeoEventExtractorDefinition.class);
  
  public final static String GEOEVENT_DEFINITION_NAME_PROPERTY = "gedName";

  public GeoEventExtractorDefinition()
  {
    try
    {
      propertyDefinitions.put(GEOEVENT_DEFINITION_NAME_PROPERTY, new PropertyDefinition(GEOEVENT_DEFINITION_NAME_PROPERTY, PropertyType.GeoEventDefinition, null, "GeoEvent Definition", "Create new GeoEvent based on this GeoEvent Definition from matching group field in the incoming GeoEvent.", true, false));
    }
    catch (Exception e)
    {
      LOG.error("Error setting up GeoEvent Extractor.", e);
    }
  }
  
  @Override
  public String getName()
  {
    return "GeoEventExtractor";
  }
  
  @Override
  public String getLabel()
  {
    return "GeoEvent Extractor";
  }

  @Override
  public String getDescription()
  {
    return "Create new GeoEvent based on the selected GeoEvent Definition from matching group field in the incoming GeoEvent.";
  }
}
