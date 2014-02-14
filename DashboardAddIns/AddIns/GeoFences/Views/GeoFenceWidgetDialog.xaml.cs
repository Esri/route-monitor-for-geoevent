using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using ESRI.ArcGIS.OperationsDashboard;
using client = ESRI.ArcGIS.Client;

namespace GeoFences.Views
{
  /// <summary>
  /// Interaction logic for GeoFenceWidgetDialog.xaml
  /// </summary>
  public partial class GeoFenceWidgetDialog : Window
  {
    public string            Caption           { get; private set; }
    public DataSource        DataSource        { get; private set; }
    public DataSource        RouteDataSource   { get; private set; }
    public client.Field      TrackIdField      { get; private set; }
    public client.Field      NameField         { get; private set; }
    public client.Field      CategoryField     { get; private set; }
    public client.Field      ActiveField       { get; private set; }
    public client.Field      GroupByField      { get; private set; }
    public client.Field      SortByField1      { get; private set; }
    public client.Field      SortByField2      { get; private set; }
    public ListSortDirection SortByFieldOrder1 { get; private set; }
    public ListSortDirection SortByFieldOrder2 { get; private set; }

    private Dictionary<string, string> _vmProperties;


    public GeoFenceWidgetDialog(IList<DataSource> dataSources, string caption,
                                string dataSourceId,
                                string trackIdFieldName,
                                string nameFieldName, string categoryFieldName, string activeFieldName,
                                string groupByFieldName, string sortByFieldName1, string sortByFieldName2,
                                ListSortDirection sortByFieldOrder1, ListSortDirection sortByFieldOrder2,
                                Dictionary<string, string> vmProperties)
    {
      this.Resources.MergedDictionaries.Add(AddInsShare.Resources.SharedDictionaryManager.SharedStyleDictionary);

      InitializeComponent();

      // When re-configuring, initialize the widget config dialog from the existing settings.
      _vmProperties = vmProperties;

      // Caption
      CaptionTextBox.Text = caption;

      // data source selector
      if (!string.IsNullOrEmpty(dataSourceId))
      {
        DataSource dataSource = OperationsDashboard.Instance.DataSources.FirstOrDefault(ds => ds.Id == dataSourceId);
        if (dataSource != null)
        {
          DataSourceSelector.SelectedDataSource = dataSource;
          if (!string.IsNullOrEmpty(trackIdFieldName))
          {
            // Track Id Field Combo Box
            client.Field field = dataSource.Fields.FirstOrDefault(fld => fld.Name == trackIdFieldName);
            TrackIdFieldComboBox.SelectedItem = field;

            // Name Field Combo Box
            field = dataSource.Fields.FirstOrDefault(fld => fld.Name == nameFieldName);
            NameFieldComboBox.SelectedItem = field;

            // Category Field Combo Box
            field = dataSource.Fields.FirstOrDefault(fld => fld.Name == categoryFieldName);
            CategoryFieldComboBox.SelectedItem = field;

            // Active Field Combo Box
            field = dataSource.Fields.FirstOrDefault(fld => fld.Name == activeFieldName);
            ActiveFieldComboBox.SelectedItem = field;

            // Group By Field Combo Box
            field = dataSource.Fields.FirstOrDefault(fld => fld.Name == groupByFieldName);
            GroupByFieldComboBox.SelectedItem = field;

            // Group By Field Combo Box
            field = dataSource.Fields.FirstOrDefault(fld => fld.Name == groupByFieldName);
            GroupByFieldComboBox.SelectedItem = field;

            // Sort By Field 1 Combo Box
            field = dataSource.Fields.FirstOrDefault(fld => fld.Name == sortByFieldName1);
            SortByField1ComboBox.SelectedItem = field;

            // Sort By Field 2 Combo Box
            field = dataSource.Fields.FirstOrDefault(fld => fld.Name == sortByFieldName2);
            SortByField2ComboBox.SelectedItem = field;
          }
        }
      }

      // Sort By Field Order Combo Boxes
      SortByFieldOrder1ComboBox.SelectedIndex = (sortByFieldOrder1 == ListSortDirection.Ascending ? 0 : 1);
      SortByFieldOrder2ComboBox.SelectedIndex = (sortByFieldOrder2 == ListSortDirection.Ascending ? 0 : 1);

      // Properties
      GeometryServiceUrlTextBox.Text    = vmProperties["GeometryServiceUrl"];
      ServiceAreaServiceUrlTextBox.Text = vmProperties["ServiceAreaServiceUrl"];
    }

    private void OKButton_Click(object sender, RoutedEventArgs e)
    {
      // Populate the data members from the widget config dialog

      Caption = CaptionTextBox.Text;
      DataSource        = DataSourceSelector.SelectedDataSource as DataSource;
      TrackIdField      = (client.Field)TrackIdFieldComboBox.SelectedItem;
      NameField         = (client.Field)NameFieldComboBox.SelectedItem;
      CategoryField     = (client.Field)CategoryFieldComboBox.SelectedItem;
      ActiveField       = (client.Field)ActiveFieldComboBox.SelectedItem;
      GroupByField      = (client.Field)GroupByFieldComboBox.SelectedItem;
      SortByField1      = (client.Field)SortByField1ComboBox.SelectedItem;
      SortByField2      = (client.Field)SortByField2ComboBox.SelectedItem;
      SortByFieldOrder1 = (ListSortDirection)SortByFieldOrder1ComboBox.SelectedItem;
      SortByFieldOrder2 = (ListSortDirection)SortByFieldOrder2ComboBox.SelectedItem;

      _vmProperties["GeometryServiceUrl"] = GeometryServiceUrlTextBox.Text;
      _vmProperties["ServiceAreaServiceUrl"] = ServiceAreaServiceUrlTextBox.Text;

      DialogResult = true;
    }

    private void CaptionTextBox_TextChanged(object sender, TextChangedEventArgs e)
    {
      ValidateInput(sender, null);
    }

    private void DataSourceSelector_SelectionChanged(object sender, EventArgs e)
    {
      DataSource dataSource = DataSourceSelector.SelectedDataSource;
      if (dataSource == null || dataSource.Fields == null)
        return;

      TrackIdFieldComboBox.ItemsSource = dataSource.Fields;
      TrackIdFieldComboBox.SelectedItem = dataSource.Fields[0];

      NameFieldComboBox.ItemsSource = dataSource.Fields;
      NameFieldComboBox.SelectedItem = dataSource.Fields[0];

      CategoryFieldComboBox.ItemsSource = dataSource.Fields;
      CategoryFieldComboBox.SelectedItem = dataSource.Fields[0];

      ActiveFieldComboBox.ItemsSource = dataSource.Fields;
      ActiveFieldComboBox.SelectedItem = dataSource.Fields[0];

      GroupByFieldComboBox.ItemsSource = dataSource.Fields;
      GroupByFieldComboBox.SelectedItem = dataSource.Fields[0];

      SortByField1ComboBox.ItemsSource = dataSource.Fields;
      SortByField1ComboBox.SelectedItem = dataSource.Fields[0];

      SortByField2ComboBox.ItemsSource = dataSource.Fields;
      SortByField2ComboBox.SelectedItem = dataSource.Fields[0];

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
