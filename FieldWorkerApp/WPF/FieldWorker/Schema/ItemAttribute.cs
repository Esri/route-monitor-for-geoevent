using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace FieldWorker.Schema
{
  public class ItemAttribute : INotifyPropertyChanged
  {
    private string _name { get; set; }
    public string Name
    {
      get { return _name; }
      set { _name = value; RaisePropertyChanged(); }
    }

    private object _value { get; set; }
    public object Value
    {
      get { return _value; }
      set { _value = value; RaisePropertyChanged(); }
    }

    public ItemAttribute(string name, object value)
    {
      Name  = name;
      Value = Value;
    }

    public event PropertyChangedEventHandler PropertyChanged = delegate { };
    protected void RaisePropertyChanged([CallerMemberName] string propertyName = "")
    {
      PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
    }
  }
}
