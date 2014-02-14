package com.esri.ges.processor.serviceAreaCalculator;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.ges.spatial.Envelope;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.Polygon;
import com.esri.ges.spatial.SpatialReference;

public class GeometryAdapter
{
	private SpatialReference sr;
	private boolean enforceServerWKID;

	public GeometryAdapter(int wkid)
	{
		setWkid(wkid);
	}

	public int getWkid()
	{
		return sr.getWkid();
	}
	public void setWkid(int wkid)
	{
		this.sr = new com.esri.ges.processor.serviceAreaCalculator.SpatialReference(wkid);
	}

	public SpatialReference getSpatialReference()
	{
		return sr;
	}

	public com.esri.core.geometry.SpatialReference adapt(SpatialReference sr)
	{
		return com.esri.core.geometry.SpatialReference.create((sr == null) ? this.sr.getWkid() : sr.getWkid());
	}

//	public SpatialReference adapt(com.esri.core.geometry.SpatialReference asr)
//	{
//		return (asr != null) ? new com.esri.ges.spatial.arcgis.SpatialReference(asr.getID()) : new com.esri.ges.spatial.arcgis.SpatialReference(sr.getWkid());
//	}

	public com.esri.core.geometry.Geometry adapt(Geometry g)
	{
		if (g != null)
		{
			switch (g.getType())
			{
			case Point:
				return toArcGISPoint((Point) g);
//			case Polygon:
//				return toArcGISPolygon((Polygon) g);
//			case Envelope:
//				return toArcGISEnvelope((Envelope) g);
//			case Polyline:
//				return toArcGISPolyline((Polyline) g);
//			case MultiPoint:
//				return toArcGISMultiPoint((MultiPoint) g);
			default:
				break;
			}
		}
		return null;
	}

//	public Geometry adapt(com.esri.core.geometry.MapGeometry g)
//	{
//		if (g != null)
//		{
//			int wkid = 4326;
//			if( g.getSpatialReference() != null )
//				wkid = g.getSpatialReference().getID();
//			switch (g.getGeometry().getType())
//			{
//			case POINT:
//				return toPoint((com.esri.core.geometry.Point) g.getGeometry(), wkid );
//			case POLYGON:
//				return toPolygon((com.esri.core.geometry.Polygon) g.getGeometry(), wkid);
//			case ENVELOPE:
//				return toEnvelope((com.esri.core.geometry.Envelope) g.getGeometry(), wkid);
//			case POLYLINE:
//				return toPolyline((com.esri.core.geometry.Polyline) g.getGeometry(), wkid);
//			case MULTIPOINT:
//				return toMultiPoint((com.esri.core.geometry.MultiPoint) g.getGeometry(), wkid);
//			default:
//				break;
//			}
//		}
//		return null;
//	}

	private com.esri.core.geometry.Point toArcGISPoint(Point p)
	{
		com.esri.core.geometry.Point ap = new com.esri.core.geometry.Point(p.getX(), p.getY(), p.getZ());
		//ap.setID(p.getSpatialReference().getWkid());
		return ap;
	}

//	private com.esri.core.geometry.Polygon toArcGISPolygon(Polygon p)
//	{
//		//    com.esri.core.geometry.Polygon ap = new com.esri.core.geometry.Polygon();
//		//    for (int ix=0; ix<p.pathCount(); ix++)
//		//    {
//		//      com.esri.core.geometry.MultiPath path = new com.esri.core.geometry.Polygon();
//		//      int pathStart = p.getPathStart(ix);
//		//      Point startPoint = p.getPoint(pathStart);
//		//      path.startPath(startPoint.getX(), startPoint.getY());
//		//
//		//      int pathSize = p.getPathSize(ix);
//		//      if (pathSize > 1)
//		//      {
//		//        for (int jx=pathStart+1; jx<pathStart+pathSize; jx++)
//		//        {
//		//          Point point = p.getPoint(jx);
//		//          path.lineTo(point.getX(), point.getY());
//		//        }
//		//      }
//		//      path.lineTo(startPoint.getX(), startPoint.getY());
//		//      path.closePathWithLine();
//		//      path.closeAllPaths();
//		//      ap.addPath(path, ix, false);
//		//    }
//		//    return ap;
//
//		// TODO: Code above generates com.esri.core.geometry.Polygon geometry with error
//		// Switched to instantiation of core ags geometry from ges Polygon through json
//		// Need to revisit this to verify if android geometry library has a bug in it
//		try
//		{
//			return (com.esri.core.geometry.Polygon) toArcGISGeometry(p.toJson()).getGeometry();
//		}
//		catch (GeometryException e)
//		{
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private com.esri.core.geometry.Geometry toArcGISPolyline(Polyline g)
//	{
//		try
//		{
//			return (com.esri.core.geometry.Polyline) toArcGISGeometry(g.toJson()).getGeometry();
//		}
//		catch (GeometryException e)
//		{
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private com.esri.core.geometry.Geometry toArcGISMultiPoint(MultiPoint g)
//	{
//		try
//		{
//			return (com.esri.core.geometry.MultiPoint) toArcGISGeometry(g.toJson()).getGeometry();
//		}
//		catch (GeometryException e)
//		{
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private com.esri.core.geometry.Envelope toArcGISEnvelope(Envelope e)
//	{
//		return new com.esri.core.geometry.Envelope(e.getXMin(), e.getYMin(), e.getXMax(), e.getYMax());
//	}
//
//	private Point toPoint(com.esri.core.geometry.Point ap, int wkid)
//	{
//		// TODO Need to change this when we have a better functioning Runtime API that preserves the spatial reference.
//		//return new com.esri.ges.spatial.arcgis.Point(ap.getX(), ap.getY(), ap.getZ(), ap.getID());
//		return new com.esri.ges.spatial.arcgis.Point(ap.getX(), ap.getY(), ap.getZ(), wkid);
//	}
//
//	private Polygon toPolygon(com.esri.core.geometry.Polygon ap, int wkid)
//	{
//		return new com.esri.ges.spatial.arcgis.Polygon(ap, wkid);
//	}
//
//	private Geometry toPolyline(com.esri.core.geometry.Polyline pl, int wkid)
//	{
//		return new com.esri.ges.spatial.arcgis.Polyline(pl, wkid);
//	}
//
//	private Geometry toMultiPoint(com.esri.core.geometry.MultiPoint mp, int wkid)
//	{
//		return new com.esri.ges.spatial.arcgis.MultiPoint(mp, wkid);
//	}
//
//	public Envelope toEnvelope(com.esri.core.geometry.Envelope ae, int wkid)
//	{
//		return new com.esri.ges.spatial.arcgis.Envelope(ae.getXMin(), ae.getYMin(), ae.getXMax(), ae.getYMax(), wkid);
//	}
//
//	private com.esri.core.geometry.MapGeometry toArcGISGeometry(String jsonString) throws GeometryException
//	{
//		try
//		{
//			JsonFactory factory = new JsonFactory();
//			JsonParser jsonParser = factory.createJsonParser(jsonString);
//			jsonParser.nextToken();
//			MapGeometry mapGeometry = GeometryEngine.jsonToGeometry(jsonParser);
//			if (mapGeometry == null)
//				throw new GeometryException("Geometry engine failed to parse a json into Geometry object: " + jsonString);
//			//			com.esri.core.geometry.Geometry geom = mapGeometry.getGeometry();
//			if( enforceServerWKID )
//			{
//				com.esri.core.geometry.SpatialReference mapSR = mapGeometry.getSpatialReference();
//				int wkid = getSpatialReference().getWkid();
//				if (mapSR != null && mapSR.getID() != wkid)
//				{
//					com.esri.core.geometry.SpatialReference targetSR = com.esri.core.geometry.SpatialReference.create(wkid);
//					mapGeometry.setGeometry( GeometryEngine.project(mapGeometry.getGeometry(), mapSR, targetSR) );
//					mapGeometry.setSpatialReference(targetSR);
//				}
//			}
//			return mapGeometry;
//		}
//		catch (Exception e)
//		{
//			throw new GeometryException(e.getMessage());
//		}
//	}
//
//	public Geometry toGeometry(String jsonString) throws GeometryException
//	{
//		try
//		{
//			MapGeometry mg = toArcGISGeometry(jsonString);
//			if( mg.getSpatialReference() == null )
//				mg.setSpatialReference(adapt(sr));
//			return adapt(mg);
//		}
//		catch(Exception e)
//		{
//			throw new GeometryException(e.getMessage());
//		}
//	}
//
//	public static void main (String[] args)
//	{
//		try
//		{
//			GeometryAdapter adapter = new GeometryAdapter(3857);
//			Geometry g = adapter.toGeometry("{\"rings\":[[[70.6203893249252,34.59273088513312],[70.64534569632487,34.569248373742326],[70.59899814943981,34.55261091451122],[70.58354896714468,34.58001201502622],[70.6203893249252,34.59273088513312]]],\"spatialReference\":{\"wkid\":4326}}");
//			System.out.println(g.getSpatialReference().getWkid());
//		}
//		catch (GeometryException e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	public String toJson(int wkid, Geometry geometry)
//	{
//		return GeometryEngine.geometryToJson(wkid, adapt(geometry));
//	}

	public String toJson(int wkid, com.esri.core.geometry.Geometry geometry) throws GeometryException
	{
		return GeometryEngine.geometryToJson(wkid, geometry);
	}

	public void setEnforceServerWKID(boolean enforceServerWKID)
	{
		this.enforceServerWKID = enforceServerWKID;
	}

}