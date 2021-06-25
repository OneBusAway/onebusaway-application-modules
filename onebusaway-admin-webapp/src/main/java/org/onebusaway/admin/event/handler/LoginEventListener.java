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
package org.onebusaway.admin.event.handler;

import org.apache.log4j.Level;
import org.onebusaway.util.logging.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Listens to @{link AuthenticationSuccessEvent} and calls logging service to log successful login event
 * @author abelsare
 *
 */
public class LoginEventListener implements ApplicationListener<AuthenticationSuccessEvent>{

	private LoggingService loggingService;
	
	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		Object obj = event.getAuthentication().getPrincipal();
		if (obj instanceof  UserDetails) {
			UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
			String component = System.getProperty("admin.chefRole");
			String message = "User '" + userDetails.getUsername() + "' logged in";
			loggingService.log(component, Level.INFO, message);
			return;
		}
		if (obj instanceof  String) {
			String component = System.getProperty("admin.chefRole");
			String message = "User '" + obj + "' logged in";
			loggingService.log(component, Level.INFO, message);
			return;
		}
		throw new IllegalArgumentException("unexpected principal" + obj + " of type " + obj.getClass());
	}

	/**
	 * @param loggingService the loggingService to set
	 */
	@Autowired
	public void setLoggingService(LoggingService loggingService) {
		this.loggingService = loggingService;
	}
	

}
