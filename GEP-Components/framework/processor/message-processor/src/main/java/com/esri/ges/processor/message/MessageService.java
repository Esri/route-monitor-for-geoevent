package com.esri.ges.processor.message;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class MessageService extends GeoEventProcessorServiceBase
{ 
  
  public MessageService()
  {
    definition = new MessageDefinition();
  }

  @Override
  public GeoEventProcessor create() throws ComponentException
  {
    return new MessageProcessor(definition);
  }


}
