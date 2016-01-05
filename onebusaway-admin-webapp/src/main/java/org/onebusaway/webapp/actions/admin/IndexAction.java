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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.onebusaway.admin.service.TemporaryPasswordService;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;


public class IndexAction extends OneBusAwayNYCAdminActionSupport {

	@Autowired
	private ConfigurationServiceClient _configurationServiceClient;
	
	private static final long serialVersionUID = 1L;
	private static Logger _log = LoggerFactory.getLogger(IndexAction.class);
	
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
	
	public boolean isPageAvailable(String key, String actionName) {
		return getConfig(key) && hasPrivilegeForPage(actionName);
	}
	
	public String getName() {
		UserRole user =  getCurrentUserValue().getRoles().iterator().next(); // use first
		String name = user.getName().split("_")[1];
		return StringUtils.capitalize(name.toLowerCase());
	}
	
	@Autowired
	private TemporaryPasswordService _temporaryPasswordService;
	
	@Autowired
	private CurrentUserService _currentUserService;
	
	public String execute() {
		super.execute();
		String username = _currentUserService.getCurrentUserAsUserIndex().getId().getValue();
		if (_temporaryPasswordService.isTemporaryPassword(username)) {
			HttpServletResponse response = ServletActionContext.getResponse();
			try {
				response.sendRedirect("usermanagement/update-password.action");
			} catch (IOException e) {
				_log.error(e.getMessage());
			}
		}
			
		return SUCCESS;
	}
}
