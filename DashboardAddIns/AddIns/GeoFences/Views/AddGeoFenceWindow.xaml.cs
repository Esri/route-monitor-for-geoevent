using System;
using System.Collections.Generic;
using System.Windows;
using System.Windows.Controls;
using AddInsShare.Common;
using AddInsShare.Schema;
using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Geometry;
using ESRI.ArcGIS.Client.Symbols;
using ESRI.ArcGIS.Client.Tasks;
using GeoFences.ViewModels;
using Newtonsoft.Json.Linq;


namespace GeoFences.Views
{
  //[DataContract]
  public partial class AddGeoFenceWindow : Window
  {
    private GeoFenceWidget    _parentWidget  = null;
    private GeoFenceViewModel _vm            = null;
    private Draw              _drawObject    = null;
    private LineSymbol        _lineSymbol    = null;
    private FillSymbol        _fillSymbol    = null;
    private FillSymbol        _saFillSymbol  = null;
    private LineSymbol        _saLineSymbol  = null;
    private Symbol            _activeSymbol  = null;
    private GraphicsLayer     _graphicsLayer = null;
    private bool              _bInitDone     = false;

    private GeometryService           _geometryService  = null;

    private RouteTask                 _routeTask                = null;
    private GraphicsLayer             _facilitiesGraphicsLayer  = null;
    private GraphicsLayer             _barriersGraphicsLayer    = null;
    private List<Graphic>             _pointBarriers            = null;
    private List<Graphic>             _polylineBarriers         = null;
    private List<Graphic>             _polygonBarriers          = null;
    private Random                    _random                   = new Random();

   EventHandler<DrawEventArgs> _drawCompleteEventHandler = null;


    public Map Map
    {
      get
      {
        if (_parentWidget == null || _parentWidget.MapWidget == null)
          return null;
        
        return _parentWidget.MapWidget.Map;
      }
    }


    public AddGeoFenceWindow(GeoFenceWidget parentWidget)
    {
      InitializeComponent();

      _parentWidget = parentWidget;
      _vm           = _parentWidget.DataGridViewModel;
      _bInitDone    = Init();

      Map.MouseClick += Map_MouseClick;
    }

    void Map_MouseClick(object sender, Map.MouseEventArgs e)
    {
      if (rbCalculateDistance.IsChecked == true)
        CalculateDistance(e.MapPoint);
      else if (rbCalculateDriveTime.IsChecked == true)
        CalculateDriveTime(e.MapPoint);
    }

    public bool IsReady()
    {
      return _bInitDone;
    }

    private bool Init()
    {
      if (!InitUI())
        return false;

      if (!CreateGraphicsLayer())
        return false;

      // create the symbols
      _lineSymbol = new SimpleLineSymbol(System.Windows.Media.Colors.Red, 4) as LineSymbol;
      _fillSymbol = new SimpleFillSymbol()
      {
        //Fill = Brushes.Yellow,
        Fill = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromArgb(100, 255, 255, 0)),
        BorderBrush = System.Windows.Media.Brushes.Green,
        BorderThickness = 1
      } as FillSymbol;

      _saFillSymbol = new SimpleFillSymbol()
      {
        //Fill = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromArgb(100, 90, 90, 90)),  // gray
        Fill = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromArgb(100, 255, 255, 0)),
        //BorderBrush = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Colors.Transparent),
        BorderBrush = System.Windows.Media.Brushes.Green,
        BorderThickness = 1
      };

      _saLineSymbol = new SimpleLineSymbol()
      {
        Color = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromArgb(100, (byte)_random.Next(0, 255), (byte)_random.Next(0, 255), (byte)_random.Next(0, 255))),
        Width = 1,
      };


      // create the Geometry Service
      _geometryService = new GeometryService(_vm.GetPropValue("GeometryServiceUrl"));
      _geometryService.BufferCompleted += GeometryService_BufferCompleted;
      _geometryService.Failed += GeometryService_Failed;


      // create the route task
      _facilitiesGraphicsLayer = new GraphicsLayer();
      _barriersGraphicsLayer = new GraphicsLayer();
      _pointBarriers = new List<Graphic>();
      _polylineBarriers = new List<Graphic>();
      _polygonBarriers = new List<Graphic>();
      string serviceAreaURL = _vm.GetPropValue("ServiceAreaServiceUrl");
      _routeTask = new RouteTask(serviceAreaURL);
      _routeTask.SolveServiceAreaCompleted += SolveServiceArea_Completed;
      _routeTask.Failed += SolveServiceArea_Failed;


      return true;
    }

    private bool InitUI()
    {
      if (!PopulateCategoriesComboBox())
      {
        Log.Trace("Add GeoFence Window - could not populate the Categories ComboBox.");
        //return false;
      }

      return true;
    }

    private bool PopulateCategoriesComboBox()
    {
      comboCategories.Items.Clear();

      if (_parentWidget == null || _parentWidget == null)
        return false;

      if (_vm == null)
        return false;

      if (_vm.CategoryFieldName == null)
      {
        Log.Trace("Add GeoFence Window - GeoFence Widget's Category Field Name configuration is missing.");
        System.Windows.MessageBox.Show("Category Field Name configuration is missing.", "GeoFence Widget Error");
        return false;
      }

      HashSet<string> categoriesSet = new HashSet<string>();
      foreach (Item geoFence in _vm.Items)
      {
        object categoryObj = geoFence.GetPropertyValue(_vm.CategoryFieldName);
        if (categoryObj == null)
          continue;

        string category = categoryObj.ToString();
        if (!String.IsNullOrEmpty(category))
          categoriesSet.Add(category);
      }

      foreach (String category in categoriesSet)
      {
        ComboBoxItem item = new ComboBoxItem();
        item.Content = category;
        comboCategories.Items.Add(item);
      }

      comboCategories.SelectedIndex = 0;
      return true;
    }

    private bool CreateGraphicsLayer()
    {
      // make sure to create only once
      if (_graphicsLayer != null)
        return false;
      if (Map == null)
        return false;

      _graphicsLayer = Map.Layers["AddGeoFenceLayer"] as GraphicsLayer;
      if (_graphicsLayer == null)
      {
        _graphicsLayer = new GraphicsLayer();
        _graphicsLayer.ID = "AddGeoFenceLayer";
        Map.Layers.Add(_graphicsLayer);
        _graphicsLayer = Map.Layers["AddGeoFenceLayer"] as GraphicsLayer;
      }

      return true;
    }

    private void RemoveGraphicsLayer()
    {
      GraphicsLayer graphicsLayer = Map.Layers["AddGeoFenceLayer"] as GraphicsLayer;
      if (graphicsLayer == null)
        return;

      Map.Layers.Remove(graphicsLayer);
      _graphicsLayer = null;
    }

    private void EnableDraw(DrawMode drawMode, bool bActivateFillSymbol)
    {
      // create a draw object
      if (_drawObject == null)
      {
        _drawObject = new Draw(Map) { LineSymbol = _lineSymbol, FillSymbol = _fillSymbol };

        if (_drawCompleteEventHandler == null)
          _drawCompleteEventHandler = new EventHandler<DrawEventArgs>(MyDrawObject_DrawComplete);

        _drawObject.DrawComplete += _drawCompleteEventHandler;
      }

      if (bActivateFillSymbol)
        _activeSymbol = _drawObject.FillSymbol;
      else
        _activeSymbol = _drawObject.LineSymbol;

      _drawObject.DrawMode  = drawMode;
      _drawObject.IsEnabled = true;
    }

    private void DisableDraw()
    {
      if (_drawObject != null)
      {
        _drawObject.IsEnabled = false;

        if (_drawCompleteEventHandler != null)
          _drawObject.DrawComplete -= _drawCompleteEventHandler;

        _drawObject = null;
      }
    }

    private void MyDrawObject_DrawComplete(object sender, DrawEventArgs e)
    {
      Geometry geometry = e.Geometry;
      //geometry = Helper.ProjectGeometryToGeographic(e.Geometry);

      if (geometry is Envelope)
        geometry = Helper.EnvelopeToPolygon(geometry);
      else if (geometry is Polyline)
        geometry = Helper.PolylineToPolygon(geometry);

      Graphic graphic = new Graphic()
      {
        Geometry = geometry,
        Symbol   = _activeSymbol,
      };

      _graphicsLayer.Graphics.Add(graphic);
      DisableDraw();
    }

    private void btnDrawRectangle_Click(object sender, RoutedEventArgs e)
    {
      DisableDraw();
      EnableDraw(DrawMode.Rectangle, true);
    }

    private void btnDrawCircle_Click(object sender, RoutedEventArgs e)
    {
      DisableDraw();
      EnableDraw(DrawMode.Circle, true);
    }

    private void btnDrawEllipse_Click(object sender, RoutedEventArgs e)
    {
      DisableDraw();
      EnableDraw(DrawMode.Ellipse, true);
    }

    private void btnDrawPolygon_Click(object sender, RoutedEventArgs e)
    {
      DisableDraw();
      EnableDraw(DrawMode.Polygon, true);
    }

    private void btnDrawFreehand_Click(object sender, RoutedEventArgs e)
    {
      DisableDraw();
      EnableDraw(DrawMode.Freehand, true);
    }

    private void btnSave_Click(object sender, RoutedEventArgs e)
    {
      String name = tbName.Text;
      String category = comboCategories.Text;
      if (String.IsNullOrEmpty(name) || String.IsNullOrEmpty(category))
      {
        System.Windows.MessageBox.Show("Name or Category are missing.", "Add GeoFence Error");
        return;
      }
      if (_graphicsLayer.Graphics.Count == 0)
      {
        System.Windows.MessageBox.Show("No GeoFences were digitized.", "Add GeoFence Error");
        return;
      }
      if (_parentWidget == null || _parentWidget._dataSource == null || _vm == null || _vm.GetFeatureLayer() == null)
      {
        System.Windows.MessageBox.Show("Feature layer not ready.", "Add GeoFence Error");
        return;
      }

      // update the attributes on the graphics layer
      if (String.IsNullOrEmpty(_vm.TrackIdFieldName) || String.IsNullOrEmpty(_vm.NameFieldName) || String.IsNullOrEmpty(_vm.CategoryFieldName) || String.IsNullOrEmpty(_vm.ActiveFieldName))
      {
        System.Windows.MessageBox.Show("'TrackId', 'Name', 'Category', or 'Active' Field is not defined.", "Add GeoFence Error");
        return;
      }

      foreach (Graphic graphic in _graphicsLayer.Graphics)
      {
        graphic.Attributes[_vm.TrackIdFieldName]  = category + "/" + name;
        graphic.Attributes[_vm.NameFieldName]     = name;
        graphic.Attributes[_vm.CategoryFieldName] = category;
        graphic.Attributes[_vm.ActiveFieldName]   = "true";
      }

      // add new graphics to the feature layer
      FeatureLayer featureLayer = _vm.GetFeatureLayer();
      foreach (Graphic graphic in _graphicsLayer.Graphics)
      {
        Graphic newGraphic = new Graphic();
        newGraphic.Geometry = graphic.Geometry;

        // attributes
        foreach (String attribName in graphic.Attributes.Keys)
          newGraphic.Attributes[attribName] = graphic.Attributes[attribName];

        // add the graphic
        featureLayer.Graphics.Add(newGraphic);
      }

      featureLayer.SaveEdits();
      featureLayer.Update();
      Close();
      return;

      /*
      // add the features directly to the feature service
      FeatureSet fs = new FeatureSet(_graphicsLayer.Graphics);
      String fsJsonString = fs.ToJson();
      JObject jObjFeatureSet = JObject.Parse(fsJsonString);
      JArray jArrayGeometries = jObjFeatureSet["features"] as JArray;
      String httpRequestBodyJSON = "f=json&features=" + jArrayGeometries.ToString();

      // HTTP POST the new GeoFences
      String uri = featureLayer.Url + "/addFeatures";
      HttpRequest request = new HttpRequest(uri, "POST", "application/x-www-form-urlencoded", 10000, httpRequestBodyJSON);
      HttpResponse response = request.ExecuteHttpRequest();
      if (response.ReportOnArcGISServerError("Add GeoFence error"))
        return;

      // refresh the layer from the feature service REST endpoint
      featureLayer.Update();

      Close();
      */
    }

    private void btnClear_Click(object sender, RoutedEventArgs e)
    {
      _graphicsLayer.ClearGraphics();
    }

    private void btnRemoveLast_Click(object sender, RoutedEventArgs e)
    {
      if (_graphicsLayer.Graphics.Count > 0)
        _graphicsLayer.Graphics.RemoveAt(_graphicsLayer.Graphics.Count - 1);
    }

    protected override void OnClosing(System.ComponentModel.CancelEventArgs e)
    {
      DisableDraw();
      RemoveGraphicsLayer();
      _parentWidget._bAddWidgetWindowOpen = false;
    }

    private void OnAddMethodChanged(object sender, RoutedEventArgs e)
    {
      if (rbDigitize.IsChecked == true)
      {
        return;
      }
      if (rbCalculateDistance.IsChecked == true)
      {
        DisableDraw();
        return;
      }
      if (rbCalculateDriveTime.IsChecked == true)
      {
        DisableDraw();
      }
    }

    private void CalculateDistance(MapPoint mapPoint)
    {
      double dDistance = 0;
      if (Double.TryParse(tbDistanceValue.Text, out dDistance) == false)
        return;
      if (dDistance <= 0)
        return;

      LinearUnit unit = EncodeLinearUnit(comboDistanceUnit.SelectionBoxItem.ToString());

      _geometryService.CancelAsync();

      Graphic clickGraphic = new Graphic();
      //clickGraphic.Symbol = LayoutRoot.Resources["DefaultMarkerSymbol"] as ESRI.ArcGIS.Client.Symbols.Symbol;
      clickGraphic.Geometry = mapPoint;

      // Input spatial reference for buffer operation defined by first feature of input geometry array
      clickGraphic.Geometry.SpatialReference = Map.SpatialReference;

      // If buffer spatial reference is GCS and unit is linear, geometry service will do geodesic buffering
      ESRI.ArcGIS.Client.Tasks.BufferParameters bufferParams = new ESRI.ArcGIS.Client.Tasks.BufferParameters()
      {
        BufferSpatialReference = new SpatialReference(4326),
        OutSpatialReference = Map.SpatialReference,
        Unit = unit,
      };
      bufferParams.Distances.Add(dDistance);
      bufferParams.Features.Add(clickGraphic);

      _geometryService.BufferAsync(bufferParams);
    }

    private void CalculateDriveTime(MapPoint mapPoint)
    {
      double dDriveTime = 0;
      if (Double.TryParse(tbDriveTimeValue.Text, out dDriveTime) == false)
        return;
      if (dDriveTime <= 0)
        return;

      // make sure the facilities graphic layer has the only one new graphic, based on the mapPoint click location
      Graphic clickGraphic = new Graphic();
      //clickGraphic.Symbol = LayoutRoot.Resources["DefaultMarkerSymbol"] as ESRI.ArcGIS.Client.Symbols.Symbol;
      clickGraphic.Geometry = mapPoint;
      int facilityNumber = 1;
      clickGraphic.Attributes.Add("FacilityNumber", facilityNumber);
      _facilitiesGraphicsLayer.Graphics.Clear();
      _facilitiesGraphicsLayer.Graphics.Add(clickGraphic);

      GenerateBarriers();

      try
      {
        RouteServiceAreaParameters routeParams = new RouteServiceAreaParameters()
        {
          Facilities = _facilitiesGraphicsLayer.Graphics,
          DefaultBreaks = tbDriveTimeValue.Text,
          TravelDirection = EncodeFacilityTravelDirections(comboFacilityTravelDirection.SelectionBoxItem.ToString()),
          //Barriers = _pointBarriers.Count > 0 ? _pointBarriers : null,
          //PolylineBarriers = _polylineBarriers.Count > 0 ? _polylineBarriers : null,
          //PolygonBarriers = _polygonBarriers.Count > 0 ? _polygonBarriers : null,

          //ExcludeSourcesFromPolygons = null,
          //MergeSimilarPolygonRanges = false,
          //OutputLines = EncodeOutputLines("None"), //"esriNAOutputLineNone"
          //OverlapLines = true,
          //OverlapPolygons = true,
          //SplitLineAtBreaks = false,
          //SplitPolygonsAtBreaks = true,

          OutputPolygons = EncodeOutputPolygons("Simplified"), //"esriNAOutputPolygonSimplified",

          TrimOuterPolygon = true,
          TrimPolygonDistance = 100.0,
          TrimPolygonDistanceUnits = esriUnits.esriMeters,
          ReturnFacilities = false,
          ReturnBarriers = false,
          ReturnPolylineBarriers = false,
          ReturnPolygonBarriers = false,
          OutSpatialReference = Map.SpatialReference,

          AccumulateAttributes = null,
          //ImpedanceAttribute = "TravelTime"
          //RestrictionAttributes = "Non-routeable segments,Avoid passenger ferries,TurnRestriction,OneWay".Split(','),
          //RestrictUTurns = EncodeRestrictUTurns("Allow Backtrack"), //"esriNFSBAllowBacktrack"
          OutputGeometryPrecision = 100.0,
          OutputGeometryPrecisionUnits = esriUnits.esriMeters
        };

        if (_routeTask.IsBusy)
          _routeTask.CancelAsync();

        _routeTask.SolveServiceAreaAsync(routeParams);
      }
      catch (Exception ex)
      {
        MessageBox.Show(ex.Message + '\n' + ex.StackTrace);
      }
    }

    private LinearUnit EncodeLinearUnit(string linearUnitString)
    {
      LinearUnit result = LinearUnit.Meter;
      switch (linearUnitString.ToLower())
      {
        case "meters":
          result = LinearUnit.Meter;
          break;
        case "kilometers":
          result = LinearUnit.Kilometer;
          break;
        case "feet":
          result = LinearUnit.Foot;
          break;
        case "miles":
          result = LinearUnit.StatuteMile;
          break;
        default:
          break;
      }
      return result;
    }

    private string EncodeOutputLines(string outputLineSelection)
    {
      string result = "esriNAOutputLineNone";
      switch (outputLineSelection.ToLower())
      {
        case "none":
          result = "esriNAOutputLineNone";
          break;
        case "true shape":
          result = "esriNAOutputLineTrueShape";
          break;
        default:
          break;
      }
      return result;
    }

    private string EncodeOutputPolygons(string outputPolygonSelection)
    {
      string result = "esriNAOutputPolygonNone";
      switch (outputPolygonSelection.ToLower())
      {
        case "none":
          result = "esriNAOutputPolygonNone";
          break;
        case "simplified":
          result = "esriNAOutputPolygonSimplified";
          break;
        case "detailed":
          result = "esriNAOutputPolygonDetailed";
          break;
        default:
          break;
      }
      return result;
    }

    private FacilityTravelDirection EncodeFacilityTravelDirections(string directionSelection)
    {
      FacilityTravelDirection ftd = FacilityTravelDirection.TravelDirectionToFacility;
      switch (directionSelection.ToLower())
      {
        case "to facility":
          ftd = FacilityTravelDirection.TravelDirectionToFacility;
          break;
        case "from facility":
          ftd = FacilityTravelDirection.TravelDirectionFromFacility;
          break;
        default:
          break;
      }
      return ftd;
    }

    private esriUnits EncodeUnits(string unitsSelection)
    {
      esriUnits units = esriUnits.esriUnknownUnits;
      switch (unitsSelection.ToLower())
      {
        case "unknown":
          units = esriUnits.esriUnknownUnits;
          break;
        case "decimal degrees":
          units = esriUnits.esriDecimalDegrees;
          break;
        case "kilometers":
          units = esriUnits.esriKilometers;
          break;
        case "meters":
          units = esriUnits.esriMeters;
          break;
        case "miles":
          units = esriUnits.esriMiles;
          break;
        case "nautical miles":
          units = esriUnits.esriNauticalMiles;
          break;
        case "inches":
          units = esriUnits.esriInches;
          break;
        case "points":
          units = esriUnits.esriPoints;
          break;
        case "feet":
          units = esriUnits.esriFeet;
          break;
        case "yards":
          units = esriUnits.esriYards;
          break;
        case "millimeters":
          units = esriUnits.esriMillimeters;
          break;
        case "centimeters":
          units = esriUnits.esriCentimeters;
          break;
        case "decimeters":
          units = esriUnits.esriDecimeters;
          break;
        default:
          break;
      }
      return units;
    }

    private string EncodeRestrictUTurns(string restrictUTurns)
    {
      string result = "esriNFSBAllowBacktrack";
      switch (restrictUTurns.ToLower())
      {
        case "allow backtrack":
          result = "esriNFSBAllowBacktrack";
          break;
        case "at dead ends only":
          result = "esriNFSBAtDeadEndsOnly";
          break;
        case "no backtrack":
          result = "esriNFSBNoBacktrack";
          break;
        default:
          break;
      }
      return result;
    }

    private void GenerateBarriers()
    {
      foreach (Graphic g in _barriersGraphicsLayer)
      {
        Type gType = g.Geometry.GetType();

        if (gType == typeof(MapPoint))
          _pointBarriers.Add(g);
        else if (gType == typeof(Polyline))
          _polylineBarriers.Add(g);
        else if (gType == typeof(Polygon) || gType == typeof(Envelope))
          _polygonBarriers.Add(g);
      }
    }

    private void GeometryService_BufferCompleted(object sender, GraphicsEventArgs args)
    {
      Graphic bufferGraphic = new Graphic();
      bufferGraphic.Geometry = args.Results[0].Geometry;
      bufferGraphic.Symbol = _fillSymbol; //LayoutRoot.Resources["BufferSymbol"] as ESRI.ArcGIS.Client.Symbols.Symbol;
      bufferGraphic.SetZIndex(1);

      _graphicsLayer.Graphics.Add(bufferGraphic);
    }

    private void GeometryService_Failed(object sender, TaskFailedEventArgs args)
    {
      MessageBox.Show("Geometry service failed: " + args.Error, "Add GeoFence Error");
    }

    private void SolveServiceArea_Completed(object sender, RouteEventArgs e)
    {
      if (e.ServiceAreaPolygons != null)
      {
        foreach (Graphic g in e.ServiceAreaPolygons)
        {
          g.Symbol = _saFillSymbol;
          if (_graphicsLayer != null)
            _graphicsLayer.Graphics.Add(g);
        }
      }

      if (e.ServiceAreaPolylines != null)
      {
        foreach (Graphic g in e.ServiceAreaPolylines)
        {
          g.Symbol = _saLineSymbol;
          if (_graphicsLayer != null)
            _graphicsLayer.Graphics.Add(g);
        }
      }
    }

    private void SolveServiceArea_Failed(object sender, TaskFailedEventArgs e)
    {
      MessageBox.Show("Network Analysis service failed: " + e.Error.Message, "Add GeoFence Error");
    }


  } // end class
} // end namespace
