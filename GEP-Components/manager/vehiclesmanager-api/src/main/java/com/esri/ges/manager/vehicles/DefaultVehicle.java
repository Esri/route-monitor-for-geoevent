package com.esri.ges.manager.vehicles;

import java.util.Date;

import com.esri.ges.spatial.Point;

public class DefaultVehicle implements Vehicle
{
  private Integer nextStopSequenceNumber;
  private String eventName;
  private Double cumulativeMinutes;
  private String panic;
  private Double speed;
  private String fuelType;
  private Double fuelEconomy;
  private Double fixedCost;
  private String specialties;
  private String capacity;
  private String vehicleGroupName;
  private String vehicleName;
  private String deviceType;
  private String deviceId;
  private Point location;
  private Date lastUpdated;
  private String note;

  @Override
  public Date getLastUpdated()
  {
    return lastUpdated;
  }

  @Override
  public Point getLocation()
  {
    return location;
  }

  @Override
  public String getDeviceId()
  {
    return deviceId;
  }

  @Override
  public String getDeviceType()
  {
    return deviceType;
  }

  @Override
  public String getVehicleName()
  {
    return vehicleName;
  }

  @Override
  public String getVehicleGroupName()
  {
    return vehicleGroupName;
  }

  @Override
  public String getCapacity()
  {
    return capacity;
  }

  @Override
  public String getSpecialties()
  {
    return specialties;
  }

  @Override
  public Double getFixedCost()
  {
    return fixedCost;
  }

  @Override
  public Double getFuelEconomy()
  {
    return fuelEconomy;
  }

  @Override
  public String getFuelType()
  {
    return fuelType;
  }

  @Override
  public Double getSpeed()
  {
    return speed;
  }

  @Override
  public String getPanic()
  {
    return panic;
  }

  @Override
  public Double getCumulativeMinutes()
  {
    return cumulativeMinutes;
  }

  @Override
  public String getEventName()
  {
    return eventName;
  }

  @Override
  public Integer getNextStopSequenceNumber()
  {
    return nextStopSequenceNumber;
  }

  @Override
  public String getNote()
  {
    return note;
  }

  @Override
  public void setLastUpdated(Date date)
  {
    this.lastUpdated = date;
  }

  @Override
  public void setLocation(Point point)
  {
    this.location = point;
  }

  @Override
  public void setDeviceId(String deviceId)
  {
    this.deviceId = deviceId;
  }

  @Override
  public void setDeviceType(String deviceType)
  {
    this.deviceType = deviceType;
  }

  @Override
  public void setVehicleGroupName(String vehicleGropuName)
  {
    this.vehicleGroupName = vehicleGropuName;
  }

  @Override
  public void setSpeed(Double speed)
  {
    this.speed = speed;
  }

  @Override
  public void setPanic(String panic)
  {
    this.panic = panic;
  }

  @Override
  public void setCumulativeMinutes(Double cumulativeMinutes)
  {
    this.cumulativeMinutes = cumulativeMinutes;
  }

  @Override
  public void setEventName(String eventName)
  {
    this.eventName = eventName;
  }

  @Override
  public void setNextStopSequenceNumber(Integer nextStopSequenceNumber)
  {
    this.nextStopSequenceNumber = nextStopSequenceNumber;
  }
  
  public void setVehicleName( String vehicleName )
  {
    this.vehicleName = vehicleName;
  }

  @Override
  public void setNote(String note)
  {
    this.note = note;
  }

  public void setFuelType(String fuelType)
  {
    this.fuelType = fuelType;
  }

  public void setFuelEconomy(Double fuelEconomy)
  {
    this.fuelEconomy = fuelEconomy;
  }

  public void setFixedCost(Double fixedCost)
  {
    this.fixedCost = fixedCost;
  }

  public void setSpecialties(String specialties)
  {
    this.specialties = specialties;
  }

  public void setCapacity(String capacity)
  {
    this.capacity = capacity;
  }
  
}
