package com.esri.ges.manager.routes.internal;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="calculateParams")
public class CalculateParamsWrapper
{
  private String routeName;
  private List<String> stops;
  private boolean optimize;
  
  public String getRouteName()
  {
    return routeName;
  }
  
  public void setRouteName(String routeName)
  {
    this.routeName = routeName;
  }
  
  public List<String> getStops()
  {
    return stops;
  }
  
  public void setStops(List<String> stops)
  {
    this.stops = stops;
  }

  public boolean isOptimize()
  {
    return optimize;
  }

  public void setOptimize(boolean optimize)
  {
    this.optimize = optimize;
  }
}
