package com.esri.ges.processor.geoeventextractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldCardinality;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldGroup;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.messaging.EventDestination;
import com.esri.ges.messaging.EventProducer;
import com.esri.ges.messaging.EventUpdatable;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.GeoEventProducer;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class GeoEevntExtractor extends GeoEventProcessorBase implements EventProducer, EventUpdatable
{
  private static final Log LOG            = LogFactory.getLog(GeoEevntExtractor.class);

  private String geoEventName;
  private Messaging messaging;
  private GeoEventProducer geoEventProducer;
  private EventDestination destination;
  private GeoEventCreator geoEventCreator;
  
  public GeoEevntExtractor(GeoEventProcessorDefinition definition, Messaging messaging) throws ComponentException
  {
    super(definition);
    this.messaging = messaging;
    this.geoEventCreator = messaging.createGeoEventCreator();
  }
  
  @Override
  public void setId(String id)
  {
    super.setId(id);
    destination = new EventDestination(getId() + ":event");
    geoEventProducer = messaging.createGeoEventProducer(destination.getName());
  }

  @Override
  public GeoEvent process(GeoEvent geoEvent) throws Exception
  {
    String owner = geoEvent.getGeoEventDefinition().getOwner();
    GeoEventDefinition ged = geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(geoEventName, owner);
    if(ged==null)
    {
      LOG.error("Cannot find the GeoEvent Definition " + geoEventName + " with owner " + owner);
      return null;
    }

    FieldDefinition fieldDef = geoEvent.getGeoEventDefinition().getFieldDefinition(geoEventName);
    if(fieldDef.getType() != FieldType.Group)
    {
      LOG.error("Corresponding field is not a group field.");
      return null;
    }
    List<FieldGroup> fieldGroups = new ArrayList<FieldGroup>();
    if(fieldDef.getCardinality()==FieldCardinality.Many)
    {
      fieldGroups = geoEvent.getFieldGroups(geoEventName);
    }
    else
    {
      fieldGroups.add(geoEvent.getFieldGroup(geoEventName));
    }
    
    for(FieldGroup fg : fieldGroups)
    {
      GeoEvent event = geoEventCreator.create(ged.getGuid());
      for(FieldDefinition fd : ged.getFieldDefinitions())
      {
        event.setField(fd.getName(), fg.getField(fd.getName()));
      }
      event.setProperty(GeoEventPropertyName.TYPE, "event");
      event.setProperty(GeoEventPropertyName.OWNER_ID, getId());
      event.setProperty(GeoEventPropertyName.OWNER_URI, definition.getUri());
      send(event);
    }
  
    return null;
  }
  
  
  @Override
  public void afterPropertiesSet()
  {
    super.afterPropertiesSet();
    if (hasProperty(GeoEventExtractorDefinition.GEOEVENT_DEFINITION_NAME_PROPERTY))
      geoEventName = getProperty(GeoEventExtractorDefinition.GEOEVENT_DEFINITION_NAME_PROPERTY).getValueAsString();
  }

  @Override
  public void send(GeoEvent msg) throws MessagingException
  {
    if(geoEventProducer == null)
    {
      if(messaging == null)
      {
        LOG.error("Messaging is null.  Unable to create geoEventProducer.");
        return;
      }
      destination = new EventDestination(getId() + ":event");
      geoEventProducer = messaging.createGeoEventProducer(destination.getName());
      if(geoEventProducer == null)
      {
        LOG.error("Unable to create geoEventProducer.");
        return;
      }
    }
    geoEventProducer.send(msg);
  }

  @Override
  public List<EventDestination> getEventDestinations()
  {
    return Arrays.asList(destination);
  }

  @Override
  public EventDestination getEventDestination()
  {
    return destination;
  }
  
}
