package com.esri.ges.manager.routes;

import java.util.Collection;
import java.util.List;

import com.esri.ges.core.Uri;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.manager.routemonitor.util.PlanStatus;

public interface RouteManager
{
  public Collection<Route> getRoutes();
  public Route getRouteByName( String name );
  public Route dispatchRoute( String name );
  public void addOrReplaceRoute( Route route );
  public void removeRoute( String name );
  public void removeAllRoutes();
  public Plan solveRoute( List<RouteWithStops> routesWithStops, String naConnection, String routeSolverPath );
  public Plan solveRouteAndCommit( List<RouteWithStops> routesWithStops, String naConnection, String routeSolverPath );
  public GeoEvent createGeoEvent( Route route, String ownerId, Uri uri);
  public void clearAllRouteFeatures(String agsConnectionName, String path, String featureService, String layer);
  public List<Route> reloadRoutes(String agsConnectionName, String path, String featureService, String layer);
  public Route convertGeoEventToRoute( GeoEvent geoEvent );
  public GeoEventDefinition getRoutesGeoEventDefinition();
  public GeoEventDefinition getRouteUpdateGeoEventDefinition();
  public GeoEventDefinition getRouteDispatchGeoEventDefinition();
  public GeoEvent resequence(GeoEvent geoEvent, String naConnection, String routeSolverPath);
  public GeoEvent createPlanGeoEvent(Plan plan, boolean updateFeatures, PlanStatus status, String message);
  public GeoEvent createUpdateRouteGeoEvent(String routeName, boolean optimize, boolean commit, String requestId, String ownerId, Uri ownerUri);
}
