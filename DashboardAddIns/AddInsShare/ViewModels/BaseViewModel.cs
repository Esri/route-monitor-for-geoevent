using System;
using System.ComponentModel;
using System.Linq.Expressions;

namespace AddInsShare.ViewModels
{
  public class BaseViewModel : INotifyPropertyChanged
  {
    public event PropertyChangedEventHandler PropertyChanged = delegate { };
    protected virtual void OnPropertyChanged(string propertyName)
    {
      System.Diagnostics.Debug.Assert(string.IsNullOrEmpty(propertyName) || GetType().GetProperty(propertyName) != null);

      PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
    }

    protected void OnPropertyChanged<T>(Expression<Func<T>> expr)
    {
      var pi = ((MemberExpression)expr.Body).Member as System.Reflection.PropertyInfo;
      if (pi != null)
        OnPropertyChanged(pi.Name);
    }
  }
}
