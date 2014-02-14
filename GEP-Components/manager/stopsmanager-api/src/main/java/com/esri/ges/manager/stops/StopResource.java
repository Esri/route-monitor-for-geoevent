package com.esri.ges.manager.stops;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.esri.ges.core.resource.Resource;
import com.esri.ges.manager.resource.ResourceBackedAttributeMap;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.GeometryType;
import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.Spatial;
import com.esri.ges.util.Validator;

public class StopResource extends ResourceBackedAttributeMap implements Stop
{
  private static final SimpleDateFormat DF = new SimpleDateFormat("hh:mm:ss");  // aa
  final public static String            ACTUAL_ARRIVAL_KEY             = "ACTUAL_ARRIVAL";
  final public static String            ACTUAL_DEPARTURE_KEY           = "ACTUAL_DEPARTURE";
  final public static String            ACTUAL_SERVICE_DURATION_KEY    = "ACTUAL_SERVICE_DURATION";
  final public static String            ADDRESS_KEY                    = "ADDRESS";
  final public static String            CURB_APPROACH_KEY               = "CURB_APPROACH";
  final public static String            CUSTOM_STOP_PROPERTIES_KEY     = "CUSTOM_STOP_PROPERTIES";
  final public static String            DELIVERY_CAPACITY_KEY          = "DELIVERY_CAPACITY";
  final public static String            DESCRIPTION_KEY                = "DESCRIPTION";
  final public static String            LAST_UPDATED_KEY               = "TIME_START";
  final public static String            SHAPE_KEY                      = "GEOMETRY";
  final public static String            MAX_VIOLATION_KEY              = "MAX_VIOLATION_TIME";
  final public static String            STOP_NAME_KEY                  = "STOP_NAME";
  final public static String            NOTE_KEY                       = "NOTE";
  final public static String            PICKUP_CAPACITY_KEY            = "PICKUP_CAPACITY";
  final public static String            PROJECTED_ARRIVAL_KEY          = "PROJECTED_ARRIVAL";
  final public static String            PROJECTED_DEPARTURE_KEY        = "PROJECTED_DEPARTURE";
  final public static String            ROUTE_NAME_KEY                 = "ROUTE_NAME";
  final public static String            SCHEDULED_ARRIVAL_KEY          = "SCHEDULED_ARRIVAL";
  final public static String            SCHEDULED_DEPARTURE_KEY        = "SCHEDULED_DEPARTURE";
  final public static String            SCHEDULED_SERVICE_DURATION_KEY = "SCHEDULED_SERVICE_DURATION";
  final public static String            SEQUENCE_NUMBER_KEY            = "SEQUENCE_NUMBER";
  final public static String            STATUS_KEY                     = "STATUS";
  final public static String            TIME_WINDOW_END_1_KEY          = "TIME_WINDOW_END1";
  final public static String            TIME_WINDOW_END_2_KEY          = "TIME_WINDOW_END2";
  final public static String            TIME_WINDOW_START_1_KEY        = "TIME_WINDOW_START1";
  final public static String            TIME_WINDOW_START_2_KEY        = "TIME_WINDOW_START2";
  final public static String            TYPE_KEY                       = "TYPE";
  final public static String            CUMULATIVE_MINUTES_KEY         = "CUMULATIVE_MINUTES";

  public StopResource(Resource resource, Spatial spatial)
  {
    super( resource, spatial );
  }
  
  public void update(DefaultStop stop)
  {
    setDateAttribute( resource, ACTUAL_ARRIVAL_KEY, stop.getActualArrival());
    setDateAttribute( resource, ACTUAL_DEPARTURE_KEY, stop.getActualDeparture());
    setIntegerAttribute( resource, ACTUAL_SERVICE_DURATION_KEY, stop.getActualServiceDuration());
    setAttribute( resource, ADDRESS_KEY, stop.getAddress());
    setAttribute( resource, CURB_APPROACH_KEY, stop.getCurbApproach());
    setAttribute( resource, CUSTOM_STOP_PROPERTIES_KEY, stop.getCustomProperties());
    setAttribute( resource, DELIVERY_CAPACITY_KEY, stop.getDeliveryCapacity());
    setAttribute( resource, DESCRIPTION_KEY, stop.getDescription());
    setDateAttribute( resource, LAST_UPDATED_KEY, stop.getLastUpdated());
    setGeometryAttribute( resource, SHAPE_KEY, stop.getLocation());
    setLongAttribute( resource, MAX_VIOLATION_KEY, stop.getMaxViolationTime());
    setAttribute( resource, STOP_NAME_KEY, stop.getName());
    setAttribute( resource, NOTE_KEY, stop.getNote());
    setAttribute( resource, PICKUP_CAPACITY_KEY, stop.getPickupCapacity());
    setDateAttribute( resource, PROJECTED_ARRIVAL_KEY, stop.getProjectedArrival());
    setDateAttribute( resource, PROJECTED_DEPARTURE_KEY, stop.getProjectedDeparture());
    setAttribute( resource, ROUTE_NAME_KEY, stop.getRouteName());
    setDateAttribute( resource, SCHEDULED_ARRIVAL_KEY, stop.getScheduledArrival());
    setDateAttribute( resource, SCHEDULED_DEPARTURE_KEY, stop.getScheduledDeparture());
    setIntegerAttribute( resource, SCHEDULED_SERVICE_DURATION_KEY, stop.getScheduledServiceDuration());
    setIntegerAttribute( resource, SEQUENCE_NUMBER_KEY, stop.getSequenceNumber());
    if(stop.getStatus()!=null)
      setStatus(stop.getStatus());
    setDateAttribute( resource, TIME_WINDOW_END_1_KEY, stop.getTimeWindowEnd1());
    setDateAttribute( resource, TIME_WINDOW_END_2_KEY, stop.getTimeWindowEnd2());
    setDateAttribute( resource, TIME_WINDOW_START_1_KEY, stop.getTimeWindowStart1());
    setDateAttribute( resource, TIME_WINDOW_START_2_KEY, stop.getTimeWindowStart2());
//    setAttribute( resource, TYPE_KEY, stop.getType());
    if(stop.getType() !=null)
      setType(stop.getType());
    setDoubleAttribute( resource, CUMULATIVE_MINUTES_KEY, stop.getCumulativeMinutes());
  }
  
  

  @Override
  public Date getActualArrival()
  {
    return getDateAttribute(ACTUAL_ARRIVAL_KEY);
  }

  @Override
  public Date getActualDeparture()
  {
    return getDateAttribute(ACTUAL_DEPARTURE_KEY);
  }

  @Override
  public Integer getActualServiceDuration()
  {
    return getIntegerAttribute(ACTUAL_SERVICE_DURATION_KEY);
  }

  @Override
  public String getAddress()
  {
    return getAttribute(ADDRESS_KEY);
  }

  @Override
  public String getCurbApproach()
  {
    return getAttribute(CURB_APPROACH_KEY);
  }

  @Override
  public String getCustomProperties()
  {
    return getAttribute(CUSTOM_STOP_PROPERTIES_KEY);
  }

  @Override
  public String getDeliveryCapacity()
  {
    return getAttribute(DELIVERY_CAPACITY_KEY);
  }

  @Override
  public String getDescription()
  {
    return getAttribute(DESCRIPTION_KEY);
  }

  @Override
  public Date getLastUpdated()
  {
    return getDateAttribute(LAST_UPDATED_KEY);
  }

  @Override
  public Point getLocation()
  {
    Point pt = null;
    String ptString = resource.getAttribute( SHAPE_KEY );
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
  public Long getMaxViolationTime()
  {
    return getLongAttribute(MAX_VIOLATION_KEY);
  }

  @Override
  public String getName()
  {
    return getAttribute(STOP_NAME_KEY);
  }

  @Override
  public String getNote()
  {
    return getAttribute(NOTE_KEY);
  }

  @Override
  public String getPickupCapacity()
  {
    return getAttribute(PICKUP_CAPACITY_KEY);
  }

  @Override
  public Date getProjectedArrival()
  {
    return getDateAttribute(PROJECTED_ARRIVAL_KEY);
  }

  @Override
  public Date getProjectedDeparture()
  {
    return getDateAttribute(PROJECTED_DEPARTURE_KEY);
  }

  @Override
  public String getRouteName()
  {
    return getAttribute(ROUTE_NAME_KEY);
  }

  @Override
  public Date getScheduledArrival()
  {
    return getDateAttribute(SCHEDULED_ARRIVAL_KEY);
  }

  @Override
  public Date getScheduledDeparture()
  {
    return getDateAttribute(SCHEDULED_DEPARTURE_KEY);
  }

  @Override
  public Integer getScheduledServiceDuration()
  {
    return getIntegerAttribute(SCHEDULED_SERVICE_DURATION_KEY);
  }

  @Override
  public Integer getSequenceNumber()
  {
    return getIntegerAttribute(SEQUENCE_NUMBER_KEY);
  }

  @Override
  public StopStatus getStatus()
  {
    String statusString = getAttribute(STATUS_KEY);
    StopStatus status = null ;
    try
    {
      status = StopStatus.valueOf( statusString );
    }
    catch( IllegalArgumentException iae )
    {
      //fall-thru
    }
    catch( NullPointerException npe )
    {
      //fall-thru
    }
    if( status == null )
    {
      if( statusString.equals( "AtStop" ) ||
          statusString.equals( "Arrive" ) )
      {
        status = StopStatus.AtStop;
      }
      else if( statusString.equals( "Depart" ) )
      {
        status = StopStatus.Completed;
      }
      else
      {
        status = StopStatus.Assigned;
      }
    }
    return status;
  }

  @Override
  public Date getTimeWindowEnd1()
  {
    return getDateAttribute(TIME_WINDOW_END_1_KEY);
  }

  @Override
  public Date getTimeWindowEnd2()
  {
    return getDateAttribute(TIME_WINDOW_END_2_KEY);
  }

  @Override
  public Date getTimeWindowStart1()
  {
    return getDateAttribute(TIME_WINDOW_START_1_KEY);
  }

  @Override
  public Date getTimeWindowStart2()
  {
    return getDateAttribute(TIME_WINDOW_START_2_KEY);
  }

  @Override
  public String getType()
  {
    String typeString = getAttribute(TYPE_KEY);
    return typeString;
  }

  @Override
  public Double getCumulativeMinutes()
  {
    return getDoubleAttribute(CUMULATIVE_MINUTES_KEY);
  }

  @Override
  public void setLastUpdated(Date lastUpdated)
  {
    setDateAttribute( resource, LAST_UPDATED_KEY, lastUpdated);
  }

  @Override
  public void setProjectedArrival(Date projectedArrival)
  {
    setDateAttribute( resource, PROJECTED_ARRIVAL_KEY, projectedArrival);
  }

  @Override
  public void setProjectedDeparture(Date projectedDeparture)
  {
    setDateAttribute( resource, PROJECTED_DEPARTURE_KEY, projectedDeparture);
  }

  @Override
  public void setActualArrival(Date actualArrival)
  {
    setDateAttribute(resource,ACTUAL_ARRIVAL_KEY, actualArrival);
  }

  @Override
  public void setActualDeparture(Date actualDeparture)
  {
    setDateAttribute(resource,ACTUAL_DEPARTURE_KEY, actualDeparture);
  }

  @Override
  public void setActualServiceDuration(Integer actualServiceDuration)
  {
    setIntegerAttribute(resource,ACTUAL_SERVICE_DURATION_KEY, actualServiceDuration);
  }

  @Override
  public void setStatus(StopStatus status)
  {
    if( status != null )
    {
      setAttribute(STATUS_KEY, status.toString() );
    }
  }
  
  @Override
  public void setType(String type)
  {
    if( type != null )
    {
      setAttribute(TYPE_KEY, type.toString() );
    }
  }
  
  @Override
  public void setSequenceNumber(Integer sequenceNumber)
  {
    setIntegerAttribute(resource,SEQUENCE_NUMBER_KEY, sequenceNumber);
  }

  @Override
  public void setNote(String note)
  {
    setAttribute(resource,NOTE_KEY, note);
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
      if( Validator.isEmpty( key ) || Validator.isEmpty( value ) )
      {
        return;
      }
      setAttribute(resource, key, value);
  }

  @Override
  public String getAttribute(String key)
  {
    return resource.getAttribute(key);
  }

  @Override
  public Set<String> getAttributeKeys()
  {
    return resource.getAttributeKeys();
  }
  
  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(getRouteName());
    sb.append("/");
    sb.append(getName());
    sb.append(" (");
    sb.append(getSequenceNumber());
    sb.append("): ");
    sb.append(getStatus().toString());
    sb.append(" ");
    if (getScheduledArrival() != null)
      sb.append(DF.format(getScheduledArrival()));
    if (getProjectedArrival() != null)
      sb.append("/"+DF.format(getProjectedArrival()));
    if (getActualArrival() != null)
      sb.append("/"+DF.format(getActualArrival()));
    sb.append(" ");
    if (getScheduledServiceDuration() != null)
      sb.append(getScheduledServiceDuration());
    if (getActualServiceDuration() != null)
      sb.append("/"+getActualServiceDuration());
    sb.append(" ");
    if (getScheduledDeparture() != null)
      sb.append(DF.format(getScheduledDeparture()));
    if (getProjectedDeparture() != null)
      sb.append("/"+DF.format(getProjectedDeparture()));
    if (getActualDeparture() != null)
      sb.append("/"+DF.format(getActualDeparture()));
    return sb.toString();
  }

  @Override
  public Set<String> getPredefinedKeys()
  {
    Set<String> output = new HashSet<String>();
    output.add(ACTUAL_ARRIVAL_KEY);
    output.add(ACTUAL_DEPARTURE_KEY);
    output.add(ACTUAL_SERVICE_DURATION_KEY);
    output.add(ADDRESS_KEY);
    output.add(CURB_APPROACH_KEY);
    output.add(CUSTOM_STOP_PROPERTIES_KEY);
    output.add(DELIVERY_CAPACITY_KEY);
    output.add(DESCRIPTION_KEY);
    output.add(LAST_UPDATED_KEY);
    output.add(SHAPE_KEY);
    output.add(MAX_VIOLATION_KEY);
    output.add(STOP_NAME_KEY);
    output.add(NOTE_KEY);
    output.add(PICKUP_CAPACITY_KEY);
    output.add(PROJECTED_ARRIVAL_KEY);
    output.add(PROJECTED_DEPARTURE_KEY);
    output.add(ROUTE_NAME_KEY);
    output.add(SCHEDULED_ARRIVAL_KEY);
    output.add(SCHEDULED_DEPARTURE_KEY);
    output.add(SCHEDULED_SERVICE_DURATION_KEY);
    output.add(SEQUENCE_NUMBER_KEY);
    output.add(STATUS_KEY);
    output.add(TIME_WINDOW_END_1_KEY);
    output.add(TIME_WINDOW_END_2_KEY);
    output.add(TIME_WINDOW_START_1_KEY);
    output.add(TIME_WINDOW_START_2_KEY);
    output.add(TYPE_KEY);
    output.add(CUMULATIVE_MINUTES_KEY);
    return output;
  }

}
