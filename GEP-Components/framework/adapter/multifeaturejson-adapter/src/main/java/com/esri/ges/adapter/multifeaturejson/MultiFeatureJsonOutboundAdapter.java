package com.esri.ges.adapter.multifeaturejson;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.AdapterType;
import com.esri.ges.adapter.OutboundAdapter;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldGroup;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.Property;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.security.GeoEventServerCryptoService;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.ByteListener;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.MessagingException;

public class MultiFeatureJsonOutboundAdapter implements OutboundAdapter
{
  private static final Log log = LogFactory.getLog(MultiFeatureJsonOutboundAdapter.class);
  
  private OutboundAdapter featureAdapter;
  private AdapterDefinition definition;
  private GeoEventCreator geoEventCreator;
  private String id;
  
  public MultiFeatureJsonOutboundAdapter(AdapterDefinition definition, OutboundAdapter featureAdapter, GeoEventCreator geoEventCreator)
  {
    this.definition = definition;
    this.featureAdapter = featureAdapter;
    this.geoEventCreator = geoEventCreator;
  }
  

  @Override
  public void receive(GeoEvent geoEvent)
  {
    String name = geoEvent.getGeoEventDefinition().getFieldDefinitions().get(0).getName();
    List<FieldDefinition> fds = geoEvent.getGeoEventDefinition().getFieldDefinitions().get(0).getChildren();

    GeoEventDefinition ged = geoEventCreator.getGeoEventDefinitionManager().searchGeoEventDefinition(name, geoEvent.getGeoEventDefinition().getOwner());

    try
    {
      if(ged == null)
      {
        ged = new DefaultGeoEventDefinition();
        ged.setName(name);
        ged.setOwner(geoEvent.getGeoEventDefinition().getOwner());
        ged.setFieldDefinitions(fds);
        
        geoEventCreator.getGeoEventDefinitionManager().addGeoEventDefinition(ged);
      }
      List<FieldGroup> fieldGroups = geoEvent.getFieldGroups(0);
      for(FieldGroup fg : fieldGroups)
      {
        GeoEvent event = geoEventCreator.create(ged.getGuid());
        for(FieldDefinition fd : fds)
        {
          event.setField(fd.getName(), fg.getField(fd.getName()));
        }
        featureAdapter.receive(event);
      }     
    }
    catch (GeoEventDefinitionManagerException e)
    {
      log.error(e);
    }
    catch (MessagingException e)
    {
      log.error(e);
    }
    catch (FieldException e)
    {
      log.error(e);
    }
  }

  @Override
  public AdapterDefinition getDefinition()
  {
//    AdapterDefinition basedef = featureAdapter.getDefinition();
//    definition.getPropertyDefinitions().putAll(basedef.getPropertyDefinitions());
    return definition;
  }

  @Override
  public AdapterType getType()
  {
    return featureAdapter.getType();
  }

  @Override
  public String getId()
  {
    return id.isEmpty() ? definition.getUri().toString() : id;
  }

  @Override
  public void setId(String id)
  {
    this.id=id;
  }

  @Override
  public void setProperty(String name, String value) throws PropertyException
  {
    featureAdapter.setProperty(name, value);
  }

  @Override
  public void shutdown()
  {
    featureAdapter.shutdown();
  }

  @Override
  public void afterPropertiesSet()
  {
    featureAdapter.afterPropertiesSet();
  }

  @Override
  public void afterPropertySet(String arg0)
  {
    featureAdapter.afterPropertySet(arg0);
  }

  @Override
  public void clearProperties()
  {
    featureAdapter.clearProperties();
  }

  @Override
  public void deleteProperty(String arg0)
  {
    featureAdapter.deleteProperty(arg0);
  }

  @Override
  public Collection<Property> getProperties()
  {
    return featureAdapter.getProperties();
  }

  @Override
  public Property getProperty(String arg0)
  {
    return featureAdapter.getProperty(arg0);
  }

  @Override
  public boolean hasProperty(String arg0)
  {
    return featureAdapter.hasProperty(arg0);
  }

  @Override
  public void setCryptoService(GeoEventServerCryptoService arg0)
  {
    featureAdapter.setCryptoService(arg0);
  }

  @Override
  public void setProperties(Collection<Property> arg0) throws PropertyException
  {
    featureAdapter.setProperties(arg0);
  }

  @Override
  public void setProperty(Property arg0) throws PropertyException
  {
    featureAdapter.setProperty(arg0);
  }

  @Override
  public void validate() throws ValidationException
  {
    featureAdapter.validate();
  }

  @Override
  public String getMIMEType() throws ComponentException
  {
    return featureAdapter.getMIMEType();
  }

  @Override
  public byte[] processData(Map<String, List<GeoEvent>> arg0) throws ComponentException
  {
    return null;
  }

  @Override
  public void setByteListener(ByteListener arg0)
  {
    featureAdapter.setByteListener(arg0);
  }

}
