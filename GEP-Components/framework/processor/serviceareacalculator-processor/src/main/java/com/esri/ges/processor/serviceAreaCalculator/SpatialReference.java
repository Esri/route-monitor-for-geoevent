package com.esri.ges.processor.serviceAreaCalculator;

import java.io.Serializable;

public class SpatialReference implements com.esri.ges.spatial.SpatialReference, Serializable
{
	private static final long serialVersionUID = 1L;
	private final int DEFAULT_WKID = 4326;
	private int wkid;
	
	public SpatialReference()
	{
	  setWkid(DEFAULT_WKID);
	}
	
	public SpatialReference(int wkid)
	{
		setWkid(wkid);
	}

	@Override
	public int getWkid()
	{
		return wkid;
	}
	@Override
	public void setWkid(int wkid)
	{
	  try
	  {
	    this.wkid = (com.esri.core.geometry.SpatialReference.create(wkid) != null) ? wkid : DEFAULT_WKID;
	  }
	  catch (IllegalArgumentException e)
	  {
	    this.wkid = DEFAULT_WKID;
	  }
	}

	@Override
	public boolean equals(com.esri.ges.spatial.SpatialReference sr)
	{
		return (sr != null && sr.getWkid() == wkid);
	}
}