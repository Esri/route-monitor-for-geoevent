package esri.mrm.mobile;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class StopsConfiguration implements Parcelable {

	private String stopType;
	private String icon;
	
	private List<PropertyDefinition> propertyDefinitions;
	
	public StopsConfiguration(JSONObject jobj)
	{
		propertyDefinitions = new ArrayList<PropertyDefinition>();
		try {
			stopType = jobj.getString("type");
			icon = jobj.getString("icon");
			JSONObject defroot = jobj.getJSONObject("propertyDefinitions");
			JSONArray propertyDefs = defroot.getJSONArray("propertyDefinition");
			for(int i=0; i<propertyDefs.length(); i++)
			{
				JSONObject propertyDefJsonObj = propertyDefs.getJSONObject(i);
				PropertyDefinition propertyDef = new PropertyDefinition(propertyDefJsonObj);
				propertyDefinitions.add(propertyDef);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public PropertyDefinition getPropertyDefinition(String name)
	{
		for(PropertyDefinition pd : propertyDefinitions)
		{
			if(pd.getPropertyName().equals(name))
			{
				return pd;
			}
		}
		return null;
	}
	
	public List<PropertyDefinition> getPropertyDefinitions(String name)
	{
	  List<PropertyDefinition> output = new ArrayList<PropertyDefinition>();
    for(PropertyDefinition pd : propertyDefinitions)
    {
      if(pd.getPropertyName().equals(name))
      {
        output.add(pd);
      }
    }
    return output;
  }
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(stopType);
		dest.writeString(icon);
		dest.writeList(propertyDefinitions);

	}
	public String getStopType() {
		return stopType;
	}
	public void setStopType(String stopType) {
		this.stopType = stopType;
	}
	public List<PropertyDefinition> getPropertyDefinitions() {
		return propertyDefinitions;
	}
	public void setPropertyDefinitions(List<PropertyDefinition> propertyDefinitions) {
		this.propertyDefinitions = propertyDefinitions;
	}
	
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
