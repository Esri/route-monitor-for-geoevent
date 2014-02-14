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
using AddInsShare.Common;

namespace AddInsShare.Windows
{
  /// <summary>
  /// Interaction logic for ModelessMessageBox.xaml
  /// </summary>
  public partial class RequestMessageBox : Window
  {
    public RequestMessageBox(string title, string message)
    {
      InitializeComponent();
      Title   = title;
      Message = message;
    }

    public RequestMessageBox(string title)
    {
      InitializeComponent();
      this.Title = title;
    }

    public RequestMessageBox()
    {
      InitializeComponent();
    }

    public string Message
    {
      get { return this.message.Text; }
      set { this.message.Text = value; }
    }

    public void Show(string title, string message, Window owner)
    {
      Title   = title;
      Message = message;
      Owner   = owner;
      Show();
    }
  }
}
