﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.Composition;
using System.Runtime.CompilerServices;
using System.Runtime.Serialization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using AddInsShare.Common;
using AddInsShare.Resources;
using AddInsShare.Schema;
using AddInsShare.ViewModels;
using Alerts.ViewModels;
using ESRI.ArcGIS.OperationsDashboard;
using OD = ESRI.ArcGIS.OperationsDashboard;

namespace Alerts.Views
{
  /// <summary>
  /// A Widget is a dockable extension to the ArcGIS Viewer application that implements IWidget. By returning true from CanConfigure, 
  /// this widget provides the ability for the user to configure the widget properties showing a settings Window in the Configure method.
  /// By implementing IDataSourceConsumer, this Widget indicates it requires a DataSource to function and will be notified when the 
  /// data source is updated or removed.
  /// </summary>
  [Export("ESRI.ArcGIS.OperationsDashboard.Widget")]
  [ExportMetadata("DisplayName", "Alerts")]
  [ExportMetadata("Description", "A widget that displays and manages alerts.")]
  [ExportMetadata("ImagePath", "/Alerts;component/Images/AlertWidgetIcon.png")]
  [ExportMetadata("DataSourceRequired", true)]
  [DataContract]
  public partial class AlertWidget : UserControl, IWidget, IDataSourceConsumer, INotifyPropertyChanged
  {

    #region Data Members

    private OD.DataSource           _dataSource       = null;
    internal BaseDataGridViewModel  DataGridViewModel = null;


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
    [DataMember(Name = "dataSourceId")]
    public string DataSourceId { get; set; }

    /// <summary>
    /// The name of the Track Id Field within the selected data source. This property is set during widget configuration.
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
    /// The GeoEvnet Processor Properties
    /// </summary>
    [DataMember(Name = "properties")]
    public Dictionary<string, string> Properties { get; set; }

    #endregion


    public AlertWidget()
    {
      this.Resources.MergedDictionaries.Add(SharedDictionaryManager.SharedStyleDictionary);
      this.Resources.MergedDictionaries.Add(SharedDictionaryManager.SharedDataTemplateDictionary);

      InitializeComponent();

      DataGridViewModel = new AlertViewModel(null);
      DataContext = DataGridViewModel;

      Caption = "Alerts";
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
      AlertViewModel vm = DataGridViewModel as AlertViewModel;
      if (vm == null)
        return false;

      try
      {
        vm.SkipUpdates = true;

        // Show the configuration dialog
        AlertWidgetDialog dialog = new AlertWidgetDialog
          ( dataSources, Caption, DataSourceId, TrackIdFieldName,
            GroupByFieldName, SortByFieldName1, SortByFieldName2,
            SortByFieldOrder1, SortByFieldOrder2,
            vm.Properties ) { Owner = owner };

        if (dialog.ShowDialog() != true)
        {
          DataGridViewModel.SkipUpdates = false;
          return false;
        }

        // Retrieve the selected values for the properties from the configuration dialog.
        Caption           = dialog.Caption;
        DataSourceId      = dialog.DataSource.Id;
        TrackIdFieldName  = dialog.TrackIdField.Name;
        GroupByFieldName  = dialog.GroupByField.Name;
        SortByFieldName1  = dialog.SortByField1.Name;
        SortByFieldName2  = dialog.SortByField2.Name;
        SortByFieldOrder1 = dialog.SortByFieldOrder1;
        SortByFieldOrder2 = dialog.SortByFieldOrder2;

        vm.TrackIdFieldName  = TrackIdFieldName;
        vm.GroupByFieldName  = GroupByFieldName;
        vm.SortByFieldName1  = SortByFieldName1;
        vm.SortByFieldName2  = SortByFieldName2;
        vm.SortByFieldOrder1 = SortByFieldOrder1;
        vm.SortByFieldOrder2 = SortByFieldOrder2;

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

    private void SetDataSource(DataSource dataSource)
    {
      if (_dataSource != dataSource)
      {
        AlertViewModel vm = DataGridViewModel as AlertViewModel;
        vm.TrackIdFieldName = TrackIdFieldName;
        vm.GroupByFieldName = GroupByFieldName;
        vm.SortByFieldName1 = SortByFieldName1;
        vm.SortByFieldName2 = SortByFieldName2;
        vm.SetDataSource(dataSource);
        _dataSource = dataSource;
      }
    }

    private void PopulateGrid()
    {
      (DataGridViewModel as BaseDataGridViewModel).Update(_dataSource);
      textBlockAlerts.Text = Caption + " (" + DataGridViewModel.GetItemsCount().ToString() + ")";
    }

    #endregion

    #region UI Handlers (to be replaced with MVVM)

    private void OnAutoGeneratingColumn(object sender, DataGridAutoGeneratingColumnEventArgs e)
    {
      e.Column.CanUserSort = true;
      e.Column.MinWidth    = 0.0;
    }

    private void AlertImage_MouseDown_1(object sender, MouseButtonEventArgs e)
    {
      if (DataContext is AlertViewModel)
      {
        DataGridViewModel.ToggleLayerVisibility();
      }
    }

    private void textBoxSearch_TextChanged_1(object sender, TextChangedEventArgs e)
    {
      if (DataContext is AlertViewModel)
        DataGridViewModel.SetFilterString(textBoxSearch.Text);
    }

    private void DisplayAssignPopup(object sender, RoutedEventArgs e)
    {
      tbAssignTo.Text = "";
      Item item = DataGridViewModel.GetSelectedItem();
      if (item != null)
      {
        string fieldName = DataGridViewModel.GetPropValue("ALERT_ASSIGN_TO_FIELD_ALIAS");
        if (!String.IsNullOrEmpty(fieldName))
        {
          object objValue = item.GetPropertyValue(fieldName);
          if (objValue != null)
            tbAssignTo.Text = objValue.ToString();
        }
      }

      puAssignPopup.IsOpen = true;
    }

    private void btnAssignToCancel_Click(object sender, RoutedEventArgs e)
    {
      puAssignPopup.IsOpen = false;
    }

    private void btnAssignToOK_Click(object sender, RoutedEventArgs e)
    {
      puAssignPopup.IsOpen = false;
      (DataGridViewModel as AlertViewModel).AssignToCommand.Execute(tbAssignTo.Text);
    }

    private void DisplayNotePopup(object sender, RoutedEventArgs e)
    {
      tbNote.Text = "";
      Item item = DataGridViewModel.GetSelectedItem();
      if (item != null)
      {
        string fieldName = DataGridViewModel.GetPropValue("ALERT_NOTE_FIELD_ALIAS");
        if (!String.IsNullOrEmpty(fieldName))
        {
          object objValue = item.GetPropertyValue(fieldName);
          if (objValue != null)
            tbNote.Text = objValue.ToString();
        }
      }

      puNotePopup.IsOpen = true;
    }

    private void btnNoteCancel_Click(object sender, RoutedEventArgs e)
    {
      puNotePopup.IsOpen = false;
    }

    private void btnNoteOK_Click(object sender, RoutedEventArgs e)
    {
      puNotePopup.IsOpen = false;
      (DataGridViewModel as AlertViewModel).NoteCommand.Execute(tbNote.Text);
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

    private void buttonDeleteAll_Click_1(object sender, RoutedEventArgs e)
    {
      if (DataGridViewModel != null)
        DataGridViewModel.OnDeleteAll(null);
    }

  }
}
