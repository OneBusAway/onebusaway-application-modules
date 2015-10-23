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
package org.onebusaway.webapp.actions.admin;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates API key for the user. Also authorizes the user to use API. 
 * @author abelsare
 *
 */
@Results({@Result(type = "redirectAction", name = "redirect", params = {
	     "actionName", "api-key"})})
public class ApiKeyAction extends NextActionSupport{

	private static final long serialVersionUID = 1L;
	private String key;
	private UserService userService;
	private UserPropertiesService userPropertiesService;
	
	/**
	 * Creates API key in the database
	 * @return success message
	 */
	public String createAPIKey() {
		UserIndexKey userIndexKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
	    UserIndex userIndex = userService.getOrCreateUserForIndexKey(userIndexKey,
	        key, false);
	    userPropertiesService.authorizeApi(userIndex.getUser(), 0);
	    addActionMessage("Key '" +key + "' created successfully");
		return SUCCESS;
	}
	
	/**
	 * Returns the key of the user being created
	 * @return the key
	 */
	//@RequiredStringValidator(message="API key is required")
	public String getKey() {
		return key;
	}
	
	/**
	 * Injects the key of the user being created
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Injects {@link UserService}
	 * @param userService the userService to set
	 */
	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Injects {@link UserPropertiesService}
	 * @param userPropertiesService the userPropertiesService to set
	 */
	@Autowired
	public void setUserPropertiesService(UserPropertiesService userPropertiesService) {
		this.userPropertiesService = userPropertiesService;
	}

}
