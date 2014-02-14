package esri.mrm.mobile;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class StopsConfigurations implements Parcelable {

	private String gepUrl;
	private String agsUrl;

	private String callback;
	List<StopsConfiguration> configurations;
	
	public StopsConfigurations(JSONObject jobj)
	{
		configurations = new ArrayList<StopsConfiguration>();
		try {
			
			JSONObject root = jobj.getJSONObject("stopsConfigurations");
			callback = root.getString("callback");
			gepUrl = root.getString("gepUrl");
			agsUrl = root.getString("agsUrl");
//			JSONArray configs = jobj.getJSONArray("stopsConfiguration");
			Object obj = root.get("stopsConfiguration");
			JSONArray configs = new JSONArray();
			if(obj instanceof JSONArray)
			{
				configs = (JSONArray)obj;
			}
			else if(obj instanceof JSONObject)
			{
				configs.put(obj);
			}
			for(int i=0; i<configs.length(); i++)
			{
				JSONObject configJsonObj = configs.getJSONObject(i);
				StopsConfiguration stopsConfig = new StopsConfiguration(configJsonObj);
				configurations.add(stopsConfig);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public StopsConfiguration getStopsConfiguration(String type)
	{
		for(StopsConfiguration config : configurations)
		{
			if(config.getStopType().equals(type))
			{
				return config;
			}
		}
		return null;
	}
	
	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public List<StopsConfiguration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<StopsConfiguration> configurations) {
		this.configurations = configurations;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(callback);
		dest.writeList(configurations);

	}
	
	public String getGepUrl() {
		return gepUrl;
	}

	public void setGepUrl(String gepUrl) {
		this.gepUrl = gepUrl;
	}

	public String getAgsUrl() {
		return agsUrl;
	}

	public void setAgsUrl(String agsUrl) {
		this.agsUrl = agsUrl;
	}

}
