package com.esri.ges.manager.routemonitor.messageutil;

import java.util.Calendar;

import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.messaging.GeoEventCreator;

public class Message
{
  final public static String MESSAGE_ID_KEY   = "MESSAGE_ID";
  final public static String TYPE_KEY         = "TYPE";
  final public static String SUBJECT_KEY      = "SUBJECT";
  final public static String MESSAGE_TIME_KEY = "MESSAGE_TIME";
  final public static String MESSAGE_FROM_KEY = "MESSAGE_FROM";
  final public static String MESSAGE_TO_KEY   = "MESSAGE_TO";
  final public static String STATUS_KEY       = "STATUS";
  final public static String MESSAGE_BODY_KEY         = "MESSAGE_BODY";
  final public static String CALLBACK_KEY     = "CALLBACK";
  
  private String id;
  private String type;
  private String subject;
  private long messageTime;
  private String messageFrom;
  private String messageTo;
  private String status;
  private String messageBody;
  private String callback;
  
  public Message()
  {
    id = java.util.UUID.randomUUID().toString();
    messageTime = Calendar.getInstance().get(Calendar.MILLISECOND);
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getSubject()
  {
    return subject;
  }

  public void setSubject(String subject)
  {
    this.subject = subject;
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

  public String getStatus()
  {
    return status;
  }

  public void setStatus(String status)
  {
    this.status = status;
  }

  public String getMessageBody()
  {
    return messageBody;
  }

  public void setMessageBody(String messageBody)
  {
    this.messageBody = messageBody;
  }

  public String getCallback()
  {
    return callback;
  }

  public void setCallback(String callback)
  {
    this.callback = callback;
  }
  
  public GeoEvent convertToGeoEvent(GeoEventCreator geoEventCreator, String gedName, String gedOwner)
  {
    try
    {
      GeoEvent geoEvent = geoEventCreator.create( gedName, gedOwner );
      geoEvent.setField( MESSAGE_ID_KEY, id );
      geoEvent.setField( TYPE_KEY, type );
      geoEvent.setField( SUBJECT_KEY, subject );
      geoEvent.setField( MESSAGE_TIME_KEY,  messageTime );
      geoEvent.setField( MESSAGE_FROM_KEY, messageFrom );
      geoEvent.setField( MESSAGE_TO_KEY, messageTo );
      geoEvent.setField( STATUS_KEY, status );
      geoEvent.setField( MESSAGE_BODY_KEY, messageBody );
      geoEvent.setField( CALLBACK_KEY, callback );
      return geoEvent;
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }
}
