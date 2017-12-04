/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.webapp.actions;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.admin.service.AccessControlService;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Basic action support required for actions.
 * @author abelsare
 *
 */
public class OneBusAwayNYCAdminActionSupport extends NextActionSupport {
	
	  private static final long serialVersionUID = 1L;

	  private static Logger _log = LoggerFactory.getLogger(OneBusAwayNYCAdminActionSupport.class);

  	  @Autowired
	  private ConfigurationServiceClient _configurationServiceClient;


	  private Date time = null;
	  
	  public void setTime(Date time) {
	    this.time = time;
	  }
	  
	  public Date getTime() {
	    if(time != null) {
	      return time;
	    } else {
	      return new Date();
	    }
	  }
	  
	 protected CurrentUserService currentUserService;
	 
	 protected UserBean getCurrentUser() {
		 UserBean user = currentUserService.getCurrentUser();
		 if (user == null)
			 user = currentUserService.getAnonymousUser();
		 return user;
	 }
	 
	 /**
	  * Checks if the current user is an anonymous user
	  * @return true if user is an anonymous user
	  */
	 public boolean isAnonymousUser() {
		 return currentUserService.isCurrentUserAnonymous();
	 }
	 
	 /**
	  * Checks if the current user is an admin user
	  * @return true if user is an admin user
	  */
	 public boolean isAdminUser() {
		 return currentUserService.isCurrentUserAdmin();
	 }
	 
	/**
	 * Injects current user service
	 * @param currentUserService the currentUserService to set
	 */
	@Autowired
	public void setCurrentUserService(CurrentUserService currentUserService) {
		this.currentUserService = currentUserService;
	}
	
	public void setSession(Map<String, Object> session) {
	    _session = session;
	}
	
	protected User getCurrentUserValue() {
		UserIndex idx = currentUserService.getCurrentUserAsUserIndex();
		return (idx == null) ? null : idx.getUser();
	} 
	
	@Override
	public String execute() {
		if (!hasPrivilegeForPage()) {
			throw new RuntimeException("Insufficient access to action " + this.getClass().getName());
		}
		return SUCCESS;
	}
	
	@Autowired
	private AccessControlService _accessControlService;

	public boolean isPageAvailable(String key, String actionName) {
	    return getConfig(key) && hasPrivilegeForPage(actionName);
	}

	public boolean hasPrivilegeForPage(String privilege) {
	  boolean authorized = _accessControlService.currentUserHasPrivilege(privilege);
	  if (!authorized)
	    _log.warn("Auth failed for " + privilege);
		return authorized;
	}
	
	public boolean hasPrivilegeForPage() {
	  boolean authorized = hasPrivilegeForPage(this.getClass().getName());
	  if (!authorized)
	    _log.warn("Auth failed for " + this.getClass().getName());
		return authorized;
	}
	public boolean getConfig(String partialKey) {
		_log.debug("partialKey=" + partialKey);
		boolean result = true; // default is to show feature
		try {
			List<Map<String, String>> components = _configurationServiceClient.getItems("api", "config", "list");
			if (components == null) {
				_log.debug("getItems call failed");
				return result;
			}
			for (Map<String, String> component: components) {
                _log.debug("component=" + component);
				if (component.containsKey("component") && "admin".equals(component.get("component"))) {
                    _log.debug("found admin component");
					if (partialKey.equals(component.get("key"))) {
                        _log.debug("found key=" + partialKey + ", and value=" + component.get("value"));
						return "true".equalsIgnoreCase(component.get("value"));
					}
				}
			}
		} catch (Exception e) {
			_log.error("config query broke:", e);
		}
		return result;
	}

}
