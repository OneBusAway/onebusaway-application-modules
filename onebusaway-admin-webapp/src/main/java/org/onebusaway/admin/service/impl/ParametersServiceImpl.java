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
package org.onebusaway.admin.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.admin.service.ParametersService;
import org.onebusaway.admin.util.ConfigurationKeyTranslator;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link ParametersService}
 * @author abelsare
 *
 */
@Component
public class ParametersServiceImpl implements ParametersService {

	private ConfigurationService configurationService;
	private ConfigurationKeyTranslator keyTranslator;
	
	@Override
	public boolean saveParameters(Map<String, String> parameters) {
		boolean saveSuccess = true;
		
		for(Map.Entry<String, String> entry: parameters.entrySet()) {
			String uiKey = entry.getKey();
			String value = entry.getValue();
			String configKey = keyTranslator.getConfigKey(uiKey);
			String component = configKey.split("[.]")[0];
			try {
				configurationService.setConfigurationValue(component, configKey, value);
			} catch (Exception e) {
				saveSuccess = false;
				e.printStackTrace();
			}
		}
		
		return saveSuccess;
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> displayParameters = new HashMap<String, String>(); 
		
		//Get config values from TDM
		Map<String, String> configParameters = configurationService.getConfiguration();
		
		for(Map.Entry<String, String> entry : configParameters.entrySet()) {
			String configKey = entry.getKey();
			String configValue = entry.getValue();
			//Translate config key to its UI counterpart. 
			String uiKey = keyTranslator.getUIKey(configKey);
			if(StringUtils.isNotBlank(uiKey)) {
				//uikey can be null if there is a mismatch between
				//the keys on TDM and admin UI
				displayParameters.put(uiKey, configValue);
			}
		}
		
		return displayParameters;
	}

	/**
	 * Injects configuration service
	 * @param configurationService the configurationService to set
	 */
	@Autowired
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/**
	 * @param keyTranslator the keyTranslator to set
	 */
	@Autowired
	public void setKeyTranslator(ConfigurationKeyTranslator keyTranslator) {
		this.keyTranslator = keyTranslator;
	}


}
