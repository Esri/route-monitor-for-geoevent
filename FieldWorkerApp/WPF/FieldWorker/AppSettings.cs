using System.Configuration;
using System.Windows;

namespace FieldWorker
{
  public class AppSettings : DependencyObject
  {
    public static string GetAppFullPath()
    {
      return System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().Location);

      //System.AppDomain.CurrentDomain.BaseDirectory
      //return System.IO.Path.GetDirectoryName(System.Diagnostics.Process.GetCurrentProcess().MainModule.FileName);
    }

    static public string GetGEPBaseUrl()
    {
      return ConfigurationManager.AppSettings.Get("GEPBaseUrl");
    }

    static public string GetCalculateRoutesUrl()
    {
      string url = GetGEPBaseUrl();
      url += "/";
      url += ConfigurationManager.AppSettings.Get("GEPCalculateRoutesEndpoint");
      return url;
    }

    static public string GetUpdateStopUrl()
    {
      string url = GetGEPBaseUrl();
      url += "/";
      url += ConfigurationManager.AppSettings.Get("GEPUpdateStopEndpoint");
      return url;
    }

  }
}
