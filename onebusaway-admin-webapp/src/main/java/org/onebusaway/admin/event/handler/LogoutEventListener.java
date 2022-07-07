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

import org.apache.logging.log4j.Level;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.util.logging.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

/**
 * Calls {@link LoggingService} to log 'logout' event 
 * @author abelsare
 *
 */
public class LogoutEventListener implements ApplicationListener<HttpSessionDestroyedEvent> {
	
	private LoggingService loggingService;

	@Override
	public void onApplicationEvent(HttpSessionDestroyedEvent event) {
		SecurityContext securityContext = (SecurityContext) event.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
		if (securityContext != null && securityContext.getAuthentication() != null
		    && securityContext.getAuthentication().getPrincipal() != null) {
		  IndexedUserDetails userDetails = (IndexedUserDetails) securityContext.getAuthentication().getPrincipal();
		  String component = System.getProperty("admin.chefRole");
		  String message = "User '" + userDetails.getUsername() + "' logged out";
		  loggingService.log(component, Level.INFO, message);
		}
	}
	
	/**
	 * @param loggingSevice the loggingSevice to set
	 */
	@Autowired
	public void setLoggingService(LoggingService loggingService) {
		this.loggingService = loggingService;
	}
}
