using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using ESRI.ArcGIS.Client;

namespace FieldWorker.Schema
{
  public class ItemSchema : ICustomTypeProvider
  {
    internal readonly List<CustomPropertyInfoHelper> CustomProperties = null;
    private  readonly Lazy<CustomType>               _customType      = null;
    private  readonly Dictionary<string, string>     _nameToAlias     = null;

    public ItemSchema()
    {
      CustomProperties = new List<CustomPropertyInfoHelper>(); 
      _customType = new Lazy<CustomType>(() => new CustomType(typeof(Item), CustomProperties));
      _nameToAlias = new Dictionary<string, string>();
    }

    public void AddProperty(Field field)
    {
      // Adds a new dynamic property value

      //hk - always allow to add
      //if (!ContainsPropertyAlias(name))
      {
        CustomProperties.Add(new CustomPropertyInfoHelper(field, typeof(Item)));
        _nameToAlias.Add(field.Name, field.Alias);
      }
    }

    public void AddProperty(Field field, Type propertyType, List<Attribute> attributes)
    {
      // Adds a new property definition

      //hk - always allow to add
      //if (!ContainsPropertyAlias(name))
      {
        CustomProperties.Add(new CustomPropertyInfoHelper(field, attributes, typeof(Item)));
        _nameToAlias.Add(field.Name, field.Alias);
      }
    }

    public void RemoveProperty(string alias)
    {
      // Removes a specific property by name
      var item = CustomProperties.FirstOrDefault(cp => cp.Name == alias);
      if (item == null)
        return;

      // remove the property
      CustomProperties.Remove(item);

      // remove the name from the _nameToAlias lookup table
      String name = AliasToName(alias);
      if (name != null)
        _nameToAlias.Remove(name);
    }

    public void ClearProperties()
    {
      // Clears all the properties from a defined object type.
      CustomProperties.Clear();
      _nameToAlias.Clear();
    }

    public String NameToAlias(string name)
    {
      if (name == null)
        return null;

      if (!_nameToAlias.ContainsKey(name))
        return null;

      return _nameToAlias[name];
    }

    public String AliasToName(string alias)
    {
      String name = null;
      foreach (KeyValuePair<string, string> pair in _nameToAlias)
      {
        if (pair.Value == alias)
        {
          name = pair.Key;
          break;
        }
      }
      return name;
    }

    public PropertyInfo[] GetProperties()
    {
      // Retrieve all the defined properties (both known and dynamic)
      return GetCustomType().GetProperties();
    }

    public Type GetCustomType()
    {
      // Gets the custom type provided by this object.
      return _customType.Value;
    }

    public bool ContainsPropertyName(string name)
    {
      string alias = NameToAlias(name);
      if (alias == null)
        return false;

      return ContainsPropertyAlias(alias);
    }

    public bool ContainsPropertyAlias(string alias)
    {
      if (CustomProperties.Any(p => 0 == string.Compare(p.Name, alias, StringComparison.OrdinalIgnoreCase))
          || typeof(Item).GetProperties().Any(p => 0 == string.Compare(p.Name, alias, StringComparison.OrdinalIgnoreCase)))
      {
        //throw new Exception("Property with this name already exists: " + alias);
        return true;
      }

      return false;
    }

    public int GetCustomPropertiesCount()
    {
      return CustomProperties.Count;
    }

  }
}
