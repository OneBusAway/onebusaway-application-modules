/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.webapp.actions.admin.usermanagement;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.onebusaway.admin.service.UserManagementService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * Creates a user in the database. 
 * @author abelsare
 *
 */
@AllowedMethods(value="createUser")
public class RegisterUserAction extends OneBusAwayNYCAdminActionSupport {

	private static final long serialVersionUID = 1L;
	private String username;
	private String password;
	private boolean admin;
	private String role;
	
	private UserManagementService userManagementService;
	
	/**
	 * Creates a new user in the system.
	 * @return success message
	 */
	public String createUser() {
		boolean valid = validateFields();
		if(valid) {
			boolean success = userManagementService.createUser(username, password, role);
			
			if(success) {
				addActionMessage("User '" +username + "' created successfully");
				return SUCCESS;
			} else {
				addActionError("Error creating user : '" +username + "'");
			}
		}
		
		return ERROR;

	}
	
	
	private boolean validateFields() {
		boolean valid = true;
		
		if(StringUtils.isBlank(username)) {
			valid = false;
			addFieldError("username", "User name is required");
		}
		
		if(StringUtils.isBlank(password)) {
			valid = false;
			addFieldError("password", "Password is required");
		}

		//only check role if admin is not set to true
		if(StringUtils.isBlank(role)) {
			valid = false;
			addFieldError("role", "Role is required");
		}
		
		return valid;
	}
	
	public void init() {
		createUser();
	}

	public List<String> getPossibleRoles() {
		return userManagementService.getManagedRoleNames();
	}

	/**
	 * Returns user name of the user being created
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Injects user name of the user being created
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username.trim();
	}

	/**
	 * Returns password of the user being created
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Injects password of the user being created
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Injects role of the user being created
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Returns role of the user being created
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Returns true if the user is created as admin
	 * @return the admin
	 */
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * Injects true if user is created as admin
	 * @param admin the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	/**
	 * @param userManagementService the userManagementService to set
	 */
	@Autowired
	public void setUserManagementService(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}
}