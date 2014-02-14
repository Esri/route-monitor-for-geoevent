package com.esri.ges.manager.routemonitor.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;

import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.Spatial;
import com.esri.ges.util.DateUtil;

public class FeatureUtil
{
  final static private Log log = LogFactory.getLog( FeatureUtil.class );

  public static List<GeoEvent> convertFeaturesToGeoEvents(List<JsonNode> features, Spatial spatial, GeoEventCreator geoEventCreator, GeoEventDefinition geoEventDefinition)
  {
    List<GeoEvent> geoEvents = new ArrayList<GeoEvent>();
//    String objectIdFieldName = "objectid";
    if(features != null)
    {
      for(JsonNode tree:features)
      {
//        if( tree.has("objectIdFieldName") )
//          objectIdFieldName = tree.get("objectIdFieldName").asText();
        if( tree.has("spatialReference") )
        {
          JsonNode sr = tree.get("spatialReference");
          if( sr.has("wkid"))
            spatial.setWkid(sr.get("wkid").asInt());
        }
        // Unnecessary
//        if( tree.has("fields") && geoEventDefinition == null )
//        {
//          importGeoEventDefinition(tree.get("fields"));
//        }
        if( tree.has("features") )
        {
          for( JsonNode feature : tree.get("features") )
          {
            GeoEvent event = parseFeature(feature, geoEventCreator, geoEventDefinition, spatial);
            geoEvents.add(event);
          }
        }
        else if( tree.has("attributes") && tree.has("geometry") )
        {
          GeoEvent event = parseFeature(tree, geoEventCreator, geoEventDefinition, spatial);
          geoEvents.add(event);
        }
      }
    }
    else
    {
      log.warn("Query result is null");
    }
    
    return geoEvents;
  }
  
  private static GeoEvent parseFeature(JsonNode feature, GeoEventCreator geoEventCreator, GeoEventDefinition geoEventDefinition, Spatial spatial)
  {
    try
    {
      String geometryString = null;
      Object geoObj = feature.get("geometry");
      if(geoObj != null )
        geometryString = geoObj.toString();
      JsonNode attributes = feature.get("attributes");

      GeoEvent event = geoEventCreator.create(geoEventDefinition.getGuid());
      for( FieldDefinition fieldDef : geoEventDefinition.getFieldDefinitions() )
      {
        FieldType fieldType = fieldDef.getType();
        JsonNode field = attributes.get(fieldDef.getName());
        if( field == null )
          continue;
        switch(fieldType)
        {
          case String:
            event.setField(fieldDef.getName(), field.asText());
            break;
          case Boolean:
            event.setField(fieldDef.getName(), new Boolean(field.asBoolean()));
            break;
          case Date:
            if(field.isTextual())
            {
//              if( customDateFormat == null || customDateFormat.isEmpty() )
                event.setField(fieldDef.getName(), DateUtil.convert(field.asText()));
//              else
//              {
//                try
//                {
//                  if( customDateParser != null )
//                    event.setField(fieldDef.getName(), customDateParser.parse(field.asText()));
//                } catch (ParseException e)
//                {
//                  event.setField(fieldDef.getName(), DateUtil.convert(field.asText()));
//                  LOG.error("Failed to parse the time value " + field.asText() + " using the custom format.", e);
//                }
//                event.setField(fieldDef.getName(), null);
//              }
            }
            else if(field.isLong())
              event.setField(fieldDef.getName(), DateUtil.convert(String.valueOf(field.asLong())));
            else if(field.isInt())
              event.setField(fieldDef.getName(), DateUtil.convert(String.valueOf(field.asInt())));
            break;
          case Double:
            event.setField(fieldDef.getName(), new Double(field.asDouble()));
            break;
          case Float:
            event.setField(fieldDef.getName(), new Float(field.asDouble()));
            break;
          case Integer:
            event.setField(fieldDef.getName(), new Integer(field.asInt()));
            break;
          case Long:
            event.setField(fieldDef.getName(), new Long(field.asLong()));
            break;
          case Short:
            event.setField(fieldDef.getName(), new Short((short)field.asInt()));
            break;
          case Geometry:
            event.setField(fieldDef.getName(), null);
            break;
          case Group:
            event.setField(fieldDef.getName(), null);
            break;
          default:
            break;
        }
      }
      if(geometryString != null)
      {
        Geometry geometry = null;
        geometry = spatial.fromJson(geometryString);
        event.setGeometry(geometry);
      }
      return event;
    } catch (GeometryException e)
    {
      log.error(e);
      return null;
    } catch (FieldException e)
    {
      log.error(e);
      return null;
    } catch (MessagingException e)
    {
      log.error(e);
      return null;
    }

  }
}
