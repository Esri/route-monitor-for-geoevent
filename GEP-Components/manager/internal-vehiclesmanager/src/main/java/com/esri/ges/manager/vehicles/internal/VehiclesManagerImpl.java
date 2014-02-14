package com.esri.ges.manager.vehicles.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;

import com.esri.ges.core.Uri;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.resource.Resource;
import com.esri.ges.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.datastore.agsconnection.ArcGISServerType;
import com.esri.ges.datastore.agsconnection.Layer;
import com.esri.ges.jaxb.resource.ResourceAttributeWrapper;
import com.esri.ges.jaxb.resource.ResourceWrapper;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.resource.ResourceManager;
import com.esri.ges.manager.resource.ResourceManagerException;
import com.esri.ges.manager.routemonitor.util.FeatureUtil;
import com.esri.ges.manager.vehicles.DefaultVehicle;
import com.esri.ges.manager.vehicles.Vehicle;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.Spatial;
import com.esri.ges.util.Validator;
import com.esri.ges.manager.vehicles.VehicleResource;

public class VehiclesManagerImpl implements VehiclesManager
{
  final private static Log log = LogFactory.getLog( VehiclesManagerImpl.class );
  private ResourceManager resourceManager;
  private Map<String,VehicleResource> vehicleMap = new ConcurrentHashMap<String,VehicleResource>();
  private Spatial spatial;
  private GeoEventCreator geoEventCreator;
  private Messaging messaging;
  private ArcGISServerConnectionManager agsConnectionManager;
  private String vehicleGEDName;
  private String vehicleGEDOwner;
  
  public void setVehicleGEDName(String vehicleGEDName)
  {
    this.vehicleGEDName = vehicleGEDName;
  }

  public void setVehicleGEDOwner(String vehicleGEDOwner)
  {
    this.vehicleGEDOwner = vehicleGEDOwner;
  }

  public void setArcGISServerConnectionManager( ArcGISServerConnectionManager manager )
  {
    this.agsConnectionManager = manager;
  }
  
  public void setMessaging( Messaging messaging )
  {
    this.messaging = messaging;
    geoEventCreator = messaging.createGeoEventCreator();
  }

  public void setSpatial( Spatial spatial )
  {
    this.spatial = spatial;
  }
  
  public void setResourceManager( ResourceManager resourceManager )
  {
    this.resourceManager = resourceManager;
  }

  @Override
  public List<Vehicle> getVehicles()
  {
    return new ArrayList<Vehicle>( vehicleMap.values() );
  }

  @Override
  public Vehicle getVehicleByName(String name)
  {
    return vehicleMap.get( name );
  }
  
  private void updateResourceWithVehicleInfo( Resource resource, Vehicle vehicle )
  {
    try
    {
      VehicleResource.updateResourceWithVehicleInformation( resource, vehicle );
      resourceManager.updateResource( resource );
    }
    catch (ResourceManagerException e)
    {
      throw new RuntimeException( e );
    }
  }
  
  private Resource addVehicleToResourceManager( Vehicle vehicle )
  {
    ResourceWrapper wrapper = new ResourceWrapper();
    wrapper.setFirstName( vehicle.getVehicleName() );
    wrapper.setLastName( "Vehicle" );
    wrapper.setType( "Vehicle" );
    wrapper.setDescription( "Vehicle Resource created by MRM extension." );
    List<ResourceAttributeWrapper> attributes = new ArrayList<ResourceAttributeWrapper>();
    attributes.add( new ResourceAttributeWrapper( VehicleResource.LAST_UPDATED_KEY, (vehicle.getLastUpdated() == null ) ? null : Long.toString( vehicle.getLastUpdated().getTime() ) ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.GEOMETRY_KEY, (vehicle.getLocation() == null ) ? null : vehicle.getLocation().toJson() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.DEVICE_ID_KEY, vehicle.getDeviceId() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.DEVICE_TYPE_KEY, vehicle.getDeviceType() )  );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.VEHICLE_GROUP_NAME_KEY, vehicle.getVehicleGroupName() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.CAPACITY_KEY, vehicle.getCapacity() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.SPECIALTIES_KEY, vehicle.getSpecialties() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.FIXED_COST_KEY, (vehicle.getFixedCost() == null ) ? null : vehicle.getFixedCost().toString() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.FUEL_ECONOMY_KEY, (vehicle.getFuelEconomy() == null ) ? null : vehicle.getFuelEconomy().toString() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.FUEL_TYPE_KEY, vehicle.getFuelType() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.SPEED_KEY, (vehicle.getSpeed() == null ) ? null : vehicle.getSpeed().toString() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.PANIC_KEY, vehicle.getPanic() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.NOTE_KEY, vehicle.getNote() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.CUMULATIVE_MINUTES_KEY, (vehicle.getCumulativeMinutes() == null ) ? null : vehicle.getCumulativeMinutes().toString() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.EVENT_NAME_KEY, vehicle.getEventName() ) );
    attributes.add( new ResourceAttributeWrapper( VehicleResource.NEXT_STOP_SEQUENCE_NUMBER_KEY, (vehicle.getNextStopSequenceNumber() == null ) ? null : vehicle.getNextStopSequenceNumber().toString() ) );
    wrapper.setAttributes(attributes);
    try
    {
      return resourceManager.createNewResource( wrapper );
    }
    catch (ResourceManagerException e)
    {
      throw new RuntimeException( e );
    }
  }

  @Override
  public void addOrReplaceVehicle(Vehicle vehicle)
  {
    Resource backingResource = null;
    
    if( vehicleMap.containsKey( vehicle.getVehicleName() ) )
    {
      backingResource = vehicleMap.get( vehicle.getVehicleName() ).getResource();
      updateResourceWithVehicleInfo( backingResource, vehicle );      
    }
    else
    {
      backingResource = addVehicleToResourceManager( vehicle );
    }
    vehicleMap.put( vehicle.getVehicleName(), new VehicleResource( backingResource, spatial ) );  
  }

  @Override
  public void removeAllVehicles()
  {
    Set<String> vehicleKeys = vehicleMap.keySet();
    if( !Validator.isEmpty( vehicleKeys ) )
    {
      VehicleResource vehicle;
      for( String key : vehicleKeys )
      {
        vehicle = vehicleMap.remove( key );
        resourceManager.deleteResource( vehicle.getResource().getId() );
      }
    }
  }

  @Override
  public GeoEvent createGeoEvent(Vehicle vehicle, String ownerId, Uri uri)
  {
    if( geoEventCreator == null )
    {
      geoEventCreator = messaging.createGeoEventCreator();
      if( geoEventCreator == null )
      {
        throw new RuntimeException( "Could not instantiate a GeoEventCreator." );
      }
    }
    GeoEvent event;
    try
    {
      event = geoEventCreator.create( vehicleGEDName, vehicleGEDOwner );
      event.setField( VehicleResource.LAST_UPDATED_KEY, vehicle.getLastUpdated() );
      event.setField( VehicleResource.DEVICE_ID_KEY, vehicle.getDeviceId() );
      event.setField( VehicleResource.DEVICE_TYPE_KEY, vehicle.getDeviceType() );
      event.setField( VehicleResource.VEHICLE_NAME_KEY, vehicle.getVehicleName() );
      event.setField( VehicleResource.VEHICLE_GROUP_NAME_KEY, vehicle.getVehicleGroupName() );
      event.setField( VehicleResource.CAPACITY_KEY, vehicle.getCapacity() );
      event.setField( VehicleResource.SPECIALTIES_KEY, vehicle.getSpecialties() );
      event.setField( VehicleResource.FIXED_COST_KEY, vehicle.getFixedCost() );
      event.setField( VehicleResource.FUEL_ECONOMY_KEY, vehicle.getFuelEconomy() );
      event.setField( VehicleResource.FUEL_TYPE_KEY, vehicle.getFuelType() );
      event.setField( VehicleResource.SPEED_KEY, vehicle.getSpeed() );
      event.setField( VehicleResource.PANIC_KEY, vehicle.getPanic() );
      event.setField( VehicleResource.NOTE_KEY, vehicle.getNote() );
      event.setField( "shape", vehicle.getLocation().toJson() );
      event.setGeometry( vehicle.getLocation() );
      event.setProperty(GeoEventPropertyName.TYPE, "event");
      event.setProperty(GeoEventPropertyName.OWNER_ID, ownerId);
      event.setProperty(GeoEventPropertyName.OWNER_URI, uri);
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
    return event;
  }

  @Override
  public Resource getResourceForVehicle(String name)
  {
    VehicleResource vehicleResource = vehicleMap.get( name );
    Resource retResource = null;
    if( vehicleResource != null )
    {
      retResource = vehicleResource.getResource();
    }
    return retResource;
  }

  @Override
  public void clearAllVehicleFeatures(String agsConnectionName, String path, String featureService, String layer)
  {
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection(agsConnectionName);
    Layer lyr =  agsConnection.getLayer(path, featureService, layer, ArcGISServerType.FeatureServer);
    agsConnection.deleteAllRecordsFromLayer(path , featureService, lyr.getId());
    removeAllVehicles();
  }

  @Override
  public List<Vehicle> reloadVehicles(String agsConnectionName, String path, String featureService, String layer)
  {
    removeAllVehicles();
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection(agsConnectionName);
    Layer lyr =  agsConnection.getLayer(path, featureService, layer, ArcGISServerType.FeatureServer);
    try
    {
      List<JsonNode> nodes = agsConnection.getAllFeatures(path, featureService, lyr.getId(), "1=1", "*", true, ArcGISServerType.FeatureServer, 0);
      List<Vehicle> vehicles = parseFeaturesToVehicles(nodes);
      for(Vehicle vehicle : vehicles)
      {
        addOrReplaceVehicle(vehicle);
      }
    }
    catch (IOException e)
    {
      log.error(e);
    }
    return getVehicles();
  }
  
  private List<Vehicle> parseFeaturesToVehicles(List<JsonNode> nodes)
  {
    GeoEvent geoEvent;
    List <Vehicle> vehicles = new ArrayList<Vehicle>();
    try
    {
      geoEvent = geoEventCreator.create(vehicleGEDName, vehicleGEDOwner);
      List<GeoEvent> geoEvents = FeatureUtil.convertFeaturesToGeoEvents(nodes, spatial, geoEventCreator, geoEvent.getGeoEventDefinition());
      for(GeoEvent ge : geoEvents)
      {
        Vehicle vehicle = createVehicleFromGeoEvent( ge );
        vehicles.add(vehicle);
      }
    }
    catch (MessagingException e)
    {
      log.error(e);
    }
    
    return vehicles;
  }
  
  public Vehicle createVehicleFromGeoEvent(GeoEvent geoEvent)
  {
    DefaultVehicle vehicle = new DefaultVehicle();
    vehicle.setCumulativeMinutes((Double) geoEvent.getField(VehicleResource.CUMULATIVE_MINUTES_KEY));
    vehicle.setDeviceId((String) geoEvent.getField(VehicleResource.DEVICE_ID_KEY));
    vehicle.setDeviceType((String) geoEvent.getField(VehicleResource.DEVICE_TYPE_KEY));
    vehicle.setEventName((String) geoEvent.getField(VehicleResource.EVENT_NAME_KEY));
    vehicle.setLastUpdated((Date) geoEvent.getField(VehicleResource.LAST_UPDATED_KEY));
    Object vehicleLocationObject = geoEvent.getField(VehicleResource.GEOMETRY_KEY);
    if (vehicleLocationObject != null)
    {
      Geometry vehicleLocation = geometryFromAttribute(vehicleLocationObject);
      if (vehicleLocation != null && vehicleLocation instanceof Point)
      {
        vehicle.setLocation((Point) vehicleLocation);
      }
    }
    vehicle.setNextStopSequenceNumber((Integer) geoEvent.getField(VehicleResource.NEXT_STOP_SEQUENCE_NUMBER_KEY));
    vehicle.setPanic((String) geoEvent.getField(VehicleResource.PANIC_KEY));
    vehicle.setSpeed((Double) geoEvent.getField(VehicleResource.SPEED_KEY));
    vehicle.setVehicleGroupName((String) geoEvent.getField(VehicleResource.VEHICLE_GROUP_NAME_KEY));
    vehicle.setVehicleName((String) geoEvent.getField(VehicleResource.VEHICLE_NAME_KEY));
    vehicle.setFixedCost((Double) geoEvent.getField(VehicleResource.FIXED_COST_KEY));
    vehicle.setFuelEconomy((Double) geoEvent.getField(VehicleResource.FUEL_ECONOMY_KEY));
    vehicle.setFuelType((String) geoEvent.getField(VehicleResource.FUEL_TYPE_KEY));
    vehicle.setCapacity((String) geoEvent.getField(VehicleResource.CAPACITY_KEY));
    vehicle.setSpecialties((String) geoEvent.getField(VehicleResource.SPECIALTIES_KEY));
    return vehicle;
  }
  
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
        log.info("Failed to convert string to Point.", e);
      }
    }
    else if( locationAttribute instanceof Geometry )
    {
      retGeom = (Geometry)locationAttribute;
    }
    return retGeom;
  }

}
