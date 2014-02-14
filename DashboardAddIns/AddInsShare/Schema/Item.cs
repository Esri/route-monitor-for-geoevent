using System;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;
using ESRI.ArcGIS.Client;


namespace AddInsShare.Schema
{
  public class Item : ICustomTypeProvider, INotifyPropertyChanged
  {
    /// <summary>
    /// Custom Type provider implementation - delegated through static methods.
    /// </summary>
    private          ItemSchema _schema  = null;
    private readonly ItemValues _values  = null;

    public         Graphic    Graphic  = null;

    #region Existing known properties
    /*
    // Existing known properties
    private string _firstName;
    private string _lastName;

    public string FirstName
    {
      get { return _firstName; }
      set { _firstName = value; RaisePropertyChanged(); }
    }

    public string LastName
    {
      get { return _lastName; }
      set { _lastName = value; RaisePropertyChanged(); }
    }
    */
    #endregion


    public Item(ItemSchema schema)
    {
      _schema = schema;
      _values = new ItemValues(_schema);

      _values.PropertyChanged += (s, e) => PropertyChanged(this, e);
    }


    public void SetPropertyValue(string propertyName, object value)
    {
      _values.SetPropertyValue(propertyName, value);
    }

    public object GetPropertyValue(string propertyName)
    {
      return _values.GetPropertyValue(propertyName);
    }

    public string GetPropertyValueAsString(string propertyName)
    {
      object objValue = _values.GetPropertyValue(propertyName);
      return (objValue == null ? null : objValue.ToString());
    }


    public event PropertyChangedEventHandler PropertyChanged = delegate { };
    protected void RaisePropertyChanged([CallerMemberName] string propertyName = "")
    {
      PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
    }


    public PropertyInfo[] GetProperties()
    {
      return _schema.GetProperties();
    }

    Type ICustomTypeProvider.GetCustomType()
    {
      return _schema.GetCustomType();
    }

    public int GetPropertiesCount()
    {
      return _schema.GetProperties().Count();
    }

    public int GetCustomPropertiesCount()
    {
      return _schema.GetCustomPropertiesCount();
    }

  }

}
