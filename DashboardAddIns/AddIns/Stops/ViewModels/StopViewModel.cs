using AddInsShare.Common;
using AddInsShare.Schema;
using AddInsShare.ViewModels;
using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Geometry;
using Newtonsoft.Json.Linq;
using Stops.Views;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Data;
using System.Windows.Input;
using System.Windows.Threading;
using WebSocketSharp;
using OD = ESRI.ArcGIS.OperationsDashboard;

namespace Stops.ViewModels
{
  class StopViewModel : BaseDataGridViewModel
  {
    public Item CutStop { get; set; }

    public string StopsRouteNameFieldName { get; set; }
    public string RoutesRouteNameFieldName { get; set; }

    public ICommand DispatchCommand     { get; protected set; }
    public ICommand DispatchAllCommand  { get; protected set; }
    public ICommand UnassignCommand     { get; protected set; }
    public ICommand CutCommand          { get; protected set; }
    public ICommand PasteCommand        { get; protected set; }
    public ICommand PasteOnGroupCommand { get; protected set; }

    private DispatcherTimer _webSocketKeepAliveTimer = null;
    private DispatcherTimer _progressDialogTimer = null;
    private bool _isCalculating = false;
    public bool IsCalculating
    {
      get { return _isCalculating; }
      set { _isCalculating = value; OnPropertyChanged(() => IsCalculating); }
    }

    private string _progressMessage1 = "Loading...";
    public string ProgressMessage1
    {
      get { return _progressMessage1; }
      set { _progressMessage1 = value; OnPropertyChanged(() => ProgressMessage1); }
    }

    private string _progressMessage2 = "Please Wait...";
    public string ProgressMessage2
    {
      get { return _progressMessage2; }
      set { _progressMessage2 = value; OnPropertyChanged(() => ProgressMessage2); }
    }

    private OD.DataSource _routesDataSource = null;

    private HashSet<string>   _edits                    = null;
    private string            _calcualteRoutesRequestId = "";
    private string            _saveEditsRequestId       = "";
    private GraphicsLayer     _customLayerStops         = null;
    private GraphicsLayer     _customLayerRoutes        = null;
    private String            _unassignedRouteName      = null;
    private StopWidget        _view;

    private WebSocket              _ws            = null;
    EventHandler                   _wsOnOpenEH    = null;
    EventHandler<CloseEventArgs>   _wsOnCloseEH   = null;
    EventHandler<ErrorEventArgs>   _wsOnErrorEH   = null;
    EventHandler<MessageEventArgs> _wsOnMessageEH = null;



    public StopViewModel(OD.DataSource dataSource, OD.DataSource routesDataSource, StopWidget view)
      : base(dataSource)
    {
      // init _wsTimer
      _webSocketKeepAliveTimer = new DispatcherTimer();
      _webSocketKeepAliveTimer.Interval = new TimeSpan(0, 0, 2);
      _webSocketKeepAliveTimer.Tick += new EventHandler(OnWebSocketKeepAliveTick);

      // init _progressDialogTimer
      _progressDialogTimer = new DispatcherTimer();
      _progressDialogTimer.Tick += new EventHandler(OnProgressDialogTimeOut);

      // init the data members with default values
      StopsRouteNameFieldName   = @"RouteName";
      RoutesRouteNameFieldName  = @"Route";
      Properties["GepHostName"]   = @"localhost";
      Properties["GepHttpPort"]   = @"6180";
      Properties["GepHttpsPort"]  = @"6143";
      Properties["GEP_LOAD_PLAN_ENDPOINT"]          = @"geoevent/rest/receiver/route-command-in";
      Properties["GEP_ROUTES_CALCULATE_ENDPOINT"]   = @"geoevent/rest/receiver/route-update-in";
      Properties["GEP_ROUTES_UPDATE_ENDPOINT"]      = @"geoevent/rest/receiver/route-update-in";
      Properties["GEP_DISPATCH_ENDPOINT"]           = @"geoevent/rest/receiver/route-message-in";
      Properties["STOPS_ROUTE_NAME_FIELD_NAME"]     = @"RouteName";
      Properties["ROUTES_ROUTE_NAME_FIELD_NAME"]    = @"RouteName";
      Properties["ROUTES_UN_ASSIGNED_ROUTE_NAME"]   = @"__Unassigned__";

      _unassignedRouteName = GetPropValue("ROUTES_UN_ASSIGNED_ROUTE_NAME");
      SetRouteDataSource(routesDataSource);

      // temp
      _view = view;

      // init commands
      DispatchCommand = new DelegatingCommand(OnDispatch);
      DispatchAllCommand = new DelegatingCommand(OnDispatchAll);
      UnassignCommand = new DelegatingCommand(OnUnassign);
      CutCommand = new DelegatingCommand(OnCut);
      PasteCommand = new DelegatingCommand(OnPaste);
      PasteOnGroupCommand = new DelegatingCommand(OnPasteOnGroup);

      _edits = new HashSet<string>();

      // Web Socket Event Handlers
      _wsOnOpenEH     = new EventHandler(WebSocket_OnOpen);
      _wsOnCloseEH    = new EventHandler<CloseEventArgs>(WebSocket_OnClose);
      _wsOnErrorEH    = new EventHandler<ErrorEventArgs>(WebSocket_OnError);
      _wsOnMessageEH  = new EventHandler<MessageEventArgs>(WebSocket_OnMessage);
    }

    private void CloseWebSocket()
    {
      try
      {
        if (_ws != null)
        {
          _ws.OnOpen    -= _wsOnOpenEH;
          _ws.OnClose   -= _wsOnCloseEH;
          _ws.OnError   -= _wsOnErrorEH;
          _ws.OnMessage -= _wsOnMessageEH;
          _ws.Close();
        }
      }
      catch(Exception)
      {
      }
    }

    private void OpenWebSocket()
    {
      try
      {
        CloseWebSocket();
        _ws = new WebSocket(GetGEPWsUrl(false) + "ws");
        _ws.OnOpen += _wsOnOpenEH;
        _ws.OnClose += _wsOnCloseEH;
        _ws.OnError += _wsOnErrorEH;
        _ws.OnMessage += _wsOnMessageEH;
        _ws.Connect();
      }
      catch (Exception e)
      {
        Log.TraceException("OpenWebSocket Error: ", e);
      }
    }

    void WebSocket_OnOpen(object sender, EventArgs e)
    {
      Log.Trace("WebSocket opened");
    }

    void WebSocket_OnClose(object sender, CloseEventArgs e)
    {
      if (InEditMode)
      {
        Log.Trace("WebSocket connection closed - " + e.Reason);
        _webSocketKeepAliveTimer.Start();
      }
      else
      {
        Log.Trace("WebSocket closed - " + e.Reason);
      }
    }

    void WebSocket_OnError(object sender, ErrorEventArgs e)
    {
      Log.Trace("WebSocket error - " + e.Message);
    }

    void WebSocket_OnMessage(object sender, MessageEventArgs e)
    {
      if (!String.IsNullOrEmpty(e.Data))
      {
        Application.Current.Dispatcher.Invoke(DispatcherPriority.Normal, new Action(delegate() { this.ApplyNewRoutes(e.Data); }));
      }
    }

    public void SetRouteDataSource(OD.DataSource routeDataSource)
    {
      _routesDataSource = routeDataSource;
    }

    private void OnWebSocketKeepAliveTick(object sender, EventArgs e)
    {
      _webSocketKeepAliveTimer.Stop();
      if (InEditMode)
        MakeSureWebSocketIsOpen();
    }

    private void OnProgressDialogTimeOut(object sender, EventArgs e)
    {
      Log.Trace("Stops Operation Timed Out");
      CloseProgressDialog();
    }


    private void OnDispatchAll(object itemObject)
    {
      DispatchAll();
    }

    private void OnDispatch(object itemObject)
    {
      Item item = itemObject as Item;
      if (item == null)
        return;
      if (item.Graphic == null)
        return;

      object routeNameObj = item.GetPropertyValue(StopsRouteNameFieldName);
      if (routeNameObj == null)
        return;

      string routeName = routeNameObj.ToString();
      string httpRequestBodyJSON = FormatDispatchRoute(routeName);
      if (httpRequestBodyJSON == null)
        return;

      SendDispatchRequest(httpRequestBodyJSON);
    }

    private string FormatDispatchRoute(string routeName)
    {
      if (String.IsNullOrEmpty(routeName))
        return null;

      string dispatcher = Helper.GetDashboardUsername();
      string requestId = GenerateRequestId("Dispatch");
      string uid = Guid.NewGuid().ToString();
      string dispatchRouteString =
        String.Format("'ID':'{0}', 'Type':'Notification', 'Subject':'Dispatch', 'MessageTime':'{1}', 'MessageFrom':'{2}', 'MessageTo':'{3}', 'Status':'Open', 'Body':'Your work orders are ready for servicing.  Please review your assignments and acknowledge that you have received this Dispatch by clicking the button below.', 'Callback':''",
                       uid,
                       DateTime.Now.ToString(@"yyyy-MM-ddTHH:mm:ss.fffzzz"),
                       dispatcher,
                       routeName);

      dispatchRouteString = "{" + dispatchRouteString.Replace("'", "\"") + "}";
      Log.Trace("Dispatching route '" + routeName + "'");
      return dispatchRouteString;
    }

    private bool SendDispatchRequest(string httpRequestBodyJSON)
    {
      String uri = GetGEPUrl(false) + GetPropValue("GEP_DISPATCH_ENDPOINT") + ".json";
      HttpRequest request = new HttpRequest(uri, "POST", "application/json", 30000, httpRequestBodyJSON);
      HttpResponse response = request.ExecuteHttpRequest();
      if (response.ReportOnGeoEventProcessorError("Stops - Dispatch error"))
        return false;

      return true;
    }

    protected void OnUnassign(object itemObject)
    {
      Item item = itemObject as Item;
      if (item == null)
        return;
      if (item.Graphic == null)
        return;
      if (!InEditMode)
        return;

      UnassignStop(item, null);
    }

    protected void OnCut(object itemObject)
    {
      Item item = itemObject as Item;
      if (item == null)
        return;
      if (!InEditMode)
        return;

      CutStop = item;
    }

    protected void OnPaste(object itemObject)
    {
      Item item = itemObject as Item;
      if (item == null)
        return;
      if (!InEditMode)
        return;

      CalculateRoute(CutStop, item, null, false, Window.GetWindow(_view));
    }

    protected void OnPasteOnGroup(object itemObject)
    {
      Item item = itemObject as Item;
      if (item == null)
        return;
      if (!InEditMode)
        return;

      string routeName = item.GetPropertyValueAsString(StopsRouteNameFieldName);
      if (routeName != null)
        CalculateRoute(CutStop, null, routeName, true, Window.GetWindow(_view));
    }

    private async Task<bool> SetEditMode(bool bEditMode)
    {
      if (InEditMode == bEditMode)
        return true;

      InEditMode = bEditMode;
      _edits.Clear();
      CutStop = null;

      // the routes layer
      FeatureLayer routesFeatureLayer = FindFeatureLayer(_routesDataSource);
      if (routesFeatureLayer == null)
        return false;

      if (InEditMode)
      {
        // switch to use custom layers

        // routes
        _customLayerRoutes = await BaseDataGridViewModel.CreateCustomLayer(routesFeatureLayer, _routesDataSource);
        _map.Layers.Add(_customLayerRoutes);
        
        // temp commented out - keep showing the original feature layer until a calculating a route
        routesFeatureLayer.Visible = false;
        _customLayerRoutes.Visible = true;

        // stops
        _customLayerStops = CreateCustomLayer();
        _map.Layers.Add(_customLayerStops);
        _featureLayer.Visible     = false;
        _customLayerStops.Visible = true;

        // web socket
        OpenWebSocket();
      }
      else
      {
        // switch to use the feature service layers

        // routes
        routesFeatureLayer.Visible = true;
        _map.Layers.Remove(_customLayerRoutes);
        _customLayerRoutes = null;

        // stops
        _featureLayer.Visible = true;
        _map.Layers.Remove(_customLayerStops);
        _customLayerStops = null;

        // web socket
        CloseWebSocket();
      }

      return true;
    }

    internal async void StartEditMode()
    {
      await SetEditMode(true);
    }

    internal async void StopEditMode()
    {
      CloseProgressDialog();
      await SetEditMode(false);
      RefreshView();
    }

    internal async void SaveEdits(Window owner)
    {
      CloseProgressDialog();

      if (!InEditMode)
        return;

      if (_edits.Count == 0)
      {
        await SetEditMode(false);
        return;
      }

      SendUpdateRouteRequest(owner);
    }

    internal bool CalculateRoute(Item fromStop, Item toStop, string toGroupRouteName, bool bOptimize, Window owner)
    {
      if (!InEditMode)
        return false;
      if (fromStop == null)
        return false;
      if (toStop == null && toGroupRouteName == null)
        return false;

      String fromRouteName = fromStop.GetPropertyValue(StopsRouteNameFieldName).ToString();
      String toRouteName = toStop != null ? toStop.GetPropertyValue(StopsRouteNameFieldName).ToString() : toGroupRouteName;

      CollectionViewGroup fromGroup = FindCollectionViewGroup(fromRouteName);
      if (fromGroup == null)
        return false;
      CollectionViewGroup toGroup = FindCollectionViewGroup(toRouteName);

      Item stopToUnassign = null;
      if (toRouteName == _unassignedRouteName && fromStop != null)
        stopToUnassign = fromStop;

      // Stops for TO GROUP
      List<String> toStops = null;
      if (toGroup == null && toRouteName == _unassignedRouteName)
      {
        // adding the first stop to the _unassignedRouteName
        toStops = new List<string>();
        toStops.Add("\"" + fromStop.GetPropertyValue(TrackIdFieldName) + "\"");
      }
      else if (toGroup != null)
      {
        // add all stops:
        //    1. if dropping on group - add the new fromStop at the beginning
        //    2. if dropping on stop  - add the new fromStop before the toStop
        toStops = new List<string>();
        if (toStop == null)
        {
          // dropping on group - add the the new fromStop at the beginning
          toStops.Add("\"" + fromStop.GetPropertyValue(TrackIdFieldName) + "\"");
        }
        foreach (Item item in toGroup.Items)
        {
          // dropping on stop  - add the new fromStop before the toStop
          if (item == toStop)
            toStops.Add("\"" + fromStop.GetPropertyValue(TrackIdFieldName) + "\"");
          if (item != fromStop)
            toStops.Add("\"" + item.GetPropertyValue(TrackIdFieldName).ToString() + "\"");
        }
      }

      // Stops for FROM GROUP
      List<String> fromStops = null;
      if (fromGroup != toGroup)
      {
        fromStops = new List<string>();
        // add all items but the one that got moved
        foreach (Item item in fromGroup.Items)
        {
          if (item != fromStop)
            fromStops.Add("\"" + item.GetPropertyValue(TrackIdFieldName).ToString() + "\"");
        }
      }

      // return if there is nothing to do
      if (fromStops == null && toStops == null)
        return true;

      // from Route
      List<String> calculateRequests = new List<string>();
      if (fromStops != null)
        calculateRequests.Add(FormulateHttpRouteRequestBody(fromRouteName, fromStops, false));

      // to Route
      if (toStops != null) //_unassignedRouteName
        calculateRequests.Add(FormulateHttpRouteRequestBody(toRouteName, toStops, bOptimize));

      // merge all requests to one request
      _calcualteRoutesRequestId = GenerateRequestId("Calculate-Routes");
      String httpRequestBodyJSON = "{\"route\":[";
      httpRequestBodyJSON += String.Join(",", calculateRequests);
      httpRequestBodyJSON += "],\"commit\":false,\"RequestId\":\"" + _calcualteRoutesRequestId + "\"}";
      SendCalculateRouteRequest(httpRequestBodyJSON, owner);

      return true;
    }

    internal bool UnassignStop(Item stop, Window owner)
    {
      if (!InEditMode)
        return false;
      if (stop == null)
        return false;

      // unassign stop
      //stop.Graphic.Attributes[_stopsRouteNameFieldName] = toRouteName;
      //stop.SetPropertyValue(_stopsRouteNameFieldName, toRouteName);

      return CalculateRoute(stop, null, _unassignedRouteName, false, owner);
    }

    private string SendCalculateRouteRequest(String httpRequestBodyJSON, Window owner)
    {
      using (new WaitCursor())
      {
        ShowProgressDialog("Calculating Routes...", "Please Wait...", owner, 30);
        MakeSureWebSocketIsOpen();

        // HTTP POST GEP to calculate the route(s)
        String uri = GetGEPUrl(false) + GetPropValue("GEP_ROUTES_CALCULATE_ENDPOINT") + ".json";
        HttpRequest request = new HttpRequest(uri, "POST", "application/json", 30000, httpRequestBodyJSON);
        Log.Trace("Calculating Routes ...");
        HttpResponse response = request.ExecuteHttpRequest();
        if (response.ReportOnGeoEventProcessorError("Stops - Calculate Edits error"))
          return null;

        return request.Response.Text;
      }
    }

    private void MakeSureWebSocketIsOpen()
    {
      if (!_ws.IsAlive)
      {
         Log.Trace("WebSocket connection closed, creating a new one");
         OpenWebSocket();
      }
    }

    internal void SendUpdateRouteRequest(Window owner)
    {
      using (new WaitCursor())
      {
        if (_edits.Count == 0)
          return;

        ShowProgressDialog("Saving Edits...", "Please Wait...", owner, 30);

        List<String> routeRequests = new List<string>();
        foreach (string routeName in _edits)
        {
          CollectionViewGroup group = FindCollectionViewGroup(routeName);
          if (group == null)
            continue;

          // stops
          List<String> stops = new List<string>();
          foreach (Item item in group.Items)
            stops.Add("\"" + item.GetPropertyValue(TrackIdFieldName).ToString() + "\"");

          // formulate route request body
          routeRequests.Add(FormulateHttpRouteRequestBody(routeName, stops, false));
        }

        // merge all requests to one request
        _saveEditsRequestId = GenerateRequestId("Update-Routes");
        string httpRequestBodyJSON = "{\"route\":[";
        httpRequestBodyJSON += String.Join("," , routeRequests);
        httpRequestBodyJSON += "],\"commit\":true,\"RequestId\":\"" + _saveEditsRequestId + "\"}";

        // HTTP POST GEP to update the route(s)
        String uri = GetGEPUrl(false) + GetPropValue("GEP_ROUTES_UPDATE_ENDPOINT") + ".json";
        HttpRequest request = new HttpRequest(uri, "POST", "application/json", 30000, httpRequestBodyJSON);
        Log.Trace("Sent Update Routes Request.");
        HttpResponse response = request.ExecuteHttpRequest();
      }
    }

    private String FormulateHttpRouteRequestBody(String routeName, List<String> stops, bool bOptimize)
    {
      String stopsString = String.Join(",", stops);
      string request = "{\"routeName\":\"" + routeName + "\",\"stops\":[" + stopsString + "],\"optimize\":" + bOptimize.ToString().ToLower() + "}";
      return request;
    }

    private String FormulateHttpRouteRequestBody(List<String> stops)
    {
      String stopsString = String.Join(",", stops);
      string request = "[" + stopsString + "]";
      return request;
    }

    private void RemoveRoute(string routeName, bool bRemoveRoute, bool bRemoveStops, Item stopToKeep)
    {
      List<Graphic> graphicsToRemove = new List<Graphic>();

      // remove stops
      if (bRemoveStops)
      {
        foreach (Graphic graphic in _customLayerStops.Graphics)
        {
          if (stopToKeep != null && graphic.Attributes[TrackIdFieldName] == stopToKeep.Graphic.Attributes[TrackIdFieldName])
            continue;

          if (graphic.Attributes.ContainsKey(StopsRouteNameFieldName))
          {
            if (graphic.Attributes[StopsRouteNameFieldName].ToString().ToLower() == routeName.ToLower())
              graphicsToRemove.Add(graphic);
          }
        }
        foreach (Graphic graphicToRemove in graphicsToRemove)
        {
          _customLayerStops.Graphics.Remove(graphicToRemove);
        }
      }

      // remove routes
      if (bRemoveRoute)
      {
        foreach (Graphic graphic in _customLayerRoutes.Graphics)
        {
          if (graphic.Attributes.ContainsKey(RoutesRouteNameFieldName))
          {
            if (graphic.Attributes[RoutesRouteNameFieldName].ToString().ToLower() == routeName.ToLower())
              graphicsToRemove.Add(graphic);
          }
        }
        foreach (Graphic graphicToRemove in graphicsToRemove)
        {
          _customLayerRoutes.Graphics.Remove(graphicToRemove);
        }
      }
    }

    private async void ApplyNewRoutes(string json)
    {
      bool bErrorResponse = false;
      string requestId = AnalyzeResponse(json, true, out bErrorResponse);
      if (requestId == null)
      {
        // this is not a response to my request
        return;
      }
      if (bErrorResponse)
      {
        // this is an error response to my request
        RefreshView();
        CloseProgressDialog();
        return;
      }

      using (new WaitCursor())
      {
        try
        {
          Log.Trace("Received new Stops and Routes:\n" + json);

          // parse the new calculated route and stops
          JObject jobjRoot = JObject.Parse(json);

          //Log.Trace("Received new Stops and Routes:\n" + jobjRoot.ToString(Newtonsoft.Json.Formatting.Indented));

          IDictionary<string, JToken> responseDictionary = jobjRoot as IDictionary<string, JToken>;
          if (responseDictionary.ContainsKey("Route-Stop"))
          {
            // stops
            JArray jStops = responseDictionary["Route-Stop"] as JArray;
            foreach (JToken stop in jStops)
            {
              // add the route name to the edited set
              JToken routeNameToken = stop.SelectToken(StopsRouteNameFieldName);
              if (routeNameToken != null)
              {
                string routeName = routeNameToken.ToString();
                _edits.Add(routeName);
              }

              // remove the existing graphic from _customLayer
              JToken trackIdToken = stop.SelectToken(TrackIdFieldName);
              if (trackIdToken != null)
              {
                string trackId = trackIdToken.ToString();
                Graphic graphicFound = FindTrackIdInGraphics(trackId, _customLayerStops.Graphics, TrackIdFieldName);
                if (graphicFound != null)
                  _customLayerStops.Graphics.Remove(graphicFound);
              }

              // add a new graphic to _customLayer
              Graphic graphic = StopToGraphic(stop);
              _customLayerStops.Graphics.Add(graphic);
            }
            _customLayerStops.Refresh();

            // update the data grid to reflect the graphics in _customLayer
            Update(_customLayerStops.Graphics);
          }

          // routes
          if (responseDictionary.ContainsKey("Route-Route"))
          {
            JArray jRoutes = responseDictionary["Route-Route"] as JArray;
            foreach (JToken route in jRoutes)
            {
              // add the route name to the edited set
              JToken routeNameToken = route.SelectToken(RoutesRouteNameFieldName);
              if (routeNameToken != null)
              {
                string routeName = routeNameToken.ToString();
                _edits.Add(routeName);
                RemoveRoute(routeName, true, false, null);

                // add a new graphic to _routesCustomLayer
                Graphic graphic = RouteToGraphic(route);
                if (graphic != null)
                  _customLayerRoutes.Graphics.Add(graphic);
              }
            }
            RefreshRoutesLayer();
          }
        }
        catch (Exception)
        {
          //TODO - report an error?
        }
      }

      if (requestId == _saveEditsRequestId)
      {
        await SetEditMode(false);
        _saveEditsRequestId = "";
      }
      else if (requestId == _calcualteRoutesRequestId)
      {
        _calcualteRoutesRequestId = "";
      }

      RefreshView();
      CloseProgressDialog();
    }

    private string AnalyzeResponse(string json, bool bShowMessageBoxOnError, out bool bErrorResponse)
    {
      bErrorResponse = true;
      if (String.IsNullOrEmpty(json))
        return null;

      string requestId = null;
      try
      {
        JObject jobj = JObject.Parse(json);

        // check if this is a response to one of my requests
        JToken requestIdToken = jobj["RequestId"];
        if (requestIdToken == null)
          return null;

        requestId = requestIdToken.ToString();
        string errorMessageCaption = "Error with request " + requestId;
        if (_calcualteRoutesRequestId.Equals(requestId))
        {
          // this is a response to my calculate routes request
          errorMessageCaption = "Calculate Routes Error";
        }
        else if (_saveEditsRequestId.Equals(requestId))
        {
          // this is a response to my save edits request
          errorMessageCaption = "Save Edits Error";
        }
        else
        {
          // this is not my request, return null
          return null;
        }

        // this is a response to my request, check the status
        JToken statusToken = jobj["Status"];
        if (statusToken != null)
        {
          string status = statusToken.ToString();
          if (status.Equals("Failed"))
          {
            bErrorResponse = true;
            JToken messageToken = jobj["Message"];
            string message = (messageToken != null) ? messageToken.ToString() : "Request Failed";
            if (bShowMessageBoxOnError)
              MessageBox.Show(message, errorMessageCaption, MessageBoxButton.OK, MessageBoxImage.Error);
          }
          else
          {
            bErrorResponse = false;
          }
        }
      }
      catch(Exception)
      {
        requestId = null;
      }

      return requestId;
    }

    private Graphic RouteToGraphic(JToken route)
    {
      Graphic graphic = new Graphic();

      JToken geometryToken = route.SelectToken("shape");
      if (geometryToken == null)
        geometryToken = route.SelectToken("SHAPE");
      if (geometryToken == null)
        return null;

      Geometry geometry = null;
      try
      {
        geometry = Geometry.FromJson(geometryToken.ToString());
      }
      catch (Exception ex)
      {
        Log.TraceException("RouteToGraphic - Can't parse the returned geometry", ex);
        return null;
      }
      graphic.Geometry = Helper.ProjectGeometryToMap(geometry, _map);

      // populate attributes
      FeatureLayer featureLayer = FindFeatureLayer(_routesDataSource);
      JToken fieldToken = null;
      foreach (Field field in featureLayer.LayerInfo.Fields)
      {
        fieldToken = route.SelectToken(field.Name);
        if (fieldToken == null)
          fieldToken = route.SelectToken(field.Alias);
        if (fieldToken == null)
          continue;

        object value = Helper.ConvertGepJsonTokenToObject(fieldToken, field.Type);
        graphic.Attributes.Add(field.Name, value == null ? null : value);
      }

      return graphic;
    }

    private Graphic StopToGraphic(JToken stop)
    {
      Graphic graphic = new Graphic();

      JToken geometryToken = stop.SelectToken("shape");
      if (geometryToken == null)
        geometryToken = stop.SelectToken("SHAPE");
      if (geometryToken == null)
        return null;

      Geometry geometry = null;
      try
      {
        geometry = Geometry.FromJson(geometryToken.ToString());
      }
      catch (Exception ex)
      {
        Log.TraceException("StopToGraphic - Can't parse the returned geometry", ex);
        return null;
      }
      graphic.Geometry = Helper.ProjectGeometryToMap(geometry, _map);

      // populate attributes
      JToken fieldToken = null;
      FeatureLayer featureLayer = GetFeatureLayer();
      foreach (Field field in featureLayer.LayerInfo.Fields)
      {
        fieldToken = stop.SelectToken(field.Name);
        if (fieldToken == null)
          fieldToken = stop.SelectToken(field.Alias);
        if (fieldToken == null)
          continue;

        object value = Helper.ConvertGepJsonTokenToObject(fieldToken, field.Type);
        graphic.Attributes.Add(field.Name, value == null ? null : value);
      }

      return graphic;
    }

    private void RefreshRoutesLayer()
    {
      if (InEditMode)
      {
        if (_customLayerRoutes != null)
          _customLayerRoutes.Refresh();
      }
      else
      {
        FeatureLayer routesFeatureLayer = FindFeatureLayer(_routesDataSource);
        if (routesFeatureLayer != null)
          routesFeatureLayer.Refresh();
      }
    }

    private void RefreshStopsLayer()
    {
      if (_customLayerStops != null)
        _customLayerStops.Refresh();
    }

    internal void RefreshLayers()
    {
      RefreshRoutesLayer();
      RefreshStopsLayer();
    }

    internal bool DispatchAll()
    {
      ICollectionView cv = CollectionViewSource.GetDefaultView(Items);
      if (cv == null || cv.Groups == null)
        return false;

      List<String> requests = new List<string>();
      foreach (CollectionViewGroup group in cv.Groups)
      {
        string routeName = group.Name.ToString();
        if (routeName == _unassignedRouteName)
          continue;

        string request = FormatDispatchRoute(routeName);
        if (request == null)
          continue;

        requests.Add(request);
      }

      string httpRequestBodyJSON = "[" + String.Join(",", requests) + "]";
      return SendDispatchRequest(httpRequestBodyJSON);
    }

    internal bool ClearPlan()
    {
      string uri = GetGEPUrl(false) + GetPropValue("GEP_LOAD_PLAN_ENDPOINT") + ".json";
      string requestId = GenerateRequestId("Clear-Plan");
      string body = "{\"Action\":\"Clear\",\"PlanFolder\":\"\",\"Status\":\"\", \"RequestId\":\"" + requestId + "\"}";
      Log.Trace("Clear Plan [" + uri + "] ...");
      HttpRequest request = new HttpRequest(uri, "POST", "application/json", 300000, body);
      HttpResponse response = request.ExecuteHttpRequest();
      if (response.ReportOnGeoEventProcessorError("Stops - Clear Plan error"))
        return false;

      return true;

    }

    internal bool LoadPlan(DateTime? date)
    {
      string uri = GetGEPUrl(false) + GetPropValue("GEP_LOAD_PLAN_ENDPOINT") + ".json";
      string dateString = date.Value.ToString("yyyyMMdd");
      string requestId = GenerateRequestId("Load-Plan");
      string body = "{\"Action\":\"Load\",\"PlanFolder\":\"" + dateString + "\", \"RequestId\":\"" + requestId + "\"}";
      Log.Trace("Load Plan '" + date.ToString() + "' [" + uri + "] ...");
      HttpRequest request = new HttpRequest(uri, "POST", "application/json", 300000, body);
      HttpResponse response = request.ExecuteHttpRequest();
      if (response.ReportOnGeoEventProcessorError("Stops - Load Plan error"))
        return false;

      RefreshView();
      return true;
    }

    private string GenerateRequestId(string forAction)
    {
      //return Guid.NewGuid().ToString();
      return Helper.GetDashboardUsername();
    }

    internal void RefreshView()
    {
      // This is a workaround and will be replaced with a more MVVM solution, where the VM is not invoking methods directly on the View
      _view.UpdateView();

      // re-apply grouping and sorting, as a workaround for the groups not sorting correctly
      ApplySettings();
    }

    internal void ShowProgressDialog(string progressMessage1, string progressMessage2, Window owner, int timeoutSec)
    {
      _progressDialogTimer.Stop();
      ProgressMessage1 = progressMessage1;
      ProgressMessage2 = progressMessage2;
      IsCalculating = true;
      _progressDialogTimer.Interval = new TimeSpan(0, 0, timeoutSec);
      _progressDialogTimer.Start();

    }

    internal void CloseProgressDialog()
    {
      _progressDialogTimer.Stop();
      IsCalculating = false;
    }

  }
}
