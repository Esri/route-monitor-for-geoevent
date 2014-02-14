package com.esri.ges.manager.routes;

import java.util.Date;
import java.util.List;

import com.esri.ges.manager.stops.Stop;
import com.esri.ges.spatial.Point;

public interface RouteWithStops extends Route
{
  public List<Stop> getStops();
  public Point getCurrentLocation();
  public Date getCurrentTimeStamp();
  public boolean isOptimize();
}
