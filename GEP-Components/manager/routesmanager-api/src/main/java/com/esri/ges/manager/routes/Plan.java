package com.esri.ges.manager.routes;

import java.util.List;

import com.esri.ges.manager.stops.Stop;

public class Plan
{
  private List<Stop> stops;
  private List<Route> routes;
  
  public Plan()
  {
    
  }
  
  public Plan( List<Stop> stops, List<Route> routes )
  {
    setStops( stops );
    setRoutes( routes );
  }

  public List<Stop> getStops()
  {
    return stops;
  }

  public void setStops(List<Stop> stops)
  {
    this.stops = stops;
  }

  public List<Route> getRoutes()
  {
    return routes;
  }

  public void setRoutes(List<Route> routes)
  {
    this.routes = routes;
  }
  
  
}
