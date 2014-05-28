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

package pt.webdetails.cdb;


import org.apache.commons.io.IOUtils;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.plugin.CorePlugin;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.plugincall.base.CallParameters;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class InterPluginBroker {

  public static void run( Map<String, Object> params, OutputStream out ) throws Exception {
    CallParameters parameters = new CallParameters();
    Iterator<String> it = params.keySet().iterator();
    while ( it.hasNext() ) {
      String key = it.next();
      Object value = params.get( key );
      parameters.put( key, value.toString() );
    }
    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDE.getId(), "renderer", "render" );
    String returnVal = pluginCall.call( parameters.getParameters() );
    IOUtils.write( returnVal, out );
    out.flush();
  }

  public static String exportCda(  String group, String id, String outputType  ) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put( "path", "public/cdb/queries/" + group + ".cda" );
    params.put( "dataAccessId", id );
    params.put( "outputType", outputType );

    InterPluginCall pluginCall = new InterPluginCall( InterPluginCall.CDA, "doQueryInterPlugin", params );
    return pluginCall.call();
  }

}
