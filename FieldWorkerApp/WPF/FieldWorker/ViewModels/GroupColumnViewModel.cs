using System.Windows.Input;

namespace FieldWorker.ViewModels
{
  public sealed class GroupColumnViewModel : BaseViewModel
  {
    private bool     _isChecked;
    private ICommand _command;


    public GroupColumnViewModel(string name, ICommand groupByCommand)
    {
      PropertyName = name;
      _command     = groupByCommand;
    }

    public string PropertyName { get; private set; }

    public bool IsChecked
    {
      get { return _isChecked; }
      set { _isChecked = value; OnPropertyChanged(() => IsChecked); }
    }

    public ICommand Command { get { return _command; } }
  }
}
