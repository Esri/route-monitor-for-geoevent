using ESRI.ArcGIS.Client;
using FieldWorker.Common;
using FieldWorker.Schema;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Configuration;
using System.Windows.Data;
using System.Windows.Input;

namespace FieldWorker.ViewModels
{
  public class StopsViewModel : BaseItemsViewModel
  {
    private static string GEPUpdateRequestIdFieldName = ConfigurationManager.AppSettings.Get("GEPUpdateRequestIdFieldName");

    private string _routeName = "";
    private FeatureLayerHelper _flStopsPending   = null;
    private Stop              _addStopBeforeStop = null;
    private Stop              _editStop          = null;

    public NameValueCollection StatusRemarkLookup           { get; protected set; }
    public NameValueCollection StatusInputLookup            { get; protected set; }
    public NameValueCollection StopRelativeImagePathLookup  { get; protected set; }
    public Stop                CutStop                      { get; protected set; }
    public ICommand            CutCommand                   { get; protected set; }
    public ICommand            PasteCommand                 { get; protected set; }
    public ICommand            AddCommand                   { get; protected set; }
    public ICommand            EditCommand                  { get; protected set; }

    /*
    public StopsViewModel() :
      base(null)
    {
      Init();
    }
    */

    public StopsViewModel(string routeName, FeatureLayerHelper flStops, FeatureLayerHelper flStopsPending):
      base(flStops)
    {
      Init();
      _routeName = routeName;
      _flStopsPending = flStopsPending;
    }

    private void Init()
    {
      TrackIdFieldName = "OBJECTID";
      GroupByFieldName = null;
      SortByFieldName1 = Stop.SequenceFieldAlias;
      SortByFieldName2 = null;
      SortByFieldOrder1 = System.ComponentModel.ListSortDirection.Ascending;
      SortByFieldOrder2 = System.ComponentModel.ListSortDirection.Ascending;

      // Read Configuration Settings
      StatusRemarkLookup          = (NameValueCollection)ConfigurationManager.GetSection("stopConfigurationGroup/statusRemarkLookupSection");
      StatusInputLookup           = (NameValueCollection)ConfigurationManager.GetSection("stopConfigurationGroup/statusInputLookupSection");
      StopRelativeImagePathLookup = (NameValueCollection)ConfigurationManager.GetSection("stopConfigurationGroup/iconLookupSection");

      // init commands
      CutCommand   = new DelegatingCommand(OnCut);
      PasteCommand = new DelegatingCommand(OnPaste);
      AddCommand   = new DelegatingCommand(OnAdd);
      EditCommand  = new DelegatingCommand(OnEdit);
    }

    override protected Item CreateItem()
    {
      Item stop = new Stop(this);
      return stop;
    }

    protected void OnCut(object stopObject)
    {
      Stop stop = stopObject as Stop;
      if (stop == null)
        return;

      CutStop = stop;
    }

    protected void OnPaste(object stopObject)
    {
      Stop beforeStop = stopObject as Stop;
      if (beforeStop == null)
        return;

      MoveStop(CutStop, beforeStop);
    }

    protected void OnAdd(object stopObject)
    {
      _addStopBeforeStop = stopObject as Stop;
    }

    protected void OnEdit(object stopObject)
    {
      _editStop = stopObject as Stop;
    }

    public bool AddStop(int durationInMinutes)
    {
      if (_addStopBeforeStop == null)
        return false;

      Graphic newStop = CreateNewStopToAdd(_addStopBeforeStop, durationInMinutes);
      if (newStop == null)
        return false;

      return SendStopToGEP(newStop, "Add Break");
    }

    public bool EditStop(int durationInMinutes)
    {
      if (_editStop == null)
        return false;

      Graphic newStop = CreateNewStopToEdit(_editStop, durationInMinutes);
      if (newStop == null)
        return false;

      return SendStopToGEP(newStop, "Edit Break");
    }

    private bool MoveStop(Stop stopToMove, Stop beforeStop)
    {
      //return CalculateRoute(stopToMove, beforeStop, false);
      //return AddMovedStopToPending(stopToMove, beforeStop);
      return SendMovedStopToGEP(stopToMove, beforeStop);
    }

    private bool AddMovedStopToPending(Stop stopToMove, Stop beforeStop)
    {
      Graphic newStop = CreateNewStopToMove(stopToMove, beforeStop);
      if (newStop == null)
        return false;

      _flStopsPending.AddFeature(newStop, false);
      _flStopsPending.SaveEdits();
      _flStopsPending.Update();
      return true;
    }

    private bool SendMovedStopToGEP(Stop stopToMove, Stop beforeStop)
    {
      Graphic newStop = CreateNewStopToMove(stopToMove, beforeStop);
      if (newStop == null)
        return false;

      return SendStopToGEP(newStop, "Move Stop");
    }

    private bool SendStopToGEP(Graphic stop, string action)
    {
      JObject featureJson = Helper.GraphicToGenericJson(stop);
      featureJson[GEPUpdateRequestIdFieldName] = GenerateRequestId(action);
      string httpRequestBodyJSON = featureJson.ToString();

      string uri = AppSettings.GetUpdateStopUrl() + ".json";
      PostHttpRequest(uri, httpRequestBodyJSON, action);

      return true;
    }

    private Graphic CreateNewStopToMove(Stop stopToMove, Stop beforeStop)
    {
      string sequenceFieldAlias = Stop.SequenceFieldAlias;
      string sequenceFieldName = beforeStop.AliasToName(sequenceFieldAlias);
      if (stopToMove.Graphic == null || !stopToMove.Graphic.Attributes.ContainsKey(sequenceFieldName))
        return null;
      if (beforeStop.Graphic == null || !beforeStop.Graphic.Attributes.ContainsKey(sequenceFieldName))
        return null;

      object newSequence = CalculateMovedStopNewSequence(stopToMove, beforeStop);
      if (newSequence == null)
        return null;

      Graphic newStop = Helper.CloneGraphic(stopToMove.Graphic);
      newStop.Attributes[sequenceFieldName] = newSequence;

      return newStop;
    }

    private object CalculateMovedStopNewSequence(Stop stopToMove, Stop beforeStop)
    {
      if (stopToMove == null)
        return null;
      if (beforeStop == null)
        return null;

      string sequenceFieldAlias = Stop.SequenceFieldAlias;

      ICollectionView cv = CollectionViewSource.GetDefaultView(Items);
      int newSequence = 0;
      foreach (Stop stop in cv)
      {
        if (stop == beforeStop)
          return stopToMove.ConvertValueByPropertyType(sequenceFieldAlias, newSequence);
        if (stop != stopToMove)
          newSequence++;
      }

      return null;
    }

    private Graphic CreateNewStopToAdd(Stop beforeStop, int durationInMinutes)
    {
      string sequenceFieldAlias = Stop.SequenceFieldAlias;
      string sequenceFieldName = beforeStop.AliasToName(sequenceFieldAlias);
      if (beforeStop.Graphic == null || !beforeStop.Graphic.Attributes.ContainsKey(sequenceFieldName))
        return null;

      Graphic newStop = Helper.CloneGraphic(beforeStop.Graphic);

      // sequence
      object newSequence = beforeStop.GetPropertyValue(sequenceFieldAlias);
      newStop.Attributes[sequenceFieldName] = newSequence;

      // name
      string idFieldAlias = Stop.IdFieldAlias;
      string idFieldName = beforeStop.AliasToName(idFieldAlias);
      string newName = "Break-" + _routeName + "-Created-" + DateTime.Now.ToString("MM_dd-HH_mm_ss");
      newStop.Attributes[idFieldName] = newName;

      // type
      string typeFieldAlias = Stop.TypeFieldAlias;
      string typeFieldName = beforeStop.AliasToName(typeFieldAlias);
      string typeBreak = Stop.BreakType;
      newStop.Attributes[typeFieldName] = typeBreak;

      // scheduled duration
      string scheduledDurationFieldAlias = Stop.ScheduledDurationFieldAlias;
      string scheduledDurationFieldName = beforeStop.AliasToName(scheduledDurationFieldAlias);
      newStop.Attributes[scheduledDurationFieldName] = durationInMinutes;

      return newStop;
    }

    private Graphic CreateNewStopToEdit(Stop stop, int durationInMinutes)
    {
      if (stop.Graphic == null)
        return null;

      Graphic newStop = Helper.CloneGraphic(stop.Graphic);

      // scheduled duration
      string scheduledDurationFieldAlias = Stop.ScheduledDurationFieldAlias;
      string scheduledDurationFieldName = stop.AliasToName(scheduledDurationFieldAlias);
      newStop.Attributes[scheduledDurationFieldName] = durationInMinutes;

      return newStop;
    }

    private bool CalculateRoute(Stop stopToMove, Stop beforeStop, bool bOptimize)
    {
      if (stopToMove == null)
        return false;
      if (beforeStop == null)
        return false;

      string fromRouteName = stopToMove.GetPropertyValueAsString(Stop.RouteNameFieldAlias);
      string toRouteName   = beforeStop.GetPropertyValueAsString(Stop.RouteNameFieldAlias);

      // create a list of stops in the new order
      List<string> stops = new List<string>();

      ICollectionView cv = CollectionViewSource.GetDefaultView(Items);
      foreach (Stop stop in cv)
      {
        if (stop == beforeStop)
          stops.Add("\"" + stopToMove.GetPropertyValueAsString(Stop.IdFieldAlias) + "\"");
        if (stop != stopToMove)
          stops.Add("\"" + stop.GetPropertyValueAsString(Stop.IdFieldAlias) + "\"");
      }

      // return if there is nothing to do
      if (stops == null)
        return true;

      // create a list of requests
      List<string> calculateRequests = new List<string>();
      calculateRequests.Add(FormulateHttpRouteRequestBody(fromRouteName, stops, false));

      // merge all requests to one request
      string requestId = GenerateRequestId("Calculate-Routes");
      string httpRequestBodyJSON = "{\"route\":[";
      httpRequestBodyJSON += String.Join(",", calculateRequests);
      httpRequestBodyJSON += "],\"commit\":true,\"RequestId\":\"" + requestId + "\"}";
      string uri = AppSettings.GetCalculateRoutesUrl() + ".json";
      PostHttpRequest(uri, httpRequestBodyJSON, "Calculate Routes");

      return true;
    }

    private string GenerateRequestId(string forAction)
    {
      //return Guid.NewGuid().ToString();
      return _routeName;
    }

    private string FormulateHttpRouteRequestBody(string routeName, List<string> stops, bool bOptimize)
    {
      string stopsString = String.Join(",", stops);
      string request = "{\"routeName\":\"" + routeName + "\",\"stops\":[" + stopsString + "],\"optimize\":" + bOptimize.ToString().ToLower() + "}";
      return request;
    }

    private string PostHttpRequest(string uri, string httpRequestBodyJSON, string logCaption)
    {
      using (new WaitCursor())
      {
        HttpRequest request = new HttpRequest(uri, "POST", "application/json", 30000, httpRequestBodyJSON);

        if (logCaption != null)
          Log.Trace(logCaption + " ...");

        HttpResponse response = request.ExecuteHttpRequest();
        if (response.ReportOnGeoEventProcessorError(logCaption + " error"))
          return null;

        return request.Response.Text;
      }
    }

  }
}
