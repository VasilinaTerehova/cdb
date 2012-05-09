/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdb;

import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISolutionRepositoryService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;

public class CdbLifeCycleListener implements IPluginLifecycleListener {

 static Log logger = LogFactory.getLog(CdbLifeCycleListener.class);
  

    private static IPentahoSession getAdminSession() {
        IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
        UserSession session = new UserSession("admin", null, false, null);
        GrantedAuthority[] auths = userDetailsRoleListService.getUserRoleListService().getAllAuthorities();
        Authentication auth = new AnonymousAuthenticationToken("admin", SecurityHelper.SESSION_PRINCIPAL, auths);
        session.setAttribute(SecurityHelper.SESSION_PRINCIPAL, auth);
        session.doStartupActions(null);
        return session;
    }
 
 
  
  @Override
  public void init() throws PluginLifecycleException {
      logger.debug("Init for CDB");
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    //Check if folder cdb and cdb/saiku and cdb/queries exist
    IPentahoSession adminSession = getAdminSession();
    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, adminSession);
    ISolutionRepositoryService repService = PentahoSystem.get(ISolutionRepositoryService.class, adminSession);
    if (!solutionRepository.resourceExists("cdb")) {
      try {
        repService.createFolder(adminSession, "", "", "cdb", "CDB");                      
      } catch (IOException ioe) {
        logger.error("Error while creating folder cdb for cdb plugin. CDB will not work", ioe);
      }
    }
    
    if (!solutionRepository.resourceExists("cdb/saiku")) {
      try {
        repService.createFolder(adminSession, "", "cdb", "saiku", "saiku");                
      } catch (IOException ioe) {
        logger.error("Error while creating folder cdb/saiku for cdb plugin. CDB will not work", ioe);
      }      
    }

    if (!solutionRepository.resourceExists("cdb/queries")) {
      try {
        repService.createFolder(adminSession, "", "cdb", "queries", "queries");                      
      } catch (IOException ioe) {
        logger.error("Error while creating folder cdb/queries for cdb plugin. CDB will not work", ioe);
      }      
    }
    
    
    
  }

  @Override
  public void unLoaded() throws PluginLifecycleException {
      logger.debug("Unload for CDB");
  }
  
}
