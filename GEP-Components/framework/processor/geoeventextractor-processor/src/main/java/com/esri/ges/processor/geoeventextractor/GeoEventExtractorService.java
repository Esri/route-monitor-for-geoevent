package com.esri.ges.processor.geoeventextractor;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class GeoEventExtractorService extends GeoEventProcessorServiceBase
{
  private Messaging messaging;;

  public GeoEventExtractorService()
  {
    definition = new GeoEventExtractorDefinition();
  }

  @Override
  public GeoEventProcessor create() throws ComponentException
  {
    return new GeoEevntExtractor(definition, messaging);
  }
 
  public void setMessaging( Messaging messaging )
  {
    this.messaging = messaging;
  }

}
