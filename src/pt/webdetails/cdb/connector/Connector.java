/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdb.connector;

import org.json.JSONObject;
import pt.webdetails.cda.dataaccess.DataAccess;
import pt.webdetails.cda.connections.Connection;

/**
 *
 * @author pdpi
 */
public interface Connector {

  public DataAccess exportCdaDataAccess(JSONObject query);

  public Connection exportCdaConnection(JSONObject query);

  public void copyQuery(String oldGroup, String oldName, String oldPath, String newPath);

  public void moveQuery(String oldGroup, String oldName, String oldPath, String newPath);

  public void deleteQuery(String group, String name);

}
