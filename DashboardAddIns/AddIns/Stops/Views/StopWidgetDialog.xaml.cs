using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using ESRI.ArcGIS.OperationsDashboard;
using client = ESRI.ArcGIS.Client;

namespace Stops.Views
{
  /// <summary>
  /// Interaction logic for StopWidgetDialog.xaml
  /// </summary>
  public partial class StopWidgetDialog : Window
  {
    public string            Caption              { get; private set; }
    public DataSource        StopDataSource       { get; private set; }
    public client.Field      TrackIdField         { get; private set; }
    public client.Field      GroupByField         { get; private set; }
    public client.Field      SortByField1         { get; private set; }
    public ListSortDirection SortByFieldOrder1    { get; private set; }
    public client.Field      SortByField2         { get; private set; }
    public ListSortDirection SortByFieldOrder2    { get; private set; }
    public client.Field      StopsRouteNameField  { get; private set; }
    public DataSource        RouteDataSource      { get; private set; }
    public client.Field      RoutesRouteNameField { get; private set; }

    private Dictionary<string, string> _vmProperties;


    public StopWidgetDialog(IList<DataSource> dataSources, string caption,
                            string stopDataSourceId,
                            string trackIdFieldName, string groupByFieldName,
                            string sortByFieldName1, string sortByFieldName2,
                            ListSortDirection sortByFieldOrder1, ListSortDirection sortByFieldOrder2, string stopsRouteNameFieldName,
                            string routeDataSourceId, string routesRouteNameFieldName,
                            Dictionary<string, string> vmProperties)
    {
      this.Resources.MergedDictionaries.Add(AddInsShare.Resources.SharedDictionaryManager.SharedStyleDictionary);

      InitializeComponent();

      // When re-configuring, initialize the widget config dialog from the existing settings.
      _vmProperties = vmProperties;

      // Caption
      CaptionTextBox.Text = caption;

      // stop data source selector
      if (!string.IsNullOrEmpty(stopDataSourceId))
      {
        DataSource stopDataSource = OperationsDashboard.Instance.DataSources.FirstOrDefault(ds => ds.Id == stopDataSourceId);
        if (stopDataSource != null)
        {
          StopDataSourceSelector.SelectedDataSource = stopDataSource;
          if (!string.IsNullOrEmpty(trackIdFieldName))
          {
            // Track Id Field Combo Box
            client.Field field = stopDataSource.Fields.FirstOrDefault(fld => fld.FieldName == trackIdFieldName);
            TrackIdFieldComboBox.SelectedItem = field;

            // Group By Field Combo Box
            field = stopDataSource.Fields.FirstOrDefault(fld => fld.FieldName == groupByFieldName);
            GroupByFieldComboBox.SelectedItem = field;

            // Group By Field Combo Box
            field = stopDataSource.Fields.FirstOrDefault(fld => fld.FieldName == groupByFieldName);
            GroupByFieldComboBox.SelectedItem = field;

            // Sort By Field 1 Combo Box
            field = stopDataSource.Fields.FirstOrDefault(fld => fld.FieldName == sortByFieldName1);
            SortByField1ComboBox.SelectedItem = field;

            // Sort By Field 2 Combo Box
            field = stopDataSource.Fields.FirstOrDefault(fld => fld.FieldName == sortByFieldName2);
            SortByField2ComboBox.SelectedItem = field;

            // Stops Route Name Field Combo Box
            field = stopDataSource.Fields.FirstOrDefault(fld => fld.FieldName == stopsRouteNameFieldName);
            StopsRouteNameComboBox.SelectedItem = field;

            // Routes Route Name Field Combo Box
            field = stopDataSource.Fields.FirstOrDefault(fld => fld.FieldName == routesRouteNameFieldName);
            RoutesRouteNameComboBox.SelectedItem = field;
          }
        }
      }


      // route data source selector
      if (!string.IsNullOrEmpty(routeDataSourceId))
      {
        DataSource routeDataSource = OperationsDashboard.Instance.DataSources.FirstOrDefault(ds => ds.Id == routeDataSourceId);
        if (routeDataSource != null)
        {
          RouteDataSourceSelector.SelectedDataSource = routeDataSource;

          // Routes Route Name Field Combo Box
          client.Field field = routeDataSource.Fields.FirstOrDefault(fld => fld.FieldName == groupByFieldName);
          RoutesRouteNameComboBox.SelectedItem = field;
        }
      }

      // Sort By Field Order Combo Boxes
      SortByFieldOrder1ComboBox.SelectedIndex = (sortByFieldOrder1 == ListSortDirection.Ascending ? 0 : 1);
      SortByFieldOrder2ComboBox.SelectedIndex = (sortByFieldOrder2 == ListSortDirection.Ascending ? 0 : 1);

      // Properties
      GepHostNameTextBox.Text  = vmProperties["GepHostName"];
      GepHttpPortTextBox.Text  = vmProperties["GepHttpPort"];
      GepHttpsPortTextBox.Text = vmProperties["GepHttpsPort"];
    }

    private void OKButton_Click(object sender, RoutedEventArgs e)
    {
      // Populate the data members from the widget config dialog

      Caption = CaptionTextBox.Text;
      StopDataSource        = StopDataSourceSelector.SelectedDataSource as DataSource;
      RouteDataSource       = RouteDataSourceSelector.SelectedDataSource as DataSource;
      TrackIdField          = (client.Field)TrackIdFieldComboBox.SelectedItem;
      GroupByField          = (client.Field)GroupByFieldComboBox.SelectedItem;
      SortByField1          = (client.Field)SortByField1ComboBox.SelectedItem;
      SortByField2          = (client.Field)SortByField2ComboBox.SelectedItem;
      SortByFieldOrder1     = (ListSortDirection)SortByFieldOrder1ComboBox.SelectedItem;
      SortByFieldOrder2     = (ListSortDirection)SortByFieldOrder2ComboBox.SelectedItem;
      StopsRouteNameField   = (client.Field)StopsRouteNameComboBox.SelectedItem;
      RoutesRouteNameField  = (client.Field)RoutesRouteNameComboBox.SelectedItem;

      _vmProperties["GepHostName"]  = GepHostNameTextBox.Text;
      _vmProperties["GepHttpPort"]  = GepHttpPortTextBox.Text;
      _vmProperties["GepHttpsPort"] = GepHttpsPortTextBox.Text;

      DialogResult = true;
    }

    private void CaptionTextBox_TextChanged(object sender, TextChangedEventArgs e)
    {
      ValidateInput(sender, null);
    }

    private void StopDataSourceSelector_SelectionChanged(object sender, EventArgs e)
    {
      DataSource dataSource = StopDataSourceSelector.SelectedDataSource;
      if (dataSource == null || dataSource.Fields == null)
        return;

      GroupByFieldComboBox.ItemsSource = dataSource.Fields;
      GroupByFieldComboBox.SelectedItem = dataSource.Fields[0];

      TrackIdFieldComboBox.ItemsSource = dataSource.Fields;
      TrackIdFieldComboBox.SelectedItem = dataSource.Fields[0];

      SortByField1ComboBox.ItemsSource = dataSource.Fields;
      SortByField1ComboBox.SelectedItem = dataSource.Fields[0];

      SortByField2ComboBox.ItemsSource = dataSource.Fields;
      SortByField2ComboBox.SelectedItem = dataSource.Fields[0];

      StopsRouteNameComboBox.ItemsSource = dataSource.Fields;
      StopsRouteNameComboBox.SelectedItem = dataSource.Fields[0];

      List<client.Field> numericFields = new List<client.Field>();
      foreach (var field in dataSource.Fields)
        ValidateInput(sender, null);
    }

    private void RouteDataSourceSelector_SelectionChanged(object sender, EventArgs e)
    {
      DataSource dataSource = RouteDataSourceSelector.SelectedDataSource;
      if (dataSource == null || dataSource.Fields == null)
        return;

      RoutesRouteNameComboBox.ItemsSource = dataSource.Fields;
      RoutesRouteNameComboBox.SelectedItem = dataSource.Fields[0];

      List<client.Field> numericFields = new List<client.Field>();
      foreach (var field in dataSource.Fields)
        ValidateInput(sender, null);
    }

    private void ValidateInput(object sender, TextChangedEventArgs e)
    {
      if (OKButton == null)
        return;

      OKButton.IsEnabled = false;
      if (string.IsNullOrEmpty(CaptionTextBox.Text))
        return;

      OKButton.IsEnabled = true;
    }

  }
}
