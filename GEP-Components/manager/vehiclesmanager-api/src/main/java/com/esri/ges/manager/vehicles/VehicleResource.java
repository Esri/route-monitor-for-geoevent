package com.esri.ges.manager.vehicles;

import java.util.Date;

import com.esri.ges.core.resource.Resource;
import com.esri.ges.manager.resource.ResourceBackedAttributeMap;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.GeometryType;
import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.Spatial;

public class VehicleResource extends ResourceBackedAttributeMap implements Vehicle
{
  final public static String LAST_UPDATED_KEY = "TIME_START"; 
  final public static String GEOMETRY_KEY = "GEOMETRY";
  final public static String DEVICE_ID_KEY = "DEVICE_ID";
  final public static String DEVICE_TYPE_KEY = "DEVICE_TYPE";
  final public static String VEHICLE_NAME_KEY = "VEHICLE_NAME";
  final public static String VEHICLE_GROUP_NAME_KEY = "VEHICLE_GROUP_NAME";
  final public static String CAPACITY_KEY = "CAPACITY";
  final public static String SPECIALTIES_KEY = "SPECIALTIES";
  final public static String FIXED_COST_KEY = "FIXED_COST";
  final public static String FUEL_ECONOMY_KEY = "FUEL_ECONOMY";
  final public static String FUEL_TYPE_KEY = "FUEL_TYPE";
  final public static String SPEED_KEY = "SPEED";
  final public static String PANIC_KEY = "PANIC";
  final public static String CUMULATIVE_MINUTES_KEY = "CUMULATIVE_MINUTES";
  final public static String EVENT_NAME_KEY = "EVENT_NAME";
  final public static String NEXT_STOP_SEQUENCE_NUMBER_KEY = "NEXT_STOP_SEQUENCE_NUMBER";
  final public static String NOTE_KEY = "NOTE";

  public VehicleResource( Resource resource, Spatial spatial )
  {
    super( resource, spatial );
  }
  
  public static void updateResourceWithVehicleInformation( Resource resource, Vehicle vehicle )
  {
    setDoubleAttribute( resource, CUMULATIVE_MINUTES_KEY, vehicle.getCumulativeMinutes() );
    setAttribute( resource, CAPACITY_KEY, vehicle.getCapacity() );
    setAttribute( resource, DEVICE_ID_KEY, vehicle.getDeviceId() );
    setAttribute( resource, DEVICE_TYPE_KEY, vehicle.getDeviceType() );
    setAttribute( resource, EVENT_NAME_KEY, vehicle.getEventName() );
    setDoubleAttribute( resource, FIXED_COST_KEY, vehicle.getFixedCost());
    setDoubleAttribute( resource, FUEL_ECONOMY_KEY, vehicle.getFuelEconomy() );
    setAttribute( resource, FUEL_TYPE_KEY, vehicle.getFuelType() );
    setGeometryAttribute( resource, GEOMETRY_KEY, vehicle.getLocation() );
    setDateAttribute( resource, LAST_UPDATED_KEY, vehicle.getLastUpdated() );
    setIntegerAttribute( resource, NEXT_STOP_SEQUENCE_NUMBER_KEY, vehicle.getNextStopSequenceNumber() );
    setAttribute( resource, NOTE_KEY, vehicle.getNote() );
    setAttribute( resource, PANIC_KEY, vehicle.getPanic() );
    setAttribute( resource, SPECIALTIES_KEY, vehicle.getSpecialties() );
    setDoubleAttribute( resource, SPEED_KEY, vehicle.getSpeed() );
    setAttribute( resource, VEHICLE_GROUP_NAME_KEY, vehicle.getVehicleGroupName() );
  }

  @Override
  public void setLastUpdated(Date date)
  {
    setDateAttribute( resource, LAST_UPDATED_KEY,  date );
  }

  @Override
  public Date getLastUpdated()
  {
    return getDateAttribute( LAST_UPDATED_KEY );
  }

  @Override
  public void setLocation(Point point)
  {
    setGeometryAttribute( resource, GEOMETRY_KEY, point );
  }

  @Override
  public Point getLocation()
  {
    Point pt = null;
    String ptString = resource.getAttribute( GEOMETRY_KEY );
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

  @Override
  public void setDeviceId(String deviceId)
  {
    setAttribute( resource, DEVICE_ID_KEY, deviceId );
  }

  @Override
  public String getDeviceId()
  {
    return resource.getAttribute( DEVICE_ID_KEY );
  }

  @Override
  public void setDeviceType(String deviceType)
  {
    setAttribute( resource, DEVICE_TYPE_KEY, deviceType );
  }

  @Override
  public String getDeviceType()
  {
    return resource.getAttribute( DEVICE_TYPE_KEY );
  }

  @Override
  public String getVehicleName()
  {
    return resource.getFirstName();
  }

  @Override
  public void setVehicleGroupName(String vehicleGropuName)
  {
    setAttribute( resource, VEHICLE_GROUP_NAME_KEY, vehicleGropuName );
  }

  @Override
  public String getVehicleGroupName()
  {
    return resource.getAttribute( VEHICLE_GROUP_NAME_KEY );
  }

  @Override
  public String getCapacity()
  {
    return resource.getAttribute( CAPACITY_KEY );
  }

  @Override
  public String getSpecialties()
  {
    return resource.getAttribute( SPECIALTIES_KEY );
  }

  @Override
  public Double getFixedCost()
  {
    return getDoubleAttribute( FIXED_COST_KEY );
  }

  @Override
  public Double getFuelEconomy()
  {
    return getDoubleAttribute( FUEL_ECONOMY_KEY );
  }

  @Override
  public String getFuelType()
  {
    return resource.getAttribute( FUEL_TYPE_KEY );
  }

  @Override
  public void setSpeed(Double speed)
  {
    setDoubleAttribute( resource, SPEED_KEY, speed );
  }

  @Override
  public Double getSpeed()
  {
    return getDoubleAttribute( SPEED_KEY );
  }

  @Override
  public void setPanic(String panic)
  {
    setAttribute( resource, PANIC_KEY, panic ); 
  }

  @Override
  public String getPanic()
  {
    return resource.getAttribute( PANIC_KEY );
  }

  @Override
  public void setCumulativeMinutes(Double cumulativeMinutes)
  {
    setDoubleAttribute( resource, CUMULATIVE_MINUTES_KEY, cumulativeMinutes );
  }

  @Override
  public Double getCumulativeMinutes()
  {
    return getDoubleAttribute( CUMULATIVE_MINUTES_KEY );
  }

  @Override
  public void setEventName(String eventName)
  {
    setAttribute( resource, EVENT_NAME_KEY, eventName );
  }

  @Override
  public String getEventName()
  {
    return resource.getAttribute( EVENT_NAME_KEY );
  }

  @Override
  public void setNextStopSequenceNumber(Integer nextStopSequenceNumber)
  {
    setIntegerAttribute( resource, NEXT_STOP_SEQUENCE_NUMBER_KEY, nextStopSequenceNumber );
  }

  @Override
  public Integer getNextStopSequenceNumber()
  {
    return getIntegerAttribute( NEXT_STOP_SEQUENCE_NUMBER_KEY );
  }

  @Override
  public void setNote(String note)
  {
    setAttribute( resource, NOTE_KEY, note );
  }

  @Override
  public String getNote()
  {
    return resource.getAttribute( NOTE_KEY );
  }
}