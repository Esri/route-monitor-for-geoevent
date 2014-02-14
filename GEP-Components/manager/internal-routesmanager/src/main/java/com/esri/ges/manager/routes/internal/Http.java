package com.esri.ges.manager.routes.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.esri.ges.datastore.agsconnection.KeyValue;

public class Http
{
  static final private Log log = LogFactory.getLog( Http.class );
  private DefaultHttpClient httpClient;
  
  Http()
  {
    httpClient = getThreadSafeClient();
  }
  
  private DefaultHttpClient getThreadSafeClient()
  {
    DefaultHttpClient client = new DefaultHttpClient();
    ClientConnectionManager mgr = client.getConnectionManager();
    
    HttpParams params = client.getParams();
    
    return new DefaultHttpClient( new PoolingClientConnectionManager( mgr.getSchemeRegistry() ), params );
  }
  
  public void shutdown()
  {
  }
  
  private String execute( HttpUriRequest request, int timeout ) throws IOException
  {
    if( timeout > 0 )
    {
      HttpParams params = new BasicHttpParams();
      HttpClientParams.setConnectionManagerTimeout( params, timeout );
      request.setParams( params );
    }
    return execute( request );
  }
  
  private String execute( HttpUriRequest request ) throws IOException
  {
    log.debug( "Executing following request: "+request.getRequestLine().toString() );
    HttpResponse response = httpClient.execute( request );
    HttpEntity entity = response.getEntity();
    String responseString = null;
    if( entity != null )
    {
      responseString = EntityUtils.toString( entity );
      log.debug( "Got response from http request: "+responseString );
    }
    
    StatusLine statusLine = response.getStatusLine();
    if( statusLine.getStatusCode() != HttpStatus.SC_OK )
    {
      String message = request.getURI().toString()+" : GET Request failed("+statusLine.toString()+")";
      log.error( message );
      return null;
    }
    
    
    return responseString;

  }
  
	String get( URL url, int timeout ) throws IOException
	{
		String content_type = "text/json";
		
		HttpGet httpget;
    try
    {
      httpget = new HttpGet( url.toURI() );
    }
    catch( URISyntaxException e )
    {
      throw new RuntimeException(e);
    }
		httpget.setHeader(HttpHeaders.CONTENT_TYPE, content_type);
		httpget.setHeader(HttpHeaders.ACCEPT, content_type);

		return execute( httpget, timeout );
	}
	
	String get( URL url, Collection<KeyValue> parameters, int timeout) throws IOException
	{
	  try
    {
      HttpGet httpGet = new HttpGet( url.toURI() );
      URI uri = httpGet.getURI();      
      if( parameters != null && parameters.size() > 0 )
      {
        StringBuffer newUriString = new StringBuffer();
        newUriString.append( uri.toString() );
        newUriString.append( '?' );
        boolean first = true;
        for( KeyValue keyValue : parameters )
        {
          if( first )
          {
            first = false;
          }
          else
          {
            newUriString.append( '&' );
          }
          newUriString.append( URLEncoder.encode( keyValue.getKey(), "UTF-8" ) );
          newUriString.append( '=' );
          newUriString.append( URLEncoder.encode( keyValue.getValue(), "UTF-8" ) );
        }
        try
        {
          httpGet.setURI( new URI( newUriString.toString() ) );
        }
        catch (URISyntaxException e)
        {
          throw new RuntimeException( e );
        }
      }
      
      return execute( httpGet, timeout );
    }
    catch( URISyntaxException e )
    {
      throw new RuntimeException(e);
    }
	}

	String post( URL url, Collection<KeyValue> parameters, int timeout) throws IOException
	{

	  List<NameValuePair> formParams = new ArrayList<NameValuePair>();
	  if( parameters != null )
	  {
	    for( KeyValue parameter : parameters )
	    {
	      formParams.add( new BasicNameValuePair( parameter.getKey(), parameter.getValue() ) );
	      log.debug( "Adding parameter ("+parameter.getKey()+"/"+parameter.getValue()+")");
	    }
	  }
	  UrlEncodedFormEntity entity = new UrlEncodedFormEntity( formParams, "UTF-8" );

	  HttpPost httpPost;
	  try
	  {
	    httpPost = new HttpPost( url.toURI() );
	  }
	  catch( URISyntaxException e )
	  {
	    throw new RuntimeException( e );
	  }
	  httpPost.setEntity( entity );
	  httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
	  httpPost.setHeader( "charset", "utf-8" );
	  
	  return execute( httpPost, timeout );
	}
	
	DefaultHttpClient getHttpClient()
	{
	  return httpClient;
	}
}
