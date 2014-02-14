package com.esri.ges.manager.routes;

import java.util.Date;

import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.Point;

public class DefaultRoute implements Route
{

  private String driverName;
  private Date lastUpdated;
  private String passengerName;
  private Date routeEnd;
  private String routeName;
  private Date routeStart;
  private Geometry shape;
  private String vehicleName;
  private Point routeStartPoint;
  private Point routeEndPoint;
  private String dispatchAck;
  
  public DefaultRoute()
  {
    
  }
  
  public DefaultRoute( Route seed )
  {
    if( seed != null )
    {
      setDriverName( seed.getDriverName() );
      setLastUpdated( seed.getLastUpdated() );
      setPassengerName( seed.getPassengerName() );
      setRouteEnd( seed.getRouteEnd() );
      setRouteName( seed.getRouteName() );
      setRouteStart( seed.getRouteStart() );
      setShape( seed.getShape() );
      setVehicleName( seed.getVehicleName() );
      setRouteStartPoint( seed.getRouteStartPoint() );
      setRouteEndPoint( seed.getRouteEndPoint() );
      setDispatchAck( seed.getDispatchAck() );
    }
  }

  @Override
  public String getDriverName()
  {
    return driverName;
  }

  @Override
  public Date getLastUpdated()
  {
    return lastUpdated;
  }

  @Override
  public String getPassengerName()
  {
    return passengerName;
  }

  @Override
  public Date getRouteEnd()
  {
    return routeEnd;
  }

  @Override
  public String getRouteName()
  {
    return routeName;
  }

  @Override
  public Date getRouteStart()
  {
    return routeStart;
  }

  @Override
  public Geometry getShape()
  {
    return shape;
  }

  @Override
  public String getVehicleName()
  {
    return vehicleName;
  }

  @Override
  public Point getRouteEndPoint()
  {
    return routeEndPoint;
  }
  
  @Override
  public Point getRouteStartPoint()
  {
    return routeStartPoint;
  }

  public void setDriverName(String driverName)
  {
    this.driverName = driverName;
  }

  public void setLastUpdated(Date lastUpdated)
  {
    this.lastUpdated = lastUpdated;
  }

  public void setPassengerName(String passengerName)
  {
    this.passengerName = passengerName;
  }

  public void setRouteEnd(Date routeEnd)
  {
    this.routeEnd = routeEnd;
  }

  public void setRouteName(String routeName)
  {
    this.routeName = routeName;
  }

  public void setRouteStart(Date routeStart)
  {
    this.routeStart = routeStart;
  }

  public void setShape(Geometry shape)
  {
    this.shape = shape;
  }

  public void setVehicleName(String vehicleName)
  {
    this.vehicleName = vehicleName;
  }
  
  public void setRouteEndPoint( Point routeEndPoint )
  {
    this.routeEndPoint = routeEndPoint;
  }

  public void setRouteStartPoint( Point routeStartPoint )
  {
    this.routeStartPoint = routeStartPoint;
  }

  @Override
  public String getDispatchAck()
  {
    return dispatchAck;
  }
  
  public void setDispatchAck(String dispatchAck)
  {
    this.dispatchAck = dispatchAck;
  }
}
