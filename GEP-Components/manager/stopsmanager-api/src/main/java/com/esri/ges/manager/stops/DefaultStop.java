package com.esri.ges.manager.stops;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.esri.ges.spatial.Point;
import com.esri.ges.util.Validator;

public class DefaultStop implements Stop
{

  private static final SimpleDateFormat DF = new SimpleDateFormat("hh:mm:ss");  // aa
  private Date actualArrival;
  private Date actualDeparture;
  private Integer actualServiceDuration;
  private String address;
  private String curbApproach;
  private String customProperties;
  private String deliveryCapacity;
  private String description;
  private Date lastUpdated;
  private Point location;
  private Long maxViolationTime;
  private String name;
  private String note;
  private String pickupCapacity;
  private Date projectedArrival;
  private Date projectedDeparture;
  private String routeName;
  private Date scheduledArrival;
  private Integer scheduledServiceDuration=0;
  private Date scheduledDeparture;
  private Integer sequenceNumber=0;
  private StopStatus status = StopStatus.Assigned;
  private Date timeWindowEnd1;
  private Date timeWindowEnd2;
  private Date timeWindowStart1;
  private Date timeWindowStart2;
  private String type;
  private Double cumulativeMinutes;
  private Map<String,String> attributes = new ConcurrentHashMap<String,String>();
  
  public DefaultStop()
  {
    
  }
  
  public DefaultStop( Stop seed )
  {
    if( seed != null )
    {
      setActualArrival( seed.getActualArrival() );
      setActualDeparture( seed.getActualDeparture() );
      setActualServiceDuration( seed.getActualServiceDuration() );
      setAddress( seed.getAddress() );
      setCurbApproach( seed.getCurbApproach() );
      setCustomProperties( seed.getCustomProperties() );
      setDeliveryCapacity( seed.getDeliveryCapacity() );
      setDescription( seed.getDescription() );
      setLastUpdated( seed.getLastUpdated() );
      setLocation( seed.getLocation() );
      setMaxViolationTime( seed.getMaxViolationTime() );
      setName( seed.getName() );
      setNote( seed.getNote() );
      setPickupCapacity( seed.getPickupCapacity() );
      setProjectedArrival( seed.getProjectedArrival() );
      setProjectedDeparture( seed.getProjectedDeparture() );
      setRouteName( seed.getRouteName() );
      setScheduledArrival( seed.getScheduledArrival() );
      setScheduledDeparture( seed.getScheduledDeparture() );
      setScheduledServiceDuration( seed.getScheduledServiceDuration() );
      setSequenceNumber( seed.getSequenceNumber() );
      setStatus( seed.getStatus() );
      setTimeWindowEnd1( seed.getTimeWindowEnd1() );
      setTimeWindowEnd2( seed.getTimeWindowEnd2() );
      setTimeWindowStart1( seed.getTimeWindowStart1() );
      setTimeWindowStart2( seed.getTimeWindowStart2() );
      setType( seed.getType() );
      setCumulativeMinutes( seed.getCumulativeMinutes() );
    }
  }

  @Override
  public Date getActualArrival()
  {
    return actualArrival;
  }

  @Override
  public Date getActualDeparture()
  {
    return actualDeparture;
  }

  @Override
  public Integer getActualServiceDuration()
  {
    return actualServiceDuration;
  }
  
  @Override
  public String getAddress()
  {
    return address;
  }

  @Override
  public String getCurbApproach()
  {
    return curbApproach;
  }

  @Override
  public String getCustomProperties()
  {
    return customProperties;
  }

  @Override
  public String getDeliveryCapacity()
  {
    return deliveryCapacity;
  }

  @Override
  public String getDescription()
  {
    return description;
  }

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
  public Long getMaxViolationTime()
  {
    return maxViolationTime;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public String getNote()
  {
    return note;
  }

  @Override
  public String getPickupCapacity()
  {
    return pickupCapacity;
  }

  @Override
  public Date getProjectedArrival()
  {
    return projectedArrival;
  }

  @Override
  public Date getProjectedDeparture()
  {
    return projectedDeparture;
  }

  @Override
  public String getRouteName()
  {
    return routeName;
  }

  @Override
  public Date getScheduledArrival()
  {
    return scheduledArrival;
  }

  @Override
  public Date getScheduledDeparture()
  {
    return scheduledDeparture;
  }

  @Override
  public Integer getScheduledServiceDuration()
  {
    return scheduledServiceDuration;
  }

  @Override
  public Integer getSequenceNumber()
  {
    return sequenceNumber;
  }

  @Override
  public StopStatus getStatus()
  {
    return status;
  }

  @Override
  public Date getTimeWindowEnd1()
  {
    return timeWindowEnd1;
  }

  @Override
  public Date getTimeWindowEnd2()
  {
    return timeWindowEnd2;
  }

  @Override
  public Date getTimeWindowStart1()
  {
    return timeWindowStart1;
  }

  @Override
  public Date getTimeWindowStart2()
  {
    return timeWindowStart2;
  }

  @Override
  public String getType()
  {
    return type;
  }
  
  @Override
  public Double getCumulativeMinutes()
  {
    return cumulativeMinutes;
  }

  @Override
  public void setActualArrival(Date actualArrival)
  {
    this.actualArrival = actualArrival;
    if(actualArrival != null)
      attributes.put(StopResource.ACTUAL_ARRIVAL_KEY, Long.toString(actualArrival.getTime()));
  }

  @Override
  public void setActualDeparture(Date actualDeparture)
  {
    this.actualDeparture = actualDeparture;
    if(actualDeparture != null)
      attributes.put(StopResource.ACTUAL_DEPARTURE_KEY, Long.toString(actualDeparture.getTime()));
  }

  @Override
  public void setActualServiceDuration(Integer actualServiceDuration)
  {
    this.actualServiceDuration = actualServiceDuration;
    if(actualServiceDuration != null)
      attributes.put(StopResource.ACTUAL_SERVICE_DURATION_KEY, Integer.toString(actualServiceDuration));
  }

  public void setAddress(String address)
  {
    this.address = address;
    if(address != null)
      attributes.put(StopResource.ADDRESS_KEY, address);
  }

  public void setCurbApproach(String curbApproach)
  {
    this.curbApproach = curbApproach;
    if(curbApproach != null)
      attributes.put(StopResource.CURB_APPROACH_KEY, curbApproach);
  }

  public void setCustomProperties(String customProperties)
  {
    this.customProperties = customProperties;
    if(customProperties != null)
      attributes.put(StopResource.CUSTOM_STOP_PROPERTIES_KEY, customProperties);
  }

  public void setDeliveryCapacity(String deliveryCapacity)
  {
    this.deliveryCapacity = deliveryCapacity;
    if(deliveryCapacity != null)
      attributes.put(StopResource.DELIVERY_CAPACITY_KEY, deliveryCapacity);
  }

  public void setDescription(String description)
  {
    this.description = description;
    if(description != null)
      attributes.put(StopResource.DESCRIPTION_KEY, description);
  }

  @Override
  public void setLastUpdated(Date lastUpdated)
  {
    this.lastUpdated = lastUpdated;
    if(lastUpdated != null)
      attributes.put(StopResource.LAST_UPDATED_KEY, Long.toString(lastUpdated.getTime()));
  }
  
  public void setLocation(Point location)
  {
    this.location = location;
    if(location != null)
      attributes.put(StopResource.SHAPE_KEY, location.toJson());
  }

  public void setMaxViolationTime(Long maxViolationTime)
  {
    this.maxViolationTime = maxViolationTime;
    if(maxViolationTime != null)
      attributes.put(StopResource.MAX_VIOLATION_KEY, Long.toString(maxViolationTime));
  }

  public void setName(String name)
  {
    this.name = name;
    if(name != null)
      attributes.put(StopResource.STOP_NAME_KEY, name);
  }

  @Override
  public void setNote(String note)
  {
    this.note = note;
    if(note != null)
      attributes.put(StopResource.NOTE_KEY, note);
  }

  public void setPickupCapacity(String pickupCapacity)
  {
    this.pickupCapacity = pickupCapacity;
    if(pickupCapacity != null)
      attributes.put(StopResource.PICKUP_CAPACITY_KEY, pickupCapacity);
  }

  @Override
  public void setProjectedArrival(Date projectedArrival)
  {
    this.projectedArrival = projectedArrival;
    if(projectedArrival != null)
      attributes.put(StopResource.PROJECTED_ARRIVAL_KEY, Long.toString(projectedArrival.getTime()));
  }

  @Override
  public void setProjectedDeparture(Date projectedDeparture)
  {
    this.projectedDeparture = projectedDeparture;
    if(projectedDeparture != null)
      attributes.put(StopResource.PROJECTED_DEPARTURE_KEY, Long.toString(projectedDeparture.getTime()));
  }

  public void setRouteName(String routeName)
  {
    this.routeName = routeName;
    if(routeName != null)
      attributes.put(StopResource.ROUTE_NAME_KEY, routeName);
  }

  public void setScheduledArrival(Date scheduledArrival)
  {
    this.scheduledArrival = scheduledArrival;
    if(scheduledArrival != null)
      attributes.put(StopResource.SCHEDULED_ARRIVAL_KEY, Long.toString(scheduledArrival.getTime()));
  }

  public void setScheduledServiceDuration(Integer scheduledServiceDuration)
  {
    this.scheduledServiceDuration = scheduledServiceDuration;
    if(scheduledServiceDuration != null)
      attributes.put(StopResource.SCHEDULED_SERVICE_DURATION_KEY, Integer.toString(scheduledServiceDuration));
  }

  public void setScheduledDeparture(Date scheduledDeparture)
  {
    this.scheduledDeparture = scheduledDeparture;
    if(scheduledDeparture != null)
      attributes.put(StopResource.SCHEDULED_DEPARTURE_KEY, Long.toString(scheduledDeparture.getTime()));
  }

  public void setSequenceNumber(Integer sequenceNumber)
  {
    this.sequenceNumber = sequenceNumber;
    if(sequenceNumber != null)
      attributes.put(StopResource.SEQUENCE_NUMBER_KEY, Integer.toString(sequenceNumber));
  }

  @Override
  public void setStatus(StopStatus status)
  {
    this.status = status;
    if(status != null)
      attributes.put(StopResource.STATUS_KEY, status.toString());
  }

  public void setTimeWindowEnd1(Date timeWindowEnd1)
  {
    this.timeWindowEnd1 = timeWindowEnd1;
    if(timeWindowEnd1 != null)
      attributes.put(StopResource.TIME_WINDOW_END_1_KEY, Long.toString(timeWindowEnd1.getTime()));
  }

  public void setTimeWindowEnd2(Date timeWindowEnd2)
  {
    this.timeWindowEnd2 = timeWindowEnd2;
    if(timeWindowEnd2 != null)
      attributes.put(StopResource.TIME_WINDOW_END_2_KEY, Long.toString(timeWindowEnd2.getTime()));
  }

  public void setTimeWindowStart1(Date timeWindowStart1)
  {
    this.timeWindowStart1 = timeWindowStart1;
    if(timeWindowStart1 != null)
      attributes.put(StopResource.TIME_WINDOW_START_1_KEY, Long.toString(timeWindowStart1.getTime()));
  }

  public void setTimeWindowStart2(Date timeWindowStart2)
  {
    this.timeWindowStart2 = timeWindowStart2;
    if(timeWindowStart2 != null)
      attributes.put(StopResource.TIME_WINDOW_START_2_KEY, Long.toString(timeWindowStart2.getTime()));
  }

  public void setType(String type)
  {
    this.type = type;
    if(type != null)
      attributes.put(StopResource.TYPE_KEY, type);
  }
  
  public void setCumulativeMinutes(Double cumulativeMinutes)
  {
    this.cumulativeMinutes = cumulativeMinutes;
    if(cumulativeMinutes != null)
      attributes.put(StopResource.CUMULATIVE_MINUTES_KEY, Double.toString(cumulativeMinutes));
  }
  
  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(routeName);
    sb.append("/");
    sb.append(name);
    sb.append(" (");
    sb.append(sequenceNumber);
    sb.append("): ");
    sb.append(status);
    sb.append(" ");
    if (scheduledArrival != null)
      sb.append(DF.format(scheduledArrival));
    if (projectedArrival != null)
      sb.append("/"+DF.format(projectedArrival));
    if (actualArrival != null)
      sb.append("/"+DF.format(actualArrival));
    sb.append(" ");
    if (scheduledServiceDuration != null)
      sb.append(scheduledServiceDuration);
    if (actualServiceDuration != null)
      sb.append("/"+actualServiceDuration);
    sb.append(" ");
    if (scheduledDeparture != null)
      sb.append(DF.format(scheduledDeparture));
    if (projectedDeparture != null)
      sb.append("/"+DF.format(projectedDeparture));
    if (actualDeparture != null)
      sb.append("/"+DF.format(actualDeparture));
    return sb.toString();
  }

  @Override
  public boolean isServiced()
  {
    StopStatus status = getStatus();
    return status == StopStatus.Completed || status == StopStatus.Exception;
  }
  
  @Override
  public void setAttribute(String key, String value)
  {
    if( !Validator.isEmpty(key) && !Validator.isEmpty( value ) )
    {
      attributes.put( key, value );
    }
  }

  @Override
  public String getAttribute(String key)
  {
    return attributes.get( key );
  }

  @Override
  public Set<String> getAttributeKeys()
  {
    return attributes.keySet();
  }

  @Override
  public Set<String> getPredefinedKeys()
  {
//    Set<String> output = new HashSet<String>();
//     output.add(StopResource.ACTUAL_ARRIVAL_KEY);
//     output.add(StopResource.ACTUAL_DEPARTURE_KEY);
//     output.add(StopResource.ACTUAL_SERVICE_DURATION_KEY);
//     output.add(StopResource.ADDRESS_KEY);
//     output.add(StopResource.CURB_APPROACH_KEY);
//     output.add(StopResource.CUSTOM_STOP_PROPERTIES_KEY);
//     output.add(StopResource.DELIVERY_CAPACITY_KEY);
//     output.add(StopResource.DESCRIPTION_KEY);
//     output.add(StopResource.LAST_UPDATED_KEY);
//     output.add(StopResource.SHAPE_KEY);
//     output.add(StopResource.MAX_VIOLATION_KEY);
//     output.add(StopResource.STOP_NAME_KEY);
//     output.add(StopResource.NOTE_KEY);
//     output.add(StopResource.PICKUP_CAPACITY_KEY);
//     output.add(StopResource.PROJECTED_ARRIVAL_KEY);
//     output.add(StopResource.PROJECTED_DEPARTURE_KEY);
//     output.add(StopResource.ROUTE_NAME_KEY);
//     output.add(StopResource.SCHEDULED_ARRIVAL_KEY);
//     output.add(StopResource.SCHEDULED_DEPARTURE_KEY);
//     output.add(StopResource.SCHEDULED_SERVICE_DURATION_KEY);
//     output.add(StopResource.SEQUENCE_NUMBER_KEY);
//     output.add(StopResource.STATUS_KEY);
//     output.add(StopResource.TIME_WINDOW_END_1_KEY);
//     output.add(StopResource.TIME_WINDOW_END_2_KEY);
//     output.add(StopResource.TIME_WINDOW_START_1_KEY);
//     output.add(StopResource.TIME_WINDOW_START_2_KEY);
//     output.add(StopResource.TYPE_KEY);
//     output.add(StopResource.CUMULATIVE_MINUTES_KEY);
//    return output;
    if(attributes == null)
      return null;
    else
      return attributes.keySet();
  }
}
