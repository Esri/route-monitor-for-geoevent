package com.esri.ges.processor.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class MessageDefinition extends GeoEventProcessorDefinitionBase
{
  final private static Log LOG = LogFactory.getLog(MessageDefinition.class);

  public MessageDefinition()
  {
    
  }
  
  @Override
  public String getName()
  {
    return "MessageProcessor";
  }
  
  @Override
  public String getLabel()
  {
    return "MessageProcessor";
  }

  @Override
  public String getDescription()
  {
    return "Performs operations on messages.";
  }
}
