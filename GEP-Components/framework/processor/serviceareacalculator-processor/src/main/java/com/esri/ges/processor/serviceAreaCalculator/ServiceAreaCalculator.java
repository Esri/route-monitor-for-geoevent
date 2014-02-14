package com.esri.ges.processor.serviceAreaCalculator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.GeometryEngine;
import com.esri.ges.core.aoi.Aoi;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.manager.aoi.AoiManager;
import com.esri.ges.manager.aoi.AoiManagerException;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.stops.GeoFenceResource;
import com.esri.ges.manager.stops.Stop;
import com.esri.ges.manager.stops.StopResource;
import com.esri.ges.manager.stops.StopStatus;
import com.esri.ges.manager.stops.StopsManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.GeometryType;
import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.Polygon;
import com.esri.ges.spatial.Spatial;

public class ServiceAreaCalculator extends GeoEventProcessorBase
{
  private static final Log LOG            = LogFactory.getLog(ServiceAreaCalculator.class);

  private  StopsManager stopsManager;
  private String naConnectionName;
  private String path;
  private int driveTime;
  private String serviceAreaType;
  private double bufferDistance;
  private ArcGISServerConnectionManager agsConnectionManager;
  private AoiManager aoiManager;
  private GeoEventCreator geoEventCreator;
  private Spatial spatial;
  private String geofenceGEDName;
  private String geofenceGEDOwner;

  protected ServiceAreaCalculator(GeoEventProcessorDefinition definition, StopsManager stopsManager, ArcGISServerConnectionManager agsConnectionManager, AoiManager aoiManager, GeoEventCreator geoEventCreator, Spatial spatial, String geofenceGEDName, String geofenceGEDOwner) throws ComponentException
  {
    super(definition);
    this.stopsManager = stopsManager;
    this.agsConnectionManager = agsConnectionManager;
    this.aoiManager = aoiManager;
    this.geoEventCreator = geoEventCreator;
    this.spatial = spatial;
    this.geofenceGEDName = geofenceGEDName;
    this.geofenceGEDOwner = geofenceGEDOwner;
  }

  @Override
  public GeoEvent process(GeoEvent geoEvent) throws Exception
  {
    GeoEvent newGeoEvent = processGeoEvent(geoEvent);
    return newGeoEvent;
  }
  
  @Override
  public void afterPropertiesSet()
  {
    super.afterPropertiesSet();
    serviceAreaType = getProperty( ServiceAreaCalculatorDefinition.SERVICE_AREA_TYPE_PROPERTY ).getValueAsString();
    if(serviceAreaType.equals(ServiceAreaCalculatorDefinition.SERVICE_AREA_TYPE_BUFFER))
      bufferDistance = Double.parseDouble(getProperty( ServiceAreaCalculatorDefinition.BUFFER_DISTANCE_PROPERTY ).getValueAsString());
    else
    {
      naConnectionName = getProperty( ServiceAreaCalculatorDefinition.NA_CONNECTION_PROPERTY ).getValueAsString();
      path = getProperty( ServiceAreaCalculatorDefinition.NA_PATH_PROPERTY ).getValueAsString();
      driveTime = Integer.parseInt(getProperty( ServiceAreaCalculatorDefinition.DRIVE_TIME_PROPERTY ).getValueAsString());
    }
  }

  protected GeoEvent processGeoEvent(GeoEvent geoEvent)
  {
    String stopName = (String)geoEvent.getField( StopResource.STOP_NAME_KEY );
    Point point = getLocation(geoEvent);
    if(point == null)
      return null;
    
    Aoi aoi = aoiManager.getAoi(stopsManager.getStopsAoiCategory(), stopName);
    if(aoi != null)
    {
      Stop oldStop = stopsManager.getStopByName(stopName);
      if(oldStop != null)
      {
        String newStatus = (String)geoEvent.getField(StopResource.STATUS_KEY);
        if(newStatus != null)
          if(newStatus.equals(StopStatus.AtStop.toString()) || newStatus.equals(StopStatus.Completed.toString())
              || newStatus.equals(StopStatus.Exception.toString()))
              return null;
        if((oldStop.getLocation().getX()==point.getX() && oldStop.getLocation().getY()==point.getY()))
          return null;
        stopsManager.convertGeoEventToStop(geoEvent, oldStop);
        if(oldStop.getStatus()==StopStatus.Canceled)
          return null;
      }
      else
      {
        Stop stop = stopsManager.createStop(stopName);
        stopsManager.convertGeoEventToStop(geoEvent, stop);
        if(stop.getStatus()==StopStatus.Canceled)
          return null;
      }
      aoiManager.deleteAoi(stopsManager.getStopsAoiCategory(), stopName);
    }
    
    if(serviceAreaType.equals(ServiceAreaCalculatorDefinition.SERVICE_AREA_TYPE_BUFFER))
      return createBufferForStop(bufferDistance, stopName, point);
    else
      return createAoiForStop(naConnectionName, path, driveTime, stopName, point);
  }
  
  protected GeoEvent createAoiForStop(String naConnectionName, String areaSolverPath, int driveTime, String stopName, Point point)
  {
    Aoi aoi = null;
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection( naConnectionName );
    if( agsConnection != null )
    {
      Geometry aoiGeom = agsConnection.getAreaAroundPoint( areaSolverPath, point, driveTime );
      List<Geometry> geometries = new ArrayList<Geometry>();
      geometries.add( aoiGeom );
      try
      {
        aoi = aoiManager.addAoi(stopsManager.getStopsAoiCategory(), stopName, geometries, true );
        // Now that we've got an instantiated Aoi, delete it from the manager so that it can create 
        // one based on the sync rule
        aoiManager.deleteAoi( aoi.getCategory(), aoi.getName() );
      }
      catch (AoiManagerException e)
      {
        LOG.error( "Unable to add Aoi for Stop "+ stopName, e);
      }
    }
    else
    {
      LOG.error( "Could not find ArcGISServer Connection "+naConnectionName );
    }

    return createGeoEventForAoi(aoi);
  }
  
  protected GeoEvent createBufferForStop(double bufferDistance, String stopName, Point point)
  {
    GeometryAdapter adapter = new GeometryAdapter(point.getSpatialReference().getWkid());
    Aoi aoi = null;

      Geometry aoiGeom = buffer(adapter, point, feetToMeters(bufferDistance));
      List<Geometry> geometries = new ArrayList<Geometry>();
      geometries.add( aoiGeom );
      try
      {
        aoi = aoiManager.addAoi(stopsManager.getStopsAoiCategory(), stopName, geometries, true );
        // Now that we've got an instantiated Aoi, delete it from the manager so that it can create 
        // one based on the sync rule
        aoiManager.deleteAoi( aoi.getCategory(), aoi.getName() );
      }
      catch (AoiManagerException e)
      {
        LOG.error( "Unable to add Aoi for Stop "+ stopName, e);
      }

    return createGeoEventForAoi(aoi);
  }
  
  // buffer function will eventually be made into the spatial library. 
  private synchronized Polygon buffer(GeometryAdapter adapter, Geometry g1, double distance)
  {
    com.esri.core.geometry.Geometry ag1 = adapter.adapt(g1);
    com.esri.core.geometry.SpatialReference webMercator = com.esri.core.geometry.SpatialReference.create(3857);
    if (g1.getSpatialReference().getWkid() != 3857)
      ag1 = GeometryEngine.project(ag1, adapter.adapt(g1.getSpatialReference()), webMercator);
    com.esri.core.geometry.Polygon buffer = GeometryEngine.buffer(ag1, webMercator, distance, webMercator.getUnit());

    com.esri.core.geometry.Geometry projectedResult = GeometryEngine.project(buffer, webMercator, adapter.adapt(g1.getSpatialReference()));
    Polygon result = new com.esri.ges.processor.serviceAreaCalculator.Polygon((com.esri.core.geometry.Polygon) projectedResult, g1.getSpatialReference().getWkid());
    return result;

  }
  
  private GeoEvent createGeoEventForAoi( Aoi aoi)
  {
    try
    {
      GeoEvent geoEvent = geoEventCreator.create( geofenceGEDName, geofenceGEDOwner );
      // All of our Aoi's only have one geometry.
      Geometry geom = aoi.getGeometries().get( 0 );
      geoEvent.setGeometry( geom );
      geoEvent.setField( GeoFenceResource.GEOFENCE_ID_KEY, aoi.getId() );
      geoEvent.setField( GeoFenceResource.CATEGORY_KEY, aoi.getCategory() );
      geoEvent.setField( GeoFenceResource.GEOFENCE_NAME_KEY, aoi.getName() );
      geoEvent.setField( GeoFenceResource.ACTIVE_KEY, Boolean.toString( aoi.isActive() ) );
      geoEvent.setField( GeoFenceResource.SHAPE_KEY, geom.toJson() );
      return geoEvent;
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }
  
  private double feetToMeters(double feet)
  {
    return feet * 0.3048;
  }
  
  private Point getLocation(GeoEvent geoEvent)
  {
    Point pt = null;

    String ptString = null;
    if(geoEvent.getGeometry() != null)
    {
      ptString = geoEvent.getGeometry().toString();
    }
    
    if( ptString != null )
    {
      try
      {
        Geometry geom = spatial.fromJson( ptString );
        if( geom.getType() == GeometryType.Point )
        {
          pt = (Point)geom;
        }
      }
      catch (GeometryException e)
      {
        throw new RuntimeException( e );
      }
    }
    return pt;
  }
}
