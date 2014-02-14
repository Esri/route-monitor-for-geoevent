package com.esri.ges.processor.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

// This processor currently is a no-op processor
public class MessageProcessor extends GeoEventProcessorBase
{
  private static final Log log            = LogFactory.getLog(MessageProcessor.class);
  
  public MessageProcessor(GeoEventProcessorDefinition definition) throws ComponentException
  {
    super(definition);
  }

  @Override
  public GeoEvent process(GeoEvent geoEvent) throws Exception
  {
    return geoEvent;
  }
  
  @Override
  public void afterPropertiesSet()
  {

  }


}
