using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.Composition;
using System.Runtime.Serialization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ESRI.ArcGIS.OperationsDashboard;
using GeoFences.ViewModels;
using AddInsShare.Common;
using AddInsShare.Resources;
using AddInsShare.ViewModels;
using OD = ESRI.ArcGIS.OperationsDashboard;

namespace GeoFences.Views
{
  /// <summary>
  /// A Widget is a dockable extension to the ArcGIS Viewer application that implements IWidget. By returning true from CanConfigure, 
  /// this widget provides the ability for the user to configure the widget properties showing a settings Window in the Configure method.
  /// By implementing IDataSourceConsumer, this Widget indicates it requires a DataSource to function and will be notified when the 
  /// data source is updated or removed.
  /// </summary>
  [Export("ESRI.ArcGIS.OperationsDashboard.Widget")]
  [ExportMetadata("DisplayName", "GeoFences")]
  [ExportMetadata("Description", "A widget that displays and manages GeoFences.")]
  [ExportMetadata("ImagePath", "/GeoFences;component/Images/GeoFenceWidgetIcon.png")]
  [ExportMetadata("DataSourceRequired", true)]
  [DataContract]
  public partial class GeoFenceWidget : UserControl, IWidget, IDataSourceConsumer, IMapTool
  {
    #region Data Members

    private MapWidget           _mapWidget = null;
    internal bool               _bAddWidgetWindowOpen = false;
    internal OD.DataSource      _dataSource = null;
    internal GeoFenceViewModel  DataGridViewModel = null;


    /// <summary>
    /// The text that is displayed in the widget's containing window. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "caption")]
    public string Caption { get; set; }

    /// <summary>
    /// The unique identifier of the widget, set by the application when the widget is added to the configuration.
    /// </summary>
    [DataMember(Name = "id")]
    public string Id { get; set; }

    /// <summary>
    /// A unique identifier of a data source in the configuration. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "dataSourceId")]
    public string DataSourceId { get; set; }

    /// <summary>
    /// The name of the Track Id Field within the selected data source. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "trackIdFieldName")]
    public string TrackIdFieldName { get; set; }

    /// <summary>
    /// The name of the Track Id Field within the selected data source. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "nameFieldName")]
    public string NameFieldName { get; set; }

    /// <summary>
    /// The name of the Track Id Field within the selected data source. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "categoryFieldName")]
    public string CategoryFieldName { get; set; }

    /// <summary>
    /// The name of the Track Id Field within the selected data source. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "activeFieldName")]
    public string ActiveFieldName { get; set; }

    /// <summary>
    /// The default group by field name. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "groupByFieldName")]
    public string GroupByFieldName { get; set; }

    /// <summary>
    /// The default sort by field name 1. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "sortByFieldName1")]
    public string SortByFieldName1 { get; set; }

    /// <summary>
    /// The default sort by field name 2. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "sortByFieldName2")]
    public string SortByFieldName2 { get; set; }

    /// <summary>
    /// The default sort by field order 1. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "sortByFieldOrder1")]
    public ListSortDirection SortByFieldOrder1 { get; set; }

    /// <summary>
    /// The default sort by field order 2. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "sortByFieldOrder2")]
    public ListSortDirection SortByFieldOrder2 { get; set; }

    /// <summary>
    /// The GeoEvnet Processor Properties
    /// </summary>
    [DataMember(Name = "properties")]
    public Dictionary<string, string> Properties { get; set; }

    #endregion


    public GeoFenceWidget()
    {
      this.Resources.MergedDictionaries.Add(SharedDictionaryManager.SharedStyleDictionary);
      this.Resources.MergedDictionaries.Add(SharedDictionaryManager.SharedDataTemplateDictionary);

      InitializeComponent();

      DataGridViewModel = new GeoFenceViewModel(null);
      DataContext = DataGridViewModel;

      Caption = "GeoFences";
      Properties = new Dictionary<string, string>();

      _bAddWidgetWindowOpen = false;

      // init the widget's data members from the VM
      TrackIdFieldName  = DataGridViewModel.TrackIdFieldName;
      NameFieldName     = DataGridViewModel.NameFieldName;
      CategoryFieldName = DataGridViewModel.CategoryFieldName;
      ActiveFieldName   = DataGridViewModel.ActiveFieldName;
      DataGridViewModel.GetProperties(Properties);
    }


    #region IWidget

    /// <summary>
    /// Activate is called when the widget is first added to the configuration, or when loading from a saved configuration, after all 
    /// widgets have been restored. Saved properties can be retrieved, including properties from other widgets.
    /// Note that some widgets may have properties which are set asynchronously and are not yet available.
    /// </summary>
    public void OnActivated()
    {
      // update the view model with the properties that were loaded from the Dashboard Configuration
      DataGridViewModel.SetProperties(Properties);
    }

    /// <summary>
    ///  Deactivate is called before the widget is removed from the configuration.
    /// </summary>
    public void OnDeactivated()
    {
    }

    /// <summary>
    /// Returns the visual representation of the widget. This Widget class is itself a UserControl which provides the
    /// visual aspect of the widget.
    /// </summary>
    public UIElement Visual
    {
      get { return this; }
    }

    #endregion

    #region IConfigurable

    /// <summary>
    ///  Provides functionality for the widget to be configured by the end user through a dialog.
    /// </summary>
    /// <param name="owner">The application window which should be the owner of the dialog.</param>
    /// <param name="dataSources">The complete list of DataSources in the configuration.</param>
    /// <returns>True if the user clicks ok, otherwise false.</returns>
    public bool Configure(Window owner, IList<OD.DataSource> dataSources)
    {
      try
      {
        if (DataGridViewModel == null)
          return false;

        DataGridViewModel.SkipUpdates = true;

        // Show the configuration dialog
        GeoFenceWidgetDialog dialog = new GeoFenceWidgetDialog
          (dataSources, Caption, DataSourceId, TrackIdFieldName,
            NameFieldName, CategoryFieldName, ActiveFieldName,
            GroupByFieldName, SortByFieldName1, SortByFieldName2,
            SortByFieldOrder1, SortByFieldOrder2, DataGridViewModel.Properties) { Owner = owner };

        if (dialog.ShowDialog() != true)
        {
          DataGridViewModel.SkipUpdates = false;
          return false;
        }

        // Retrieve the selected values for the properties from the configuration dialog.
        Caption           = dialog.Caption;
        DataSourceId      = dialog.DataSource.Id;
        TrackIdFieldName  = dialog.TrackIdField.Name;
        NameFieldName     = dialog.NameField.Name;
        CategoryFieldName = dialog.CategoryField.Name;
        ActiveFieldName   = dialog.ActiveField.Name;
        GroupByFieldName  = dialog.GroupByField.Name;
        SortByFieldName1  = dialog.SortByField1.Name;
        SortByFieldName2  = dialog.SortByField2.Name;
        SortByFieldOrder1 = dialog.SortByFieldOrder1;
        SortByFieldOrder2 = dialog.SortByFieldOrder2;

        SetMapWidget(dialog.DataSource);

        DataGridViewModel.TrackIdFieldName  = TrackIdFieldName;
        DataGridViewModel.NameFieldName     = NameFieldName;
        DataGridViewModel.CategoryFieldName = CategoryFieldName;
        DataGridViewModel.ActiveFieldName   = ActiveFieldName;
        DataGridViewModel.GroupByFieldName  = GroupByFieldName;
        DataGridViewModel.SortByFieldName1  = SortByFieldName1;
        DataGridViewModel.SortByFieldName2  = SortByFieldName2;
        DataGridViewModel.SortByFieldOrder1 = SortByFieldOrder1;
        DataGridViewModel.SortByFieldOrder2 = SortByFieldOrder2;

        DataGridViewModel.GetProperties(Properties);

        DataGridViewModel.ApplySettings();
      }
      catch (Exception ex)
      {
        Log.TraceException("ResourceWidget::Configure", ex);
      }

      DataGridViewModel.SkipUpdates = false;
      return true;
    }

    #endregion

    #region IDataSourceConsumer

    /// <summary>
    /// Called when a DataSource is removed from the configuration. 
    /// </summary>
    /// <param name="dataSource">The DataSource being removed.</param>
    public void OnRemove(OD.DataSource dataSource)
    {
      // Respond to data source being removed. The application framework will automatically show the widget as disabled
      // if it's currently configured data source is removed.
      if (dataSource.Id == DataSourceId)
        DataSourceId = null;
    }

    /// <summary>
    /// Returns the Ids of the DataSources that the widget uses.
    /// </summary>
    public string[] DataSourceIds
    {
      get { return new string[] { DataSourceId }; }
    }

    /// <summary>
    /// Called when a DataSource is updated. Test the DataSource.Id to respond to the DataSource of interest.
    /// </summary>
    /// <param name="dataSource">The DataSource being updated.</param>
    public void OnRefresh(OD.DataSource dataSource)
    {
      if (dataSource.Id == DataSourceId)
      {
        // if required, respond to the update from the selected data source. consider using an async method.
        SetDataSource(dataSource);
        PopulateGrid();
      }
    }

    private bool SetDataSource(DataSource dataSource)
    {
      if (_dataSource != dataSource)
      {
        GeoFenceViewModel vm = DataGridViewModel as GeoFenceViewModel;
        vm.TrackIdFieldName  = TrackIdFieldName;
        vm.NameFieldName     = NameFieldName;
        vm.CategoryFieldName = CategoryFieldName;
        vm.ActiveFieldName   = ActiveFieldName;
        vm.GroupByFieldName  = GroupByFieldName;
        vm.SortByFieldName1  = SortByFieldName1;
        vm.SortByFieldName2  = SortByFieldName2;
        vm.SetDataSource(dataSource);
        _dataSource = dataSource;
      }

      return SetMapWidget(dataSource);
    }

    private void PopulateGrid()
    {
      (DataGridViewModel as BaseDataGridViewModel).Update(_dataSource);
      textBlockGeoFences.Text = /*Caption +*/ " (" + DataGridViewModel.GetItemsCount().ToString() + ")";
    }

    #endregion

    #region IMapTool

    private bool SetMapWidget(DataSource dataSource)
    {
      try
      {
        if (OperationsDashboard.Instance == null)
          return false;
      }
      catch (Exception)
      {
        return false;
        }

      _mapWidget = (MapWidget)OperationsDashboard.Instance.FindWidget(dataSource);
      return true;
    }

    public MapWidget MapWidget
    {
      get { return _mapWidget;  }
      set { _mapWidget = value; }
    }

    /// <summary>
    ///  Determines if the Configure method is called after the widget is created, before it is added to the configuration. Provides an opportunity to gather user-defined settings.
    /// </summary>
    /// <value>Return true if the Configure method should be called, otherwise return false.</value>
    public bool CanConfigure
    {
      get { return true; }
    }

    /// <summary>
    ///  Provides functionality for the widget to be configured by the end user through a dialog.
    /// </summary>
    /// <param name="owner">The application window which should be the owner of the dialog.</param>
    /// <param name="dataSources">The complete list of DataSources in the configuration.</param>
    /// <returns>True if the user clicks ok, otherwise false.</returns>
    public bool Configure(Window owner)
    {
      return Configure(owner, null);
    }

    #endregion

    #region UI Handlers (to be replaced with MVVM)

    private void OnAutoGeneratingColumn(object sender, DataGridAutoGeneratingColumnEventArgs e)
    {
      e.Column.CanUserSort = true;
      e.Column.MinWidth    = 0.0;
    }

    private void buttonAddGeoFence_Click(object sender, RoutedEventArgs e)
    {
      if (_bAddWidgetWindowOpen)
        return;

      AddGeoFenceWindow addGeoFenceWindow = new AddGeoFenceWindow(this);
      if (!addGeoFenceWindow.IsReady())
        return;

      addGeoFenceWindow.Owner = Window.GetWindow(this);
      addGeoFenceWindow.WindowStartupLocation = WindowStartupLocation.CenterOwner;
      _bAddWidgetWindowOpen = true;
      addGeoFenceWindow.Show();
    }

    private void GeoFenceImage_MouseDown_1(object sender, MouseButtonEventArgs e)
    {
      if (DataContext is GeoFenceViewModel)
        DataGridViewModel.ToggleLayerVisibility();
    }

    private void textBoxSearch_TextChanged_1(object sender, TextChangedEventArgs e)
    {
      if (DataContext is GeoFenceViewModel)
        (DataContext as GeoFenceViewModel).SetFilterString(textBoxSearch.Text);
    }

    #endregion

    private void buttonDeleteAll_Click_1(object sender, RoutedEventArgs e)
    {
      if (DataGridViewModel != null)
        DataGridViewModel.OnDeleteAll(null);
    }

  }
}
