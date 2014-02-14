using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Geometry;
using ESRI.ArcGIS.Client.Tasks;
using ESRI.ArcGIS.Client.WebMap;
using FieldWorker.Common;
using FieldWorker.Schema;
using FieldWorker.ViewModels;
using System;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.Configuration;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Threading;

namespace FieldWorker.Views
{
  public partial class MainWindow : Window
  {
    private Map          _map          = null;
    private WebMap       _webMap       = null;
    private Login        _loginDialog  = null;
    private String       _username     = null;
    private String       _routeName    = null;

    private FeatureLayerHelper _flVehicles        = null;
    private FeatureLayerHelper _flWorkers         = null;
    private FeatureLayerHelper _flStops           = null;
    private FeatureLayerHelper _flRoutes          = null;
    private FeatureLayerHelper _flMessages        = null;
    private FeatureLayerHelper _flStopsPending    = null;
    private FeatureLayerHelper _flMessagesPending = null;
    private FeatureLayerHelper _flRouteAssignment = null;

    private String _vehiclesVehicleNameFieldName  = null;
    private String _routesVehicleNameFieldName    = null;
    private String _routesRouteNameFieldName      = null;
    private String _stopsRouteNameFieldName       = null;
    private String _workersDriverNameFieldName    = null;
    private String _messagesRouteNameFieldName    = null;

    private String _vehiclesVehicleNameFieldAlias = ConfigurationManager.AppSettings.Get("VehiclesVehicleNameFieldAlias");
    private String _routesVehicleNameFieldAlias   = ConfigurationManager.AppSettings.Get("RoutesVehicleNameFieldAlias");
    private String _routesRouteNameFieldAlias     = ConfigurationManager.AppSettings.Get("RoutesRouteNameFieldAlias");
    private String _stopsRouteNameFieldAlias      = ConfigurationManager.AppSettings.Get("StopsRouteNameFieldAlias");
    private String _workersDriverNameFieldAlias   = ConfigurationManager.AppSettings.Get("WorkersDriverNameFieldAlias");
    private String _messagesRouteNameFieldAlias   = ConfigurationManager.AppSettings.Get("MessagesRouteNameFieldAlias");

    private String _layerNameVehicles         = ConfigurationManager.AppSettings.Get("LayerNameVehicles");
    private String _layerNameWorkers          = ConfigurationManager.AppSettings.Get("LayerNameWorkers");
    private String _layerNameStops            = ConfigurationManager.AppSettings.Get("LayerNameStops");
    private String _layerNameRoutes           = ConfigurationManager.AppSettings.Get("LayerNameRoutes");
    private String _layerNameMessages         = ConfigurationManager.AppSettings.Get("LayerNameMessages");
    private String _layerNameStopsPending     = ConfigurationManager.AppSettings.Get("LayerNameStopsPending");
    private String _layerNameMessagesPending  = ConfigurationManager.AppSettings.Get("LayerNameMessagesPending");
    private String _layerNameRouteAssignment  = ConfigurationManager.AppSettings.Get("LayerNameRouteAssignment");

    private String _gepBaseUrl   = ConfigurationManager.AppSettings.Get("GEPBaseUrl");
    private String _webMapItemId = ConfigurationManager.AppSettings.Get("WebMapItemID");

    private StopsViewModel    _vmStops;
    private MessagesViewModel _vmMessages;
    private DispatcherTimer   _updateMapTimer;
    private DispatcherTimer   _updateStopsTimer;
    private DispatcherTimer   _updateMessagesTimer;

    private bool _skipUpdates = false;

    private System.Windows.Media.Brush _defaultButtonBackgroud     = null;
    private System.Windows.Media.Brush _highlightedButtonBackgroud = System.Windows.Media.Brushes.LightGreen;


    public MainWindow()
    {
      InitializeComponent();
      ReadConfigurationSettings();

      // Wire up the method that will handle the Challenge for credentials.
      IdentityManager.Current.ChallengeMethod = Challenge;

      // Use the toolkit's SignInDialog for all credential needs
      //IdentityManager.Current.ChallengeMethodEx = SignInDialog.DoSignInEx;

      StopPanel.Visibility    = System.Windows.Visibility.Collapsed;
      MessagePanel.Visibility = System.Windows.Visibility.Collapsed;

      stopUpdatableAttributesDataGrid.CommandBindings.Add(new CommandBinding(ApplicationCommands.Paste, new ExecutedRoutedEventHandler(stopUpdatableAttributesDataGrid_ExecutedRoutedEventHandler), new CanExecuteRoutedEventHandler(stopUpdatableAttributesDataGrid_CanExecuteRoutedEventHandler)));
    }

    private void ReadConfigurationSettings()
    {
      // place holder for a Generic App Settings
      NameValueCollection genericAppSettingsSection = (NameValueCollection)ConfigurationManager.GetSection("genericAppSettings");
    }

    private string[] GetStatusRemarks(string key)
    {
      string value = _vmStops.StatusRemarkLookup.Get(key);
      if (value == null)
        return null;

      return value.Split(',');
    }

    private void LookupFieldNames()
    {
      if (_vehiclesVehicleNameFieldName == null)
        _vehiclesVehicleNameFieldName = _flVehicles.FieldAliasToName(_vehiclesVehicleNameFieldAlias);
      if (_routesVehicleNameFieldName == null)
        _routesVehicleNameFieldName   = _flRoutes.FieldAliasToName(_routesVehicleNameFieldAlias);
      if (_routesRouteNameFieldName == null)
        _routesRouteNameFieldName     = _flRoutes.FieldAliasToName(_routesRouteNameFieldAlias);
      if (_stopsRouteNameFieldName == null)
        _stopsRouteNameFieldName = _flStops.FieldAliasToName(_stopsRouteNameFieldAlias);
      if (_workersDriverNameFieldName == null)
        _workersDriverNameFieldName = _flWorkers.FieldAliasToName(_workersDriverNameFieldAlias);
      if (_messagesRouteNameFieldName == null)
        _messagesRouteNameFieldName = _flMessages.FieldAliasToName(_messagesRouteNameFieldAlias);
    }

    private void Window_Loaded(object sender, RoutedEventArgs e)
    {
      if (_loginDialog == null)
        _loginDialog = new Login("", "", this);

      TryLoadWebMap(_webMapItemId);
    }

    private void TryLoadWebMap(string itemId)
    {
      try
      {
        // Create a new WebMap Document
        ESRI.ArcGIS.Client.WebMap.Document document = new ESRI.ArcGIS.Client.WebMap.Document();

        // Handle the GetMapCompleted event.
        document.GetMapCompleted += (s, getMapCompletedEventArgs) =>
        {
          // Check the Error property of the GetMapCompletedEventArgs for a connection or login failure.
          if (getMapCompletedEventArgs.Error != null)
          {
            // Show an error message box
            string message = getMapCompletedEventArgs.Error.Message + "\nWould you like to retry?";
            MessageBoxResult result = MessageBox.Show(message, "Error Loading WebMap", MessageBoxButton.YesNo, MessageBoxImage.Error);
            if (result == MessageBoxResult.Yes)
              TryLoadWebMap(itemId);
            else
              Application.Current.Shutdown();

            return;
          }

          // Webmap loaded sucessfully, insert the loaded map into the window and continue
          MapGrid.Children.Insert(0, getMapCompletedEventArgs.Map);
          _map = getMapCompletedEventArgs.Map;
          _webMap = getMapCompletedEventArgs.WebMap;
          _username = MainSignInDialog.UserName;
          MapLoaded(getMapCompletedEventArgs);
        };

        // Call the GetMayAsync method to retrieve the WebMap by ArcGIS.com item identifier.
        document.GetMapAsync(itemId, null);
      }
      catch (Exception ex)
      {
        System.Diagnostics.Debug.WriteLine(ex.Message);
      }
    }

    private void MapLoaded(GetMapCompletedEventArgs args)
    {
      try
      {
        _flVehicles         = new FeatureLayerHelper(_map, _layerNameVehicles, FeatureLayer.QueryMode.Snapshot, true);
        _flWorkers          = new FeatureLayerHelper(_map, _layerNameWorkers, FeatureLayer.QueryMode.Snapshot, true);
        _flStops            = new FeatureLayerHelper(_map, _layerNameStops, FeatureLayer.QueryMode.Snapshot, true);
        _flRoutes           = new FeatureLayerHelper(_map, _layerNameRoutes, FeatureLayer.QueryMode.Snapshot, true);
        _flMessages         = new FeatureLayerHelper(_map, _layerNameMessages, FeatureLayer.QueryMode.Snapshot, false);
        _flStopsPending     = new FeatureLayerHelper(_map, _layerNameStopsPending, FeatureLayer.QueryMode.Snapshot, false);
        _flMessagesPending  = new FeatureLayerHelper(_map, _layerNameMessagesPending, FeatureLayer.QueryMode.Snapshot, false);
        _flRouteAssignment  = new FeatureLayerHelper(_map, _layerNameRouteAssignment, FeatureLayer.QueryMode.Snapshot, false);

        _routeName = QueryRouteNameByUserName();

        StopPanel.Visibility    = System.Windows.Visibility.Visible;
        MessagePanel.Visibility = System.Windows.Visibility.Visible;

        StopsListBox.SelectedIndex = 0;
        MessagesListBox.SelectedIndex = 0;
        DataContext = this;

        // Map refresh timer
        int mapRefreshRateSec = 0;
        if (!Int32.TryParse(ConfigurationManager.AppSettings.Get("MapRefreshRateSec"), out mapRefreshRateSec))
          mapRefreshRateSec = 5;

        _updateMapTimer = new DispatcherTimer();
        _updateMapTimer.Interval = new TimeSpan(0, 0, mapRefreshRateSec);
        _updateMapTimer.Tick += new EventHandler(UpdateMapTimer_Tick);
        //_updateMapTimer.Start();

        // Stops refresh timer
        int stopsRefreshRateSec = 0;
        if (!Int32.TryParse(ConfigurationManager.AppSettings.Get("StopsRefreshRateSec"), out stopsRefreshRateSec))
          stopsRefreshRateSec = 5;

        _updateStopsTimer = new DispatcherTimer();
        _updateStopsTimer.Interval = new TimeSpan(0, 0, stopsRefreshRateSec);
        _updateStopsTimer.Tick += new EventHandler(UpdateStopsTimer_Tick);
        //_updateStopsTimer.Start();

        // Messages refresh timer
        int messagesRefreshRateSec = 0;
        if (!Int32.TryParse(ConfigurationManager.AppSettings.Get("MessagesRefreshRateSec"), out messagesRefreshRateSec))
          messagesRefreshRateSec = 5;

        _updateMessagesTimer = new DispatcherTimer();
        _updateMessagesTimer.Interval = new TimeSpan(0, 0, messagesRefreshRateSec);
        _updateMessagesTimer.Tick += new EventHandler(UpdateMessagesTimer_Tick);
        //_updateMessagesTimer.Start();

        RefreshAll();
      }
      catch (Exception ex)
      {
        System.Diagnostics.Debug.WriteLine(ex.Message);
      }
    }

    private void Challenge(string url, Action<IdentityManager.Credential, Exception> callback, IdentityManager.GenerateTokenOptions options)
    {
      // Option 1: Access to url, callback, and options passed to SignInDialog
      //SignInDialog.DoSignIn(url, callback, options);

      // Option 2: Use Popup to contain SignInDialog
      //var popup = new Popup
      //{
      //    HorizontalOffset = 200,
      //    VerticalOffset = 200
      //};

      //SignInDialog signInDialog = new SignInDialog()
      //{
      //    Width = 300,
      //    Url = url,
      //    IsActive = true,
      //    Callback = (credential, ex) =>
      // {
      //     callback(credential, ex);
      //     popup.IsOpen = false;
      // }
      //};
      //popup.Child = signInDialog;
      //popup.IsOpen = true;

      // Option 3: Use a template to define SignInDialog content
      //MainSignInDialog.Url = url;
      //MainSignInDialog.IsActive = true;
      //MainSignInDialog.Visibility = System.Windows.Visibility.Visible;

      // Option 4: Use the default Runtime template to define SignInDialog content
      if (_username != null)
        MainSignInDialog.UserName = _username;

      MainSignInDialog.Url = url;
      MainSignInDialog.IsActive = true;
      MainSignInDialog.Visibility = System.Windows.Visibility.Visible;

      MainSignInDialog.Callback = (credential, ex) =>
      {
        callback(credential, ex);
        MainSignInDialog.Visibility = System.Windows.Visibility.Collapsed;
        MainSignInDialog.IsActive = false;
      };
   }

    private void ChallengeWithTheLoginDialog(string url, System.Action<IdentityManager.Credential, Exception> callback, IdentityManager.GenerateTokenOptions options)
    {
      _loginDialog.ShowDialog();

      // When challenged for a token generate a set of credentials based on the specified username and password.
      IdentityManager.Current.GenerateCredentialAsync(url, _loginDialog.UserName, _loginDialog.Password, (credential, ex) =>
      {
        // Raise the Action to return to the method that triggered the challenge (GetMapAsync).
        callback(credential, ex);
      }, options);
    }

    private string QueryRouteNameByUserName()
    {
      string routeName = null;

      while (String.IsNullOrEmpty(_username))
      {
        _loginDialog.ShowDialog();
        _username = _loginDialog.UserName;
      }

      try
      {
        string accountNameFieldAlias = ConfigurationManager.AppSettings.Get("RouteAssignmentAccountFieldAlias");
        string accountNameFieldName = _flRouteAssignment.FieldAliasToName(accountNameFieldAlias);

        QueryTask queryTask = new QueryTask(_flRouteAssignment.Url);
        ESRI.ArcGIS.Client.Tasks.Query query = new ESRI.ArcGIS.Client.Tasks.Query();
        query.Where = accountNameFieldName + "='" + _username + "'";
        query.ReturnGeometry = false;
        query.OutFields.AddRange(new string[] { "*" });
        queryTask.Execute(query);
        if (queryTask.LastResult.Features.Count() > 0)
        {
          string routeNameFieldAlias = ConfigurationManager.AppSettings.Get("RouteAssignmentRouteFieldAlias");
          string routeNameFieldName = _flRouteAssignment.FieldAliasToName(routeNameFieldAlias);
          Graphic feature = queryTask.LastResult.Features.First();
          if (feature.Attributes.ContainsKey(routeNameFieldName))
            routeName = feature.Attributes[routeNameFieldName].ToString();
        }
      }
      catch (Exception ex)
      {
        System.Diagnostics.Debug.WriteLine(ex.Message);
      }

      if (routeName == null)
      {
        MessageBox.Show("Please make sure '" + _username + "' is listed in the '" + _flRouteAssignment.FeatureLayer.DisplayName + "' feature service layer.", "Error retreiving the Route Name for " + _username, MessageBoxButton.OK, MessageBoxImage.Error);
        Application.Current.Shutdown();
      }

      return routeName;
    }

    private Graphic QueryRouteFeatureByName(String routeName)
    {
      LookupFieldNames();
      if (_routesRouteNameFieldName == null)
        return null;

      // TODO: get the feature directly from the feature layer instead of querying for it

      QueryTask queryTask = new QueryTask(_flRoutes.Url);
      ESRI.ArcGIS.Client.Tasks.Query query = new ESRI.ArcGIS.Client.Tasks.Query();
      query.Where = (_routesRouteNameFieldName + "='" + routeName + "'");
      query.ReturnGeometry = true;
      query.OutFields.AddRange(new string[] { "*" });
      queryTask.Execute(query);
      if (queryTask.LastResult.Features.Count() > 0)
        return queryTask.LastResult.Features.First();
      else
        return null;
    }

    private void RefreshMessagesFeatureLayer()
    {
      LookupFieldNames();
      if (_messagesRouteNameFieldName != null)
      {
        if (String.IsNullOrWhiteSpace(_routeName))
          _flMessages.Where = "1=1";
        else
          _flMessages.Where = _messagesRouteNameFieldName + "='" + _routeName + "'";

        _flMessages.Update();
      }
    }

    private void RefreshStopsFeatureLayer()
    {
      LookupFieldNames();
      if (_stopsRouteNameFieldName != null)
      {
        if (String.IsNullOrWhiteSpace(_routeName))
          _flStops.Where = "1=1";
        else
          _flStops.Where = _stopsRouteNameFieldName + "='" + _routeName + "'";

        _flStops.Update();
      }
    }

    private void RefreshRoutesFeatureLayer()
    {
      LookupFieldNames();
      if (_routesRouteNameFieldName != null)
      {
        if (String.IsNullOrWhiteSpace(_routeName))
          _flRoutes.Where = "1=1";
        else
          _flRoutes.Where = _routesRouteNameFieldName + "='" + _routeName + "'";

        _flRoutes.Update();
      }
    }

    private void RefreshVehiclesFeatureLayer(Graphic routeFeature)
    {
      LookupFieldNames();
      if (_routesVehicleNameFieldName != null)
      {
        string vehicleName = routeFeature.Attributes[_routesVehicleNameFieldName].ToString();
        if (_vehiclesVehicleNameFieldName != null && vehicleName != null)
        {
          _flVehicles.Where = _vehiclesVehicleNameFieldName + "='" + vehicleName + "'";
          _flVehicles.Update();
        }
      }
    }

    private void RefreshWorkersFeatureLayer(Graphic routeFeature)
    {
      LookupFieldNames();
      if (_workersDriverNameFieldName != null)
      {
        string driverName = routeFeature.Attributes[_workersDriverNameFieldName].ToString();
        if (_workersDriverNameFieldName != null && driverName != null)
        {
          _flWorkers.Where = _workersDriverNameFieldName + "='" + driverName + "'";
          _flWorkers.Update();
        }
      }
    }

    private void RefreshMapLayers()
    {
      Graphic routeFeature = QueryRouteFeatureByName(_routeName);
      if (routeFeature == null)
        return;

      RefreshRoutesFeatureLayer();
      RefreshStopsFeatureLayer();
      RefreshVehiclesFeatureLayer(routeFeature);
      RefreshWorkersFeatureLayer(routeFeature);
    }

    private void StopsListBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
      RefreshStopsFeatureLayer();
      BindSelectedStop(true);
    }

    private void BindSelectedStop(bool bRefreshUpdatableAttributes)
    {
      if (_defaultButtonBackgroud == null)
        _defaultButtonBackgroud = AtStopButton.Background;

      if (StopsListBox.SelectedItems.Count == 0)
        return;

      //Stop stop = e.AddedItems[0] as Stop;
      Stop stop = StopsListBox.SelectedItems[0] as Stop;
      if (stop == null)
        return;

      // Stop ID
      string stopId = stop.GetPropertyValueAsString(Stop.IdFieldAlias);
      if (stopId == null)
      {
        Log.Trace("Stop Details - missing stop ID field alias - '" + Stop.IdFieldAlias + "'");
        stopId = "";
      }
      StopNameText.Text = stopId;

      // Stop Address
      string address = stop.GetPropertyValueAsString(Stop.AddressFieldAlias);
      if (address == null)
      {
        Log.Trace("Stop Details - missing address field alias - '" + Stop.AddressFieldAlias + "'");
        address = "";
      }
      StopAddressText.Text = address;

      // Stop Sequence
      string sequence = stop.GetPropertyValueAsString(Stop.SequenceFieldAlias);
      if (sequence == null)
      {
        Log.Trace("Stop Details - missing sequence field alias - '" + Stop.SequenceFieldAlias + "'");
        sequence = "";
      }
      StopSequenceText.Text = "#" + sequence;

      // Stop Type
      string type = stop.Type;
      if (type == null)
        type = "Stop";

      StopTypeText.Text = type;

      // Stop Image
      StopImage.Source = stop.ImageSource;

      // Stop ETA
      StopEtaText.Text = stop.ETA;

      // Stop ETD
      StopEtdText.Text = stop.ETD;

      // Stop Status
      string status = stop.Status;
      if (status == null)
        status = "";

      // AtStop Button
      string key = type + "," + Stop.StopStatusAtStop;
      string[] buttonRemarks = GetStatusRemarks(key);
      if (buttonRemarks == null || buttonRemarks.Length == 0)
        AtStopButton.Visibility = Visibility.Collapsed;
      else
        AtStopButton.Visibility = Visibility.Visible;

      if (status.Equals(Stop.StopStatusAtStop))
        AtStopButton.Background = _highlightedButtonBackgroud;
      else
        AtStopButton.Background = _defaultButtonBackgroud;

      // Completed Button
      key = type + "," + Stop.StopStatusCompleted;
      buttonRemarks = GetStatusRemarks(key);
      if (buttonRemarks == null || buttonRemarks.Length == 0)
        CompletedButton.Visibility = Visibility.Collapsed;
      else
        CompletedButton.Visibility = Visibility.Visible;

      if (status.Equals(Stop.StopStatusCompleted))
        CompletedButton.Background = _highlightedButtonBackgroud;
      else
        CompletedButton.Background = _defaultButtonBackgroud;

      // Exception Button
      key = type + "," + Stop.StopStatusException;
      buttonRemarks = GetStatusRemarks(key);
      if (buttonRemarks == null || buttonRemarks.Length == 0)
        ExceptionButton.Visibility = Visibility.Collapsed;
      else
        ExceptionButton.Visibility = Visibility.Visible;

      if (status.Equals(Stop.StopStatusException))
        ExceptionButton.Background = _highlightedButtonBackgroud;
      else
        ExceptionButton.Background = _defaultButtonBackgroud;

      // Attributes
      stopAttributesDataGrid.ItemsSource = stop.GetAttributes();

      // Updatable Attributes
      if (bRefreshUpdatableAttributes)
        stopUpdatableAttributesDataGrid.ItemsSource = stop.UpdatableAttributes;

      // Show or hide the Updates Stack Panel
      if (stopUpdatableAttributesDataGrid.ItemsSource == null || ((ObservableCollection<ItemAttribute>)stopUpdatableAttributesDataGrid.ItemsSource).Count == 0)
        UpdatesStackPanel.Visibility = Visibility.Collapsed;
      else
        UpdatesStackPanel.Visibility = Visibility.Visible;
    }

    private void BtnDismissWorkOrderDetailPopup_Click(object sender, RoutedEventArgs e)
    {
      //TODO
    }

    private void MessagesListBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
      RefreshMessagesFeatureLayer();
      BindSelectedMessage();
    }

    private void BindSelectedMessage()
    {
      if (MessagesListBox.SelectedItems.Count == 0)
        return;

      Message message = MessagesListBox.SelectedItems[0] as Message;
      if (message == null)
        return;

      // Message Subject
      string subject = message.GetPropertyValueAsString(Message.SubjectFieldAlias);
      if (subject == null)
      {
        Log.Trace("Message Details - missing message subject field alias - '" + Message.SubjectFieldAlias + "'");
        subject = "";
      }
      MessageSubjectText.Text = subject;

      // Message From
      string from = message.GetPropertyValueAsString(Message.FromFieldAlias);
      if (from == null)
      {
        Log.Trace("Message Details - missing message from field alias - '" + Message.FromFieldAlias + "'");
        from = "";
      }
      MessageFromText.Text = from;

      // Message Time
      string time = message.GetPropertyValueAsString(Message.TimeFieldAlias);
      if (time == null)
      {
        Log.Trace("Message Details - missing message time field alias - '" + Message.TimeFieldAlias + "'");
        time = "";
      }
      MessageTimeText.Text = time;

      // Message Body
      string body = message.GetPropertyValueAsString(Message.BodyFieldAlias);
      if (body == null)
      {
        Log.Trace("Message Details - missing message body field alias - '" + Message.BodyFieldAlias + "'");
        body = "";
      }
      MessageBodyText.Text = body;
    }

    private void MessageAckButton_MouseEnter(object sender, MouseEventArgs e)
    {
      //_updateMessagesTimer.Stop();
    }

    private void MessageAckButton_MouseLeave(object sender, MouseEventArgs e)
    {
      //_updateMessagesTimer.Start();
    }

    private void MessageAckButton_Click(object sender, RoutedEventArgs e)
    {
      Message message = _vmMessages.SelectedItem as Message;
      if (message == null)
        return;

      _skipUpdates = true;

      //RefreshMessagesFeatureLayer();

      // find the corresponding feature
      Graphic feature = _flMessages.FindFeatureByOID(message.Graphic);
      if (feature == null)
        feature = message.Graphic;

      if (feature == null)
      {
        _skipUpdates = false;
        return;
      }


      // make sure the Geometry is not null
      if (feature.Geometry == null)
        feature.Geometry = new MapPoint(0, 0);

      // Set the message status
      string messageAckedStatus = ConfigurationManager.AppSettings.Get("MessageAckedStatus");
      SetItemAttribute(message, feature, Message.StatusFieldAlias, messageAckedStatus);

      _flMessagesPending.AddFeature(feature, true);
      _flMessagesPending.SaveEdits();
      //_flMessagesPending.Update();

      _flMessages.SaveEdits();
      _flMessages.Update();

      _skipUpdates = false;
    }

    private void RefreshStops()
    {
      if (_vmStops == null)
      {
        _vmStops = new StopsViewModel(_routeName, _flStops, _flStopsPending);
        StopsListBox.DataContext = _vmStops;
        StopPanel.DataContext = _vmStops;
        stopAttributesDataGrid.DataContext = _vmStops;
      }

      LookupFieldNames();
      if (_stopsRouteNameFieldName == null)
        return;

      string where = "1=1";
      if (!String.IsNullOrWhiteSpace(_routeName))
        where = (_stopsRouteNameFieldName + "='" + _routeName + "'");

      _vmStops.Update(_flStops, where);

      BindSelectedStop(false);
    }

    private void RefreshMessages()
    {
      if (_vmMessages == null)
      {
        _vmMessages = new MessagesViewModel(_flMessages);
        MessagesListBox.DataContext = _vmMessages;
        MainTabControl.DataContext = _vmMessages;
      }

      LookupFieldNames();
      if (_messagesRouteNameFieldName == null)
        return;

      string where = "1=1";
      if (!String.IsNullOrWhiteSpace(_routeName))
        where = (_messagesRouteNameFieldName + "='" + _routeName + "'");

      _vmMessages.Update(_flMessages, where);

      // update the tab's image
      MessagesTabImage.Source = _vmMessages.InternalResourceTabImageSource;

      BindSelectedMessage();
    }

    private void AtStopButton_Click(object sender, RoutedEventArgs e)
    {
      SetStopStatus("AtStop");
    }

    private void CompletedButton_Click(object sender, RoutedEventArgs e)
    {
      SetStopStatus("Completed");
    }

    private void ExceptionButton_Click(object sender, RoutedEventArgs e)
    {
      SetStopStatus("Exception");
    }

    private void StopUpdateButton_Click(object sender, RoutedEventArgs e)
    {
      Stop stop = _vmStops.SelectedItem as Stop;
      if (stop == null)
        return;

      // Update the field with the value
      _skipUpdates = true;

      //RefreshStopsFeatureLayer();

      // find the corresponding feature
      Graphic feature = _flStops.FindFeatureByOID(stop.Graphic);
      if (feature == null)
      {
        _skipUpdates = false;
        return;
      }

      // Set the status
      foreach (ItemAttribute itemAttribute in stop.UpdatableAttributes)
      {
        string alias = itemAttribute.Name;
        if (alias == null || !stop.ContainsPropertyAlias(alias))
          continue;

        SetItemAttribute(stop, feature, alias, itemAttribute.Value);

        // clear the value after the update operation
        itemAttribute.Value = "";
      }

      _flStopsPending.AddFeature(feature, true);
      _flStopsPending.SaveEdits();
      //_flStopsPending.Update();

      _flStops.SaveEdits();
      _flStops.Update();

      _skipUpdates = false;
    }

    private void SetStopStatus(string newStatus)
    {
      Stop stop = _vmStops.SelectedItem as Stop;
      if (stop == null)
        return;

      // Stop Type
      string type = stop.Type;
      if (type == null)
        type = "Stop";

      // Stop Status
      string status = stop.Status;
      if (status == null)
        status = "";

      // Lookup Remark Options
      string key = type + "," + newStatus;
      string[] statusRemarks = GetStatusRemarks(key);
      if (statusRemarks == null || statusRemarks.Length < 1)
        return;

      // If only one empty Remark Option
      if (statusRemarks.Length == 1 && String.IsNullOrEmpty(statusRemarks[0]))
      {
        // set status and remark
        SetStopStatus(stop, newStatus, null);
        //RefreshStops();
        return;
      }

      // Reset the popup window remarks
      SetStopStatusStackPanel.Children.Clear();
      foreach (string remark in statusRemarks)
      {
        RadioButton rb = new RadioButton() { Content = remark, GroupName = "StatusGroup", FontSize = 20 };
        SetStopStatusStackPanel.Children.Add(rb);
      }

      // Show the popup window
      SetStopStatusText.Text = newStatus;
      SetStopStatusPopup.IsOpen = true;
    }

    private void UpdateStopStatusButton_Click(object sender, RoutedEventArgs e)
    {
      Stop stop = _vmStops.SelectedItem as Stop;
      if (stop == null)
        return;

      RadioButton rb = null;
      foreach (var child in SetStopStatusStackPanel.Children)
      {
        RadioButton rbChild = child as RadioButton;
        if (rbChild == null)
          continue;

        if (rbChild.IsChecked != null && rbChild.IsChecked.Value)
        {
          rb = rbChild;
          break;
        }
      }
      if (rb == null)
      {
        SetStopStatusPopup.IsOpen = false;
        return;
      }

      string newRemark = rb.Content.ToString();

      // set status and remark
      SetStopStatus(stop, SetStopStatusText.Text, newRemark);

      // Hide the popup window
      SetStopStatusPopup.IsOpen = false;

      //RefreshStops();
    }

    private void CancelStopStatusButton_Click(object sender, RoutedEventArgs e)
    {
      SetStopStatusPopup.IsOpen = false;
    }

    private void SetStopStatus(Stop stop, string status, string remark)
    {
      if (stop == null || status == null)
        return;

      _skipUpdates = true;

      //RefreshStopsFeatureLayer();

      // find the corresponding feature
      Graphic feature = _flStops.FindFeatureByOID(stop.Graphic);
      if (feature == null)
      {
        _skipUpdates = false;
        return;
      }

      // Set the status
      SetItemAttribute(stop, feature, Stop.StatusFieldAlias, status);

      // Set the remark
      if (remark != null)
        SetItemAttribute(stop, feature, Stop.RemarkFieldAlias, remark);

      // Stop Last Updated
      DateTime now = DateTime.Now;
      SetItemAttribute(stop, feature, Stop.LastUpdatedFieldAlias, now);

      // Stop Actual Arrival
      if (status.Equals("AtStop"))
        SetItemAttribute(stop, feature, Stop.ActualArrivalFieldAlias, DateTime.Now);

      if (status.Equals("Completed") || status.Equals("Exception"))
      {
        // Stop Actual Departure
        string stopsActualDepartureAlias = ConfigurationManager.AppSettings.Get("StopsActualDepartureAlias");
        SetItemAttribute(stop, feature, stopsActualDepartureAlias, now);

        // Stop Actual Duration
        string stopsActualArrivalAlias = ConfigurationManager.AppSettings.Get("StopsActualArrivalAlias");
        object actualArrivalObj = stop.GetPropertyValue(stopsActualArrivalAlias);
        if (actualArrivalObj is DateTime)
        {
          DateTime actualArrival = (DateTime)actualArrivalObj;
          TimeSpan duration = now - actualArrival;
          string stopsActualDurationAlias = ConfigurationManager.AppSettings.Get("StopsActualDurationAlias");
          SetItemAttribute(stop, feature, stopsActualDurationAlias, duration.TotalMinutes);
        }
      }


      _flStopsPending.AddFeature(feature, true);
      _flStopsPending.SaveEdits();
      //_flStopsPending.Update();

      _flStops.SaveEdits();
      _flStops.Update();

      _skipUpdates = false;
    }

    private bool SetItemAttribute(Item item, Graphic feature, string fieldAlias, object value)
    {
      string fieldName = item.AliasToName(fieldAlias);
      item.SetPropertyValue(fieldAlias, value);
      if (!feature.Attributes.ContainsKey(fieldName))
        return false;

      object convertedValue = item.ConvertValueByPropertyType(fieldAlias, value);
      feature.Attributes[fieldName] = convertedValue;
      return true;
    }

    private void MainTabControl_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
      if (!(e.Source is TabControl))
        return;

      TabControl tabControl = e.Source as TabControl;
      TabItem tabItem = tabControl.SelectedItem as TabItem;
      if (tabItem == null)
        return;

      switch (tabItem.Name)
      {
        case "StopsTabItem":
          UpdateStopsTimer_Tick(null, null);
          break;
        case "MessagesTabItem":
          UpdateMessagesTimer_Tick(null, null);
          break;
        case "MapTabItem":
          UpdateMapTimer_Tick(null, null);
          break;
      }
    }

    void UpdateMapTimer_Tick(object sender, EventArgs e)
    {
      if (_skipUpdates)
        return;

      if (_updateMapTimer == null)
        return;

      _updateMapTimer.Stop();
      RefreshMapLayers();
      _updateMapTimer.Start();
    }

    void UpdateStopsTimer_Tick(object sender, EventArgs e)
    {
      if (_skipUpdates)
        return;

      if (_updateStopsTimer == null)
        return;

      _updateStopsTimer.Stop();
      RefreshStops();
      _updateStopsTimer.Start();
    }
    
    void UpdateMessagesTimer_Tick(object sender, EventArgs e)
    {
      if (_skipUpdates)
        return;

      if (_updateMessagesTimer == null)
        return;

      _updateMessagesTimer.Stop();
      RefreshMessages();
      _updateMessagesTimer.Start();
    }

    private void RefreshAll()
    {
      UpdateMapTimer_Tick(null, null);
      UpdateStopsTimer_Tick(null, null);
      UpdateMessagesTimer_Tick(null, null);
    }

    private void stopAttributesDataGrid_AutoGeneratingColumn(object sender, DataGridAutoGeneratingColumnEventArgs e)
    {
      e.Column.CanUserSort = true;
    }

    private void stopUpdatableAttributesDataGrid_AutoGeneratingColumn(object sender, DataGridAutoGeneratingColumnEventArgs e)
    {
      e.Column.CanUserSort = true;

      // don't allow to edit the first column
      DataGrid gd = sender as DataGrid;
      //if (e.PropertyName.Equals("Name"))
      if (gd.Columns.Count == 0)
        e.Column.IsReadOnly = true;
      else
        e.Column.IsReadOnly = false;
    }

    private void stopUpdatableAttributesDataGrid_BeginningEdit(object sender, DataGridBeginningEditEventArgs e)
    {
      // don't allow to edit the first column
      //if (e.Column.DisplayIndex == 0)
      //  e.Cancel = true;
    }

    private void stopUpdatableAttributesDataGrid_ExecutedRoutedEventHandler(object sender, ExecutedRoutedEventArgs e)
    {
      var cells = stopUpdatableAttributesDataGrid.SelectedCells;
      if (cells.Count != 1)
        return;

      ItemAttribute item = cells[0].Item as ItemAttribute;
      if (item == null)
        return;

      item.Value = Clipboard.GetText().Trim();
    }

    private void stopUpdatableAttributesDataGrid_CanExecuteRoutedEventHandler(object sender, CanExecuteRoutedEventArgs e)
    {
      var cells = stopUpdatableAttributesDataGrid.SelectedCells;

      // supporting paste operation into only one selected cell
      if (cells.Count != 1)
      {
        e.CanExecute = false;
        return;
      }

      // don't allow to edit the first column
      DataGridCellInfo cellInfo = cells[0];
      if (cellInfo == null || cellInfo.Column.DisplayIndex < 1)
      {
        e.CanExecute = false;
        return;
      }

      ItemAttribute item = cellInfo.Item as ItemAttribute;
      if (item == null)
      {
        e.CanExecute = false;
        return;
      }

      e.CanExecute = Clipboard.ContainsText();
    }

    private void AddStopMenuItem_Click(object sender, RoutedEventArgs e)
    {
      EditStopTitle.Text        = "Add Break";
      EditStopDurationText.Text = "15";
      EditStopPopup.IsOpen      = true;
    }

    private void EditStopMenuItem_Click(object sender, RoutedEventArgs e)
    {
      Stop stop = StopsListBox.SelectedItems[0] as Stop;
      if (stop == null)
        return;

      EditStopTitle.Text        = "Edit Break";
      EditStopDurationText.Text = stop.ScheduledDuration.ToString();
      EditStopPopup.IsOpen      = true;
    }

    private void EditStopUpButton_Click(object sender, RoutedEventArgs e)
    {
      int durationInMinutes = Helper.TextToInt(EditStopDurationText.Text, 15);
      durationInMinutes += 5;
      if (durationInMinutes >= 60)
        durationInMinutes = 0;

      EditStopDurationText.Text = durationInMinutes.ToString();
      EditStopDurationTextBlock.Text = EditStopDurationText.Text + " Minutes";
    }

    private void EditStopDownButton_Click(object sender, RoutedEventArgs e)
    {
      int durationInMinutes = Helper.TextToInt(EditStopDurationText.Text, 15);
      durationInMinutes -= 5;
      if (durationInMinutes <= 0)
        durationInMinutes = 60;

      EditStopDurationText.Text = durationInMinutes.ToString();
      EditStopDurationTextBlock.Text = EditStopDurationText.Text + " Minutes";
    }

    private void EditStopOkButton_Click(object sender, RoutedEventArgs e)
    {
      EditStopPopup.IsOpen = false;
      int durationInMinutes = int.Parse(EditStopDurationText.Text);
      if (EditStopTitle.Text == "Add Break")
        _vmStops.AddStop(durationInMinutes);
      else
        _vmStops.EditStop(durationInMinutes);
    }

    private void EditStopCancelButton_Click(object sender, RoutedEventArgs e)
    {
      EditStopPopup.IsOpen = false;
    }

    private void MoveStopListBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
      // TBD - place holder for future needs ...
    }

    private void MoveStopOkButton_Click(object sender, RoutedEventArgs e)
    {
      // TBD - place holder for future needs ...
    }

  }
}
