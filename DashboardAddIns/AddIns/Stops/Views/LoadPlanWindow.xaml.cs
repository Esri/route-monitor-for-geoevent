using System;
using System.Windows;
using Stops.ViewModels;

namespace Stops.Views
{
  /// <summary>
  /// Interaction logic for LoadPlanWindow.xaml
  /// </summary>
  public partial class LoadPlanWindow : Window
  {
    private StopViewModel _stopVM = null;


    internal LoadPlanWindow(StopViewModel stopVM)
    {
      InitializeComponent();

      _stopVM = stopVM;
    }

    protected override void OnPreviewMouseUp(System.Windows.Input.MouseButtonEventArgs e)
    {
      // The following is a workaround to fix the issue where a WPF Toolkit Calendar takes two clicks to get focus
      // see also:
      //  http://stackoverflow.com/questions/2425951/wpf-toolkit-calendar-takes-two-clicks-to-get-focus
      //  http://stackoverflow.com/questions/5543119/wpf-button-takes-two-clicks-to-fire-click-event

      base.OnPreviewMouseUp(e);
      if (System.Windows.Input.Mouse.Captured is System.Windows.Controls.Primitives.CalendarItem)
      {
        System.Windows.Input.Mouse.Capture(null);
      }
    }
    
    private void btnLoadCancel_Click(object sender, RoutedEventArgs e)
    {
      // close the window
      Close();
    }

    private void btnLoadOK_Click(object sender, RoutedEventArgs e)
    {
      DateTime? date = StopDateCalendar.SelectedDate;
      if (date == null)
        date = DateTime.Now;

      if (!_stopVM.LoadPlan(date))
        return;

      // invoke load plan on GEP

      // close the window
      Close();
    }
  }
}
