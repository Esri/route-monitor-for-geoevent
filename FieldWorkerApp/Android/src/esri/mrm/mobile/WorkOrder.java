package esri.mrm.mobile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.esri.core.map.Field;
import com.esri.core.map.Graphic;

import android.R.integer;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class WorkOrder implements Parcelable {

	private Map<String, Object> resource;
	
	private Map<String, Integer> fieldTypes;
	
	private Context context;
	
	private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
	
	private Graphic graphic;
	
	private Map<String, String> fieldAliases;
	
	public WorkOrder(Graphic graphic, Map<String, Integer> fieldTypes, Map<String, String> fieldAliases, Context context)
	{
		resource = new HashMap<String, Object>();
		Iterator it = graphic.getAttributes().entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String alias = (String)fieldAliases.get((String)pairs.getKey());
	        resource.put(alias, pairs.getValue());
	    }
	    
	    this.fieldTypes = fieldTypes; 
	    this.context = context;
	    this.graphic = graphic;
	    this.fieldAliases = WorkOrderUtility.sortByValue(fieldAliases);
	}
	
	public Graphic getGraphic()
  {
    return graphic;
  }

  private String getString(int id)
  {
    return context.getResources().getString(id);
  }
  
	public Object getAttributes(String name)
	{
		return resource.get(name);
	}
	
  public void setAttributes(String name, String value)
  {
    Object obj = resource.get(name);
    // If conversion failed, don't update
    try
    {
      if (obj instanceof Integer)
      {
        resource.put(name, Integer.parseInt(value));
      }
      else if (obj instanceof Double)
      {
        resource.put(name, Double.parseDouble(value));
      }
      else if (obj instanceof Long)
      {
        resource.put(name, Long.parseLong(value));
      }
      else if (obj instanceof String)
      {
        resource.put(name, value);
      }
      else
      {
        if (resource.containsKey(name))
        {
          switch (fieldTypes.get(name))
          {
            case Field.esriFieldTypeDate:
              resource.put(name, Long.valueOf(value));
              break;
            case Field.esriFieldTypeDouble:
              resource.put(name, Double.valueOf(value));
              break;
            case Field.esriFieldTypeInteger:
              resource.put(name, Integer.valueOf(value));
              break;
            case Field.esriFieldTypeSmallInteger:
              resource.put(name, Integer.valueOf(value));
              break;
            case Field.esriFieldTypeString:
              resource.put(name, value);
              break;
            default:
              resource.put(name, value);
              break;
          }
        }
        else
        {
          throw new Exception("Type not found");
        }
      }
      resource.put(getString(R.string.ALIAS_LAST_UPDATED), new Date().getTime());
    }
    catch (Exception e)
    {
      // If conversion failed, don't update
    }
  }
	
  public void setLastUpdated(long milliseconds)
  {
    resource.put(getString(R.string.ALIAS_LAST_UPDATED), milliseconds);
  }
//	public String getId() {
//		
//	}

//	public void setId(String id) {
//		this.id = id;
//	}

	public String getEtaTime() {
		Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_PROJECTED_ARRIVAL));
		if(obj != null)
		{
			return timeFormat.format(new Date((Long)obj));
		}
		return "";
	}
	
	public String getScheduledArrival() {
    Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_SCHEDULED_ARRIVAL));
    if(obj != null)
    {
      return timeFormat.format(new Date((Long)obj));
    }
    return "";
  }
	
	public String getProjectedArrival() {
    Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_PROJECTED_ARRIVAL));
    if(obj != null)
    {
      return timeFormat.format(new Date((Long)obj));
    }
    return "";
  }
	
	public String getProjectedDeparture() {
	  Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_PROJECTED_DEPARTURE));
	  if(obj != null)
	  {
	    return timeFormat.format(new Date((Long)obj));
	  }
	  return "";
	}
	
	public String getActualArrival() {
	  Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_ACTUAL_ARRIVAL));
	  if(obj != null)
	  {
	    return timeFormat.format(new Date((Long)obj));
	  }
	  return "";
	}
	
	public Long getActualArrivalAsLong() {
	  Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_ACTUAL_ARRIVAL));
	  return (Long)obj;
	}
	
	public String getActualDeparture() {
	  Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_ACTUAL_DEPARTURE));
	  if(obj != null)
	  {
	    return timeFormat.format(new Date((Long)obj));
	  }
	  return "";
	}
	
	public Long getActualDepartureAsLong() {
	  Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_ACTUAL_DEPARTURE));
	  return (Long)obj;
	}

//	public void setEtaTime(String eta) {
//		this.etaTime = eta;
//	}

	public int getDistance() {
		// What should we return here?
		return 0;
	}

//	public void setDistance(int distance) {
//		this.distance = distance;
//	}

	public String getType() {
		return (String)resource.get(getString(R.string.ALIAS_STOPSLAYER_TYPE));
	}

	public void setType(String type) {
	  resource.put(getString(R.string.ALIAS_STOPSLAYER_TYPE), type);
	}

//	public boolean isSelected() {
//		return selected;
//	}

	public void setSelected(boolean selected) {
		// TODO: clean up - what is the purpose of this setter?
		//this.selected = selected;
	}

//	public int describeContents() {
//		return 0;
//	}

//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}

//	public String getTask() {
//		return task;
//	}
//
//	public void setTask(String task) {
//		this.task = task;
//	}

//	public String getArrivalTime() {
//		return arrivalTime;
//	}
//
//	public void setArrivalTime(String arrivalTime) {
//		this.arrivalTime = arrivalTime;
//	}

//	public String getDepartureTime() {
//		return departureTime;
//	}
//
//	public void setDepartureTime(String departureTime) {
//		this.departureTime = departureTime;
//	}

	public String getStatus() {
		Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_STATUS));
		if(obj != null)
		{
			return (String)obj;
		}
		return null;
	}

	public void setStatus(String status) {
		resource.put(getString(R.string.ALIAS_STOPSLAYER_STATUS), status);
	}
	
//	public String getResourceId() {
//		return resourceId;
//	}

	public void setResourceId(String resourceId) {
		// TODO: clean up - what is the purpose of this setter?
		//this.resourceId = resourceId;
	}

	public String getStopName() {
		Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_STOP_NAME));
		if(obj != null)
		{
			return (String)obj;
		}
		return null;
	}

	public void setStopName(String stopName) {
		resource.put(getString(R.string.ALIAS_STOPSLAYER_STOP_NAME), stopName);
	}

	public String getEtdTime() {
		Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_SCHEDULED_DEPARTURE));
		if(obj != null)
		{
			return timeFormat.format(new Date((Long)obj));
		}
		return null;
	}

	public void setEtdTime(String etdTime) {
		// TODO: will probably be needed
		//this.etdTime = etdTime;
	}
	
	public String getPictureSubUrl() {
		Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_PICTURE_LOCATION));
		if(obj != null)
		{
			return (String)obj;
		}
		return null;
	}
	
	public String getRouteName() {
    Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_ROUTE_NAME));
    if(obj != null)
    {
      return (String)obj;
    }
    return null;
  }
	
	public String getAddress() {
		Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_ADDRESS));
		if(obj != null)
		{
			return (String)obj;
		}
		return null;
	}


	public int getScheduledDuration() {
    Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_SCHEDULED_DURATION));
    if(obj != null)
    {
      return (Integer)obj;
    }
    return 0;
  }
	
	public void setScheduledDuration(int duration) {
    resource.put(getString(R.string.ALIAS_STOPSLAYER_SCHEDULED_DURATION), duration);
  }
	
	public int getSequence() {
		Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_SEQUENCE));
		if(obj != null)
		{
			return (Integer)obj;
		}
		return (Integer) null;
	}
	
	public void setSequence(int sequence) {
		resource.put(getString(R.string.ALIAS_STOPSLAYER_SEQUENCE), sequence);
	}
	
	// Just to be backwards compatible.
	public String getId() {
		Object obj = resource.get(getString(R.string.ALIAS_STOPSLAYER_SEQUENCE));
		if(obj != null)
		{
			return obj.toString();
		}
		return  null;
	}
	
//	public void setId(String sequence) {
//		resource.put(KEY_SEQUENCE_NUMBER, sequence);
//	}
	
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub

	}
	
	public Map<String, Object> getAllAttributes()
	{
	  Map<String, Object> fieldNameValueMap = new HashMap<String, Object>();
    Iterator it = fieldAliases.entrySet().iterator();
    while (it.hasNext())
    {
      Map.Entry pairs = (Map.Entry) it.next();
      if (resource.containsKey(pairs.getValue()))
      {
        fieldNameValueMap.put((String) pairs.getKey(), resource.get(pairs.getValue()));
      }
    }
    return fieldNameValueMap;
	}
	
	public String getJsonString()
	{
    JSONObject obj = new JSONObject(getAllAttributes());
    return obj.toString();
  }
	
	public String getJsonString(String requestId) throws JSONException
  {
    JSONObject obj = new JSONObject(getAllAttributes());
    obj.put("RequestId", requestId);

    return obj.toString();
  }
  

  public Map<String, Integer> getFieldTypes()
  {
    return fieldTypes;
  }
  
  public Map<String, String> getFieldAliases()
  {
    return fieldAliases;
  }

  public Context getContext()
  {
    return context;
  }
	
	

}
