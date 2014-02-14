using System;
using System.Collections.Generic;
using AddInsShare.Common;
using AddInsShare.Schema;
using AddInsShare.ViewModels;
using ESRI.ArcGIS.Client;
using OD = ESRI.ArcGIS.OperationsDashboard;

namespace GeoFences.ViewModels
{
  class GeoFenceViewModel : BaseDataGridViewModel
  {
    public DelegatingCommand ToggleActiveCommand { get; protected set; }

    public String NameFieldName     { get; set; }
    public String CategoryFieldName { get; set; }
    public String ActiveFieldName   { get; set; }


    public GeoFenceViewModel(OD.DataSource dataSource)
      : base(dataSource)
    {
      // init the data members with default values
      TrackIdFieldName  = "GeoFenceId";
      NameFieldName     = "Name";
      CategoryFieldName = "Category";
      ActiveFieldName   = "Active";
      Properties["GeometryServiceUrl"]    = @"http://tasks.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer";
      Properties["ServiceAreaServiceUrl"] = @"http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Network/USA/NAServer/Service%20Area";

      // init the commands
      ToggleActiveCommand = new DelegatingCommand(OnToggleActive);
    }

    private void OnToggleActive(object obj)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;
      if (!item.Graphic.Attributes.ContainsKey("OBJECTID"))
        return;
      FeatureLayer featureLayer = GetFeatureLayer();
      if (featureLayer == null)
        return;
      int oid = Int32.Parse(item.Graphic.Attributes["OBJECTID"].ToString());
      if (oid == 0)
        return;

      String activeState = item.Graphic.Attributes[ActiveFieldName].ToString().ToLower();
      if (activeState.Equals("true"))
        activeState = "false";
      else
        activeState = "true";

      item.Graphic.Attributes[ActiveFieldName] = activeState;
      //featureLayer.SaveEdits();
      //featureLayer.Update();

      /*
      GraphicCollection gc = new GraphicCollection();
      gc.Add(item.Graphic);
      FeatureSet fs = new FeatureSet(gc);
      String fsJsonString = fs.ToJson();
      JObject jObjFeatureSet = JObject.Parse(fsJsonString);
      JArray jArrayFeatures = jObjFeatureSet["features"] as JArray;
      String httpRequestBodyJSON = "f=json&features=" + jArrayFeatures.ToString();
      */

      String httpRequestBodyJSON = "f=json&features=";
      httpRequestBodyJSON += "[ { \"attributes\": { \"OBJECTID\": ";
      httpRequestBodyJSON += oid;
      httpRequestBodyJSON += ", \"Active\": \"" + activeState + "\" } } ]";

      // HTTP POST the new GeoFences
      String uri = featureLayer.Url + "/updateFeatures";
      HttpRequest request = new HttpRequest(uri, "POST", "application/x-www-form-urlencoded", 10000, httpRequestBodyJSON);
      HttpResponse response = request.ExecuteHttpRequest();
      response.ReportOnArcGISServerError("Toggle Active GeoFence State error");
    }
  }
}
