using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.ComponentModel.Composition;
using System.Runtime.CompilerServices;
using System.Runtime.Serialization;
using System.Windows;
using System.Windows.Controls;
using RouteMonitor.WidgetConfig;
using ESRI.ArcGIS.OperationsDashboard;
using System.Linq;
using AddInsShare.ViewModels;

namespace RouteMonitor.Views
{
    /// <summary>
    /// A Widget is a dockable add-in class for Operations Dashboard for ArcGIS that implements IWidget. By returning true from CanConfigure, 
    /// this widget provides the ability for the user to configure the widget properties showing a settings Window in the Configure method.
    /// By implementing IDataSourceConsumer, this Widget indicates it requires a DataSource to function and will be notified when the 
    /// data source is updated or removed.
    /// </summary>
    // hide widget - [Export("ESRI.ArcGIS.OperationsDashboard.Widget")]
    [ExportMetadata("DisplayName", "Overview Map")]
    [ExportMetadata("Description", "A widget that displays an overview map.")]
    [ExportMetadata("ImagePath", "/RouteMonitor;component/Images/OverviewMapWidgetIcon.png")]
    [ExportMetadata("DataSourceRequired", true)]
    [DataContract]
    public partial class OverviewMapWidget : UserControl, IWidget, INotifyPropertyChanged
    {

        private MapWidget _mapWidget = null;
        internal BaseDataGridViewModel DataGridViewModel = null;

        /// <summary>
        /// A unique identifier of a data source in the configuration. This property is set during widget configuration.
        /// </summary>
        [DataMember(Name = "dataSourceId")]
        public string DataSourceId { get; set; }

        /// <summary>
        /// The name of a field within the selected data source. This property is set during widget configuration.
        /// </summary>
        [DataMember(Name = "field")]
        public string Field { get; set; }


        /// <summary>
        /// Id of the Map widget within the Opreational view.
        /// </summary>
        [DataMember(Name = "mapWidgetId")]
        public string MapWidgetID { get; set; }

        private string _layerURL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
        /// <summary>
        /// URL for the OverviewMap Layer.
        /// </summary>
        [DataMember(Name = "layerURL")]
        public string LayerURL
        {
            get
            {
                return _layerURL;
            }

            set
            {
                if (value != _layerURL)
                {
                    _layerURL = value;
                    NotifyPropertyChanged();
                }
            }
        }


        public OverviewMapWidget()
        {
            InitializeComponent();
        }

        void _mapWidget_Initialized(object sender, EventArgs e)
        {
            MyOverviewMap.Map = MapWidget.Map;
        }


        private void UpdateControls()
        {
            TiledLayer.Url = LayerURL;

            IEnumerable<ESRI.ArcGIS.OperationsDashboard.MapWidget> enumMapWidgets = OperationsDashboard.Instance.Widgets.OfType<ESRI.ArcGIS.OperationsDashboard.MapWidget>();

            foreach (Widget widget in enumMapWidgets)
            {
                if (widget.Id == MapWidgetID)
                {
                    this.MapWidget = (MapWidget)widget;
                    _mapWidget.Initialized -= _mapWidget_Initialized;
                    _mapWidget.Initialized += _mapWidget_Initialized;
                    break;
                }
            }
            if (_mapWidget.IsInitialized)
            {
                MyOverviewMap.Map = MapWidget.Map;
            }
        }

        #region IWidget Members

        private string _caption = "Overview Map";
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
        /// OnActivated is called when the widget is first added to the configuration, or when loading from a saved configuration, after all 
        /// widgets have been restored. Saved properties can be retrieved, including properties from other widgets.
        /// Note that some widgets may have properties which are set asynchronously and are not yet available.
        /// </summary>
        public void OnActivated()
        {
            UpdateControls();
        }

        /// <summary>
        ///  OnDeactivated is called before the widget is removed from the configuration.
        /// </summary>
        public void OnDeactivated()
        {
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
        public bool Configure(Window owner, IList<DataSource> dataSources)
        {
            OverviewMapWidgetDialog dialog = new OverviewMapWidgetDialog(Caption, MapWidgetID, LayerURL) { Owner = owner };
            if (dialog.ShowDialog() != true)
                return false;

            // Retrieve the selected values for the properties from the configuration dialog.
            Caption = dialog.Caption;
            LayerURL = dialog.LayerURL;
            MapWidgetID = ((MapWidget)dialog.LocalMapWidget).Id;

            // The default UI simply shows the values of the configured properties.
            UpdateControls();
            return true;
        }

        #endregion

        public MapWidget MapWidget
        {
            get { return _mapWidget; }
            set { _mapWidget = value; }
        }


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
