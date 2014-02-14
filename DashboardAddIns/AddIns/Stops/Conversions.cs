using System;
using System.Windows;
using System.Windows.Data;
using AddInsShare.Schema;
using AddInsShare.ViewModels;
using Stops.ViewModels;

namespace Stops
{
  [ValueConversion(typeof(DateTime), typeof(String))]
  public class StopUnassignVisibilityConverter : IValueConverter
  {
    public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // value to Visibility
      StopViewModel vm = value as StopViewModel;
      if (vm == null)
        return Visibility.Visible;

      Item item = vm.SelectedItem as Item;
      if (item == null)
        return Visibility.Visible;

      String stopsRouteNameFieldName = vm.StopsRouteNameFieldName;
      if (!item.Graphic.Attributes.ContainsKey(stopsRouteNameFieldName))
        return Visibility.Visible;

      String unassignedRouteName = vm.GetPropValue("ROUTES_UN_ASSIGNED_ROUTE_NAME");
      if (item.Graphic.Attributes[stopsRouteNameFieldName].ToString() == unassignedRouteName)
        return Visibility.Collapsed;

      return Visibility.Visible;
    }

    public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // N/A
      return DependencyProperty.UnsetValue;
    }
  }

  [ValueConversion(typeof(Boolean), typeof(String))]
  public class CalculateRoutesVisibilityConverter : IValueConverter
  {
    public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // value to Visibility
      bool calculating = (bool) value;

      if (calculating)
        return Visibility.Visible;

      return Visibility.Hidden;
    }

    public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // N/A
      return DependencyProperty.UnsetValue;
    }
  }

  [ValueConversion(typeof(Boolean), typeof(Boolean))]
  public class InvertBooleanConverter : IValueConverter
  {
    public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // value to Visibility
      bool calculating = (bool)value;
      return !calculating;
    }

    public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // N/A
      return DependencyProperty.UnsetValue;
    }
  }

  [ValueConversion(typeof(DateTime), typeof(String))]
  public class StopPasteVisibilityConverter : IValueConverter
  {
    public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // value to Visibility
      StopViewModel vm = value as StopViewModel;
      if (vm == null)
        return Visibility.Collapsed;

      Item item = vm.CutStop as Item;
      if (item == null)
        return Visibility.Collapsed;

      return Visibility.Visible;
    }

    public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // N/A
      return DependencyProperty.UnsetValue;
    }
  }

}
