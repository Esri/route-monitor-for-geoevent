package com.esri.ges.manager.messages;

import com.esri.ges.core.Uri;
import com.esri.ges.core.geoevent.GeoEvent;

public interface MessagesManager
{
  public GeoEvent createGeoEvent(Message message, String ownerId, Uri uri);
  public void clearAllMessageFeatures(String agsConnectionName, String path, String featureService, String layer);
}
