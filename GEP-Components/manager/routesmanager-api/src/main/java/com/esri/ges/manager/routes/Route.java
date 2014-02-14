package com.esri.ges.manager.routes;

import java.util.Date;

import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.Point;

public interface Route
{
  public String getDriverName();
  public Date getLastUpdated();
  public String getPassengerName();
  public Date getRouteEnd();
  public String getRouteName();
  public Date getRouteStart();
  public Geometry getShape();
  public String getVehicleName();
//  public RouteStatus getStatus();
  public Point getRouteStartPoint();
  public Point getRouteEndPoint();
  public String getDispatchAck();
}
