using System;

namespace AddInsShare.Common
{
  public class Log
  {
    public static void Trace(String message)
    {
      //System.Diagnostics.Debug.WriteLine("RouteMonitor - " + message);
      System.Diagnostics.Debug.Print("RouteMonitor - " + message);
    }

    public static void TraceException(String caption, Exception ex)
    {
      Trace(caption + " exception - " + ex.Message);
    }
  }
}
