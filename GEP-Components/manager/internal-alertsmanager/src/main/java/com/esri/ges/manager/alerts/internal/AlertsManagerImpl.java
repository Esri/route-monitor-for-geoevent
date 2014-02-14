package com.esri.ges.manager.alerts.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.datastore.agsconnection.ArcGISServerType;
import com.esri.ges.datastore.agsconnection.Layer;
import com.esri.ges.manager.alerts.AlertsManager;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;

public class AlertsManagerImpl implements AlertsManager
{
  final private static Log log = LogFactory.getLog( AlertsManagerImpl.class );

  private ArcGISServerConnectionManager agsConnectionManager;
  
  @Override
  public void clearAllAlertFeatures(String agsConnectionName, String path, String featureService, String layer)
  {
    ArcGISServerConnection agsConnection = agsConnectionManager.getArcGISServerConnection(agsConnectionName);
    Layer lyr =  agsConnection.getLayer(path, featureService, layer, ArcGISServerType.FeatureServer);
    agsConnection.deleteAllRecordsFromLayer(path , featureService, lyr.getId());
  }
  public void setArcGISServerConnectionManager( ArcGISServerConnectionManager manager )
  {
    this.agsConnectionManager = manager;
  }
}
