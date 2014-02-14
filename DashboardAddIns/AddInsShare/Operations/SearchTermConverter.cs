using System;
using System.Windows.Data;

namespace AddInsShare
{
  public class SearchTermConverter : IMultiValueConverter
  {
    #region IMultiValueConverter Members

    public object Convert(object[] values, Type targetType, object parameter, System.Globalization.CultureInfo culture)
    {
      var stringValue = values[0] == null ? string.Empty : values[0].ToString();
      var searchTerm = values[1] as string;

      return !string.IsNullOrEmpty(searchTerm) &&
             !string.IsNullOrEmpty(stringValue) &&
             stringValue.ToLower().Contains(searchTerm.ToLower());
    }

    public object[] ConvertBack(object value, Type[] targetTypes, object parameter, System.Globalization.CultureInfo culture)
    {
      throw new NotSupportedException();
    }

    #endregion
  }
}
