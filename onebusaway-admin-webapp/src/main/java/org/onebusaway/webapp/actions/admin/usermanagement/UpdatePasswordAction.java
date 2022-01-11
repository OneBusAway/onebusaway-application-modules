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

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.onebusaway.admin.model.ui.UserDetail;
import org.onebusaway.admin.service.TemporaryPasswordService;
import org.onebusaway.admin.service.UserManagementService;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@AllowedMethods({"updatePassword"})
public class UpdatePasswordAction extends OneBusAwayNYCAdminActionSupport {

	private static final long serialVersionUID = 1L;

	private static Logger _log = LoggerFactory.getLogger(UpdatePasswordAction.class);
	private String newPassword;
	private String confirmPassword;

	@Autowired
	private UserManagementService _userManagementService;
	
	@Autowired
	private CurrentUserService _currentUserService;
	
	@Autowired
	private TemporaryPasswordService _temporaryPasswordService;
	
	public void updatePassword() throws Exception {
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/plain");
		
		if (! confirmPassword.equals(newPassword)) {
			_log.info("passwords do not match");
			response.getWriter().print(ERROR);
		}
		
		String username = _currentUserService.getCurrentUserAsUserIndex().getId().getValue();
		UserDetail detail = _userManagementService.getUserDetail(username);
		detail.setPassword(newPassword);
		_userManagementService.updateUser(detail);
		_temporaryPasswordService.setTemporaryPassword(username, false);
		
		response.getWriter().print(SUCCESS);
	}
	
	
	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	
}
