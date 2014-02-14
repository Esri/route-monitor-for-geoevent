using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using ESRI.ArcGIS.OperationsDashboard;

namespace RouteMonitor.WidgetConfig
{

    /// <summary>
    /// Interaction logic for OverviewMapWidgetDialog.xaml
    /// </summary>
    public partial class OverviewMapWidgetDialog : Window
    {

        public Object LocalMapWidget { get; private set; }
        public string Caption { get; private set; }
        public string LayerURL { get; private set; }

        public OverviewMapWidgetDialog(string initialCaption, string initialWidgetId, string initialLayerURL)
        {
            InitializeComponent();

            // When re-configuring, initialize the widget config dialog from the existing settings.
            CaptionTextBox.Text = initialCaption;

            if (!string.IsNullOrEmpty(initialLayerURL))
            {
                LayerURLText.Text = initialLayerURL;
            }

            IEnumerable<ESRI.ArcGIS.OperationsDashboard.MapWidget> enumMapWidgets = OperationsDashboard.Instance.Widgets.OfType<ESRI.ArcGIS.OperationsDashboard.MapWidget>();

            foreach (MapWidget widget in enumMapWidgets)
            {
                MapWidgetIDComboBox.Items.Add(widget);
                MapWidgetIDComboBox.SelectedIndex = 0;
            }

            if (!string.IsNullOrEmpty(initialWidgetId))
            {
                Widget widget = (Widget)OperationsDashboard.Instance.Widgets.FirstOrDefault(ds => ds.Id == initialWidgetId);
                MapWidgetIDComboBox.SelectedItem = widget;
            }
        }

        private void OKButton_Click(object sender, RoutedEventArgs e)
        {
            Caption = CaptionTextBox.Text;
            LayerURL = LayerURLText.Text;
            LocalMapWidget = MapWidgetIDComboBox.SelectedItem;
            DialogResult = true;
        }
    }
}
