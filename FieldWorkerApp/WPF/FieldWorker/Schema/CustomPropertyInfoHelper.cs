using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using ESRI.ArcGIS.Client;
using FieldWorker.Common;

namespace FieldWorker.Schema
{
  // Custom implementation of the PropertyInfo
  internal class CustomPropertyInfoHelper : PropertyInfo
  {
    readonly string           _name;
    readonly string           _alias;
    internal readonly Type    _type;
    readonly Field            _field;
    readonly MethodInfo       _getMethod;
    readonly MethodInfo       _setMethod;
    readonly List<Attribute>  _attributes = new List<Attribute>();

    public CustomPropertyInfoHelper(Field field, Type ownerType)
    {
      _name       = field.Name;
      _alias      = field.Alias;
      _type       = Helper.GetSystemTypeFromFieldType(field.Type);
      _field      = field;
      _getMethod  = ownerType.GetMethods().Single(m => m.Name == "GetPropertyValue" && !m.IsGenericMethod);
      _setMethod  = ownerType.GetMethod("SetPropertyValue");
    }

    public CustomPropertyInfoHelper(Field field, List<Attribute> attributes, Type propertyOwner)
    {
      _name       = field.Name;
      _alias      = field.Alias;
      _type       = Helper.GetSystemTypeFromFieldType(field.Type);
      _field      = field;
      _attributes = attributes;
    }


    public override PropertyAttributes Attributes
    {
      get { throw new NotImplementedException(); }
    }

    public override bool CanRead
    {
      get { return true; }
    }

    public override bool CanWrite
    {
      get { return true; }
    }

    public override MethodInfo[] GetAccessors(bool nonPublic)
    {
      throw new NotImplementedException();
    }

    public override MethodInfo GetGetMethod(bool nonPublic)
    {
      return _getMethod;
    }

    public override ParameterInfo[] GetIndexParameters()
    {
      return new ParameterInfo[0];
    }

    public override MethodInfo GetSetMethod(bool nonPublic)
    {
      return _setMethod;
    }

    // Returns the value from the dictionary stored in the Customer's instance.
    public override object GetValue(object obj, BindingFlags invokeAttr, Binder binder, object[] index, System.Globalization.CultureInfo culture)
    {
      return _getMethod.Invoke(obj, new object[] { _alias });
      //return obj.GetType().GetMethod("GetPropertyValue").Invoke(obj, new object[] { _alias });
    }

    public override Type PropertyType
    {
      get { return _type; }
    }

    // Sets the value in the dictionary stored in the Customer's instance.
    public override void SetValue(object obj, object value, BindingFlags invokeAttr, Binder binder, object[] index, System.Globalization.CultureInfo culture)
    {
      _setMethod.Invoke(obj, new[] { _alias, value });
      //obj.GetType().GetMethod("SetPropertyValue").Invoke(obj, new[] { _alias, value });
    }

    public override Type DeclaringType
    {
      get { throw new NotImplementedException(); }
    }

    public override object[] GetCustomAttributes(Type attributeType, bool inherit)
    {
      var attrs = from a in _attributes where a.GetType() == attributeType select a;
      return attrs.ToArray();
    }

    public override object[] GetCustomAttributes(bool inherit)
    {
      return _attributes.ToArray();
    }

    public override bool IsDefined(Type attributeType, bool inherit)
    {
      throw new NotImplementedException();
    }

    public override string Name
    {
      get { return _alias; }
    }

    public override Type ReflectedType
    {
      get { throw new NotImplementedException(); }
    }


    internal List<Attribute> CustomAttributesInternal
    {
      get { return _attributes; }
    }

    internal string Alias
    {
      get { return _alias; }
    }

    internal string ActualName
    {
      get { return _name; }
    }

    internal Field Field
    {
      get { return _field; }
    }
  }
}