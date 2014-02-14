package com.esri.ges.manager.stops;

import java.util.Date;
import java.util.Set;

import com.esri.ges.spatial.Point;

public interface Stop
{
  public Date getActualArrival();
  public Date getActualDeparture();
  public Integer getActualServiceDuration();
  public String getAddress();
  public String getCurbApproach();
  public String getCustomProperties();
  public String getDeliveryCapacity();
  public String getDescription();
  public Date getLastUpdated();
  public Point getLocation();
  public Long getMaxViolationTime();
  public String getName();
  public String getNote();
  public String getPickupCapacity();
  public Date getProjectedArrival();
  public Date getProjectedDeparture();
  public String getRouteName();
  public Date getScheduledArrival();
  public Date getScheduledDeparture();
  public Integer getScheduledServiceDuration();
  public Integer getSequenceNumber();
  public StopStatus getStatus();
  public Date getTimeWindowEnd1();
  public Date getTimeWindowEnd2();
  public Date getTimeWindowStart1();
  public Date getTimeWindowStart2();
  public String getType();
  public Double getCumulativeMinutes();

  public void setLastUpdated(Date lastUpdated);
  public void setProjectedArrival(Date projectedArrival);
  public void setProjectedDeparture(Date projectedDeparture);
  public void setActualArrival(Date actualArrival);
  public void setActualDeparture(Date actualDeparture);
  public void setActualServiceDuration(Integer actualServiceDuration);
  public void setStatus(StopStatus status);
  public void setType(String type);
  public void setNote(String note);
  public void setSequenceNumber(Integer sequenceNumber);
  
  public boolean isServiced();
  
  public void setAttribute( String key, String value );
  public String getAttribute( String key );
  public Set<String> getAttributeKeys();
  public Set<String> getPredefinedKeys();
}
