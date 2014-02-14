using System;

namespace FieldWorker.Common
{
  public class Log
  {
    public static void Trace(String message)
    {
      //System.Diagnostics.Debug.WriteLine("FieldWorker - " + message);
      System.Diagnostics.Debug.Print("FieldWorker - " + message);
    }

    public static void TraceException(String caption, Exception ex)
    {
      Trace(caption + " exception - " + ex.Message);
    }
  }
}
