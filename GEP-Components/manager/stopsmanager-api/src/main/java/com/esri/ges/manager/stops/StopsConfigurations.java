package com.esri.ges.manager.stops;

import java.util.Map;

public interface StopsConfigurations
{
  public Map<String, StopsConfiguration> getStopsConfigurations();
  public boolean hasStopsConfiguration(String name);
  public StopsConfiguration getStopsConfiguration(String name);
  
  public String getCallback();
  public void setCallback(String callback);
}
