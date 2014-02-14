using System;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Geometry;
using ESRI.ArcGIS.Client.Projection;
using Newtonsoft.Json.Linq;

namespace AddInsShare.Common
{
  public class Helper
  {
    private static DateTime UNIX_EPOCH = new DateTime(1970, 1, 1, 0, 0, 0);

    public static Type GetSystemTypeFromField(Field field)
    {
      switch (field.Type)
      {
        case ESRI.ArcGIS.Client.Field.FieldType.Integer:
        case ESRI.ArcGIS.Client.Field.FieldType.OID:
          return typeof(System.Int32?);
        case ESRI.ArcGIS.Client.Field.FieldType.SmallInteger:
          return typeof(System.Int16?);
        case ESRI.ArcGIS.Client.Field.FieldType.Double:
          return typeof(System.Double?);
        case ESRI.ArcGIS.Client.Field.FieldType.Single:
          return typeof(System.Single?);
        case ESRI.ArcGIS.Client.Field.FieldType.String:
        case ESRI.ArcGIS.Client.Field.FieldType.GlobalID:
        case ESRI.ArcGIS.Client.Field.FieldType.GUID:
        case ESRI.ArcGIS.Client.Field.FieldType.XML:
          return typeof(System.String);
        case ESRI.ArcGIS.Client.Field.FieldType.Date:
          return typeof(System.DateTime?);
        case ESRI.ArcGIS.Client.Field.FieldType.Geometry:
        case ESRI.ArcGIS.Client.Field.FieldType.Blob:
        case ESRI.ArcGIS.Client.Field.FieldType.Raster:
        case ESRI.ArcGIS.Client.Field.FieldType.Unknown:
          return typeof(System.Object);
        default:
          return typeof(System.Object);
      }
    }

    public static Object ConvertGepJsonTokenToObject(JToken fieldToken, Field.FieldType fieldType)
    {
      switch (fieldType)
      {
        case ESRI.ArcGIS.Client.Field.FieldType.Integer:
        case ESRI.ArcGIS.Client.Field.FieldType.OID:
          return fieldToken.ToObject<Int32?>();
        case ESRI.ArcGIS.Client.Field.FieldType.SmallInteger:
          return fieldToken.ToObject<Int16?>();
        case ESRI.ArcGIS.Client.Field.FieldType.Double:
          return fieldToken.ToObject<Double?>();
        case ESRI.ArcGIS.Client.Field.FieldType.Single:
          return fieldToken.ToObject<Single?>();
        case ESRI.ArcGIS.Client.Field.FieldType.String:
        case ESRI.ArcGIS.Client.Field.FieldType.GlobalID:
        case ESRI.ArcGIS.Client.Field.FieldType.GUID:
        case ESRI.ArcGIS.Client.Field.FieldType.XML:
          return fieldToken.ToObject<String>();
        case ESRI.ArcGIS.Client.Field.FieldType.Date:
          return UnixTimeToDateTime((long?)fieldToken);
        case ESRI.ArcGIS.Client.Field.FieldType.Geometry:
        case ESRI.ArcGIS.Client.Field.FieldType.Blob:
        case ESRI.ArcGIS.Client.Field.FieldType.Raster:
        case ESRI.ArcGIS.Client.Field.FieldType.Unknown:
          return fieldToken.ToObject<Object>();
        default:
          return fieldToken.ToObject<Object>();
      }
    }

    public static DateTime? UnixTimeToDateTime(long? unixTicksMS)
    {
      if (unixTicksMS == null)
        return null;

      return new DateTime(UNIX_EPOCH.Ticks + (long)unixTicksMS * 10000);
    }

    public static Geometry ProjectGeometryToMap(Geometry geometry, Map map)
    {
      if (geometry == null || map == null)
        return geometry;

      if (geometry.SpatialReference.WKID == map.SpatialReference.WKID)
        return geometry;

      WebMercator webMercator = new WebMercator();

      // convert from WGS84 to Web-Mercator
      if (IsWGS84SR(geometry.SpatialReference) && IsWebMercatorSR(map.SpatialReference))
        return webMercator.FromGeographic(geometry);

      // convert from Web-Mercator to WGS84
      if (IsWebMercatorSR(geometry.SpatialReference) && IsWGS84SR(map.SpatialReference))
        return webMercator.ToGeographic(geometry);

      // not supported SRs - return the non projected geometry
      return geometry;
    }

    public static Geometry ProjectGeometryToGeographic(Geometry geometry)
    {
      if (IsWGS84SR(geometry.SpatialReference))
        return geometry;

      WebMercator webMercator = new WebMercator();

      // convert from Web-Mercator to WGS84
      return webMercator.ToGeographic(geometry);
    }

    public static bool IsWebMercatorSR(SpatialReference sr)
    {
      if (sr == null)
        return false;
      if (sr.WKID == 102100 || sr.WKID == 3857)
        return true;

      return false;
    }

    public static bool IsWGS84SR(SpatialReference sr)
    {
      if (sr == null)
        return false;
      if (sr.WKID == 4326)
        return true;

      return false;
    }

    public static Geometry EnvelopeToPolygon(Geometry geometry)
    {
      if (!(geometry is Envelope))
        return null;

      Envelope envelope = geometry as Envelope;
      Polygon polygon = new Polygon();
      polygon.SpatialReference = envelope.SpatialReference; 

      PointCollection pointCollection = new PointCollection();

      MapPoint topLeft = new MapPoint();
      topLeft.X = envelope.XMin;
      topLeft.Y = envelope.YMax; 
      pointCollection.Add(topLeft); 

      MapPoint topRight = new MapPoint();
      topRight.X = envelope.XMax;
      topRight.Y = envelope.YMax; 
      pointCollection.Add(topRight);

      MapPoint bottomRight = new MapPoint();
      bottomRight.X = envelope.XMax;
      bottomRight.Y = envelope.YMin;
      pointCollection.Add(bottomRight);

      MapPoint bottomLeft = new MapPoint();
      bottomLeft.X = envelope.XMin;
      bottomLeft.Y = envelope.YMin;
      pointCollection.Add(bottomLeft);

      pointCollection.Add(topLeft); 

      polygon.Rings.Add(pointCollection);

      return polygon as Geometry;
    }

    public static Geometry PolylineToPolygon(Geometry geometry)
    {
      if (!(geometry is Polyline))
        return null;

      Polyline polyline = geometry as Polyline;
      Polygon polygon = new Polygon();
      polygon.SpatialReference = polyline.SpatialReference;

      MapPoint newPoint;
      PointCollection newPointCollection;
      foreach (PointCollection pointCollection in polyline.Paths)
      {
        // create a new point collection with a copy of the points
        newPointCollection = new PointCollection();
        foreach (MapPoint point in pointCollection)
        {
          newPoint = new MapPoint(point.X, point.Y, point.Z, point.M, point.SpatialReference);
          newPointCollection.Add(newPoint);
        }

        // repeat the first point at the end
        MapPoint firstPoint = pointCollection[0];
        newPoint = new MapPoint(firstPoint.X, firstPoint.Y, firstPoint.Z, firstPoint.M, firstPoint.SpatialReference);
        newPointCollection.Add(newPoint);

        // add the new point cpllection as a ring
        polygon.Rings.Add(newPointCollection);
      }

      return polygon as Geometry;
    }

    public static DependencyObject GetLogicalParent(UserControl control)
    {
      DependencyObject parent = control.Parent;
      while (!(parent is UserControl))
        parent = LogicalTreeHelper.GetParent(parent);

      return parent;
    }

    public static DependencyObject GetVisualParent(UserControl control)
    {
      DependencyObject parent = control.Parent;
      while (!(parent is UserControl))
        parent = System.Windows.Media.VisualTreeHelper.GetParent(parent);

      return parent;
    }

    public static string GetDashboardUsername()
    {
      //var credsEnum = IdentityManager.Current.Credentials.GetEnumerator();
      //credsEnum.MoveNext();
      //string username = credsEnum.Current.UserName;

      var creds = IdentityManager.Current.Credentials.First();
      string username = creds.UserName;

      return username;
    }

  }
}
