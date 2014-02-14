﻿using System;
using System.Windows.Input;

namespace FieldWorker.ViewModels
{
  public class DelegatingCommand : ICommand
  {
    private readonly Action<object> _execute;
    private readonly Func<object, bool> _canExecute;

    public DelegatingCommand(Action<object> execute, Func<object, bool> canExecute = null)
    {
      _execute = execute;
      _canExecute = canExecute;
    }

    public bool CanExecute(object parameter)
    {
      return (_canExecute == null) || _canExecute(parameter);
    }

    public event EventHandler CanExecuteChanged
    {
      add { CommandManager.RequerySuggested += value; }
      remove { CommandManager.RequerySuggested -= value; }
    }

    public void Execute(object parameter)
    {
      _execute(parameter);
    }
  }
}
