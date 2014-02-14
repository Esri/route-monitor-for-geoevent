using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;

namespace AddInsShare.Schema
{
  class ItemValues
  {
    private          ItemSchema                 _schema               = null;
    private readonly Dictionary<string, object> _customPropertyValues = new Dictionary<string, object>();

    /// <summary>
    /// Occurs when a property value changes.
    /// </summary>
    public event PropertyChangedEventHandler PropertyChanged = delegate { };



    public ItemValues(ItemSchema schema)
    {
      _schema = schema;

      foreach (var property in _schema.GetCustomType().GetProperties())
        _customPropertyValues.Add(property.Name, null);
    }

    public int GetCustomPropertiesCount()
    {
      return _customPropertyValues.Count;
    }

    /// <summary>
    /// Returns a specific property value by name
    /// </summary>
    /// <param name="propertyName"></param>
    /// <returns></returns>
    public object GetPropertyValue(string propertyName)
    {
      if (propertyName == null)
        return null;

      String alias = _schema.NameToAlias(propertyName);

      // try to find by name if no alias is defined
      if (alias == null)
        alias = propertyName;

      if (_customPropertyValues.ContainsKey(alias))
        return _customPropertyValues[alias];

      return null;
      //throw new Exception("There is no property " + alias);
    }

    /// <summary>
    /// Returns a cast property value by name
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="propertyName"></param>
    /// <returns></returns>
    public TV GetPropertyValue<TV>(string propertyName)
    {
      return (TV)GetPropertyValue(propertyName);
    }

    /// <summary>
    /// Sets a property value by name
    /// </summary>
    /// <param name="propertyName"></param>
    /// <param name="value"></param>
    public void SetPropertyValue(string propertyName, object value)
    {
      String alias =_schema.NameToAlias(propertyName);

      // try to find by name if no alias is defined
      if (alias == null)
        alias = propertyName;

      CustomPropertyInfoHelper propertyInfo = _schema.CustomProperties.FirstOrDefault(prop => prop.Name == alias);
      if (propertyInfo == null)
        return;

      //hk - allow to add types on the fly
      //if (propertyInfo == null || !_customPropertyValues.ContainsKey(alias))
      //  throw new Exception("There is no property with the name " + alias);

      if (ValidateValueType(value, propertyInfo._type))
      {
        if (!_customPropertyValues.ContainsKey(alias))
        {
          //hk - allow to add types on the fly
          _customPropertyValues.Add(alias, value);
          RaisePropertyChanged(alias);
        }
        else if (_customPropertyValues[alias] != (object)value)
        {
          _customPropertyValues[alias] = value;
          RaisePropertyChanged(alias);
        }
      }
      else
      {
        throw new Exception("Value is of the wrong type or null for a non-nullable type.");
      }
    }

    /// <summary>
    /// Validates the value for the given type
    /// </summary>
    /// <param name="value"></param>
    /// <param name="type"></param>
    /// <returns></returns>
    private bool ValidateValueType(object value, Type type)
    {
      return value == null
          ? !type.IsValueType || type.IsGenericType && type.GetGenericTypeDefinition() == typeof(Nullable<>)
          : type.IsAssignableFrom(value.GetType());
    }

    /// <summary>
    /// Raises a property change notification
    /// </summary>
    /// <param name="propertyName"></param>
    protected void RaisePropertyChanged([CallerMemberName] string propertyName = "")
    {
      PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
    }

  }
}
