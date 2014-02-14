package esri.mrm.mobile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.esri.core.map.Graphic;

public class Notification implements Parcelable {
	private String id;
	private String type;
	private String subject;
	private long time;
	private String messageFrom;
	private String messageTo;
	private String status;
	private String body;
	private String callback;
	private String objectIdField;
	private int objectId;
	
	private Map<String, String> fieldAliases;
	
	private Context context;
	
  public Notification(JSONObject jobj, Context context)
  {
    this.context = context;

  }

  public Notification(Graphic graphic, Context context, String objectIdField,  Map<String, String> fieldAliases)
  {
    try
    {
      this.fieldAliases = WorkOrderUtility.sortByValue(fieldAliases);
      this.context = context;
      this.objectIdField = objectIdField;
      objectId = (Integer)graphic.getAttributeValue(objectIdField);
      
      Iterator it = graphic.getAttributes().entrySet().iterator();
      while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry)it.next();
          String alias = (String)fieldAliases.get((String)pairs.getKey());
          if(alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_ID)))
          {
            id=pairs.getValue().toString();
          }
          else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_TYPE)))
          {
            type = pairs.getValue().toString();
          }
          else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_SUBJECT)))
          {
            subject = pairs.getValue().toString();
          }
          else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_MESSAGE_TIME)))
          {
            time = (Long) pairs.getValue();
          }
          else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_MESSAGE_FROM)))
          {
            messageFrom = pairs.getValue().toString();
          }
          else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_MESSAGE_TO)))
          {
            messageTo = pairs.getValue().toString();
          }
          else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_STATUS)))
          {
            status = pairs.getValue().toString();
          }
          else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_BODY)))
          {
            body = pairs.getValue().toString();
          }
          else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_CALLBACK)))
          {
            callback = pairs.getValue().toString();
          }
      }
      
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public Map<String, Object> getUpdateAttributes()
  {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(objectIdField, objectId);
    
    Iterator it = fieldAliases.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        String alias = (String)pairs.getValue();
        if(alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_ID)))
        {
          map.put((String)pairs.getKey(), id);
        }
        else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_TYPE)))
        {
          map.put((String)pairs.getKey(), type);
        }
        else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_SUBJECT)))
        {
          map.put((String)pairs.getKey(), subject);
        }
        else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_MESSAGE_TIME)))
        {
          map.put((String)pairs.getKey(), time);
        }
        else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_MESSAGE_FROM)))
        {
          map.put((String)pairs.getKey(), messageFrom);
        }
        else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_MESSAGE_TO)))
        {
          map.put((String)pairs.getKey(), messageTo);
        }
        else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_STATUS)))
        {
          map.put((String)pairs.getKey(), status);
        }
        else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_BODY)))
        {
          map.put((String)pairs.getKey(), body);
        }
        else if (alias.equals(context.getResources().getString(R.string.ALIAS_MESSAGESLAYER_CALLBACK)))
        {
          map.put((String)pairs.getKey(), callback);
        }
    }
    return map;
  }
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(type);
		dest.writeString(subject);		
		dest.writeString(Long.toString(time));
		dest.writeString(messageFrom);
		dest.writeString(messageTo);
		dest.writeString(status);
		dest.writeString(body);
		dest.writeString(callback);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}


	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}
	
	private String getString(int id)
  {
    return context.getResources().getString(id);
  }

  public String getMessageFrom()
  {
    return messageFrom;
  }

  public void setMessageFrom(String messageFrom)
  {
    this.messageFrom = messageFrom;
  }

  public String getMessageTo()
  {
    return messageTo;
  }

  public void setMessageTo(String messageTo)
  {
    this.messageTo = messageTo;
  }

  public String getCallback()
  {
    return callback;
  }

  public void setCallback(String callback)
  {
    this.callback = callback;
  }
  
  public String getTimeString()
  {
    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
     Calendar calendar = Calendar.getInstance();
     calendar.setTimeInMillis(time);
     return formatter.format(calendar.getTime());
  }
}
