using System.Windows;

namespace AddInsShare.Resources
{
  public static class SharedDictionaryManager
  {
    private static ResourceDictionary _sharedStyleDictionary;
    private static ResourceDictionary _sharedDataTemplateDictionary;

    public static ResourceDictionary SharedStyleDictionary
    {
      get
      {
        if (_sharedStyleDictionary == null)
        {
          System.Uri resourceLocater = new System.Uri("/AddInsShare;component/Resources/Styles.xaml", System.UriKind.RelativeOrAbsolute);
          _sharedStyleDictionary = (ResourceDictionary)Application.LoadComponent(resourceLocater);
        }

        return _sharedStyleDictionary;
      }
    }

    public static ResourceDictionary SharedDataTemplateDictionary
    {
      get
      {
        if (_sharedDataTemplateDictionary == null)
        {
          System.Uri resourceLocater = new System.Uri("/AddInsShare;component/Resources/DataTemplates.xaml", System.UriKind.Relative);
          _sharedDataTemplateDictionary = (ResourceDictionary)Application.LoadComponent(resourceLocater);
        }

        return _sharedDataTemplateDictionary;
      }
    }
  }
}
