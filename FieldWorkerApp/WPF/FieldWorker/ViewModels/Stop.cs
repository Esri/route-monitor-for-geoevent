using FieldWorker.Common;
using FieldWorker.Schema;
using System;
using System.Collections.ObjectModel;
using System.Configuration;
using System.Windows.Media.Imaging;

namespace FieldWorker.ViewModels
{
  public class Stop : Item
  {
    public static string IdFieldAlias = ConfigurationManager.AppSettings.Get("StopsIdFieldAlias");
    public static string SequenceFieldAlias = ConfigurationManager.AppSettings.Get("StopsSequenceFieldAlias");
    public static string AddressFieldAlias = ConfigurationManager.AppSettings.Get("StopsAddressFieldAlias");
    public static string TypeFieldAlias = ConfigurationManager.AppSettings.Get("StopsTypeFieldAlias");
    public static string StatusFieldAlias = ConfigurationManager.AppSettings.Get("StopsStatusFieldAlias");
    public static string EtaFieldAlias = ConfigurationManager.AppSettings.Get("StopsEtaFieldAlias");
    public static string EtdFieldAlias = ConfigurationManager.AppSettings.Get("StopsEtdFieldAlias");
    public static string ScheduledArrivalAlias = ConfigurationManager.AppSettings.Get("StopsScheduledArrivalAlias");
    public static string RouteNameFieldAlias = ConfigurationManager.AppSettings.Get("StopsRouteNameFieldAlias");
    public static string RemarkFieldAlias = ConfigurationManager.AppSettings.Get("StopsRemarkFieldAlias");
    public static string LastUpdatedFieldAlias = ConfigurationManager.AppSettings.Get("StopsLastUpdatedAlias");
    public static string ActualArrivalFieldAlias = ConfigurationManager.AppSettings.Get("StopsActualArrivalAlias");
    public static string ScheduledDurationFieldAlias = ConfigurationManager.AppSettings.Get("ScheduledDurationFieldAlias");

    public static string StopStatusAtStop = ConfigurationManager.AppSettings.Get("StopStatusAtStop");
    public static string StopStatusCompleted = ConfigurationManager.AppSettings.Get("StopStatusCompleted");
    public static string StopStatusException = ConfigurationManager.AppSettings.Get("StopStatusException");


    string                              _updatableAttributesForType = null;
    ObservableCollection<ItemAttribute> _updatableAttributes        = null;


    public Stop(StopsViewModel vmItems) :
      base(vmItems)
    {
      //
    }

    public string Type
    {
      get
      {
        string type = this.GetPropertyValueAsString(Stop.TypeFieldAlias);
        if (type == null)
        {
          Log.Trace("Missing stop type field alias - '" + Stop.TypeFieldAlias + "'");
          return null;
        }

        return type;
      }
    }

    public string Status
    {
      get
      {
        string status = this.GetPropertyValueAsString(Stop.StatusFieldAlias);
        if (status == null)
          Log.Trace("Missing stop status field alias - '" + Stop.StatusFieldAlias + "'");

        return status;
      }
    }

    public BitmapSource ImageSource
    {
      get
      {
        StopsViewModel vm = _vmItems as StopsViewModel;
        string type = this.Type;
        string relativePath = vm.StopRelativeImagePathLookup.Get(type);
        if (relativePath == null)
          relativePath = vm.StopRelativeImagePathLookup.Get("Default");
        if (relativePath == null)
          return null;

        return ImagesCache.Instance.Get(relativePath, false);
      }
    }

    public BitmapSource InternalResourceImageSource
    {
      get
      {
        StopsViewModel vm = _vmItems as StopsViewModel;
        string type = this.Type;
        string relativePath = vm.StopRelativeImagePathLookup.Get(type);
        if (relativePath == null)
          relativePath = vm.StopRelativeImagePathLookup.Get("Default");
        if (relativePath == null)
          return null;

        return ImagesCache.Instance.Get(relativePath, true);
      }
    }

    public string Line1
    {
      get
      {
        string sequence  = GetPropertyValueAsString(Stop.SequenceFieldAlias, "?");
        string id        = GetPropertyValueAsString(Stop.IdFieldAlias);
        string scheduled = GetPropertyValueAsString(Stop.ScheduledArrivalAlias);
        string eta       = GetPropertyValueAsString(Stop.EtaFieldAlias);

        string line = "#" + sequence;

        if (!String.IsNullOrEmpty(id))
          line += "    " + id;

        if (!String.IsNullOrEmpty(scheduled))
          line += "    Scheduled: " + scheduled;

        if (!String.IsNullOrEmpty(eta))
          line += "    ETA: " + eta;

        return line;
      }
      //set { _line1 = value; RaisePropertyChanged(); }
    }

    public string Line2
    {
      get
      {
        string line = "";
        string address = GetPropertyValueAsString(Stop.AddressFieldAlias);
        line = address;
        return line;
      }
      //set { _line2 = value; RaisePropertyChanged(); }
    }

    public string ETA
    {
      get
      {
        string eta = "";
        object etaObj = GetPropertyValue(Stop.EtaFieldAlias);
        if (etaObj == null)
          Log.Trace("Stop - missing ETA field alias - '" + Stop.EtaFieldAlias + "'");
        if (etaObj != null && etaObj is DateTime)
          eta = ((DateTime)etaObj).ToString("hh:mm tt") + " Arrival";

        return eta;
      }
    }

    public string ETD
    {
      get
      {
        string etd = "";
        object etdObj = GetPropertyValue(Stop.EtdFieldAlias);
        if (etdObj == null)
          Log.Trace("Stop - missing ETD field alias - '" + Stop.EtdFieldAlias + "'");
        if (etdObj != null && etdObj is DateTime)
          etd = ((DateTime)etdObj).ToString("hh:mm tt") + " Departure";

        return etd;
      }
    }

    public int ScheduledDuration
    {
      get
      {
        string duration = this.GetPropertyValueAsString(Stop.ScheduledDurationFieldAlias);
        if (duration == null)
        {
          Log.Trace("Missing stop scheduled duration field alias - '" + Stop.ScheduledDurationFieldAlias + "'");
          return 0;
        }

        return Helper.TextToInt(duration, 0);
      }
    }

    public ObservableCollection<ItemAttribute> UpdatableAttributes
    {
      get
      {
        string type = this.Type;
        if (type == _updatableAttributesForType)
          return _updatableAttributes;

        _updatableAttributesForType = type;
        _updatableAttributes = new ObservableCollection<ItemAttribute>();

        string inputFieldAliases = (_vmItems as StopsViewModel).StatusInputLookup.Get(type);
        if (inputFieldAliases == null)
          return _updatableAttributes;

        string[] aliasesArray = inputFieldAliases.Split(',');
        foreach (string alias in aliasesArray)
          _updatableAttributes.Add(new ItemAttribute(alias, ""));

        return _updatableAttributes;
      }
    }


    static public string BreakType
    {
      get
      {
        return ConfigurationManager.AppSettings.Get("StopTypeBreak");
      }
    }

    public bool IsBreak
    {
      get
      {
        string myType = this.Type;
        if (myType == null)
          return false;

        string breakType = Stop.BreakType;

        return myType.Equals(breakType);
      }
    }

  }
}


/*
Fields:
OBJECTID ( type: esriFieldTypeOID , alias: OBJECTID , editable: false , nullable: false )
sequence_number ( type: esriFieldTypeSmallInteger , alias: Sequence , editable: true , nullable: true )
stop_name ( type: esriFieldTypeString , alias: Stop , editable: true , nullable: true , length: 510 )
type ( type: esriFieldTypeString , alias: Type , editable: true , nullable: true , length: 50 )
status ( type: esriFieldTypeString , alias: Status , editable: true , nullable: true , length: 50 )
last_updated ( type: esriFieldTypeDate , alias: Last Updated , editable: true , nullable: true , length: 36 )
scheduled_arrival ( type: esriFieldTypeDate , alias: Scheduled Arrival , editable: true , nullable: true , length: 36 )
projected_arrival ( type: esriFieldTypeDate , alias: Projected Arrival , editable: true , nullable: true , length: 36 )
actual_arrival ( type: esriFieldTypeDate , alias: Actual Arrival , editable: true , nullable: true , length: 36 )
scheduled_service_duration ( type: esriFieldTypeSmallInteger , alias: Scheduled Duration , editable: true , nullable: true )
actual_service_duration ( type: esriFieldTypeSmallInteger , alias: Actual Duration , editable: true , nullable: true )
scheduled_departure ( type: esriFieldTypeDate , alias: Scheduled Departure , editable: true , nullable: true , length: 36 )
projected_departure ( type: esriFieldTypeDate , alias: Projected Departure , editable: true , nullable: true , length: 36 )
actual_departure ( type: esriFieldTypeDate , alias: Actual Departure , editable: true , nullable: true , length: 36 )
pickup_capacity ( type: esriFieldTypeString , alias: Pickup Capacity , editable: true , nullable: true , length: 255 )
delivery_capacity ( type: esriFieldTypeString , alias: Delivery Capacity , editable: true , nullable: true , length: 255 )
time_window_start1 ( type: esriFieldTypeDate , alias: Time Window Start 1 , editable: true , nullable: true , length: 36 )
time_window_end1 ( type: esriFieldTypeDate , alias: Time Window End 1 , editable: true , nullable: true , length: 36 )
time_window_start2 ( type: esriFieldTypeDate , alias: Time Window Start 2 , editable: true , nullable: true , length: 36 )
time_window_end2 ( type: esriFieldTypeDate , alias: Time Window End 2 , editable: true , nullable: true , length: 36 )
max_violation_time ( type: esriFieldTypeInteger , alias: Max Violation , editable: true , nullable: true )
curb_approach ( type: esriFieldTypeString , alias: Curb Approach , editable: true , nullable: true , length: 50 )
address ( type: esriFieldTypeString , alias: Address , editable: true , nullable: true , length: 510 )
custom_stop_properties ( type: esriFieldTypeString , alias: Custom Properties , editable: true , nullable: true , length: 510 )
description ( type: esriFieldTypeString , alias: Description , editable: true , nullable: true , length: 510 )
note ( type: esriFieldTypeString , alias: Note , editable: true , nullable: true , length: 510 )
route_name ( type: esriFieldTypeString , alias: Route Name , editable: true , nullable: true , length: 510 )
exception_reason ( type: esriFieldTypeString , alias: Exception Reason , editable: true , nullable: true , length: 256 )
meter_read ( type: esriFieldTypeInteger , alias: Meter Read , editable: true , nullable: true )
picture_location ( type: esriFieldTypeString , alias: Picture Location , editable: true , nullable: true , length: 256 )
*/