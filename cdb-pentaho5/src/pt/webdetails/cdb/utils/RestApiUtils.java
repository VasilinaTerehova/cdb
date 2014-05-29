/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdb.utils;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RestApiUtils {

  public static Map<String, Map<String, Object>> buildBloatedMap( HttpServletRequest request,
                                                                  HttpServletResponse response,
                                                                  HttpHeaders headers ) {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();

    mainMap.put( "request", buildRequestMap( request, headers ) );
    mainMap.put( "path", buildPathMap( request, response, headers ) );

    return mainMap;

  }

  protected static Map<String, Object> buildRequestMap( HttpServletRequest request, HttpHeaders headers ) {
    Map<String, Object> requestMap = new HashMap<String, Object>();
    Enumeration e = request.getParameterNames();
    while ( e.hasMoreElements() ) {
      Object o = e.nextElement();
      requestMap.put( o.toString(), request.getParameter( o.toString() ) );
    }
    Form form =
      ( (ContainerRequest) headers ).getFormParameters();
    Iterator<String> it = form.keySet().iterator();
    while ( it.hasNext() ) {
      String next = it.next();
      requestMap.put( next, form.get( next ).get( 0 ) );
    }

    return requestMap;
  }

  protected static Map<String, Object> buildPathMap( HttpServletRequest request, HttpServletResponse response,
                                                     HttpHeaders headers ) {
    Map<String, Object> pathMap = new HashMap<String, Object>();
    pathMap.put( "httprequest", request );
    pathMap.put( "httpresponse", response );
    if ( headers != null ) {
      pathMap.put( "contentType", headers.getRequestHeader( "contentType" ) );
    }
    return pathMap;
  }

  public static IParameterProvider getRequestParameters( Map<String, Map<String, Object>> bloatedMap ) {
    return new SimpleParameterProvider( bloatedMap.get( "request" ) );
  }

  public static IParameterProvider getPathParameters( Map<String, Map<String, Object>> bloatedMap ) {
    return new SimpleParameterProvider( bloatedMap.get( "path" ) );
  }

}
