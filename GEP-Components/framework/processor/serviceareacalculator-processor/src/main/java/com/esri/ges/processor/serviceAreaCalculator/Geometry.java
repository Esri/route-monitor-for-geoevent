package com.esri.ges.processor.serviceAreaCalculator;

import com.esri.ges.spatial.GeometryType;
import com.esri.ges.spatial.SpatialReference;

public abstract class Geometry implements com.esri.ges.spatial.Geometry
{
  private static final long serialVersionUID = -2044519499121463954L;
  private static final int DEFAULT_WKID = 4326;
  private SpatialReference sr = new com.esri.ges.processor.serviceAreaCalculator.SpatialReference(DEFAULT_WKID);
  
  @Override
  public abstract GeometryType getType();
  
  public abstract String toJson();
  
  @Override
  public SpatialReference getSpatialReference()
  {
    return sr;
  }
  public void setSpatialReference(SpatialReference sr)
  {
    this.sr = (sr != null) ? sr : new com.esri.ges.processor.serviceAreaCalculator.SpatialReference(DEFAULT_WKID);
  }
  
  @Override
  public String toString()
  {
    return toJson();
  }
}