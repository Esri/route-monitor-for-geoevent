using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Controls;
using System.Windows;

namespace AddInsShare.Controls
{
  /// <summary>
  /// Implements a Metro style linear progress/busy indicator
  /// </summary>
  internal class MetroProgressBar : ProgressBar
  {

    static MetroProgressBar()
    {
      DefaultStyleKeyProperty.OverrideMetadata(typeof(MetroProgressBar), new FrameworkPropertyMetadata(typeof(MetroProgressBar)));
    }

    public override void OnApplyTemplate()
    {
      base.OnApplyTemplate();

      this.LayoutUpdated += new EventHandler(MetroProgressBar_LayoutUpdated);
    }

    // forces the animations to re-start and apply their new offsets
    void MetroProgressBar_LayoutUpdated(object sender, EventArgs e)
    {
      IsIndeterminate = false;
      IsIndeterminate = true;
    }

    public double BubbleSize
    {
      get { return (double)GetValue(BubbleSizeProperty); }
      set { SetValue(BubbleSizeProperty, value); }
    }

    /// <summary>
    /// The size of each bubble (width and height) in the progress indicator
    /// </summary>
    public static readonly DependencyProperty BubbleSizeProperty =
        DependencyProperty.Register("BubbleSize", typeof(double), typeof(MetroProgressBar), new PropertyMetadata(4.0));
  }
}
