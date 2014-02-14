package esri.mrm.mobile;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.portal.Portal;

import android.app.Application;

public class AGSObjects extends Application
{
  private String vehicleId;
  private String routeId;
  private String username;
  private ArcGISFeatureLayer stopsLayer;
  private ArcGISFeatureLayer stopsPendingLayer;
  private ArcGISFeatureLayer routesLayer;
  private ArcGISFeatureLayer messagesLayer;
  private ArcGISFeatureLayer messagesPendingLayer;
  private ArcGISFeatureLayer vehiclesLayer;
  private Portal portal;

  public ArcGISFeatureLayer getVehiclesLayer()
  {
    return vehiclesLayer;
  }

  public void setVehiclesLayer(ArcGISFeatureLayer vehiclesLayer)
  {
    this.vehiclesLayer = vehiclesLayer;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public Portal getPortal()
  {
    return portal;
  }

  public void setPortal(Portal portal)
  {
    this.portal = portal;
  }

  public ArcGISFeatureLayer getMessagesLayer()
  {
    return messagesLayer;
  }

  public ArcGISFeatureLayer getMessagesPendingLayer()
  {
    return messagesPendingLayer;
  }

  public void setMessagesPendingLayer(ArcGISFeatureLayer messagesPendingLayer)
  {
    this.messagesPendingLayer = messagesPendingLayer;
  }

  public void setMessagesLayer(ArcGISFeatureLayer messagesLayer)
  {
    this.messagesLayer = messagesLayer;
  }

  public ArcGISFeatureLayer getStopsLayer()
  {
    return stopsLayer;
  }

  public void setStopsLayer(ArcGISFeatureLayer stopsLayer)
  {
    this.stopsLayer = stopsLayer;
  }

  public ArcGISFeatureLayer getStopsPendingLayer()
  {
    return stopsPendingLayer;
  }

  public void setStopsPendingLayer(ArcGISFeatureLayer stopsPendingLayer)
  {
    this.stopsPendingLayer = stopsPendingLayer;
  }

  public String getVehicleId()
  {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId)
  {
    this.vehicleId = vehicleId;
  }

  public ArcGISFeatureLayer getRoutesLayer()
  {
    return routesLayer;
  }

  public void setRoutesLayer(ArcGISFeatureLayer routesLayer)
  {
    this.routesLayer = routesLayer;
  }

  public String getRouteId()
  {
    return routeId;
  }

  public void setRouteId(String routeId)
  {
    this.routeId = routeId;
  }

  public void clear()
  {
    System.out.println("+-+-+-+-+ Clearing AGSObjects +-+-+-+-+");
    vehicleId = null;
    routeId = null;
    username = null;
    stopsLayer = null;
    stopsPendingLayer = null;
    routesLayer = null;
    messagesLayer = null;
    messagesPendingLayer = null;
    vehiclesLayer = null;
    portal = null;
  }
  
  
}
