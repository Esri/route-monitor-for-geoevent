using System;
using AddInsShare.Common;
using AddInsShare.Schema;
using AddInsShare.ViewModels;
using OD = ESRI.ArcGIS.OperationsDashboard;

namespace Alerts.ViewModels
{
  class AlertViewModel : BaseDataGridViewModel
  {
    public DelegatingCommand DismissCommand { get; protected set; }
    public DelegatingCommand AssignToCommand { get; protected set; }
    public DelegatingCommand NoteCommand { get; protected set; }


    public AlertViewModel(OD.DataSource dataSource)
      : base(dataSource)
    {
      // init the data members with default values
      Properties["GepHostName"]                   = @"localhost";
      Properties["GepHttpPort"]                   = @"6180";
      Properties["GepHttpsPort"]                  = @"6143";
      Properties["GEP_INCIDENTS_ENDPOINT"]        = @"geoevent/admin/incidents";
      Properties["ALERT_ASSIGN_TO_FIELD_ALIAS"]   = @"Assigned To";
      Properties["ALERT_ASSIGN_TO_URI_PROP"]      = @"AssignedTo";
      Properties["ALERT_NOTE_FIELD_ALIAS"]        = @"Note";
      Properties["ALERT_NOTE_URI_PROP"]           = @"Note";
      Properties["ALERT_DISMISS_STATUS_URI_PROP"] = @"Dismissed";
      Properties["ALERT_DISMISSED_VALUE"]         = @"true";

      // init the commands
      DismissCommand   = new DelegatingCommand(OnDismiss);
      AssignToCommand  = new DelegatingCommand(OnAssignTo);
      NoteCommand      = new DelegatingCommand(OnNote);
    }

    private void OnDismiss(object obj)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;

      String uri = GetGEPUrl(true) + GetPropValue("GEP_INCIDENTS_ENDPOINT") + "/" +
                   item.GetPropertyValue(TrackIdFieldName) as String +
                   "/properties.json?" +
                   GetPropValue("ALERT_DISMISS_STATUS_URI_PROP") +
                   "=" +
                   GetPropValue("ALERT_DISMISSED_VALUE");

      HttpRequest request = new HttpRequest(uri, "POST", "application/json", 1000);
      request.SetCredentials("arcgis", "manager");
      HttpResponse response = request.ExecuteHttpRequest();
      response.ReportOnGeoEventProcessorError("Alert dismiss error");
    }

    private void OnAssignTo(object assignToName)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;

      String uri = GetGEPUrl(true) + GetPropValue("GEP_INCIDENTS_ENDPOINT") + "/" +
                   item.GetPropertyValue(TrackIdFieldName) as String +
                   "/properties.json?" +
                   GetPropValue("ALERT_ASSIGN_TO_URI_PROP") +
                   "=" +
                   assignToName.ToString();

      HttpRequest request = new HttpRequest(uri, "POST", "application/json", 1000);
      request.SetCredentials("arcgis", "manager");
      HttpResponse response = request.ExecuteHttpRequest();
      response.ReportOnGeoEventProcessorError("Alert Assign-To error");
    }

    private void OnNote(object note)
    {
      Item item = GetSelectedItem();
      if (item == null)
        return;

      String uri = GetGEPUrl(true) + GetPropValue("GEP_INCIDENTS_ENDPOINT") + "/" +
                   item.GetPropertyValue(TrackIdFieldName) as String +
                   "/properties.json?" +
                   GetPropValue("ALERT_NOTE_URI_PROP") +
                   "=" +
                   note.ToString();

      HttpRequest request = new HttpRequest(uri, "POST", "application/json", 1000);
      request.SetCredentials("arcgis", "manager");
      HttpResponse response = request.ExecuteHttpRequest();
      response.ReportOnGeoEventProcessorError("Alert note error");
    }

  }
}
