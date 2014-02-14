using System;
using System.Collections.Generic;
using System.Windows.Media.Imaging;

namespace FieldWorker.Common
{
  // a singletin class which manages the application's images cache
  public class ImagesCache
  {
    private Dictionary<string, BitmapSource> _images = null;

    private static ImagesCache _cache = null;
    public static ImagesCache Instance
    {
      get
      {
        if (_cache == null)
          _cache = new ImagesCache();

        return _cache;
      }
    }

    // the private contructor
    private ImagesCache()
    {
      _images = new Dictionary<string,BitmapSource>();
    }

    public BitmapSource Get(string relativeImagePath, bool bInternalResource)
    {
      if (_images.ContainsKey(relativeImagePath))
        return _images[relativeImagePath];

      BitmapSource image = null;

      if (bInternalResource)
        image = LoadInternalImage(relativeImagePath);
      else
        image = LoadExternalImage(relativeImagePath);

      if (image != null)
        _images[relativeImagePath] = image;

      return image;
    }

    public BitmapSource LoadExternalImage(string relativeImagePath)
    {
      BitmapImage image = new BitmapImage();
      string path = AppSettings.GetAppFullPath() + relativeImagePath;
      if (!Helper.FileExists(path))
        return null;

      try
      {
        // BitmapImage.UriSource must be in a BeginInit/EndInit block
        image.BeginInit();
        image.CacheOption = BitmapCacheOption.OnLoad;
        image.UriSource = new Uri(path);

        // To save significant application memory, set the DecodePixelWidth or   
        // DecodePixelHeight of the BitmapImage value of the image source to the desired  
        // height or width of the rendered image. If you don't do this, the application will  
        // cache the image as though it were rendered as its normal size rather then just  
        // the size that is displayed. 
        // Note: In order to preserve aspect ratio, set DecodePixelWidth 
        // or DecodePixelHeight but not both.
        //image.DecodePixelWidth = 200;

        image.EndInit();
      }
      catch (Exception ex)
      {
        Log.TraceException("Error trying to load image file - '" + path + "'", ex);
        return null;
      }

      return image;
    }

    public BitmapSource LoadInternalImage(string relativeImagePath)
    {
      // this is when the image is an internal resource
      string path = "pack://application:,,," + ".." + relativeImagePath;
      BitmapFrame image = null;
      try
      {
        image = BitmapFrame.Create(new Uri(path));
      }
      catch (Exception ex)
      {
        Log.TraceException("Error trying to load internal resource - '" + relativeImagePath + "'", ex);
        return null;
      }

      return image;
    }

  }
}
