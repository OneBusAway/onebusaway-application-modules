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
import java.util.Map;

import org.onebusaway.admin.service.AccessControlService;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.CurrentUserService;
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
	
	public boolean hasPrivilegeForPage(String privilege) {
		return _accessControlService.currentUserHasPrivilege(privilege);
	}
	
	public boolean hasPrivilegeForPage() {
		return hasPrivilegeForPage(this.getClass().getName());
	}

}
