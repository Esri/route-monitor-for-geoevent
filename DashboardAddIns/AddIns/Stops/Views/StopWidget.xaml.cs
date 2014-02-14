using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.Composition;
using System.Runtime.CompilerServices;
using System.Runtime.Serialization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Data;
using System.Windows.Input;
using System.Windows.Media;
using AddInsShare.Common;
using AddInsShare.Resources;
using AddInsShare.ViewModels;
using ESRI.ArcGIS.OperationsDashboard;
using Stops.ViewModels;
using OD = ESRI.ArcGIS.OperationsDashboard;

namespace Stops.Views
{
  /// <summary>
  /// A Widget is a dockable extension to the ArcGIS Viewer application that implements IWidget. By returning true from CanConfigure, 
  /// this widget provides the ability for the user to configure the widget properties showing a settings Window in the Configure method.
  /// By implementing IDataSourceConsumer, this Widget indicates it requires a DataSource to function and will be notified when the 
  /// data source is updated or removed.
  /// </summary>
  [Export("ESRI.ArcGIS.OperationsDashboard.Widget")]
  [ExportMetadata("DisplayName", "Stops")]
  [ExportMetadata("Description", "A widget that displays and manages vehicle stops.")]
  [ExportMetadata("ImagePath", "/Stops;component/Images/StopWidgetIcon.png")]
  [ExportMetadata("DataSourceRequired", true)]
  [DataContract]
  public partial class StopWidget : UserControl, IWidget, IDataSourceConsumer, INotifyPropertyChanged
  {

    #region Data Members

    private  OD.DataSource          _stopDataSource   = null;
    private  OD.DataSource          _routeDataSource  = null;
    internal BaseDataGridViewModel  DataGridViewModel = null;

    int _dragRowIndex = -1;

    private string _caption = "Default Caption";
    /// <summary>
    /// The text that is displayed in the widget's containing window title bar. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "caption")]
    public string Caption
    {
      get
      {
        return _caption;
      }
      set
      {
        if (value != _caption)
        {
          _caption = value;
          NotifyPropertyChanged();
        }
      }
    }

    /// <summary>
    /// The unique identifier of the widget, set by the application when the widget is added to the configuration.
    /// </summary>
    [DataMember(Name = "id")]
    public string Id { get; set; }

    /// <summary>
    /// A unique identifier of a data source in the configuration. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "stopDataSourceId")]
    public string StopDataSourceId { get; set; }

    /// <summary>
    /// A unique identifier of a data source in the configuration. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "routeDataSourceId")]
    public string RouteDataSourceId { get; set; }

    /// <summary>
    /// The name of the Track Id Field within the selected Stops data source. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "trackIdFieldName")]
    public string TrackIdFieldName { get; set; }

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
    /// The name of the Route-Name Field within the selected Stops data source. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "stopsRouteNameFieldName")]
    public string StopsRouteNameFieldName { get; set; }

    /// <summary>
    /// The name of the Route-Name Field within the selected Routes data source. This property is set during widget configuration.
    /// </summary>
    [DataMember(Name = "routesRouteNameFieldName")]
    public string RoutesRouteNameFieldName { get; set; }

    /// <summary>
    /// The GeoEvnet Processor Properties
    /// </summary>
    [DataMember(Name = "properties")]
    public Dictionary<string, string> Properties { get; set; }

    #endregion


    public StopWidget()
    {
      this.Resources.MergedDictionaries.Add(SharedDictionaryManager.SharedStyleDictionary);
      this.Resources.MergedDictionaries.Add(SharedDictionaryManager.SharedDataTemplateDictionary);

      InitializeComponent();

      DataGridViewModel = new StopViewModel(null, null, this);
      DataContext = DataGridViewModel;

      Caption = "Stops";
      Properties = new Dictionary<string, string>();

      // init the widget's data members from the VM
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

    #region IConfigurable

    /// <summary>
    ///  Provides functionality for the widget to be configured by the end user through a dialog.
    /// </summary>
    /// <param name="owner">The application window which should be the owner of the dialog.</param>
    /// <param name="dataSources">The complete list of DataSources in the configuration.</param>
    /// <returns>True if the user clicks ok, otherwise false.</returns>
    public bool Configure(Window owner, IList<OD.DataSource> dataSources)
    {
      StopViewModel vm = DataGridViewModel as StopViewModel;
      if (vm == null)
        return false;

      try
      {
        vm.SkipUpdates = true;

        // Show the configuration dialog.
        StopWidgetDialog dialog = new StopWidgetDialog
          ( dataSources, Caption, StopDataSourceId, TrackIdFieldName,
            GroupByFieldName, SortByFieldName1, SortByFieldName2,
            SortByFieldOrder1, SortByFieldOrder2, StopsRouteNameFieldName,
            RouteDataSourceId, RoutesRouteNameFieldName,
            vm.Properties ) { Owner = owner };

        if (dialog.ShowDialog() != true)
        {
          vm.SkipUpdates = false;
          return false;
        }

        // Retrieve the selected values for the properties from the configuration dialog.
        Caption                   = dialog.Caption;
        StopDataSourceId          = dialog.StopDataSource.Id;
        TrackIdFieldName          = dialog.TrackIdField.Name;
        GroupByFieldName          = dialog.GroupByField.Name;
        SortByFieldName1          = dialog.SortByField1.Name;
        SortByFieldName2          = dialog.SortByField2.Name;
        SortByFieldOrder1         = dialog.SortByFieldOrder1;
        SortByFieldOrder2         = dialog.SortByFieldOrder2;
        StopsRouteNameFieldName   = dialog.StopsRouteNameField.Name;
        RouteDataSourceId         = dialog.RouteDataSource.Id;
        RoutesRouteNameFieldName  = dialog.RoutesRouteNameField.Name;

        vm.TrackIdFieldName          = TrackIdFieldName;
        vm.GroupByFieldName          = GroupByFieldName;
        vm.SortByFieldName1          = SortByFieldName1;
        vm.SortByFieldName2          = SortByFieldName2;
        vm.SortByFieldOrder1         = SortByFieldOrder1;
        vm.SortByFieldOrder2         = SortByFieldOrder2;
        vm.StopsRouteNameFieldName   = StopsRouteNameFieldName;
        vm.RoutesRouteNameFieldName  = RoutesRouteNameFieldName;

        vm.GetProperties(Properties);

        vm.ApplySettings();
      }
      catch (Exception ex)
      {
        Log.TraceException("ResourceWidget::Configure", ex);
      }

      vm.SkipUpdates = false;
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
      if (dataSource.Id == StopDataSourceId)
        StopDataSourceId = null;
      if (dataSource.Id == RouteDataSourceId)
        RouteDataSourceId = null;
    }

    /// <summary>
    /// Returns the Ids of the DataSources that the widget uses.
    /// </summary>
    public string[] DataSourceIds
    {
      get { return new string[] { StopDataSourceId, RouteDataSourceId }; }
    }

    /// <summary>
    /// Called when a DataSource is updated. Test the DataSource.Id to respond to the DataSource of interest.
    /// </summary>
    /// <param name="dataSource">The DataSource being updated.</param>
    public void OnRefresh(OD.DataSource dataSource)
    {
      // if required, respond to the update from the selected data source. consider using an async method.
      if (dataSource.Id == RouteDataSourceId)
      {
        SetRouteDataSource(dataSource);
      }
      if (dataSource.Id == StopDataSourceId)
      {
        // if required, respond to the update from the selected data source. consider using an async method.
        SetStopDataSource(dataSource);
        PopulateGrid();
      }
    }

    private void SetStopDataSource(DataSource stopDataSource)
    {
      if (_stopDataSource != stopDataSource)
      {
        StopViewModel vm = DataGridViewModel as StopViewModel;
        vm.SetDataSource(stopDataSource);
        vm.TrackIdFieldName = TrackIdFieldName;
        vm.GroupByFieldName = GroupByFieldName;
        vm.SortByFieldName1 = SortByFieldName1;
        vm.SortByFieldName2 = SortByFieldName2;
        vm.StopsRouteNameFieldName = StopsRouteNameFieldName;
        _stopDataSource = stopDataSource;
      }
    }

    public void SetRouteDataSource(DataSource routeDataSource)
    {
      if (_routeDataSource != routeDataSource)
      {
        StopViewModel vm = DataGridViewModel as StopViewModel;
        vm.SetRouteDataSource(routeDataSource);
        vm.RoutesRouteNameFieldName = RoutesRouteNameFieldName;
        _routeDataSource = routeDataSource;
      }
    }

    private void PopulateGrid()
    {
      if (_stopDataSource == null || _routeDataSource == null)
        return;

      (DataGridViewModel as BaseDataGridViewModel).Update(_stopDataSource);
      textBlockStops.Text = /*Caption +*/ " (" + DataGridViewModel.GetItemsCount().ToString() + ")";
    }

    #endregion

    #region UI Handlers (to be replaced with MVVM)

    private void OnAutoGeneratingColumn(object sender, DataGridAutoGeneratingColumnEventArgs e)
    {
      e.Column.CanUserSort = true;
      e.Column.MinWidth    = 0.0;
    }

    private void StopImage_MouseDown_1(object sender, MouseButtonEventArgs e)
    {
      if (DataGridViewModel != null)
        DataGridViewModel.ToggleLayerVisibility();
    }

    private void textBoxSearch_TextChanged_1(object sender, TextChangedEventArgs e)
    {
      if (DataGridViewModel != null)
        DataGridViewModel.SetFilterString(textBoxSearch.Text);
    }

    private void buttonClearPlan_Click_1(object sender, RoutedEventArgs e)
    {
      StopViewModel stopVM = DataGridViewModel as StopViewModel;
      if (stopVM == null)
        return;

      MessageBoxResult confirmation = System.Windows.MessageBox.Show("Are you sure you would like to clear the plan?", "Clear Plan", MessageBoxButton.YesNo);
      if (confirmation == MessageBoxResult.Yes)
        stopVM.ClearPlan();
    }

    private void buttonLoadPlan_Click_1(object sender, RoutedEventArgs e)
    {
      StopViewModel stopVM = DataGridViewModel as StopViewModel;
      if (stopVM == null)
        return; 

      LoadPlanWindow loadPlanWindow = new LoadPlanWindow(stopVM);
      loadPlanWindow.Owner = Window.GetWindow(this);
      loadPlanWindow.WindowStartupLocation = WindowStartupLocation.CenterOwner;
      loadPlanWindow.ShowDialog();
    }

    private void OnDispatch(object sender, RoutedEventArgs e)
    {
      var item = ((sender as MenuItem).DataContext as CollectionViewGroup).Items[0];
      (DataGridViewModel as StopViewModel).DispatchCommand.Execute(item);
    }

    private void OnDispatchAll(object sender, RoutedEventArgs e)
    {
      var item = ((sender as MenuItem).DataContext as CollectionViewGroup).Items[0];
      (DataGridViewModel as StopViewModel).DispatchAllCommand.Execute(item);
    }

    private void OnPasteOnGroup(object sender, RoutedEventArgs e)
    {
      var item = ((sender as MenuItem).DataContext as CollectionViewGroup).Items[0];
      (DataGridViewModel as StopViewModel).PasteOnGroupCommand.Execute(item);
    }

    private void OnExpanderDrop(object sender, DragEventArgs e)
    {
      StopViewModel stopVM = DataGridViewModel as StopViewModel;
      if (stopVM == null)
        return;
      if (_dragRowIndex < 0)
        return;
      //Schema.Item dragStop = e.Data.GetData(typeof(Schema.Item)) as Schema.Item;
      AddInsShare.Schema.Item dragStop = dgStops.Items[_dragRowIndex] as AddInsShare.Schema.Item;
      if (dragStop == null)
        return;

      // dropped on a Group?
      String routeName = GetDroppedOnGroupRouteName(e);
      if (routeName != null)
      {
        Log.Trace("Dropped item " + _dragRowIndex + " on group " + routeName);
        stopVM.CalculateRoute(dragStop, null, routeName, true, Window.GetWindow(this));
        return;
      }

      // dropped on an Item?
      int dropIndex = this.GetDataGridItemCurrentRowIndex(e.GetPosition);
      if (dropIndex < 0)
        return;
      if (dropIndex == _dragRowIndex)
        return;
      Log.Trace("Dropped item " + _dragRowIndex + " on item " + dropIndex);

      AddInsShare.Schema.Item dropStop = dgStops.Items[dropIndex] as AddInsShare.Schema.Item;
      if (dropStop == null)
        return;

      // Temp workaround - use the ALT key to drop onto the group (optimize == true)
      bool bOptimize = (e.KeyStates == DragDropKeyStates.AltKey);

      stopVM.CalculateRoute(dragStop, dropStop, null, bOptimize, Window.GetWindow(this));
    }

    private String GetDroppedOnGroupRouteName(DragEventArgs e)
    {
      DependencyObject expanderMainStackPanelDO = (e.Source as FrameworkElement).Parent;
      StackPanel expanderMainStackPanel = expanderMainStackPanelDO as StackPanel;
      if (expanderMainStackPanel == null)
        return null;

      DependencyObject expanderDO = expanderMainStackPanel.Parent;
      Expander expander = expanderDO as Expander;
      if (expander == null)
        return null;

      StackPanel headerStackPanel = expander.Header as StackPanel;
      if (headerStackPanel == null)
        return null;

      String routeName = null;
      foreach (var item in headerStackPanel.Children)
      {
        TextBlock textBlock = item as TextBlock;
        if (textBlock == null)
          continue;

        string name = textBlock.Name;
        if (name == "groupName")
        {
          routeName = textBlock.Text;
          break;
        }
      }

      return routeName;
    }

    private void buttonToggleEdit_Click_1(object sender, RoutedEventArgs e)
    {
      StopViewModel stopVM = (DataGridViewModel as StopViewModel);

      if (stopVM.InEditMode)
        stopVM.StopEditMode();
      else
        stopVM.StartEditMode();

      UpdateButtonsState();
    }

    private void buttonSaveEdits_Click_1(object sender, RoutedEventArgs e)
    {
      StopViewModel stopVM = (DataGridViewModel as StopViewModel);
      stopVM.SaveEdits(Window.GetWindow(this));

      UpdateButtonsState();
    }

    private void buttonDeleteAll_Click_1(object sender, RoutedEventArgs e)
    {
      if (DataGridViewModel != null)
        DataGridViewModel.OnDeleteAll(null);
    }

    private void buttonDispatchAll_Click_1(object sender, RoutedEventArgs e)
    {
      StopViewModel stopVM = (DataGridViewModel as StopViewModel);
      stopVM.DispatchAll();
    }

    private void UpdateButtonsState()
    {
      StopViewModel stopVM = (DataGridViewModel as StopViewModel);
      bool bInEditMode = stopVM.InEditMode;
      buttonSaveEdits.IsEnabled = bInEditMode;
      dgStops.AllowDrop = bInEditMode;
      buttonLoadPlan.IsEnabled = !bInEditMode;
      buttonClearPlan.IsEnabled = !bInEditMode;

      if (bInEditMode)
      {
        buttonToggleEdit.Content = "Cancel Edits";
        dgStops.RowStyle = (Style)FindResource("dgRowStyleInEditMode");
      }
      else
      {
        buttonToggleEdit.Content = "Start Edit";
        dgStops.RowStyle = (Style)FindResource("dgRowStyle");
      }
    }

    internal void UpdateView()
    {
      UpdateButtonsState();
    }

    #endregion

    #region Drag and Drop Support

    // Declare a Delegate which will return the position of the DragDropEventArgs and the MouseButtonEventArgs event object
    public delegate Point GetDragDropPosition(IInputElement theElement);

    private void OnPreviewMouseDown(object sender, MouseButtonEventArgs e)
    {
      StopViewModel stopVM =  DataGridViewModel as StopViewModel;
      if (stopVM == null)
        return;

      bool bInEditMode = stopVM.InEditMode;

      // Adjust the Expander Context Menu on mouse right click
      if (e.RightButton == MouseButtonState.Pressed)
      {
        ContextMenu expanderContextMenu = (ContextMenu)FindResource("dgExpanderMenu");
        if (expanderContextMenu != null && expanderContextMenu.Items != null)
        {
          foreach (var item in expanderContextMenu.Items)
          {
            MenuItem menuItem = item as MenuItem;
            if (menuItem == null || menuItem.Header == null || menuItem.Header.ToString() == null)
              continue;

            if (menuItem.Header.ToString().Equals("Paste"))
            {
              if (bInEditMode && stopVM.CutStop != null)
                menuItem.Visibility = Visibility.Visible;
              else
                menuItem.Visibility = Visibility.Collapsed;
            }
          }
        }
        return;
      }

      // Support Drag & Drop
      if (e.LeftButton == MouseButtonState.Pressed)
      {
        if (!bInEditMode)
        {
          e.Handled = false;
          return;
        }

        _dragRowIndex = GetDataGridItemCurrentRowIndex(e.GetPosition);
        if (_dragRowIndex < 0)
          return;

        Log.Trace("Dragging item " + _dragRowIndex + " ...");

        AddInsShare.Schema.Item selectedStop = dgStops.Items[_dragRowIndex] as AddInsShare.Schema.Item;
        if (selectedStop == null)
          return;

        if (DragDrop.DoDragDrop(dgStops, selectedStop, DragDropEffects.Move) != DragDropEffects.None)
        {
          // now This Item will be dropped at a new location and so the new Selected Item
          dgStops.SelectedItem = selectedStop;
        }
      }
    }

    private int GetDataGridItemCurrentRowIndex(GetDragDropPosition pos)
    {
      int currrentIndex = -1;
      for (int i = 0; i < this.dgStops.Items.Count; i++)
      {
        DataGridRow item = GetDataGridRowItem(i);
        if (item == null)
          continue;

        if (IsTheMouseOnVisual(item, pos))
        {
          currrentIndex = i;
          break;
        }
      }
      return currrentIndex;
    }

    private void EnumVisuals(Visual visual)
    {
      for (int i = 0; i < VisualTreeHelper.GetChildrenCount(visual); i++)
      {
        // Retrieve child visual at specified index value.
        Visual childVisual = (Visual)VisualTreeHelper.GetChild(visual, i);

        // Do processing of the child visual object. 

        // Enumerate children of the child visual object.
        EnumVisuals(childVisual);
      }
    }

    private void EnumDataGridCollectionViewGroups()
    {
      ICollectionView cv = CollectionViewSource.GetDefaultView(DataGridViewModel.Items);
      foreach (CollectionViewGroup group in cv.Groups)
      {
        Log.Trace(group.Name.ToString());
      }
    }

    private DataGridRow GetDataGridRowItem(int index)
    {
      if (dgStops.ItemContainerGenerator.Status != GeneratorStatus.ContainersGenerated)
        return null;

      return dgStops.ItemContainerGenerator.ContainerFromIndex(index) as DataGridRow;
    }

    private bool IsTheMouseOnVisual(Visual visual, GetDragDropPosition pos)
    {
      Rect  posBounds   = VisualTreeHelper.GetDescendantBounds(visual);
      Point theMousePos = pos((IInputElement)visual);
      return posBounds.Contains(theMousePos);
    }

    #endregion

    #region INotifyPropertyChanged Members

    public event PropertyChangedEventHandler PropertyChanged;

    // This method is called by the Set accessor of each property.
    // The CallerMemberName attribute that is applied to the optional propertyName
    // parameter causes the property name of the caller to be substituted as an argument.
    private void NotifyPropertyChanged([CallerMemberName] String propertyName = "")
    {
      if (PropertyChanged != null)
      {
        PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
      }
    }


    #endregion
  }

}
