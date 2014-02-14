package com.esri.ges.adapter.multifeaturejson;

import java.util.List;

import com.esri.ges.adapter.Adapter;
import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.AdapterService;
import com.esri.ges.adapter.AdapterServiceBase;
import com.esri.ges.adapter.OutboundAdapter;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.registry.adapter.AdapterRegistry;

public class MultiFeatureJsonOutboundAdapterService extends AdapterServiceBase
{
  private AdapterRegistry adapterRegistry;
  private GeoEventCreator geoEventCreator;
  private String featureAdapterName;
  private OutboundAdapter featureAdapter;
 
  
  public MultiFeatureJsonOutboundAdapterService()
  {
    definition = new MultiFeatureJsonOutboundAdapterDefinition();
  }

  @Override
  public Adapter createAdapter() throws ComponentException
  {
    List<AdapterService> adapterServices = adapterRegistry.findOutboundAdapters(null);
    
    for (AdapterService adapterService : adapterServices)
    {
      if (adapterService.getAdapterDefinition().getName().equalsIgnoreCase(featureAdapterName))
      {
        featureAdapter = (OutboundAdapter) adapterService.createAdapter();
      }
    }
    AdapterDefinition basedef = featureAdapter.getDefinition();
    definition.getPropertyDefinitions().putAll(basedef.getPropertyDefinitions());
    return new MultiFeatureJsonOutboundAdapter(definition, featureAdapter, geoEventCreator);
  }
  
  public void setFeatureAdapterName(String featureAdapterName)
  {
    this.featureAdapterName = featureAdapterName;
  }

  public void setMessaging( Messaging messaging )
  {
    this.geoEventCreator = messaging.createGeoEventCreator();
  }

  public void setAdapterRegistry(AdapterRegistry adapterRegistry)
  {
    this.adapterRegistry = adapterRegistry;
  }
  
}
