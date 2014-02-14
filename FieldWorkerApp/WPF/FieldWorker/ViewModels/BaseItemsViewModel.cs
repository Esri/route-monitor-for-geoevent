using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Tasks;
using FieldWorker.Common;
using FieldWorker.Schema;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Windows.Data;
using System.Windows.Input;

namespace FieldWorker.ViewModels
{
  public abstract class BaseItemsViewModel : BaseViewModel
  {
    protected FeatureLayerHelper  _featureLayerHelper = null;
    private   String              _filterString;
    private   bool                _bDefaultsApplied = false;

    protected ObservableCollection<Item> _items;
    protected List<Item>                 _filteredItems;
    protected List<GroupColumnViewModel> _groupByColumnViewModels = null;

    protected ItemSchema                 _schema                  = null;
    public ItemSchema Schema
    {
      get { return _schema; }
    }

    public ICommand GroupByDelegatingCommand { get; protected set; }

    public BaseItemsViewModel ViewModel { get; protected set; }
    private Object _selectedItem = null;
    public Object SelectedItem
    {
      get { return _selectedItem; }
      set { _selectedItem = value; OnPropertyChanged(() => ViewModel); }
    }

    public bool ConvertToLocalTimeZone { get; set; }

    public Dictionary<string, string> Properties { get; set; }
    public string GetPropValue(string name)
    {
      if (!Properties.ContainsKey(name))
        return null;

      return Properties[name];
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


    //hk - REVIEW ...
    public String TrackIdFieldName { get; set; }
    public String GroupByFieldName { get; set; }
    public String SortByFieldName1 { get; set; }
    public String SortByFieldName2 { get; set; }

    public ListSortDirection SortByFieldOrder1 { get; set; }
    public ListSortDirection SortByFieldOrder2 { get; set; }


    public BaseItemsViewModel(FeatureLayerHelper featureLayerHelper)
    {
      Init();
      SetFeatureLayer(featureLayerHelper);

      ViewModel = this;
      Properties = new Dictionary<string, string>();

      ConvertToLocalTimeZone = true;

      // init commands
      GroupByDelegatingCommand = new DelegatingCommand(OnGroupByAction);

      //hk Update(featureLayer);
    }

    private void Init()
    {
      _items         = new ObservableCollection<Item>();
      _filteredItems = new List<Item>();
    }

    virtual protected Item CreateItem()
    {
      return new Item(this);
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

    public void SetFeatureLayer(FeatureLayerHelper featureLayerHelper)
    {
      if (featureLayerHelper == null || featureLayerHelper.FeatureLayer == null)
        return;

      if (_featureLayerHelper == null || _featureLayerHelper.FeatureLayer != featureLayerHelper.FeatureLayer)
      {
        // this will cause the schema to get recreated
        _schema = null;

        // clear the grouping
        _groupByColumnViewModels = null;
      }

      _featureLayerHelper = featureLayerHelper;
    }

    public void Update(FeatureLayerHelper featureLayerHelper, string where)
    {
      if (SkipUpdates)
        return;
      if (featureLayerHelper == null || featureLayerHelper.FeatureLayer == null)
        return;
      if (featureLayerHelper.FeatureLayer.HasEdits)
        return;
      if (String.IsNullOrEmpty(where))
        where = "1=1";

      QueryTask queryTask = new QueryTask(featureLayerHelper.FeatureLayer.Url);
      Query query = new Query();
      query.Where = where;
      query.ReturnGeometry = true;
      query.OutFields.AddRange(new string[] { "*" });
      queryTask.Execute(query);
      try
      {
        SetFeatureLayer(featureLayerHelper);
        Update(queryTask.LastResult);
      }
      catch (Exception ex)
      {
        Log.TraceException("Updating " + featureLayerHelper.FeatureLayer.DisplayName, ex);
      }
    }

    protected void Update(FeatureSet featureSet)
    {
      Update(featureSet.Features, featureSet.Fields);
    }

    private void Update(IList<Graphic> graphics, List<Field> fields)
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
          Item newItem = CreateNewItem(graphic, fields);
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
      //hk graphic.Geometry = Helper.ProjectGeometryToMap(graphic.Geometry, _map);
      item.Graphic = graphic;

      return true;
    }

    protected Item CreateNewItem(Graphic graphic, List<Field> fields)
    {
      // make sure the schema is up to date
      if (_schema == null)
      {
        if (fields == null)
          return null;

        _schema = new ItemSchema();
        Field oidField = null;
        foreach (Field field in fields)
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
      Item item = CreateItem();

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
        _bDefaultsApplied = ApplySettings();

        // temp workaround to make sure to apply changes
        _bDefaultsApplied = false;
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

      ApplySortBy(SortByFieldName1, SortByFieldOrder1, SortByFieldName2, SortByFieldOrder2);

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
      if (!_schema.ContainsPropertyAlias(groupByName))
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

    public int GetItemsCount()
    {
      return Items.Count();
    }
  }
}
