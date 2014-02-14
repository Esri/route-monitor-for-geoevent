using FieldWorker.ViewModels;
using System;
using System.Windows;
using System.Windows.Data;

namespace FieldWorker.Common
{
  [ValueConversion(typeof(DateTime), typeof(String))]
  public class StopPasteVisibilityConverter : IValueConverter
  {
    public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // value to Visibility
      StopsViewModel vm = value as StopsViewModel;
      if (vm == null)
        return Visibility.Collapsed;

      Stop stop = vm.CutStop as Stop;
      if (stop == null)
        return Visibility.Collapsed;

      return Visibility.Visible;
    }

    public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // N/A
      return DependencyProperty.UnsetValue;
    }
  }

  [ValueConversion(typeof(DateTime), typeof(String))]
  public class StopAddVisibilityConverter : IValueConverter
  {
    public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // value to Visibility
      StopsViewModel vm = value as StopsViewModel;
      if (vm == null)
        return Visibility.Collapsed;

      // TODO - add cases for Visibility.Collapsed

      return Visibility.Visible;
    }

    public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // N/A
      return DependencyProperty.UnsetValue;
    }
  }

  [ValueConversion(typeof(DateTime), typeof(String))]
  public class StopEditVisibilityConverter : IValueConverter
  {
    public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // value to Visibility
      StopsViewModel vm = value as StopsViewModel;
      if (vm == null)
        return Visibility.Collapsed;

      Stop stop = vm.SelectedItem as Stop;
      if (stop == null)
        return Visibility.Collapsed;

      return stop.IsBreak ? Visibility.Visible : Visibility.Collapsed;
    }

    public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo info)
    {
      // N/A
      return DependencyProperty.UnsetValue;
    }
  }

}
