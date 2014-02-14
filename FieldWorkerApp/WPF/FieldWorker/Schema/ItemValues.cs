using FieldWorker.Common;
using FieldWorker.ViewModels;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;

namespace FieldWorker.Schema
{
  public class ItemValues
  {
    private          ItemSchema                          _schema           = null;
    private readonly Dictionary<string, ItemAttribute>   _valuesDictionary = new Dictionary<string, ItemAttribute>();
    private readonly ObservableCollection<ItemAttribute> _valuesCollection = new ObservableCollection<ItemAttribute>();

    /// <summary>
    /// Occurs when a property value changes.
    /// </summary>
    public event PropertyChangedEventHandler PropertyChanged = delegate { };


    public ItemValues(BaseItemsViewModel vmItems)
    {
      _schema = vmItems.Schema;

      foreach (var property in _schema.GetCustomType().GetProperties())
      {
        ItemAttribute newValue = new ItemAttribute(property.Name, null);
        _valuesDictionary.Add(property.Name, newValue);
        _valuesCollection.Add(newValue);
      }
    }


    public ObservableCollection<ItemAttribute> GetAttributes()
    {
      return _valuesCollection;
    }

    public int GetCustomPropertiesCount()
    {
      return _valuesDictionary.Count;
    }

    public object GetPropertyValue(string propertyName)
    {
      if (propertyName == null)
        return null;

      String alias = _schema.NameToAlias(propertyName);

      // try to find by name if no alias is defined
      if (alias == null)
        alias = propertyName;

      if (_valuesDictionary.ContainsKey(alias))
        return _valuesDictionary[alias].Value;

      return null;
      //throw new Exception("There is no property " + alias);
    }

    public TV GetPropertyValue<TV>(string propertyName)
    {
      return (TV)GetPropertyValue(propertyName);
    }

    public bool SetPropertyValue(string propertyName, object value)
    {
      String alias =_schema.NameToAlias(propertyName);

      // try to find by name if no alias is defined
      if (alias == null)
        alias = propertyName;

      CustomPropertyInfoHelper propertyInfo = _schema.CustomProperties.FirstOrDefault(prop => prop.Name == alias);
      if (propertyInfo == null)
        return false;

      // allow adding types on the fly
      //if (propertyInfo == null || !_customPropertyValues.ContainsKey(alias))
      //  throw new Exception("There is no property with the name " + alias);

      object convertedValue = value;
      if (!ValidateValueType(value, propertyInfo._type))
        convertedValue = Helper.ConvertValue(value, propertyInfo.Field.Type);

      if (!_valuesDictionary.ContainsKey(alias))
      {
        // allow adding types on the fly
        ItemAttribute newValue = new ItemAttribute(alias, convertedValue);
        _valuesDictionary.Add(alias, newValue);
        _valuesCollection.Add(newValue);
        RaisePropertyChanged(alias);
      }
      else
      {
        ItemAttribute currentValue = _valuesDictionary[alias];
        if (currentValue.Value != (object)value)  //TODO - consider comparing by ToString() values instead
        {
          currentValue.Value = convertedValue;
          RaisePropertyChanged(alias);
        }
      }

      return true;
    }

    public object ConvertValueByPropertyType(string propertyName, object value)
    {
      String alias = _schema.NameToAlias(propertyName);

      // try to find by name if no alias is defined
      if (alias == null)
        alias = propertyName;

      CustomPropertyInfoHelper propertyInfo = _schema.CustomProperties.FirstOrDefault(prop => prop.Name == alias);
      if (propertyInfo == null)
        return null;

      if (ValidateValueType(value, propertyInfo._type))
        return value;

      object convertedValue = value;
      if (!ValidateValueType(value, propertyInfo._type))
        convertedValue = Helper.ConvertValue(value, propertyInfo.Field.Type);

      return convertedValue;
    }

    private bool ValidateValueType(object value, Type type)
    {
      return value == null
          ? !type.IsValueType || type.IsGenericType && type.GetGenericTypeDefinition() == typeof(Nullable<>)
          : type.IsAssignableFrom(value.GetType());
    }

    protected void RaisePropertyChanged([CallerMemberName] string propertyName = "")
    {
      PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
    }

  }
}
