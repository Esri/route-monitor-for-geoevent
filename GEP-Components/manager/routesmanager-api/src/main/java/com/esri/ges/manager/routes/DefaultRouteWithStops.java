package com.esri.ges.manager.routes;

import java.util.Date;
import java.util.List;

import com.esri.ges.manager.stops.Stop;
import com.esri.ges.spatial.Point;

public class DefaultRouteWithStops extends DefaultRoute implements RouteWithStops
{
  private List<Stop> stops;
  private Point currentLocation;
  private boolean optimize;
  private Date currentTimeStamp;
  
  public DefaultRouteWithStops()
  {
    super();
  }
  
  public DefaultRouteWithStops( Route route, List<Stop> stops, Point currentLocation, Date currentTimeStamp, boolean optimize )
  {
    super( route );
    setStops( stops );
    setCurrentLocation( currentLocation );
    setCurrentTimeStamp(currentTimeStamp);
    setOptimize(optimize);
  }
  
  public void setStops( List<Stop> stops )
  {
    this.stops = stops;
  }
  
  @Override
  public Point getCurrentLocation()
  {
    return currentLocation;
  }

  public void setCurrentLocation(Point currentLocation)
  {
    this.currentLocation = currentLocation;
  }

  @Override
  public List<Stop> getStops()
  {
    return stops;
  }

  @Override
  public boolean isOptimize()
  {
    return optimize;
  }
  
  public void setOptimize( boolean optimize )
  {
    this.optimize = optimize;
  }
  
  public Date getCurrentTimeStamp()
  {
    return currentTimeStamp;
  }

  public void setCurrentTimeStamp(Date currentTimeStamp)
  {
    this.currentTimeStamp = currentTimeStamp;
  }

}
