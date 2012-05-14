/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdb.connector;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import pt.webdetails.cda.connections.Connection;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cpf.persistence.PersistenceEngine;
import pt.webdetails.cpf.repository.RepositoryUtils;

/**
 *
 * @author pdpi
 */
public class ConnectorEngine {

  static ConnectorEngine instance;
  private static final Log logger = LogFactory.getLog(ConnectorEngine.class);

  private ConnectorEngine() {
  }

  public static ConnectorEngine getInstance() {
    if (instance == null) {
      instance = new ConnectorEngine();
    }
    return instance;
  }

  protected Connector getConnector(String connectorName) throws ConnectorRuntimeException, ConnectorNotFoundException {
    try {
      Class connectorClass = Class.forName("pt.webdetails.cdb.connector." + connectorName + "Connector");
      return (Connector) connectorClass.getConstructor().newInstance();
    } catch (ClassNotFoundException e) {
      throw new ConnectorNotFoundException(e);
    } catch (Exception e) {
      throw new ConnectorRuntimeException(e);
    }
  }

  public String[] listConnectors() {
    String[] types = {"Saiku"};
    return types;
  }

  public static void exportCda(String groupId) {
    PersistenceEngine eng = PersistenceEngine.getInstance();
    try {

      Map<String,String> params = new HashMap<String,String>();
      params.put("group",groupId);
      params.put("user",PentahoSessionHolder.getSession().getName());
      JSONObject response = eng.query("select * from Query where group = :group and userid = :user",params);
      JSONArray queries = (JSONArray) response.get("object");
      CdaSettings cda = new CdaSettings(groupId, null);
      for (int i = 0; i < queries.length(); i++) {
        JSONObject query = queries.getJSONObject(i);
        String type = query.getString("type");
        try {
          Connector conn = ConnectorEngine.getInstance().getConnector(type);

          Connection connection = conn.exportCdaConnection(query);
          if (connection == null) {
            logger.error("Connection is null. Something is wrong with query "
                    + query.getString("name") + ". Ignoring.");
            continue;
          }
          cda.addConnection(connection);
          DataAccess dataAccess = conn.exportCdaDataAccess(query);
          cda.addDataAccess(dataAccess);
        } catch (ConnectorException e) {
          logger.error(e);
        }
      }
      RepositoryUtils.writeSolutionFile("cdb/queries", PentahoSessionHolder.getSession().getName() + "." + groupId + ".cda", cda.asXML());
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public void process(IParameterProvider requestParams, IParameterProvider pathParams, OutputStream out) {

    String method = requestParams.getStringParameter("method", "");
    if ("exportCda".equals(method)) {
      String group = requestParams.getStringParameter("group", "");
      exportCda(group);
    } else if ("copyQuery".equals(method)) {
      String id = requestParams.getStringParameter("id", ""),
              newGuid = requestParams.getStringParameter("newguid", "");
      copyQuery(id, newGuid);
    } else if ("deleteQuery".equals(method)) {
      String id = requestParams.getStringParameter("id", "");
      deleteQuery(id);
    } else {
      logger.error("Unsupported method");
    }
  }

  public void copyQuery(String id, String newGuid) {
    PersistenceEngine eng = PersistenceEngine.getInstance();
    try {

      Map<String,String> params = new HashMap<String,String>();
      params.put("id",id);
      JSONObject response = eng.query("select * from Query where @rid = :id",params);
      JSONObject query = (JSONObject) ((JSONArray) response.get("object")).get(0);
      String type = query.get("type").toString(),
              oldGuid = query.get("guid").toString();
      getConnector(type).copyQuery(oldGuid, newGuid);
    } catch (Exception e) {
      logger.error(e);
    }
  }

  public void deleteQuery(String id) {
    PersistenceEngine eng = PersistenceEngine.getInstance();
    try {
      Map<String,String> params = new HashMap<String,String>();
      params.put("id",id);
      JSONObject response = eng.query("select type from Query where @rid = :id",params);
      JSONObject query = (JSONObject) ((JSONArray) response.get("object")).get(0);
      String type = query.get("type").toString(),
              guid = query.get("guid").toString();
      getConnector(type).deleteQuery(guid);
    } catch (Exception e) {
      logger.error(e);
    }
  }

}
