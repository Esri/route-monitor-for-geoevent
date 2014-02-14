package com.esri.ges.manager.vehicles;

import java.util.Date;

import com.esri.ges.spatial.Point;

public interface Vehicle
{
  public void setLastUpdated( Date date );
  public Date getLastUpdated();
  public void setLocation( Point point );
  public Point getLocation();
  public void setDeviceId( String deviceId );  
  public String getDeviceId();
  public void setDeviceType( String deviceType );
  public String getDeviceType();
  public String getVehicleName();
  public void setVehicleGroupName( String vehicleGropuName );
  public String getVehicleGroupName();
  public String getCapacity();
  public String getSpecialties();
  public Double getFixedCost();
  public Double getFuelEconomy();
  public String getFuelType();
  public void setSpeed( Double speed );
  public Double getSpeed();
  public void setPanic( String panic );
  public String getPanic();
  public void setCumulativeMinutes( Double cumulativeMinutes );
  public Double getCumulativeMinutes();
  public void setEventName( String eventName );
  public String getEventName();
  public void setNextStopSequenceNumber( Integer nextStopSequenceNumber );
  public Integer getNextStopSequenceNumber();
  public void setNote( String note );
  public String getNote();
}
