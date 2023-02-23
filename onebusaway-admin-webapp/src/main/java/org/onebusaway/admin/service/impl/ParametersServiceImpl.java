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

import java.io.File;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.service.ParametersService;
import org.onebusaway.admin.util.ConfigurationKeyTranslator;
import org.onebusaway.util.impl.configuration.ConfigFileStructure;
import org.onebusaway.util.impl.configuration.ConfigParameter;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link ParametersService}
 * @author abelsare
 *
 */
@Component
public class ParametersServiceImpl implements ParametersService {

	private static Logger _log = LoggerFactory.getLogger(ParametersServiceImpl.class);
	private ConfigurationService configurationService; // todo not currently used
	private ConfigurationKeyTranslator keyTranslator; // todo not currently used
	private String configFile = "/var/lib/oba/config.json";
	public void setConfigFile(String config) {
		this.configFile = config;
	}
	
	@Override
	public boolean saveParameters(Map<String, List<ConfigParameter>> configParameters) {
		boolean saveSuccess = true;

		ObjectMapper mapper = new ObjectMapper();
		try {
			ConfigFileStructure cfs = mapper.readValue(new File(configFile), ConfigFileStructure.class);
			cfs.setAgencies(mergeParameters(cfs.getAgencies(), configParameters));
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(configFile), cfs);
		} catch (Exception e) {
			saveSuccess = false;
			_log.error("exception saveParams=", e, e);
		}
		
		return saveSuccess;
	}

	private Map<String, List<ConfigParameter>> mergeParameters(Map<String, List<ConfigParameter>> existing, Map<String, List<ConfigParameter>> updated) {
		if (existing == null) return updated;
		if (updated == null) return existing;

		for (String agencyKey : existing.keySet()) {
			List<ConfigParameter> merged = mergeConfigParameter(existing.get(agencyKey), updated.get(agencyKey));
			existing.put(agencyKey, merged);
		}
		for (String agencyKey : updated.keySet()) {
			List<ConfigParameter> merged = mergeConfigParameter(existing.get(agencyKey), null);
			existing.put(agencyKey, merged);
		}

		return existing;
	}

	private List<ConfigParameter> mergeConfigParameter(List<ConfigParameter> existing, List<ConfigParameter> updated) {
		if (existing == null || existing.isEmpty()) return updated;
		if (updated == null || updated.isEmpty()) return existing;

		for (ConfigParameter configParameter : existing) {
			ConfigParameter updatedParameter = findConfigParameter(updated, configParameter.getKey());
			if (updatedParameter != null) {
				configParameter.setValue(updatedParameter.getValue());
			}
		}

		for (ConfigParameter configParameter : updated) {
			ConfigParameter existingParameter = findConfigParameter(existing, configParameter.getKey());
			if (existingParameter == null) {
				existing.add(configParameter);
			}
		}

		return existing;
	}

	private ConfigParameter findConfigParameter(List<ConfigParameter> list, String searchKey) {
		for (ConfigParameter configParameter : list) {
			if (searchKey.equals(configParameter.getKey())) {
				return configParameter;
			}
		}

		return null;
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> displayParameters = new HashMap<String, String>();

		//Get parameters from the file
		try {
			ObjectMapper mapper = new ObjectMapper();

			ConfigFileStructure configFileStructure = mapper.readValue(new File(configFile), ConfigFileStructure.class);
			if (configFileStructure == null) {
				_log.warn("empty file, defaulting params");
				return displayParameters;
			}

			Map<String, List<ConfigParameter>> agencyParameters = configFileStructure.getAgencies();
			if (agencyParameters == null) return displayParameters;
			for (String agencyKey : agencyParameters.keySet()) {
				List<ConfigParameter> configParameters = agencyParameters.get(agencyKey);
				for (ConfigParameter configParameter : configParameters) {
					displayParameters.put(agencyKey + "_" + configParameter.getKey(),
									configParameter.getValue());
				}
			}

			return displayParameters;
		} catch(Exception e) {
			_log.error("exception with parameters: ", e, e);
			return displayParameters;
		}
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
