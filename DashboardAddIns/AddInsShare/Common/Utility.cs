using System;
using System.Collections.Generic;
using System.Security;
using System.Security.Cryptography;
using System.Windows.Threading;
using ESRI.ArcGIS.Client.Bing;
using ESRI.ArcGIS.Client.Geometry;

namespace AddInsShare.Common
{
  /// <summary>
  /// Useful utility methods.
  /// </summary>
  public static class Utility
  {
    public static DateTime Epoch = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

    /// <summary>
    /// Conversion factor - meters to miles.
    /// </summary>
    public const double MetersToMiles = 0.000621371192237334;

    /// <summary>
    /// Conversion factor - meters to feet.
    /// </summary>
    public const double MetersToFeet = 3.28083989501312;

    /// <summary>
    /// Conversion factor - feet to meters.
    /// </summary>
    public const double FeetToMeters = 0.3048;

    /// <summary>
    /// Conversion factor - meters to centimeters.
    /// </summary>
    public const double MetersToCentimeters = 100;

    /// <summary>
    /// Conversion factor - meters to kilometers.
    /// </summary>
    public const double MetersToKilometers = 0.001;

    /// <summary>
    /// Conversion factor - meters to inches.
    /// </summary>
    public const double MetersToInches = 39.3700787;

    /// <summary>
    /// Conversion factor - meters to yards.
    /// </summary>
    public const double MetersToYards = 1.0936133;

    /// <summary>
    /// Conversion factor - meters to Nautical Miles.
    /// </summary>
    public const double MetersToNauticalMiles = 0.000539;

    /// <summary>
    /// Conversion factor - square meters to square miles.
    /// </summary>
    public const double SquareMetersToSquareMiles = 3.861021e-07;

    /// <summary>
    /// Conversion factor - square meters to square kilometers.
    /// </summary>
    public const double SquareMetersToSquareKilometers = 1.0e-06;

    /// <summary>
    /// Conversion factor - square meters to hectares.
    /// </summary>
    public const double SquareMetersToHectares = 0.0001;

    /// <summary>
    /// Conversion factor - square meters to acres.
    /// </summary>
    public const double SquareMetersToAcres = 0.000247;

    /// <summary>
    /// Conversion factor - square meters to square feet.
    /// </summary>
    public const double SquareMetersToSquareFeet = 10.7639104;


    /// <summary>
    /// Calculates the length of the polyline.
    /// </summary>
    public static double CalculateLengthMeters(Polyline polyline, MapUnits mapUnits)
    {
      double length = 0;

      foreach (ESRI.ArcGIS.Client.Geometry.PointCollection points in polyline.Paths)
        for (int i = 1; i < points.Count; i++)
        {
          double value = CalculateLengthMeters(points[i - 1], points[i], mapUnits);
          if (value >= 0)
            length += value;
          else
            return value; //failed to calculate length, return
        }

      return length;
    }

    /// <summary>
    /// Calculates the length in meters between two WebMercator points using spherical distance.
    /// </summary>
    /// <param name="p1"></param>
    /// <param name="p2"></param>
    /// <returns></returns>
    public static double CalculateLengthMeters(MapPoint p1, MapPoint p2, MapUnits mapUnits)
    {
      switch (mapUnits)
      {
        case MapUnits.WebMercatorMeters:
          try
          {
            MapPoint g1 = p1.WebMercatorToGeographic();
            MapPoint g2 = p2.WebMercatorToGeographic();
            return GetSphericalDistanceMeters(g1, g2);
          }
          catch (ArgumentException)
          {
            //if the point is outside web mercator coordinates return NAN
            return double.NaN;
          }

        case MapUnits.DecimalDegrees:
          return GetSphericalDistanceMeters(p1, p2);

        case MapUnits.Feet:
          return Math.Sqrt(Math.Pow(p1.X - p2.X, 2) + Math.Pow(p1.Y - p2.Y, 2)) * FeetToMeters;

        default:
          return Math.Sqrt(Math.Pow(p1.X - p2.X, 2) + Math.Pow(p1.Y - p2.Y, 2));
      }
    }

    /// <summary>
    /// Calculates the spherical distance between two geographic points.
    /// </summary>
    public static double GetSphericalDistanceMeters(MapPoint start, MapPoint end)
    {
      const double EarthRadius = 6378137; // meters

      double lon1 = start.X / 180 * Math.PI;
      double lon2 = end.X / 180 * Math.PI;
      double lat1 = start.Y / 180 * Math.PI;
      double lat2 = end.Y / 180 * Math.PI;
      return 2 * Math.Asin(Math.Sqrt(Math.Pow((Math.Sin((lat1 - lat2) / 2)), 2) +
       Math.Cos(lat1) * Math.Cos(lat2) * Math.Pow(Math.Sin((lon1 - lon2) / 2), 2))) * EarthRadius;
    }

    /// <summary>
    /// Calls the callback after the specified delay (ms). Uses a timer.
    /// </summary>
    /// <param name="delay">delay (ms)</param>
    /// <param name="callback"></param>
    public static void DelayedCallback(int delay, Action callback)
    {
      DispatcherTimer timer = new DispatcherTimer();
      timer.Interval = new TimeSpan(0, 0, 0, 0, delay); //ms
      EventHandler tickHandler = null;
      tickHandler = (object sender, EventArgs e) =>
      {
        timer.Stop();
        timer.Tick -= tickHandler;

        //invoke the callback
        callback();
      };
      timer.Tick += tickHandler;
      timer.Start();
    }

    private static byte[] s_aditionalEntropy = { 213, 200, 17, 26, 35, 242, 23, 18, 1, 010, 011, 213, 55, 94, 213, 200, 17, 26, 35, 242, 23, 18, 1, 010, 011, 213, 55, 94, 1, 010, 011, 213, 55, 94, 213, 200, 17, 26, 35, 242 };

    internal static string EncryptString(SecureString input)
    {
      byte[] encryptedData = ProtectedData.Protect(System.Text.Encoding.Unicode.GetBytes(ToInsecureString(input)),
                                                   s_aditionalEntropy,
                                                   DataProtectionScope.CurrentUser);
      return Convert.ToBase64String(encryptedData);
    }

    internal static SecureString DecryptString(string encryptedData)
    {
      try
      {
        byte[] decryptedData = ProtectedData.Unprotect(Convert.FromBase64String(encryptedData),
                                                       s_aditionalEntropy,
                                                       DataProtectionScope.CurrentUser);
        return ToSecureString(System.Text.Encoding.Unicode.GetString(decryptedData));
      }
      catch
      {
        return new SecureString();
      }
    }

    internal static SecureString ToSecureString(string input)
    {
      SecureString secure = new SecureString();
      foreach (char c in input)
        secure.AppendChar(c);
      secure.MakeReadOnly();
      return secure;
    }

    internal static string ToInsecureString(SecureString input)
    {
      string returnValue = string.Empty;
      IntPtr ptr = System.Runtime.InteropServices.Marshal.SecureStringToBSTR(input);
      try
      {
        returnValue = System.Runtime.InteropServices.Marshal.PtrToStringBSTR(ptr);
      }
      finally
      {
        System.Runtime.InteropServices.Marshal.ZeroFreeBSTR(ptr);
      }
      return returnValue;
    }

    /// <summary>
    /// Capitalize the first letter of the name
    /// </summary>
    /// <param name="name"></param>
    /// <returns></returns>
    internal static string ToTitleCase(string name)
    {
      string fixedName = "";
      try
      {
        System.Globalization.CultureInfo cultureInfo = System.Threading.Thread.CurrentThread.CurrentCulture;
        System.Globalization.TextInfo TextInfo = cultureInfo.TextInfo;
        fixedName = TextInfo.ToTitleCase(name);
      }
      catch
      {
        fixedName = name;
      }
      return fixedName;
    }

    /// <summary>
    /// Generates a new unique name within a collection. 
    /// </summary>
    internal static string GenerateUniqueName<T>(string baseName, IEnumerable<T> items, Func<T, string> getItemNameDelegate, bool caseSensitive = true)
    {
      if (items == null)
        return baseName;

      int counter = 1;
      string name = baseName;

      while (true)
      {
        if (IsNameUnique(name, items, getItemNameDelegate, caseSensitive))
          break;

        name = String.Format("{0} {1}", baseName, counter++);
      }

      return name;
    }

    /// <summary>
    /// Checks if a given string is unique within a given collection.
    /// </summary>
    internal static bool IsNameUnique<T>(string name, IEnumerable<T> items, Func<T, string> getItemNameDelegate, bool caseSensitive = true)
    {
      if (items == null)
        return true;

      foreach (T item in items)
      {
        string string1 = name;
        string string2 = getItemNameDelegate(item);
        if (caseSensitive == false)
        {
          string1 = string1.ToLower();
          string2 = string2.ToLower();
        }

        if (String.Compare(string1, string2) == 0)
          return false;
      }
      return true;
    }

    /// <summary>
    /// Formats the lat/lon values of the specified map point.
    /// </summary>
    ///<remarks>The Y value is interpreted as latitude, the X value as longitude.</remarks>
    public static void GetLatLonStrings(MapPoint mapPoint, out string lat, out string lon, LatLonFormats latLonFormat = LatLonFormats.DegreesMinutesSeconds)
    {
      lat = FormatLatLon(mapPoint.Y, CoordinateType.Latitude, latLonFormat);
      lon = FormatLatLon(mapPoint.X, CoordinateType.Longitude, latLonFormat);
    }

    enum CoordinateType { Latitude, Longitude };

    /// <summary>
    /// Converts a latitude or longitude coordinate to a string.
    /// </summary>
    static string FormatLatLon(double coordinate, CoordinateType coordinateType, LatLonFormats latLonFormat)
    {
      bool isNegative = coordinate < 0;
      string coordStr = "";
      switch (latLonFormat)
      {
        case LatLonFormats.DegreesMinutesSeconds:
        default:
          {
            coordinate = Math.Abs(coordinate);
            string heading = coordinateType == CoordinateType.Latitude ? (isNegative ? "S" : "N") : (isNegative ? "W" : "E");

            double d = Math.Floor(coordinate);
            double m = Math.Floor((coordinate - d) * 60);
            double s = Math.Round((((coordinate - d) * 60) - m) * 60);

            coordStr = string.Format("{0}°{1}'{2}\"{3}", d.ToString(), m.ToString().PadLeft(2, '0'), s.ToString().PadLeft(2, '0'), heading);
            break;
          }
        case LatLonFormats.DegreesMinutesDecSeconds:
          {
            coordinate = Math.Abs(coordinate);
            string heading = coordinateType == CoordinateType.Latitude ? (isNegative ? "S" : "N") : (isNegative ? "W" : "E");

            double d = Math.Floor(coordinate);
            double m = Math.Floor((coordinate - d) * 60);
            double s = Math.Round((((coordinate - d) * 60) - m) * 60, 2);

            coordStr = string.Format("{0}°{1}'{2}\"{3}", d.ToString(), m.ToString().PadLeft(2, '0'), s.ToString().PadLeft(2, '0'), heading);
            break;
          }
        case LatLonFormats.DecimalDegrees:
          {
            coordStr = string.Format("{0}°", Math.Round(coordinate, 6).ToString());
            break;
          }
      }
      return coordStr;
    }

  }

  /// <summary>
  /// Specifies the units of coordinates in a layer/map.
  /// </summary>
  public enum MapUnits
  {
    Unknown,
    WebMercatorMeters,
    DecimalDegrees,
    Meters,
    Feet
  }

  /// <summary>
  /// Specifies the different formats for lat/lon coordinates.
  /// </summary>
  public enum LatLonFormats
  {
    DegreesMinutesSeconds,
    DegreesMinutesDecSeconds,
    DecimalDegrees
  }
}
