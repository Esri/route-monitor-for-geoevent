package com.esri.ges.manager.stops;

import java.util.Map;

import com.esri.ges.core.property.PropertyDefinition;

public interface StopsConfiguration
{
  public String getStopType();
  
  public Map<String, PropertyDefinition> getPropertyDefinitions();
  public boolean hasPropertyDefinition(String name);
  public PropertyDefinition getPropertyDefinition(String name);
}
