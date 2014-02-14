package com.esri.ges.manager.messages.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.Uri;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.datastore.agsconnection.ArcGISServerType;
import com.esri.ges.datastore.agsconnection.Layer;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.messages.Message;
import com.esri.ges.manager.messages.MessagesManager;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;

public class MessagesManagerImpl implements MessagesManager
{
  final private static Log log = LogFactory.getLog( MessagesManagerImpl.class );
  private GeoEventCreator geoEventCreator;
  private Messaging messaging;
  private ArcGISServerConnectionManager agsConnectionManager;
  private String msgGEDName;
  private String msgGEDOwner;

  public void setMsgGEDName(String msgGEDName)
  {
    this.msgGEDName = msgGEDName;
  }

  public void setMsgGEDOwner(String msgGEDOwner)
  {
    this.msgGEDOwner = msgGEDOwner;
  }

  public void setArcGISServerConnectionManager( ArcGISServerConnectionManager manager )
  {
    this.agsConnectionManager = manager;
  }
  
  public void setMessaging( Messaging messaging )
  {
    this.messaging = messaging;
    geoEventCreator = messaging.createGeoEventCreator();
  }
  
  
  @Override
  public GeoEvent createGeoEvent(Message message, String ownerId, Uri uri)
  {
    try
    {
      GeoEvent geoEvent = geoEventCreator.create( msgGEDName, msgGEDOwner );
      geoEvent.setField( Message.MESSAGE_ID_KEY, message.getId() );
      geoEvent.setField( Message.TYPE_KEY, message.getType() );
      geoEvent.setField( Message.SUBJECT_KEY, message.getSubject() );
      geoEvent.setField( Message.MESSAGE_TIME_KEY,  message.getMessageTime() );
      geoEvent.setField( Message.MESSAGE_FROM_KEY, message.getMessageFrom() );
      geoEvent.setField( Message.MESSAGE_TO_KEY, message.getMessageTo() );
      geoEvent.setField( Message.STATUS_KEY, message.getStatus() );
      geoEvent.setField( Message.MESSAGE_BODY_KEY, message.getMessageBody() );
      geoEvent.setField( Message.CALLBACK_KEY, message.getCallback() );
      geoEvent.setProperty(GeoEventPropertyName.TYPE, "event");
      geoEvent.setProperty(GeoEventPropertyName.OWNER_ID, ownerId);
      geoEvent.setProperty(GeoEventPropertyName.OWNER_URI, uri);
      return geoEvent;
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
  }

  @Override
  public void clearAllMessageFeatures(String agsConnectionName, String path, String featureService, String layer)
  {
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection(agsConnectionName);
    Layer lyr =  agsConnection.getLayer(path, featureService, layer, ArcGISServerType.FeatureServer);
    agsConnection.deleteAllRecordsFromLayer(path , featureService, lyr.getId());
  }

}
