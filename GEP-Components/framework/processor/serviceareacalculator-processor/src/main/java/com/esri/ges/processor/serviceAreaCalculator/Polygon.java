package com.esri.ges.processor.serviceAreaCalculator;

import java.util.ArrayList;
import java.util.List;

import com.esri.ges.spatial.GeometryType;
import com.esri.ges.spatial.Point;

public class Polygon extends Geometry implements com.esri.ges.spatial.Polygon
{
	private static final long serialVersionUID = -6055402972765730637L;
	private List<Path> paths = new ArrayList<Path>();

	public Polygon()
	{
		;
	}
	
	public Polygon(com.esri.core.geometry.Polygon ap, int wkid)
	{
		for (int i=0; i < ap.getPathCount(); i++)
		{
			int pathStart = ap.getPathStart(i);
			com.esri.core.geometry.Point p = ap.getPoint(pathStart);
			startPath(p.getX(), p.getY(), p.getZ());

			int pathSize = ap.getPathSize(i);
			if (pathSize > 1)
			{
				for (int j = pathStart + 1; j < pathStart + pathSize; j++)
				{
				  p = ap.getPoint(j);
					lineTo(p.getX(), p.getY(), p.getZ());
				}
			}
		}
		closeAllPaths();
		this.setSpatialReference(new SpatialReference(wkid));
	}

	@Override
	public GeometryType getType()
	{
		return GeometryType.Polygon;
	}

	@Override
	public void startPath(double x, double y, double z)
	{
		Path path = new Path(getSpatialReference());
		path.addPoint(new com.esri.ges.processor.serviceAreaCalculator.Point(x, y, z, getSpatialReference().getWkid()));
		paths.add(path);
	}

	@Override
	public void lineTo(double x, double y, double z)
	{
		Path path = (pathCount() > 0) ? paths.get(pathCount()-1) : null;
		if (path == null)
			startPath(x, y, z);
		else
			path.addPoint(new com.esri.ges.processor.serviceAreaCalculator.Point(x, y, z, getSpatialReference().getWkid()));
	}

	@Override
	public void closeAllPaths()
	{
		for (Path path  : paths)
			path.close();
	}

	@Override
	public int pathCount()
	{
		return paths.size();
	}

	public int pointCount()
	{
		int count=0;
		for (int j=0; j < pathCount(); j++)
			count += paths.get(j).size();
		return count;
	}

	private boolean isValidPathIndex(int pathIndex)
	{
		return pathCount() > 0 && pathIndex >= 0 && pathIndex < pathCount();
	}

	@Override
	public int getPathStart(int pathIndex) throws IndexOutOfBoundsException
	{
		if (isValidPathIndex(pathIndex))
		{
			int i=0;
			for (int j=0; j < pathIndex; j++)
				i+=paths.get(j).size();
			return i;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("Path index " + pathIndex + " is out of bound: ");
		sb.append("Indexes in the range between [0, ").append(pathCount()-1).append("] are allowed.");
		throw new IndexOutOfBoundsException(sb.toString());
	}

	@Override
	public int getPathSize(int pathIndex) throws IndexOutOfBoundsException
	{
		if (isValidPathIndex(pathIndex))
			return paths.get(pathIndex).size();
		StringBuffer sb = new StringBuffer();
		sb.append("Path index " + pathIndex + " is out of bound: ");
		sb.append("Indexes in the range between [0, ").append(pathCount()-1).append("] are allowed.");
		throw new IndexOutOfBoundsException(sb.toString());
	}

	@Override
	public Point getPoint(int pointIndex) throws IndexOutOfBoundsException
	{
		int points = pointCount();
		if (points > 0 && pointIndex >= 0 && pointIndex < points)
		{
			int i=0;
			for (int j=0; j < pathCount(); j++)
			{
				Path path = paths.get(j);
				if (pointIndex > (i+path.size()-1))
					i += path.size();
				else
					return path.getPoint(pointIndex-i);
			}
		}
		StringBuffer sb = new StringBuffer();
		sb.append("Point index " + pointIndex + " is out of bound: ");
		sb.append("Indexes in the range between [0, ").append(points).append("] are allowed.");
		throw new IndexOutOfBoundsException(sb.toString());
	}

	@Override
	public String toJson()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{\"rings\":[");
		for (int i=0; i < pathCount(); i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(paths.get(i).toString());
		}
		sb.append("],\"spatialReference\":{\"wkid\":").append((getSpatialReference() != null) ? getSpatialReference().getWkid() : 4326).append("}}");
		return sb.toString();
	}
}