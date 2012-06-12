/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdb;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cdb.exporters.ExporterEngine;
import pt.webdetails.cdb.connector.ConnectorEngine;
import pt.webdetails.cdb.query.QueryEngine;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.InvalidOperationException;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.olap.OlapUtils;
import pt.webdetails.cpf.persistence.PersistenceEngine;

/**
 *
 * @author pdpi
 */
public class CdbContentGenerator extends SimpleContentGenerator {

  private static final long serialVersionUID = 1L;
  
  private static Map<String, Method> exposedMethods = new HashMap<String, Method>();
  static{
    //to keep case-insensitive methods
    exposedMethods = getExposedMethods(CdbContentGenerator.class, true);
  }
  
  @Override
  protected Method getMethod(String methodName) throws NoSuchMethodException {
    Method method = exposedMethods.get(StringUtils.lowerCase(methodName) );
    if(method == null) {
      throw new NoSuchMethodException();
    }
    return method;
  }
  
  @Override
  public String getPluginName() {
    return "cdb";
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void home(OutputStream out) throws IOException {

    IParameterProvider requestParams = parameterProviders.get("request");
    IParameterProvider pathParams = parameterProviders.get("path");
    ServletRequestWrapper wrapper = (ServletRequestWrapper) pathParams.getParameter("httprequest");
    String root = wrapper.getServerName() + ":" + wrapper.getServerPort();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("solution", "system");
    params.put("path", "cdb/presentation/");
    params.put("file", "cdb.wcdf");
    params.put("absolute", "true");
    params.put("root", root);

    //add request parameters
    ServletRequest request = getRequest();
    @SuppressWarnings("unchecked")//should always be String
    Enumeration<String> originalParams = request.getParameterNames();
    // Iterate and put the values there
    while (originalParams.hasMoreElements()) {
      String originalParam = originalParams.nextElement();
      params.put(originalParam, request.getParameter(originalParam));
    }

    if (requestParams.hasParameter("mode") && requestParams.getStringParameter("mode", "Render").equals("edit")) {

      // Send this to CDE

      redirectToCdeEditor(out, params);
      return;

    }

    InterPluginCall pluginCall = new InterPluginCall(InterPluginCall.CDE, "Render", params);
    pluginCall.setResponse(getResponse());
    pluginCall.setOutputStream(out);
    pluginCall.run();

  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void storage(OutputStream out) throws IOException, InvalidOperationException {
    PersistenceEngine engine = PersistenceEngine.getInstance();
    writeOut(out, engine.process(getRequestParameters(), userSession));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void export(OutputStream out) {
    ExporterEngine engine = ExporterEngine.getInstance();
    engine.process(getRequestParameters(), getPathParameters(), out);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void doQuery(OutputStream out) throws IOException {
    IParameterProvider requestParams = getRequestParameters();
    
    String group = requestParams.getStringParameter("group", ""),
           id = requestParams.getStringParameter("id", ""),
           outputType = requestParams.getStringParameter("outputType", "json");
    
    writeOut(out, ExporterEngine.exportCda(group, id, outputType));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void connector(OutputStream out) throws IOException {
    ConnectorEngine engine = ConnectorEngine.getInstance();
    engine.process(getRequestParameters(), getPathParameters(), out);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void query(OutputStream out) throws IOException {
    QueryEngine engine = QueryEngine.getInstance();
    engine.process(getRequestParameters(), getPathParameters(), out);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void olapUtils(OutputStream out) throws IOException {
    OlapUtils utils = new OlapUtils();
    writeOut(out, utils.process(getRequestParameters()).toString());
  }
  
  private void redirectToCdeEditor(OutputStream out, Map<String, Object> params) throws IOException {
    
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append("../pentaho-cdf-dd/edit");
    if(params.size() > 0) urlBuilder.append("?");
    
    List<String> paramArray = new ArrayList<String>();
    for(String key : params.keySet()){
      Object value = params.get(key);
      if (value instanceof String) {
          paramArray.add(key + "=" + URLEncoder.encode((String) value, getEncoding()));
      }
    }

    urlBuilder.append(StringUtils.join(paramArray, "&"));
    redirect(urlBuilder.toString());
  }

//  private void redirectToCDE(OutputStream out, Map<String, Object> params) throws IOException {
//
//
//    StringBuilder str = new StringBuilder();
//
//    str.append("<html><head><title>Redirecting</title>");
//
//
//    str.append("<meta http-equiv=\"REFRESH\" content=\"0; url=../pentaho-cdf-dd/edit?");
//
//    List<String> paramArray = new ArrayList<String>();
//    for(String key : params.keySet()){
//      Object value = params.get(key);
//      if (value instanceof String) {
//        paramArray.add(key + "=" + URLEncoder.encode((String) value, "UTF-8"));
//      }
//
//    }
//
//    str.append(StringUtils.join(paramArray, "&"));
//
//    str.append("\"></head>");
//    str.append("<body>Redirecting</body></html>");
//    
//    writeOut(out, str.toString());
//
//    return;
//
//  }

}
