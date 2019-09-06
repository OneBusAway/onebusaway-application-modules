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

	private static final long serialVersionUID = 1L;
	private static Logger _log = LoggerFactory.getLogger(IndexAction.class);
	

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
		// Don't check permissions
		String username = _currentUserService.getCurrentUserAsUserIndex().getId().getValue();
		if (_temporaryPasswordService.isTemporaryPassword(username)) {
			HttpServletResponse response = ServletActionContext.getResponse();
			try {
				response.sendRedirect("/admin/usermanagement/update-password.action");
			} catch (IOException e) {
				_log.error(e.getMessage());
			}
		}
			
		return SUCCESS;
	}
}
