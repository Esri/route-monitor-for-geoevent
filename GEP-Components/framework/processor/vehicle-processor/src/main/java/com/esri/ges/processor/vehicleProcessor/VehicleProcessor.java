package com.esri.ges.processor.vehicleProcessor;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.property.Property;
import com.esri.ges.manager.vehicles.DefaultVehicle;
import com.esri.ges.manager.vehicles.Vehicle;
import com.esri.ges.manager.vehicles.VehicleResource;
import com.esri.ges.manager.vehicles.VehiclesManager;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.Spatial;

public class VehicleProcessor extends GeoEventProcessorBase
{
  private static final Log LOG            = LogFactory.getLog(VehicleProcessor.class);

  private VehiclesManager vehiclesManager;
  private Spatial spatial;

  protected VehicleProcessor(GeoEventProcessorDefinition definition, VehiclesManager vehiclesManager, Spatial spatial) throws ComponentException
  {
    super(definition);
    this.vehiclesManager = vehiclesManager;
    this.spatial = spatial;
  }

  @Override
  public GeoEvent process(GeoEvent geoEvent) throws Exception
  {
    processGeoEvent(geoEvent);
    return geoEvent;
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(definition.getName());
    sb.append("/");
    sb.append(definition.getVersion());
    sb.append("[");
    for (Property p : getProperties())
    {
      sb.append(p.getDefinition().getPropertyName());
      sb.append(":");
      sb.append(p.getValue());
      sb.append(" ");
    }
    sb.append("]");
    return sb.toString();
  }

  protected void processGeoEvent(GeoEvent geoEvent)
  {
    Vehicle vehicle = createVehicleFromGeoEvent(geoEvent);
//    vehicle.setNextStopSequenceNumber(1);
    vehiclesManager.addOrReplaceVehicle(vehicle);
  }

  private Vehicle createVehicleFromGeoEvent(GeoEvent geoEvent)
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
