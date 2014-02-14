package com.esri.ges.manager.stops;

import java.util.ArrayList;
import java.util.List;

public class CallbackParameter
{
  private String name;
  private String displayName;
  private List<Object> values;
  private Object defaultValue;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }

  public List<Object> getValues() { return values; }
  public void setValues(List<Object> values) { this.values = values; }
  public void addValue(Object value)
  {
    if (value == null)
      return;

    if (values == null)
      values = new ArrayList<Object>();

    values.add(value);
  }

  public Object getDefaultValue() { return defaultValue; }
  public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
  
  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("Parameter(");
    sb.append(name);
    sb.append(",");
    sb.append(displayName);
    sb.append(",[");
    if (values != null)
      for (Object v : values)
        if (v != null)
          sb.append(" "+v.toString());
    sb.append("],");
    sb.append(defaultValue);
    sb.append(")");
    return sb.toString();
  }

}
