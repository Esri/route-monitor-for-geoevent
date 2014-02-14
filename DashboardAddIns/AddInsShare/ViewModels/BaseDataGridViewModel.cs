using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Threading.Tasks;
using System.Windows.Data;
using System.Windows.Input;
using AddInsShare.Common;
using AddInsShare.Resources;
using AddInsShare.Schema;
using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Geometry;
using ESRI.ArcGIS.Client.Symbols;
using ESRI.ArcGIS.OperationsDashboard;
using OD = ESRI.ArcGIS.OperationsDashboard;

namespace AddInsShare.ViewModels
{
  public abstract class BaseDataGridViewModel : BaseViewModel
  {
    protected Map           _map = null;
    protected FeatureLayer  _featureLayer = null;
    private   String        _filterString;
    private   bool          _bDefaultsApplied = false;
    private   OD.DataSource _dataSource = null;

    protected ObservableCollection<Item> _items                   = new ObservableCollection<Item>();
    protected List<Item>                 _filteredItems           = new List<Item>();
    protected List<GroupColumnViewModel> _groupByColumnViewModels = null;
    protected ItemSchema                 _schema                  = null;
    protected Item                       _followItem              = null;

    public ICommand PanToCommand { get; protected set; }
    public ICommand ZoomToCommand { get; protected set; }
    public ICommand HighlightCommand { get; protected set; }
    public ICommand FollowCommand { get; protected set; }
    public ICommand StopFollowCommand { get; protected set; }
    public ICommand DeleteItemCommand { get; protected set; }
    public ICommand DeleteAllCommand { get; protected set; }
    public ICommand GroupByDelegatingCommand { get; protected set; }

    public BaseDataGridViewModel ViewModel { get; protected set; }
    private Object _selectedItem = null;
    public Object   SelectedItem
    {
      get { return _selectedItem; }
      set { _selectedItem = value; OnPropertyChanged(() => ViewModel); }
    }

    public bool     InEditMode{ get; protected set; }
    public bool     ConvertToLocalTimeZone { get; set; }

    public Dictionary<string, string> Properties { get; set; }
    public string GetPropValue(string name)
    {
      if (!Properties.ContainsKey(name))
        return null;

      return Properties[name];
    }

    protected string GetGEPUrl(bool bHttps)
    {
      // e.g. "http://localhost:6180/"

      string url = "";
      if (bHttps)
        url = "https://";
      else
        url = "http://";

      url += GetPropValue("GepHostName") + ":";

      if (bHttps)
        url += GetPropValue("GepHttpsPort");
      else
        url += GetPropValue("GepHttpPort");

      url += "/";
      return url;
    }

    protected string GetGEPWsUrl(bool bWSS)
    {
      // e.g. "ws://localhost:6180/"

      string url = "";
      if (bWSS)
        url = "wss://";
      else
        url = "ws://";

      url += GetPropValue("GepHostName") + ":";

      if (bWSS)
        url += GetPropValue("GepHttpsPort");
      else
        url += GetPropValue("GepHttpPort");

      url += "/";
      return url;
    }

    private bool _bSkipUdates = false;
    public bool SkipUpdates
    {
      get { return _bSkipUdates; }
      set { _bSkipUdates = value; }
    }


    public ObservableCollection<Schema.Item> Items
    {
      get
      {
        return _items;
      }
      protected set
      {
        _items = value;
      }
    }

    protected CollectionViewGroup FindCollectionViewGroup(String groupName)
    {
      ICollectionView cv = CollectionViewSource.GetDefaultView(Items);

      if (cv == null || cv.Groups == null)
        return null;

      foreach (CollectionViewGroup group in cv.Groups)
      {
        if (group.Name.ToString() == groupName)
          return group;
      }

      return null;
    }

    protected GraphicsLayer CreateCustomLayer()
    {
      GraphicsLayer customLayer = new GraphicsLayer();
      if (_featureLayer != null)
        customLayer.DisplayName = "Edit - " + _featureLayer.DisplayName;
      else
        customLayer.DisplayName = "Edit - Layer";


      customLayer.Renderer = _featureLayer.Renderer;

      // copy all graphics
      foreach (Item item in _items)
      {
        Graphic featureGraphic = item.Graphic;
        if (featureGraphic == null)
          continue;

        Graphic graphic = new Graphic();
        foreach (String attributeName in featureGraphic.Attributes.Keys)
        {
          graphic.Attributes[attributeName] = featureGraphic.Attributes[attributeName];
        }

        graphic.Geometry = featureGraphic.Geometry;
        graphic.Symbol   = featureGraphic.Symbol;

        customLayer.Graphics.Add(graphic);
      }

      return customLayer;
    }

    public String TrackIdFieldName { get; set; }
    public String GroupByFieldName { get; set; }
    public String SortByFieldName1 { get; set; }
    public String SortByFieldName2 { get; set; }

    public ListSortDirection SortByFieldOrder1 { get; set; }
    public ListSortDirection SortByFieldOrder2 { get; set; }


    public BaseDataGridViewModel(OD.DataSource dataSource)
    {
      ViewModel = this;
      Properties = new Dictionary<string, string>();

      InEditMode             = false;
      ConvertToLocalTimeZone = true;

      // init commands
      PanToCommand             = new DelegatingCommand(OnPanTo);
      ZoomToCommand            = new DelegatingCommand(OnZoomTo);
      HighlightCommand         = new DelegatingCommand(OnHighlight);
      FollowCommand            = new DelegatingCommand(OnFollow);
      StopFollowCommand        = new DelegatingCommand(OnStopFollow);
      DeleteItemCommand        = new DelegatingCommand(OnDeleteItem);
      DeleteAllCommand         = new DelegatingCommand(OnDeleteAll);
      GroupByDelegatingCommand = new DelegatingCommand(OnGroupByAction);

      Update(dataSource);
    }

    public void GetProperties(Dictionary<string, string> properties)
    {
      // update the Widget's data members from the VM so that they will get persisted with the widget
      if (properties == null)
        return;

      foreach (KeyValuePair<string, string> entry in Properties)
        properties[entry.Key] = entry.Value;
    }

    public void SetProperties(Dictionary<string, string> properties)
    {
      // update the VM from the View's data members
      if (properties == null)
        return;

      foreach (KeyValuePair<string, string> entry in properties)
        Properties[entry.Key] = entry.Value;
    }

    public Item GetSelectedItem()
    {
      return SelectedItem as Item;
    }


    public void SetDataSource(OD.DataSource dataSource)
    {
      if (_dataSource != dataSource)
      {
        // this will cause the schema to get recreated
        _schema = null;

        // clear the grouping
        _groupByColumnViewModels = null;

        _dataSource = dataSource;
        _map = GetMap();
        _featureLayer = FindFeatureLayer(_dataSource);
      }
    }

    async public void Update(OD.DataSource dataSource)
    {
      if (dataSource == null)
        return;
      if (SkipUpdates)
        return;
      if (InEditMode)
        return;

      // Execute an async query
      OD.Query query = new OD.Query()
      {
        SortField = SortByFieldName1,
        SortOrder = ESRI.ArcGIS.Client.Tasks.SortOrder.Ascending,
        ReturnGeometry = true
      };
      OD.QueryResult queryResult = await dataSource.ExecuteQueryAsync(query);
      if (queryResult.Canceled || queryResult.Features == null)
      {
        Items.Clear();
        return;
      }

      //hk - RESEARCH THIS APPROACH
      /*
      List<ListItem> items = new List<ListItem>();
      int i = 0;
      foreach (var result in queryResult.Features)
      {
        items.Add(new ListItem(this, result, dataSource));
        if (++i == ListWidget.MaxCount)
          break;
      }
      */

      try
      {
        SetDataSource(dataSource);
        Update(queryResult.Features);
      }
      catch (Exception ex)
      {
        Log.TraceException("Updating " + dataSource.Name, ex);
      }

    }

    public static async Task<GraphicsLayer> CreateCustomLayer(FeatureLayer featureLayer, OD.DataSource dataSource)
    {
      if (featureLayer == null || dataSource == null)
        return null;

      GraphicsLayer customLayer = new GraphicsLayer();
      customLayer.DisplayName = "Edit - " + featureLayer.DisplayName;
      customLayer.Renderer = featureLayer.Renderer;

      // Execute an async query
      OD.Query query = new OD.Query();
      query.ReturnGeometry = true;
      OD.QueryResult queryResult = await dataSource.ExecuteQueryAsync(query);
      if (queryResult.Canceled || queryResult.Features == null)
        return null;

      CopyGraphics(queryResult.Features, customLayer.Graphics);
      return customLayer;
    }

    internal static void CopyGraphics(System.Collections.Generic.IList<Graphic> fromCollection, GraphicCollection toCollection)
    {
      // copy all graphics
      foreach (Graphic featureGraphic in fromCollection)
      {
        Graphic graphic = new Graphic();
        foreach (String attributeName in featureGraphic.Attributes.Keys)
          graphic.Attributes[attributeName] = featureGraphic.Attributes[attributeName];

        graphic.Geometry = featureGraphic.Geometry;
        graphic.Symbol = featureGraphic.Symbol;

        toCollection.Add(graphic);
      }
    }

    protected FeatureLayer FindFeatureLayer(OD.DataSource dataSource)
    {
      if (dataSource == null)
        return null;
      MapWidget mapWidget = GetMapWidget(dataSource);
      if (mapWidget == null)
        return null;

      return mapWidget.FindFeatureLayer(dataSource);
    }

    public FeatureLayer GetFeatureLayer()
    {
      return _featureLayer;
    }

    internal MapWidget GetMapWidget(OD.DataSource dataSource)
    {
      if (OperationsDashboard.Instance == null)
        return null;

      IWidget widget = OperationsDashboard.Instance.FindWidget(dataSource);
      if (widget is MapWidget)
      {
        MapWidget mapWidget = widget as MapWidget;
        mapWidget.IsDrawingProgressVisible = false;
        return mapWidget;
      }

      return null;
    }

    internal Map GetMap()
    {
      if (_dataSource == null)
        return null;

      MapWidget mapWidget = GetMapWidget(_dataSource);
      if (mapWidget == null)
        return null;

      return mapWidget.Map;
    }

    protected void OnPanTo(object obj)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;

      PanTo(item);
    }

    private void PanTo(Item item)
    {
      if (item.Graphic == null)
        return;
      if (_map == null)
        return;
      if (item.Graphic.Geometry == null)
        return;

      try
      {
        _map.PanTo(item.Graphic.Geometry);
      }
      catch (Exception exception)
      {
        ShowExceptionMessageBoxSR(exception, _map, item.Graphic.Geometry);
      }
    }

    protected void OnZoomTo(object obj)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;
      if (item.Graphic == null)
        return;
      if (item.Graphic.Geometry == null)
        return;
      if (_map == null)
        return;

      ZoomTo(_map, item.Graphic.Geometry.Extent);
    }

    void ZoomTo(Map map, Envelope extent)
    {
      if (map == null || extent == null)
        return;

      if (extent.Width > 0 && extent.Height > 0)
      {
        extent = extent.Expand(1.1);
      }
      else
      {
        double width = map.Extent.Width / 6;
        double height = map.Extent.Height / 6;
        extent = new Envelope(extent.XMin - width, extent.YMin - height, extent.XMin + width, extent.YMin + height);
        //extent.SpatialReference = map.SpatialReference;
      }

      try
      {
        map.ZoomTo(extent);
      }
      catch (Exception exception)
      {
        ShowExceptionMessageBoxSR(exception, map, extent);
      }
    }

    private void ShowExceptionMessageBoxSR(Exception exception, Map map, Geometry geometry)
    {
      System.Windows.MessageBox.Show(String.Format
        (
          "{0}\n\nMap's SR WKID = {1}\nItem's SR WKID = {2}",
          exception.Message,
          map.SpatialReference.WKID.ToString(),
          geometry.SpatialReference.WKID.ToString()
        ), "Error" );
    }

    protected void OnHighlight(object obj)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;
      if (item.Graphic == null)
        return;

      if (_map == null)
        return;

      Highlight(_map, item.Graphic.Geometry);
    }

    void Highlight(Map map, Geometry geometry)
    {
      GraphicsLayer layer = FindLayer(map, "DispatchHighlightFeedback");

      Graphic graphic = new Graphic()
      {
        Geometry = geometry.Extent.GetCenter(),
        Symbol = Resources.AppResources.Dictionary["HighlightMarkerSymbol"] as Symbol
      };

      layer.Graphics.Add(graphic);

      Utility.DelayedCallback(2000, () => { layer.Graphics.Remove(graphic); });
    }

    protected void OnFollow(object obj)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;
      if (item.Graphic == null)
        return;

      GetFollowGraphicsLayer(item);
      _followItem = item;
    }

    protected void OnStopFollow(object obj)
    {
      RemoveFollowGraphicsLayer();
      _followItem = null;
    }

    protected void OnDeleteItem(object obj)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;
      if (_featureLayer == null)
        return;
      if (!item.Graphic.Attributes.ContainsKey("OBJECTID"))
        return;

      Graphic graphicToDelete = null;
      string objectId = item.Graphic.Attributes["OBJECTID"].ToString();
      foreach (Graphic graphic in _featureLayer.Graphics)
      {
        object currentObjectId = graphic.Attributes["OBJECTID"];
        if (currentObjectId.ToString() == objectId)
        {
          graphicToDelete = graphic;
          break;
        }
      }

      if (graphicToDelete != null)
      {
        bool bDeleted = _featureLayer.Graphics.Remove(graphicToDelete);
        _featureLayer.SaveEdits();
        _featureLayer.Update();
        return;
      }

      /*
      // HTTP POST delete a GeoFence
      String uri = _featureLayer.Url + "/deleteFeatures";
      String httpRequestBodyJSON = "f=json&objectIds=" + item.Graphic.Attributes["OBJECTID"];
      HttpRequest request = new HttpRequest(uri, "POST", "application/x-www-form-urlencoded", 10000, httpRequestBodyJSON);
      HttpResponse response = request.ExecuteHttpRequest();
      response.ReportOnArcGISServerError("Delete item error");
      _featureLayer.Update();
      */
    }

    public void OnDeleteAll(object obj)
    {
      if (_featureLayer == null)
        return;

      SkipUpdates = true;
      _featureLayer.Graphics.Clear();
      _featureLayer.SaveEdits();
      _featureLayer.Update();
      SkipUpdates = false;
    }

    GraphicsLayer FindLayer(Map map, string layerId)
    {
      if (map.Layers[layerId] == null)
      {
        GraphicsLayer layer = new GraphicsLayer() { ID = layerId };
        map.Layers.Add(layer);
      }
      return map.Layers[layerId] as GraphicsLayer;
    }

    protected void Update(IList<Graphic> graphics)
    {
      if (SkipUpdates)
        return;

      // Delete removed items
      IList<Item> itemsToRemove = GetItemsToRemove(Items, graphics);
      foreach (Item item in itemsToRemove)
      {
        Items.Remove(item);
        Log.Trace("Update - removed " + itemsToRemove.Count + " items.");
      }
      itemsToRemove.Clear();
      itemsToRemove = GetItemsToRemove(_filteredItems, graphics);
      foreach (Item item in itemsToRemove)
        _filteredItems.Remove(item);

      // Update existing items and add new ones
      foreach (Graphic graphic in graphics)
      {
        string trackId = graphic.Attributes[TrackIdFieldName].ToString();
        if (trackId == null)
          continue;

        Item itemFound = null;

        // look for the item in Items
        itemFound = FindTrackIdInItems(trackId, Items);
        if (itemFound != null)
        {
          // item found in Items, update the item
          UpdateItem(itemFound, graphic);
          //Log.Trace("Update - updated item " + itemFound.Graphic.Attributes[TrackIdFieldName] + ".");
          if (!IsItemIncludedForFilter(itemFound))
          {
            // move the item to the _filteredItems
            Items.Remove(itemFound);
            Log.Trace("Update - removed item " + itemFound.Graphic.Attributes[TrackIdFieldName] + ".");
            _filteredItems.Add(itemFound);
          }
        }
        else
        {
          // item was not found in Items, look for it in _filteredItems
          itemFound = FindTrackIdInItems(trackId, _filteredItems);
          if (itemFound != null)
          {
            // item found in _filteredItems, update the item
            UpdateItem(itemFound, graphic);
            if (IsItemIncludedForFilter(itemFound))
            {
              // move the item to the Items
              _filteredItems.Remove(itemFound);
              Items.Add(itemFound);
              Log.Trace("Update - added item " + itemFound.Graphic.Attributes[TrackIdFieldName] + ".");
            }
          }
        }

        if (itemFound == null)
        {
          // item was not found, add it as a new item
          Item newItem = CreateNewItem(graphic);
          if (newItem == null)
            continue;

          if (IsItemIncludedForFilter(newItem))
          {
            Items.Add(newItem);
            Log.Trace("Update - added item " + newItem.Graphic.Attributes[TrackIdFieldName] + ".");
          }
          else
          {
            _filteredItems.Add(newItem);
          }
        }
      }

      InitDefaults();

      Follow();
    }

    private void Follow()
    {
      if (_followItem == null)
        return;

      UpdateFollowGraphic(_followItem);
      PanTo(_followItem);
    }

    private void UpdateFollowGraphic(Item followItem)
    {
      if (followItem == null || followItem.Graphic == null)
        return;

      GraphicsLayer followGraphicsLayer = GetFollowGraphicsLayer(null);
      if (followGraphicsLayer == null)
        return;

      followGraphicsLayer.Graphics[0].Geometry = followItem.Graphic.Geometry;
    }

    private GraphicsLayer GetFollowGraphicsLayer(Item followItem)
    {
      Map map = GetMap();
      if (map == null)
        return null;

      // make sure to create only once
      String id = "RouteMonitor-FollowSymbolLayer";
      foreach (Layer layer in map.Layers)
      {
        if (!(layer is GraphicsLayer))
          continue;

        if (layer.ID == id)
          return layer as GraphicsLayer;
      }

      Graphic graphic = new Graphic { Symbol = AppResources.Dictionary["FollowedFeatureHighlightSymbol"] as Symbol };
      if (followItem != null && followItem.Graphic != null)
        graphic.Geometry = followItem.Graphic.Geometry;

      GraphicsLayer followGraphicsLayer = new GraphicsLayer();
      followGraphicsLayer.Graphics.Add(graphic);
      followGraphicsLayer.ID = id;

      map.Layers.Add(followGraphicsLayer);

      return followGraphicsLayer;
    }

    private bool RemoveFollowGraphicsLayer()
    {
      Map map = GetMap();
      if (map == null)
        return false;

      // make sure to create only once
      String id = "RouteMonitor-FollowSymbolLayer";
      foreach (Layer layer in map.Layers)
      {
        if (!(layer is GraphicsLayer))
          continue;

        if (layer.ID == id)
        {
          map.Layers.Remove(layer);
          return true;
        }
      }

      return false;
    }

    private IList<Item> GetItemsToRemove(IList<Item> items, IList<Graphic> graphics)
    {
      List<Item> itemsToRemove = new List<Item>();
      foreach (Item item in items)
      {
        object trackIdObj = item.GetPropertyValue(TrackIdFieldName);
        if (trackIdObj != null)
        {
          string trackId = trackIdObj.ToString();
          Graphic graphicFound = FindTrackIdInGraphics(trackId, graphics, TrackIdFieldName);
          if (graphicFound == null)
            itemsToRemove.Add(item);
        }
      }
      return itemsToRemove;
    }

    protected Graphic FindTrackIdInGraphics(string trackId, IList<Graphic> graphics, string key)
    {
      if (trackId == null)
        return null;

      foreach (Graphic graphic in graphics)
      {
        if (graphic.Attributes == null)
          continue;
        if (!graphic.Attributes.ContainsKey(key))
          continue;
        object graphicTrackIdObj = graphic.Attributes[key];
        if (graphicTrackIdObj == null)
          continue;
        string graphicTrackId = graphicTrackIdObj.ToString();
        if (graphicTrackId == null)
          continue;
        if (graphicTrackId.ToUpper() == trackId.ToUpper())
          return graphic;
      }

      return null;
    }

    private Item FindTrackIdInItems(string trackId, IList<Item> items)
    {
      if (trackId == null)
        return null;

      foreach (Item item in items)
      {
        string itemTrackId = item.GetPropertyValue(TrackIdFieldName).ToString();
        if (itemTrackId == null)
          continue;
        if (itemTrackId.ToUpper() == trackId.ToUpper())
          return item;
      }

      return null;
    }

    protected bool UpdateItem(Item item, Graphic graphic)
    {
      // update the item's properties
      foreach (KeyValuePair<string, object> pair in graphic.Attributes)
      {
        object value = pair.Value;

        if (ConvertToLocalTimeZone && pair.Value is DateTime)
        {
          // convert the DateTime attribute to local time zone
          DateTime localTime = ((DateTime)value).ToLocalTime();
          value = localTime as object;
        }

        item.SetPropertyValue(pair.Key, value);
      }

      // update the item's graphic
      graphic.Geometry = Helper.ProjectGeometryToMap(graphic.Geometry, _map);
      item.Graphic = graphic;

      return true;
    }

    protected Item CreateNewItem(Graphic graphic)
    {
      // make sure the schema is up to date
      if (_schema == null)
      {
        if (_featureLayer == null)
          return null;

        _schema = new ItemSchema();
        Field oidField = null;
        foreach (Field field in _featureLayer.LayerInfo.Fields)
        {
          if (field.Type == Field.FieldType.OID)  // (field.Name.ToUpper() != "OBJECTID")
            oidField = field;
          else
            _schema.AddProperty(field);
        }

        // add the OID field at the end
        if (oidField != null)
          _schema.AddProperty(oidField);
      }

      // create a new item
      Item item = new Item(_schema);

      // set item attributes
      if (!UpdateItem(item, graphic))
        return null;

      return item;
    }

    public IEnumerable<GroupColumnViewModel> GroupByColumnViewModels
    {
      get
      {
        if (_groupByColumnViewModels == null)
        {
          if (_schema == null)
            return null;

          PropertyInfo[] propInfos = _schema.GetProperties();
          if (propInfos == null)
            return null;

          if (propInfos.Count() == 0)
            return null;

          // create a new ViewModel collection
          _groupByColumnViewModels = new List<GroupColumnViewModel>();

          // add a ViewModel for "None"
          _groupByColumnViewModels.Add(new GroupColumnViewModel("None", GroupByDelegatingCommand) { IsChecked = true });

          // add ViewModels for each of the properties
          foreach (PropertyInfo pi in propInfos)
            _groupByColumnViewModels.Add(new GroupColumnViewModel(pi.Name, GroupByDelegatingCommand));
        }

        return _groupByColumnViewModels;
      }
    }

    private void InitDefaults()
    {
      if (!_bDefaultsApplied)
      {
        ApplySettings();
        _bDefaultsApplied = true;
      }
    }

    public bool ApplySettings()
    {
      if (_schema == null)
        return false;

      // if null or empty group by string - keep default group name as "None"
      String groupByFieldName = GroupByFieldName == null ? null : _schema.NameToAlias(GroupByFieldName);
      if (!String.IsNullOrEmpty(groupByFieldName))
        ApplyGroupBy(groupByFieldName);

      String sortByFieldName1 = SortByFieldName1 == null ? null : _schema.NameToAlias(SortByFieldName1);
      String sortByFieldName2 = SortByFieldName2 == null ? null : _schema.NameToAlias(SortByFieldName2);
      ApplySortBy(sortByFieldName1, SortByFieldOrder1, sortByFieldName2, SortByFieldOrder2);

      return true;
    }

    private bool ApplySortBy(String fieldName1, ListSortDirection sortDirection1, String fieldName2, ListSortDirection sortDirection2)
    {
      if (_schema == null)
        return false;

      ICollectionView cv = CollectionViewSource.GetDefaultView(_items);
      ICollectionViewLiveShaping cvls = cv as ICollectionViewLiveShaping;
      if (!cv.CanSort)
        return false;

      cvls.IsLiveSorting   = true;

      cv.SortDescriptions.Clear();
      //cvls.LiveSortingProperties.Clear();

      if (!String.IsNullOrEmpty(fieldName1))
      {
        cv.SortDescriptions.Add(new SortDescription(fieldName1, sortDirection1));
        //cvls.LiveSortingProperties.Add(fieldName1);
      }

      if (!String.IsNullOrEmpty(fieldName2))
      {
        cv.SortDescriptions.Add(new SortDescription(fieldName2, sortDirection2));
        //cvls.LiveSortingProperties.Add(fieldName2);
      }

      return true;
    }

    private bool ApplyGroupBy(String groupByName)
    {
      if (_schema == null)
        return false;

      ICollectionView cv = CollectionViewSource.GetDefaultView(_items);
      ICollectionViewLiveShaping cvls = cv as ICollectionViewLiveShaping;
      if (!cv.CanGroup)
        return false;

      foreach (var viewModel in GroupByColumnViewModels)
        viewModel.IsChecked = (viewModel.PropertyName == groupByName);

      cvls.IsLiveGrouping  = true;
      cvls.IsLiveFiltering = true;
      cvls.IsLiveSorting   = true;

      // clear the group
      cv.GroupDescriptions.Clear();
      cvls.LiveGroupingProperties.Clear();

      // if groupByName not in schema - set the group to "None"
      if (!_schema.DoesAliasExist(groupByName))
        return false;

      // set the group
      if (groupByName != "None")
      {
        cv.GroupDescriptions.Add(new PropertyGroupDescription(groupByName));
        cvls.LiveGroupingProperties.Add(groupByName);
      }

      // refresh the live properties
      cvls.LiveFilteringProperties.Clear();
      cvls.LiveSortingProperties.Clear();
      PropertyInfo[] propInfos = _schema.GetProperties();
      foreach (var propInfo in propInfos)
      {
        cvls.LiveFilteringProperties.Add(propInfo.Name);
        cvls.LiveSortingProperties.Add(propInfo.Name);
        //cvls.LiveFilteringProperties.Add(propInfo.Name);
      }

      return true;
    }

    private void OnGroupByAction(object vm)
    {
      GroupColumnViewModel groupColumnViewModel = vm as GroupColumnViewModel;
      string groupByName = groupColumnViewModel != null ? groupColumnViewModel.PropertyName : "None";

      ICollectionView cv = CollectionViewSource.GetDefaultView(_items);
      if (cv.CanGroup)
        ApplyGroupBy(groupByName);
    }

    private void ApplyFilter(Boolean enableFilter)
    {
      //ApplyWpfFilter(enableFilter);
      ApplyManualFilter(enableFilter);
    }

    private void ApplyManualFilter(Boolean enableFilter)
    {
      if (!enableFilter)
      {
        foreach (Item item in _filteredItems)
          Items.Add(item);

        _filteredItems.Clear();
        return;
      }

      // backup filtered items to prevFilteredItems
      List<Item> prevFilteredItems = _filteredItems;
      _filteredItems = new List<Item>();

      // add items to the Items list
      foreach (Item item in prevFilteredItems)
      {
        if (IsItemIncludedForFilter(item))
          Items.Add(item);
        else
          _filteredItems.Add(item);
      }

      // mark new filtered items (store them in a new temp list) to br removed from the items list
      List<Item> itemsToRemove = new List<Item>();
      foreach (Item item in Items)
      {
        if (!IsItemIncludedForFilter(item))
          itemsToRemove.Add(item);
      }

      // move filtered items from Items to _filteredItems
      foreach (Item item in itemsToRemove)
      {
        _filteredItems.Add(item);
        Items.Remove(item);
      }
    }

    private void ApplyWpfFilter(Boolean enableFilter)
    {
      ICollectionView cv = CollectionViewSource.GetDefaultView(_items);
      ICollectionViewLiveShaping cvls = cv as ICollectionViewLiveShaping;
      if (!enableFilter)
      {
        cv.Filter = null;
        return;
      }

      if (cv != null && cv.CanFilter)
      {
        if (cvls != null && cvls.CanChangeLiveFiltering)
        {
          if (_schema != null)
          {
            PropertyInfo[] propInfos = _schema.GetProperties();
            cvls.LiveFilteringProperties.Clear();
            foreach (var propInfo in propInfos)
              cvls.LiveFilteringProperties.Add(propInfo.Name);
          }
          cvls.IsLiveFiltering = true;

          cv.Filter = IsItemIncludedForFilter;
        }
      }
    }

    public bool IsItemIncludedForFilter(object item)
    {
      if (String.IsNullOrEmpty(_filterString))
        return true;

      string filterString = _filterString.ToLower();

      Item dynamicItem = (Item)item;
      PropertyInfo[] propInfos = dynamicItem.GetProperties();
      foreach (var propInfo in propInfos)
      {
        var prop = dynamicItem.GetPropertyValue(propInfo.Name);
        if (prop == null)
          continue;

        if (prop.ToString().ToLower().Contains(filterString))
          return true;
      }

      return false;
    }

    public void SetFilterString(string filterString)
    {
      SkipUpdates = true;
      _filterString = filterString;
      ApplyFilter(true);
      SkipUpdates = false;
    }

    public void ToggleLayerVisibility()
    {
      if (_featureLayer == null)
        return;

      _featureLayer.Visible = !_featureLayer.Visible;
    }

    public int GetItemsCount()
    {
      return Items.Count();
    }

    internal void ReloadItems()
    {
      //TODO
      //_filteredItems.Clear();
      //Items.Clear();
    }
  }
}
