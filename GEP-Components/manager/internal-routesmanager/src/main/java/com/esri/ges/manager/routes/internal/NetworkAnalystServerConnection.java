package com.esri.ges.manager.routes.internal;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.esri.ges.datastore.agsconnection.KeyValue;
import com.esri.ges.datastore.agsconnection.Location;
import com.esri.ges.datastore.agsconnection.NamedGeometry;
import com.esri.ges.datastore.agsconnection.SolvedRoute;
import com.esri.ges.spatial.Geometry;
import com.esri.ges.spatial.GeometryException;
import com.esri.ges.spatial.Spatial;

public class NetworkAnalystServerConnection
{

  private static final Log log = LogFactory.getLog(NetworkAnalystServerConnection.class);
  final static Object[] geometryPath = new Object[] { "geometry" };
  private Spatial spatial;
  final static Object[] attributesPath = new Object[] { "attributes" };
  final static Object[] wkidPath = new Object[] {"spatialReference", "wkid" };
  private Http localhttp;
  private URL url;
  private static final int defaultTimeout = 30000;
  
  public NetworkAnalystServerConnection(Spatial spatial, URL url)
  {
    this.spatial = spatial;
    this.url = url;
  }

  public SolvedRoute solveRoute(String path, List<Location> locations, boolean optimize, Date startTime)
  {
    StringBuffer urlString = new StringBuffer();
    urlString.append( url.toExternalForm() );
    urlString.append( path );
    Collection<KeyValue> params = new ArrayList<KeyValue>();
    params.add( new KeyValue( "f", "json" ) );
    params.add( new KeyValue( "stops", convertToFeaturesJson( locations, false ) ) );
    params.add( new KeyValue( "ignoreInvalidLocations", "false") );
    params.add( new KeyValue( "returnRoutes", "true" ) );
    params.add( new KeyValue( "returnStops", "true" ) );
    params.add( new KeyValue( "outputLines", "esriNAOutputLineTrueShape" ) );
    params.add( new KeyValue( "preserveFirstStop", "true" ) );
    params.add( new KeyValue( "preserveLastStop", "true" ) );
    if(startTime != null)
      params.add( new KeyValue( "startTime", Long.toString(startTime.getTime()) ) );
    if( optimize )
    {
      params.add( new KeyValue( "findBestSequence", "true" ) );
    }
    else
    {
      params.add( new KeyValue( "findBestSequence", "false" ) );

    }
    try
    {
      URL url = new URL( urlString.toString() );
      localhttp = new Http();
      String reply = localhttp.post(url, params, defaultTimeout );
      
      if( reply != null )
      {
        return parseRouteSolverReply( reply );
      }
      log.error( "Did not get back a valid response from NA solve call." );
    }
    catch( Exception e )
    {
      log.error("Failed trying to send request to NA.", e );
    }
    return null;
  }
  
  private SolvedRoute parseRouteSolverReply(String reply)
  {
    SolvedRoute solvedRoute = null;
    ObjectMapper mapper = new ObjectMapper();
    try
    {
      JsonNode response = mapper.readTree( reply );
      List<Location> locations = processStopsFromReply( getNodeFollowingPath( response, new Object[] { "stops"} ) );
      List<NamedGeometry> shapes = getGeometriesFromNAReply( getNodeFollowingPath( response, new Object[] { "routes" } ) );
      solvedRoute = new SolvedRoute();
      solvedRoute.setLocations( locations );
      solvedRoute.setRoutes( shapes );
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
    return solvedRoute;
  }
  
  private List<NamedGeometry> getGeometriesFromNAReply( JsonNode jsonNode ) throws GeometryException
  {
    if( jsonNode == null )
    {
      log.error( "Could not find routes node in Json returned by Network Analyst" );
      return null;
    }
    int wkid  = getNodeFollowingPath( jsonNode, wkidPath ).getIntValue();
    String wkidStr = Integer.toString( wkid );
    String geometryString;
    Geometry geometryObject;
    List<NamedGeometry> retList = new ArrayList<NamedGeometry>();
    NamedGeometry newNamedGeometry;
    for( JsonNode feature : getNodeFollowingPath(jsonNode, new Object[] { "features" } ) )
    {
      geometryString = geometryStringFromJsonNode( getNodeFollowingPath( feature, geometryPath ), wkidStr );
      geometryObject = spatial.fromJson( geometryString );
      newNamedGeometry = new NamedGeometry( getNodeFollowingPath(feature,new Object[] { "attributes", "Name" }).asText(),
          geometryObject, true,
          getAttributesFromNode( getNodeFollowingPath( feature, attributesPath) ) );
      retList.add( newNamedGeometry );
    }
    return retList;
  }
  
  private String geometryStringFromJsonNode( JsonNode geometry, String outSR )
  {
    String geometryString = geometry.toString();
    return geometryString.substring(0, geometryString.length()-1) + ",\"spatialReference\":{\"wkid\":"+outSR+"}}";
  }
  
  private List<Location> processStopsFromReply(JsonNode jsonNode )
  {
    if( jsonNode == null )
    {
      log.error( "Could not find stops node in Json returned by Network Analyst" );
      return null;
    }
    int wkid  = getNodeFollowingPath( jsonNode, new Object[] { "spatialReference", "wkid" } ).getIntValue();
    ArrayList<Location> retLocations = new ArrayList<Location>(jsonNode.get( "features" ).size());
    JsonNode geometryNode;

    Location newLocation;
//    Integer sequence;
    for( JsonNode feature : jsonNode.get( "features" ) )
    {
      geometryNode = getNodeFollowingPath( feature, geometryPath );
      newLocation = new Location();
      newLocation.setPoint( spatial.createPoint( geometryNode.get( "x" ).asDouble(), geometryNode.get( "y" ).asDouble(), wkid ) );
      newLocation.setAttributes( getAttributesFromNode( getNodeFollowingPath( feature, attributesPath) ) );
//      sequence = (Integer)newLocation.getAttributes().get( "Sequence" );
      retLocations.add( newLocation );
    }
    Collections.sort(retLocations, new CustomComparator());
    return retLocations;
  }
  
  private Map<String, Object> getAttributesFromNode(JsonNode attributesNode )
  {
    Map<String,Object> retMap = new HashMap<String,Object>();
    Iterator<String> fieldNames = attributesNode.getFieldNames();
    String fieldName;
    JsonNode currNode;
    while( fieldNames.hasNext() )
    {
      fieldName = fieldNames.next();
      currNode = attributesNode.get( fieldName );
      retMap.put( fieldName, getObjectFromNode( currNode ) );
    }
    return retMap;
  }

  private Object getObjectFromNode(JsonNode node)
  {
    if( node.isInt() )
    {
      return node.asInt();
    }
    if( node.isTextual() )
    {
      return node.asText();
    }
    if( node.isBoolean() )
    {
      return node.asBoolean();
    }
    if( node.isDouble() )
    {
      return node.asDouble();
    }
    return null;
  }

  private JsonNode getNodeFollowingPath( JsonNode jsonNode, Object[] nodePath )
  {
    for( Object property : nodePath )
    {
      if( property instanceof String )
      {
        jsonNode = jsonNode.get( (String)property );
      }
      else if( property instanceof Integer )
      {
        Integer index = (Integer)property;
        jsonNode = jsonNode.get( index );
      }
      if( jsonNode == null )
      {
        break;
      }
    }
    return jsonNode;
  }
  
  private String convertToFeaturesJson( List<Location> locations, boolean addSequence )
  {
    StringBuffer sb = new StringBuffer();
    sb.append("{\"type\":\"features\",\"features\":[");
    Integer locationIndex=0;
    Map<String,Object> attributes;
    Map<String,Object> emptyHashMap = null;
    for( Location location : locations )
    {
      if( locationIndex != 0 )
      {
        sb.append( ',' );
      }
      locationIndex++;
      sb.append( "{\"geometry\":" );
      sb.append( removeZFromGeom( location.getPoint().toJson() ) );
      attributes = location.getAttributes();
      if( attributes == null )
      {
        if( emptyHashMap == null )
        {
          emptyHashMap = new HashMap<String,Object>();
        }
        attributes = emptyHashMap;
        attributes.put( "Sequence", locationIndex );
      }
      if( addSequence && attributes != emptyHashMap )
      {
        attributes.put( "Sequence", locationIndex );
      }
      sb.append( ",\"attributes\":{" );
      int index = 0;
      for( String key : attributes.keySet() )
      {
        if( index != 0 )
        {
          sb.append( ',' );
        }
        index++;
        sb.append( '"' );
        sb.append( key );
        sb.append( "\":");
        Object value = attributes.get( key );

        if( value == null )
        {
          sb.append( "null" );
        }
        else
        {
          boolean isString = value instanceof String;
          if( isString )
          {
            sb.append( '"' );
          }
          sb.append( value.toString() );
          if( isString )
          {
            sb.append( '"' );
          }
        }
      }
      sb.append( "}" );

      sb.append( '}' );
    }
    sb.append("]}");
    return sb.toString();
  }

  private String removeZFromGeom( String geomString )
  {
    geomString = new String( geomString );
    JsonFactory factory = new JsonFactory();
    ObjectMapper mapper = new ObjectMapper(factory);
    JsonParser parser;
    try
    {
      parser = factory.createJsonParser( geomString.getBytes() );
      TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
      HashMap<String,Object> o = mapper.readValue(parser, typeRef);
      if( o.containsKey( "z" ) )
      {
        o.remove( "z" );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue( baos, o );
        geomString = baos.toString();
      }
    }
    catch( Exception e )
    {
      throw new RuntimeException( e );
    }
    return geomString;
  }
  
  public class CustomComparator implements Comparator<Location> 
  {
    @Override
    public int compare(Location o1, Location o2) 
    {
      Integer seq1 = (Integer)o1.getAttributes().get( "Sequence" );
      Integer seq2 = (Integer)o2.getAttributes().get( "Sequence" );
      return seq1.compareTo(seq2);
    }
}
  
}
