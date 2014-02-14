package com.esri.ges.processor.autoarrivaldeparture;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class AutoArrivalDepartureProcessorDefinition extends GeoEventProcessorDefinitionBase
{
  final private static Log LOG = LogFactory.getLog(AutoArrivalDepartureProcessorDefinition.class);
  public AutoArrivalDepartureProcessorDefinition()
  {
    try
    {
      GeoEventDefinition md = new DefaultGeoEventDefinition();
      md.setName("incident");
      List<FieldDefinition> ads = new ArrayList<FieldDefinition>();
      ads.add(new DefaultFieldDefinition("id", FieldType.String));
      ads.add(new DefaultFieldDefinition("name", FieldType.String));
      ads.add(new DefaultFieldDefinition("type", FieldType.String));
      ads.add(new DefaultFieldDefinition("status", FieldType.String));
      ads.add(new DefaultFieldDefinition("alertType", FieldType.String));
      ads.add(new DefaultFieldDefinition("openCondition", FieldType.String));
      ads.add(new DefaultFieldDefinition("closeCondition", FieldType.String));
      ads.add(new DefaultFieldDefinition("description", FieldType.String));
      ads.add(new DefaultFieldDefinition("timestamp", FieldType.Date, "TIME_START"));
      ads.add(new DefaultFieldDefinition("definitionName", FieldType.String));
      ads.add(new DefaultFieldDefinition("definitionOwner", FieldType.String));
      ads.add(new DefaultFieldDefinition("trackId", FieldType.String, "TRACK_ID"));
      ads.add(new DefaultFieldDefinition("shape", FieldType.Geometry, "GEOMETRY"));
      ads.add(new DefaultFieldDefinition("duration", FieldType.Long));
      ads.add(new DefaultFieldDefinition("dismissed", FieldType.Boolean));
      ads.add(new DefaultFieldDefinition("assignedTo", FieldType.String));
      ads.add(new DefaultFieldDefinition("note", FieldType.String));
      md.setFieldDefinitions(ads);
      geoEventDefinitions.put(md.getName(), md);
    }
    catch(Exception e)
    {
      LOG.error("Error setting up Incident Detector Definition.", e);
    }
  }
  
  @Override
  public String getName()
  {
    return "AutoArrivalDepartureProcessor";
  }
  
  @Override
  public String getLabel()
  {
    return "Auto Arrival and Departure Processor";
  }

  @Override
  public String getDescription()
  {
    return "Automatically detects arrival and departure using GeoFences in the AutoArrival category.";
  }
}
