/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdb.connector;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.connections.mondrian.JndiConnection;
import pt.webdetails.cda.connections.mondrian.MondrianJndiConnectionInfo;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.dataaccess.MdxDataAccess;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.RepositoryAccess;

/**
 *
 * @author pdpi
 */
public class SaikuConnector implements Connector {

  private static final Log logger = LogFactory.getLog(SaikuConnector.class);
  
  private static final String path = "cdb/saiku";

  @Override
  public DataAccess exportCdaDataAccess(JSONObject query) {
    String id, name, queryContent;
    try {
      id = query.getString("name");
      JSONObject definition = new JSONObject(query.getString("definition"));
      name = id;
      queryContent = definition.getString("query");
      DataAccess dataAccess = new MdxDataAccess(id, name, id, queryContent);
      //dataAccess;
      return dataAccess;
    } catch (JSONException e) {
      return null;
    }
  }

  @Override
  public Connection exportCdaConnection(JSONObject query) {
    String jndi, catalog, cube, id;
    try {
      id = query.getString("name");
      JSONObject definition = new JSONObject(query.getString("definition"));
      jndi = definition.getString("jndi");
      cube = definition.getString("cube");
      catalog = definition.getString("catalog");
      MondrianJndiConnectionInfo cinfo = new MondrianJndiConnectionInfo(jndi, catalog, cube);
      Connection conn = new JndiConnection(id, cinfo);
      return conn;
    } catch (JSONException e) {
      return null;
    }
  }

  @Override
  public void copyQuery(String oldGuid, String newGuid) {
    String newFileName = newGuid + ".saiku",
           oldFileName = oldGuid + ".saiku";
    try {
      RepositoryAccess.getRepository().copySolutionFile(Util.joinPath(path, oldFileName), Util.joinPath(path, newFileName));
    } catch (IOException e) {
      logger.error(e);
    }
  }

  @Override
  public void deleteQuery(String guid) {
    String fileName = guid + ".saiku";
    RepositoryAccess.getRepository().removeFile(path + "/" + fileName);
  }

}
