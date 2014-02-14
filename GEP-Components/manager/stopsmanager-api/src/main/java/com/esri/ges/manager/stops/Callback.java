package com.esri.ges.manager.stops;

import java.util.ArrayList;
import java.util.Collection;

public class Callback
{
  private String path;
  private Collection<CallbackParameter> parameters;

  public String getPath() { return path; }
  public void setPath(String path) { this.path = path; }

  public Collection<CallbackParameter> getParameters() { return parameters; }
  public void setParameters(Collection<CallbackParameter> parameters) { this.parameters = parameters; }
  public void addParameter(CallbackParameter parameter)
  {
    if (parameter == null)
      return;

    if (parameters == null)
      parameters = new ArrayList<CallbackParameter>();

    parameters.add(parameter);
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("Callback(");
    sb.append(path);
    sb.append(",");
    if (parameters != null)
      for (CallbackParameter p : parameters)
        if (p != null)
          sb.append(" "+p.toString());
    sb.append(")");
    return sb.toString();
  }
}