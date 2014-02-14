using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Controls;
using System.Windows;

namespace AddInsShare.Controls
{
  /// <summary>
  /// Implements a Metro style rotating ring progress/busy indicator
  /// </summary>
  internal class MetroProgressRing : ProgressBar
  {
    static MetroProgressRing()
    {
      DefaultStyleKeyProperty.OverrideMetadata(typeof(MetroProgressRing), new FrameworkPropertyMetadata(typeof(MetroProgressRing)));
    }

    public double BubbleSize
    {
      get { return (double)GetValue(BubbleSizeProperty); }
      set { SetValue(BubbleSizeProperty, value); }
    }

    /// <summary>
    /// The size of each bubble (width and height) in the progress ring
    /// </summary>
    public static readonly DependencyProperty BubbleSizeProperty =
        DependencyProperty.Register("BubbleSize", typeof(double), typeof(MetroProgressRing), new PropertyMetadata(4.0));

  }
}
