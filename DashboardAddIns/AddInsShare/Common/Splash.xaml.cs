using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace AddInsShare.Common
{
  /// <summary>
  /// Interaction logic for Splash.xaml
  /// </summary>
  public partial class Splash : Window
  {
    private static Splash splash = new Splash();

    // To refresh the UI immediately
    private delegate void RefreshDelegate();
    private static void Refresh(DependencyObject obj)
    {
      obj.Dispatcher.Invoke(System.Windows.Threading.DispatcherPriority.Render, (RefreshDelegate)delegate { });
    }

    public Splash()
    {
      InitializeComponent();
    }

    public static void BeginDisplay(Window owner)
    {
      splash.Owner = owner;
      splash.Show();
    }

    public static void EndDisplay()
    {
      splash.Close();
    }

    public static void Working(string test)
    {
      splash.statuslbl.Content = test;
      Refresh(splash.statuslbl);
    }
  }
}
