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

namespace FieldWorker.Views
{
  public partial class Login : Window
  {
    public string UserName { get; set; }
    public string Password { get; set; }

    public Login(string userName, string password, Window owner)
    {
      InitializeComponent();
      Owner = owner;

      UserName = userName;
      Password = password;

      UsernameTextBox.Text = UserName;
      PasswordTextBox.Password = Password;
    }

    private void OK_Click(object sender, RoutedEventArgs e)
    {
      UserName = UsernameTextBox.Text;
      Password = PasswordTextBox.Password;
      Hide();
    }

    private void Window_Activated(object sender, EventArgs e)
    {
      if (UsernameTextBox.Text == "")
        UsernameTextBox.Focus();
    }
  }
}
