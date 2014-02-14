using FieldWorker.Common;
using FieldWorker.Schema;
using System;
using System.Configuration;
using System.Windows.Media.Imaging;

namespace FieldWorker.ViewModels
{
  class Message : Item
  {
    public static string IdFieldAlias = ConfigurationManager.AppSettings.Get("messagesIdFieldAlias");
    public static string StatusFieldAlias = ConfigurationManager.AppSettings.Get("MessagesStatusFieldAlias");
    public static string SubjectFieldAlias = ConfigurationManager.AppSettings.Get("MessagesSubjectFieldAlias");
    public static string FromFieldAlias = ConfigurationManager.AppSettings.Get("MessagesFromFieldAlias");
    public static string TimeFieldAlias = ConfigurationManager.AppSettings.Get("MessagesTimeFieldAlias");
    public static string BodyFieldAlias = ConfigurationManager.AppSettings.Get("MessagesBodyFieldAlias");


    public Message(MessagesViewModel vmItems) :
      base(vmItems)
    {
      //
    }

    public string Status
    {
      get
      {
        string status = this.GetPropertyValueAsString(Message.StatusFieldAlias);
        if (status == null)
          Log.Trace("Missing message status field alias - '" + Message.StatusFieldAlias + "'");

        return status;
      }
    }

    public BitmapSource ImageSource
    {
      get
      {
        return ImagesCache.Instance.Get(this.StatusToRelativeImagePath(), false);
      }
    }

    public BitmapSource InternalResourceImageSource
    {
      get
      {
        return ImagesCache.Instance.Get(this.StatusToRelativeImagePath(), true);
      }
    }

    public string Line1
    {
      get
      {
        string subject = GetPropertyValueAsString(Message.SubjectFieldAlias, "?");

        string line = subject;
        return line;
      }
      //set { _line1 = value; RaisePropertyChanged(); }
    }

    public string Line2
    {
      get
      {
        string from   = GetPropertyValueAsString(Message.FromFieldAlias, "?");
        string status = this.Status;
        string time   = GetPropertyValueAsString(Message.TimeFieldAlias);

        string line = "";

        if (!String.IsNullOrEmpty(from))
          line = "From: " + from;

        if (!String.IsNullOrEmpty(status))
          line += "    Status: " + status;

        if (!String.IsNullOrEmpty(time))
          line += "       " + time;

        return line;
      }
      //set { _line2 = value; RaisePropertyChanged(); }
    }

    public string StatusToRelativeImagePath()
    {
      if (IsNewMessage())
        return "/Images/MessageUnread.png";

      return "/Images/MessageReplied.png";
    }

    public bool IsNewMessage()
    {
      String status = GetPropertyValueAsString(Message.StatusFieldAlias);

      string messageAckedStatus = ConfigurationManager.AppSettings.Get("MessageAckedStatus");

      return !messageAckedStatus.Equals(status);
    }

  }

}


/*
Fields:
OBJECTID ( type: esriFieldTypeOID , alias: OBJECTID , editable: false , nullable: false )
id ( type: esriFieldTypeString , alias: ID , editable: true , nullable: true , length: 50 )
type ( type: esriFieldTypeString , alias: Type , editable: true , nullable: true , length: 50 )
subject ( type: esriFieldTypeString , alias: Subject , editable: true , nullable: true , length: 256 )
message_time ( type: esriFieldTypeDate , alias: MessageTime , editable: true , nullable: true , length: 36 )
message_from ( type: esriFieldTypeString , alias: MessageFrom , editable: true , nullable: true , length: 512 )
message_to ( type: esriFieldTypeString , alias: MessageTo , editable: true , nullable: true , length: 512 )
status ( type: esriFieldTypeString , alias: Status , editable: true , nullable: true , length: 50 )
body ( type: esriFieldTypeString , alias: Body , editable: true , nullable: true , length: 1024 )
callback ( type: esriFieldTypeString , alias: Callback , editable: true , nullable: true , length: 512 )
*/