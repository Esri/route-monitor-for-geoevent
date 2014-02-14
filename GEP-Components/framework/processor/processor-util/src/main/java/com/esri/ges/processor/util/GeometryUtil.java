package com.esri.ges.processor.util;

import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;

public class GeometryUtil
{
  protected Geometry geometryFromAttribute( Object locationAttribute )
  {
    Geometry retGeom = null;
    if( locationAttribute instanceof String )
    {
      try
      {
        retGeom = spatial.fromJson( (String)locationAttribute );
      }
      catch (GeometryException e)
      {
        LOG.info("Failed to convert string to Point.", e);
      }
    }
    else if( locationAttribute instanceof Geometry )
    {
      retGeom = (Geometry)locationAttribute;
    }
    return retGeom;
  }
}
