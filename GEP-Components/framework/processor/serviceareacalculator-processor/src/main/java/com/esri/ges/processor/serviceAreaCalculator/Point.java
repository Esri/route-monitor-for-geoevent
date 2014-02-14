package com.esri.ges.processor.serviceAreaCalculator;

import com.esri.ges.spatial.GeometryType;

public class Point extends Geometry implements com.esri.ges.spatial.Point
{
	private static final long serialVersionUID = 4674601204045871541L;
	private double x;
	private double y;
	private double z;

	public Point()
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
		
	}

	public Point(double x, double y, int wkid)
	{
		this.x = x;
		this.y = y;
		this.z = 0;
		this.setSpatialReference(new SpatialReference(wkid));
	}

	public Point(double x, double y, double z, int wkid)
	{
		this(x, y, wkid);
		this.z = z;
	}

	@Override
	public GeometryType getType()
	{
		return GeometryType.Point;
	}

	@Override
	public double getX()
	{
		return x;
	}
	@Override
	public void setX(double x)
	{
		this.x = x;
	}

	@Override
	public double getY()
	{
		return y;
	}
	@Override
	public void setY(double y)
	{
		this.y = y;
	}

	@Override
	public double getZ()
	{
		return z;
	}
	@Override
	public void setZ(double z)
	{
		this.z = z;
	}
	
	@Override
	public boolean equals(com.esri.ges.spatial.Point point)
	{
		return point != null && point.getX() == x && point.getY() == y && point.getZ() == z && point.getSpatialReference().equals(getSpatialReference());
	}

	@Override
	public String toJson()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{\"x\":").append(x);
		sb.append(", \"y\":").append(y);
		sb.append(", \"z\":").append(z);
		sb.append(", \"spatialReference\":{\"wkid\":").append(getSpatialReference().getWkid()).append("}}");
		return sb.toString();
	}
}