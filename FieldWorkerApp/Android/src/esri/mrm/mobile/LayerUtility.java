package esri.mrm.mobile;

import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.core.map.Field;

public class LayerUtility
{
  public static String getFieldNamebyAlias(ArcGISFeatureLayer layer, String alias)
  {
    String out = "";
    Field[] fields = layer.getFields();
    for(int i=0; i<fields.length; i++)
    {
      if(fields[i].getAlias().equals(alias))
        out = fields[i].getName();
    }
    return out;
  }
}
