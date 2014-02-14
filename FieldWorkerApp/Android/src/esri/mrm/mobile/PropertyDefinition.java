package esri.mrm.mobile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class PropertyDefinition implements Parcelable {

	private String propertyName;
	private String label;
	private String description;
	private String propertyType;
	private Object defaultValue;
	private boolean mandatory;
	private boolean readOnly;
	private String dependsOn;
	private List<Object> allowedValues;
	
	
	public PropertyDefinition(JSONObject jobj)
	{
		try
		{
			Iterator iter = jobj.keys();
		    while(iter.hasNext()){
		        String key = (String)iter.next();
		        if(key.equals("propertyName"))
		        {
		        	propertyName = jobj.getString(key);
		        }
		        else if (key.equals("label"))
		        {
		        	label = jobj.getString(key);
		        }
		        else if (key.equals("description"))
		        {
		        	description = jobj.getString(key);
		        }
		        else if (key.equals("propertyType"))
		        {
		        	propertyType = jobj.getString(key);
		        }
		        else if (key.equals("defaultValue"))
		        {
		        	defaultValue = jobj.get(key);
		        }
		        else if (key.equals("mandatory"))
		        {
		        	mandatory = jobj.getBoolean(key);
		        }
		        else if (key.equals("readOnly"))
		        {
		        	readOnly = jobj.getBoolean(key);
		        }
		        else if (key.equals("dependsOn"))
		        {
		        	dependsOn = jobj.getString(key);
		        }
		        else if (key.equals("allowedValues"))
		        {
//		        	JSONArray allowedValuesJsonObjs = jobj.getJSONArray(key);
		        	JSONObject obj = jobj.getJSONObject(key);
		        	JSONArray allowedValuesJsonObjs = obj.getJSONArray("value");
		        	if(allowedValuesJsonObjs != null)
		        	{
		        		allowedValues = new ArrayList<Object>();
		        		for(int i=0; i<allowedValuesJsonObjs.length(); i++)
		        		{
		        			allowedValues.add(allowedValuesJsonObjs.get(i));
		        		}
		        	}
		        }
		    }
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public boolean hasAllowedValues()
	{
		if(allowedValues != null)
		{
			if(allowedValues.size()>0)
			{
				return true;
			}
		}
		return false;
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(propertyName);
		dest.writeString(label);
		dest.writeString(description);
		dest.writeString(propertyType);
		dest.writeValue(defaultValue);
		dest.writeByte((byte) (mandatory ? 1 : 0));
		dest.writeByte((byte) (readOnly ? 1 : 0));
		dest.writeString(dependsOn);
		dest.writeList(allowedValues);
		

	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}

	public List<Object> getAllowedValues() {
		return allowedValues;
	}

	public void setAllowedValues(List<Object> allowedValues) {
		this.allowedValues = allowedValues;
	}

}
