using System;
using System.Collections.Generic;
using System.Linq;
using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Tasks;
using FieldWorker.Common;

namespace FieldWorker.ViewModels
{
  public class FeatureLayerHelper
  {
    public FeatureLayer FeatureLayer = null;
    public List<Field>  Fields       = null;

    public FeatureLayerHelper(Map map, string name, FeatureLayer.QueryMode mode, bool visible)
    {
      this.FeatureLayer = (FeatureLayer)map.Layers.FirstOrDefault(layer => (layer is FeatureLayer) && ((FeatureLayer)layer).DisplayName.ToLower().Equals(name.ToLower()));
      if (this.FeatureLayer == null)
      {
        Log.Trace("Could not find layer with name '" + name + "'");
        return;
      }

      this.FeatureLayer.Visible  = visible;
      this.FeatureLayer.Mode     = mode;
      this.FeatureLayer.AutoSave = false;

      this.Fields = QueryLayerFields(this.FeatureLayer);
      if (this.Fields == null)
        Log.Trace("Could not query layer fields '" + name + "'");

      this.FeatureLayer.Initialized += FeatureLayer_Initialized;
      this.FeatureLayer.Initialize();

      this.FeatureLayer.BeginSaveEdits += FeatureLayer_BeginSaveEdits;
      this.FeatureLayer.EndSaveEdits += FeatureLayer_EndSaveEdits;
      this.FeatureLayer.UpdateCompleted += FeatureLayer_UpdateCompleted;
      this.FeatureLayer.UpdateFailed += FeatureLayer_UpdateFailed;
      this.FeatureLayer.SaveEditsFailed += FeatureLayer_SaveEditsFailed;
    }

    private void FeatureLayer_Initialized(object sender, EventArgs e)
    {
      this.Fields = this.FeatureLayer.LayerInfo.Fields;
      //TODO - redesign this
    }

    private void FeatureLayer_BeginSaveEdits(object sender, BeginEditEventArgs e)
    {
    }

    private void FeatureLayer_SaveEditsFailed(object sender, TaskFailedEventArgs e)
    {
    }

    private void FeatureLayer_UpdateFailed(object sender, TaskFailedEventArgs e)
    {
    }

    private void FeatureLayer_UpdateCompleted(object sender, EventArgs e)
    {
    }

    private void FeatureLayer_EndSaveEdits(object sender, EndEditEventArgs e) 
    {
    }

    public string Where
    {
      set
      {
        if (this.FeatureLayer == null)
          return;

        this.FeatureLayer.Where = value;
      }
      get
      {
        if (this.FeatureLayer == null)
          return null;

        return this.FeatureLayer.Where;
      }
    }

    public string Url
    {
      set
      {
        if (this.FeatureLayer == null)
          return;

        this.FeatureLayer.Url = value;
      }
      get
      {
        if (this.FeatureLayer == null)
          return null;

        return this.FeatureLayer.Url;
      }
    }

    public bool SaveEdits()
    {
      if (this.FeatureLayer == null)
        return false;

      this.FeatureLayer.SaveEdits();
      return true;
    }

    public bool Update()
    {
      if (this.FeatureLayer == null)
        return false;
      if (this.FeatureLayer.HasEdits)
        return false;

      this.FeatureLayer.Update();
      return true;
    }

    public string FieldAliasToName(string alias)
    {
      if (this.Fields == null)
        return null;

      return FieldAliasToName(this.Fields, alias);
    }

    static public string FieldAliasToName(List<Field> fields, string alias)
    {
      if (fields == null)
        return null;

      Field field = fields.FirstOrDefault(fld => fld.Alias.Equals(alias));
      return field != null ? field.Name : null;

      /*
      foreach (Field field in fields)
      {
        if (field.Alias.Equals(alias))
          return field.Name;
      }
      return null;
      */
    }

    public Graphic FindFeatureByOID(Graphic graphic)
    {
      string oidFieldName = this.FeatureLayer.LayerInfo.ObjectIdField;

      string myOID = graphic.Attributes[oidFieldName].ToString();

      foreach (Graphic feature in this.FeatureLayer.Graphics)
      {
        string featureOID = feature.Attributes[oidFieldName].ToString();
        if (featureOID.Equals(myOID))
          return feature;
      }

      return null;
    }

    private List<Field> QueryLayerFields(FeatureLayer featureLayer)
    {
      if (featureLayer == null || featureLayer.Url == null)
        return null;

      try
      {
        QueryTask queryTask = new QueryTask(featureLayer.Url);
        Query query = new Query();
        query.Where = "1=1";
        query.ReturnGeometry = false;
        query.OutFields.AddRange(new string[] { "*" });
        queryTask.Execute(query);
        if (queryTask.LastResult != null)
          return queryTask.LastResult.Fields;
      }
      catch (Exception ex)
      {
        Log.TraceException("Failed querying layer '" + featureLayer.Url + "'", ex);
        return null;
      }

      return null;
    }

    public void AddFeature(Graphic feature, bool bClone)
    {
      Graphic newFeature = feature;

      if (bClone)
        newFeature = Helper.CloneGraphic(feature);

      // add the graphic
      this.FeatureLayer.Graphics.Add(newFeature);
    }

  }
}
