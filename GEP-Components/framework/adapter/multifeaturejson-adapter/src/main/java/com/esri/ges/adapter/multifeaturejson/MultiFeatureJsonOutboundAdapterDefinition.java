package com.esri.ges.adapter.multifeaturejson;

import com.esri.ges.adapter.AdapterDefinitionBase;
import com.esri.ges.adapter.AdapterType;

public class MultiFeatureJsonOutboundAdapterDefinition extends AdapterDefinitionBase
{

  public MultiFeatureJsonOutboundAdapterDefinition()
  {
    super(AdapterType.OUTBOUND);
  }

  @Override
  public String getName()
  {
    return "MultiFeatureJson";
  }
  
  @Override
  public String getLabel()
  {
    return "Multiple Features Json Adapter";
  }
  
  @Override
  public String getDomain()
  {
    return "com.esri.ges.adapter.outbound";
  }

  @Override
  public String getDescription()
  {
    return "This adapter accepts GeoEvents that consists of multiple instances of another GeoEvent.";
  }

  
}
