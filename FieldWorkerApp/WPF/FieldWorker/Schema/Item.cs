using ESRI.ArcGIS.Client;
using FieldWorker.ViewModels;
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Reflection;
using System.Runtime.CompilerServices;

namespace FieldWorker.Schema
{
  public class Item : ICustomTypeProvider, INotifyPropertyChanged
  {
    protected        BaseItemsViewModel  _vmItems = null;

    private          ItemSchema          _schema  = null;
    private readonly ItemValues          _values  = null;

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


    public Item(BaseItemsViewModel vmItems)
    {
      _vmItems = vmItems;
      _schema  = vmItems.Schema;
      _values  = new ItemValues(vmItems);

      _values.PropertyChanged += (s, e) => PropertyChanged(this, e);
    }


    //[Browsable(false)]
    //public ObservableCollection<ItemAttribute> Attributes
    //{ get { return GetAttributes(); } }


    public ObservableCollection<ItemAttribute> GetAttributes()
    { 
      return _values.GetAttributes();
    }

    public void SetPropertyValue(string propertyName, object value)
    {
      _values.SetPropertyValue(propertyName, value);
    }

    public object ConvertValueByPropertyType(string propertyName, object value)
    {
      return _values.ConvertValueByPropertyType(propertyName, value);
    }

    public object GetPropertyValue(string propertyName)
    {
      return _values.GetPropertyValue(propertyName);
    }

    public string GetPropertyValueAsString(string propertyName, string defaultValue = null)
    {
      object obj = _values.GetPropertyValue(propertyName);
      if (obj == null)
        return defaultValue;

      return obj.ToString();
    }


    public event PropertyChangedEventHandler PropertyChanged = delegate { };
    protected void RaisePropertyChanged([CallerMemberName] string propertyName = "")
    {
      PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
    }


    public PropertyInfo[] GetProperties()
    {
      if (_schema == null)
        return null;

      return _schema.GetProperties();
    }

    Type ICustomTypeProvider.GetCustomType()
    {
      if (_schema == null)
        return null;

      return _schema.GetCustomType();
    }

    public int GetPropertiesCount()
    {
      if (_schema == null)
        return 0;

      return _schema.GetProperties().Count();
    }

    public int GetCustomPropertiesCount()
    {
      if (_schema == null)
        return 0;

      return _schema.GetCustomPropertiesCount();
    }

    public bool ContainsPropertyAlias(string alias)
    {
      if (_schema == null)
        return false;

      return _schema.ContainsPropertyAlias(alias);
    }

    public bool ContainsPropertyName(string name)
    {
      if (_schema == null)
        return false;

      return _schema.ContainsPropertyName(name);
    }

    public string AliasToName(string alias)
    {
      if (_schema == null)
        return null;

      return _schema.AliasToName(alias);
    }
  }

}
