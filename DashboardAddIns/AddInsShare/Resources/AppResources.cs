using System;
using System.Windows;

namespace AddInsShare.Resources
{
  public static class AppResources
  {
    static ResourceDictionary _resourceDictionary;
    public static ResourceDictionary Dictionary
    {
      get
      {
        if (_resourceDictionary == null)
        {
          _resourceDictionary = new ResourceDictionary();

          // this is the AddInsShare resources dictionary source
          _resourceDictionary.Source = new Uri("/AddInsShare;component/Resources/AppResources.xaml", System.UriKind.RelativeOrAbsolute);

          // this is the shared opserations dashboard application resources dictionary source
          //_resourceDictionary.Source = new Uri("pack://application:,,,/OperationsDashboard;component/Resources/AppResources.xaml");
        }
        return _resourceDictionary;
      }
    }
  }

}
