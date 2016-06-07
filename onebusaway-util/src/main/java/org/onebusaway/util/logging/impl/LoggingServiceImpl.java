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
package org.onebusaway.util.logging.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.onebusaway.util.logging.LoggingService;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link LoggingService}
 * @author abelsare
 *
 */
public class LoggingServiceImpl implements LoggingService {

	private ConfigurationServiceClient configurationServiceClient;
	
	private static final Logger log = LoggerFactory.getLogger(LoggingServiceImpl.class);
	
	@Override
	public String log(String component, Level priority, String message) {
		log.info("Starting to log message : {}", message );
		
		if(StringUtils.isBlank(message)) {
			throw new IllegalArgumentException("Message to log cannot be blank");
		}
		
		String responseText = configurationServiceClient.log("log", component, priority.toInt(), message);
		
		log.info("Returning response from server");
		
		return responseText;
	}

	/**
	 * Injects {@link ConfigurationServiceClient}
	 * @param configurationServiceClient the configurationServiceClient to set
	 */
	@Autowired
	public void setConfigurationServiceClient(
			ConfigurationServiceClient configurationServiceClient) {
		this.configurationServiceClient = configurationServiceClient;
	}

}
