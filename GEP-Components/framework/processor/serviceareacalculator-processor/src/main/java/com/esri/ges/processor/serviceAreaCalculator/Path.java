package com.esri.ges.processor.serviceAreaCalculator;

import java.util.ArrayList;
import java.util.List;

import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.SpatialReference;

public class Path
{
  private List<Point> path = new ArrayList<Point>();
  private SpatialReference spatialReference;
  
  public Path( SpatialReference sr )
  {
    spatialReference = sr;
  }
  
  public Point getPoint(int pointIndex) throws IndexOutOfBoundsException
  {
    if (size() == 0)
      return null;
    if (pointIndex < 0 || pointIndex >= size())
    {
      StringBuffer sb = new StringBuffer();
      sb.append("Point index " + pointIndex + " is out of bound: ");
      sb.append("Indexes in the range between [0, ").append(size()-1).append("] are allowed.");
      throw new IndexOutOfBoundsException(sb.toString());
    }
    return path.get(pointIndex);
  }
  
  public void addPoint(Point point)
  {
    if (point != null)
      path.add(point);
  }
  
  public int size()
  {
    return isClosed() ? path.size()-1 : path.size();
  }
  
  public boolean isClosed()
  {
    return (path.size() > 0) ? path.get(0).equals(path.get(path.size()-1)) : false;
  }
  
  public void close()
  {
    if (path.size() > 0 && !isClosed())
    {
      Point p = path.get(0);
      addPoint(new com.esri.ges.processor.serviceAreaCalculator.Point(p.getX(), p.getY(), p.getZ(), spatialReference.getWkid()));
    }
  }
  
  public String toJson()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    for (int i=0; i < path.size(); i++)
    {
      if (i > 0)
        sb.append(",");
      Point p = path.get(i);
      sb.append("[").append(p.getX()).append(",").append(p.getY()).append(",").append(p.getZ()).append("]");
    }
    sb.append("]");
    return sb.toString();
  }
  
  @Override
  public String toString()
  {
    return toJson();
  }
}