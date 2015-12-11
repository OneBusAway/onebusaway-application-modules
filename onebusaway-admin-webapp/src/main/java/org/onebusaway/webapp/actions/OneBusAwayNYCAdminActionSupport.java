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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Basic action support required for actions.
 * @author abelsare
 *
 */
public class OneBusAwayNYCAdminActionSupport extends NextActionSupport {
	
	private static final long serialVersionUID = 1L;

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
		return currentUserService.getCurrentUserAsUserIndex().getUser();
	}
	
	public boolean hasRoleInList(List<String> roleList) {
		User user = getCurrentUserValue();
		Set<UserRole> roles = user.getRoles();
		for (UserRole role : roles)
			if (roleList.contains(role.getName()))
				return true;
		return false;
	}
	
	@Override
	public String execute() throws Exception {
		if (!hasPermissionsForPage())
			throw new Exception("Insufficient access");
		return SUCCESS;
	}
	
	@Autowired
	private ConfigurationServiceClient _configurationServiceClient;
	
	public boolean hasPermissionsForPage(String name) {
		try {
			String item = _configurationServiceClient.getItem("permission", name);
			List<String> roleList = Arrays.asList(item.split(","));
			return hasRoleInList(roleList);
		}
		catch(Exception e) {
			// If no permission is specified in config, assume its OK.
			return true;
		}
	}
	
	public boolean hasPermissionsForPage() {
		String name = this.getClass().getSimpleName();
		return hasPermissionsForPage(name);
	}

}
