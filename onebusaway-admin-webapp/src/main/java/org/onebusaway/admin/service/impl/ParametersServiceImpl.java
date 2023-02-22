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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.onebusaway.admin.service.ParametersService;
import org.onebusaway.admin.util.ConfigurationKeyTranslator;
import org.onebusaway.util.impl.configuration.ConfigurationServiceClientFileImpl;
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


		String[] agenciesExcludingScheduled = parameters.get("admin.agenciesExcludingScheduled").split(",");
		//TODO can't leave this hard-coded like this
		HashMap<String, String> displayNameToIdMap = new HashMap<>();
		displayNameToIdMap.put("WMATA", "1");
		displayNameToIdMap.put("DASH", "2");
		displayNameToIdMap.put("TestAgency", "3");

		ObjectMapper mapper = new ObjectMapper();
		try {
			ConfigFileStructure cfs = mapper.readValue(new File("/opt/wmata/oba/config.json"), ConfigFileStructure.class);
			for (Map.Entry<String,String> agency : displayNameToIdMap.entrySet()){
				cfs.agencies.get(agency.getValue()).hideScheduleInfo = false;

			}
			for(String agency : agenciesExcludingScheduled){
				String agencyId = displayNameToIdMap.get(agency);
				cfs.agencies.get(agencyId).hideScheduleInfo = true;

			}
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File("/opt/wmata/oba/config.json"), cfs);
			configurationService.setConfigurationValue("admin", "agenciesExcludingScheduled", parameters.get("admin.agenciesExcludingScheduled"));



//		for(Map.Entry<String, String> entry: parameters.entrySet()) {
//			String uiKey = entry.getKey();
//			String value = entry.getValue();
//			String configKey = keyTranslator.getConfigKey(uiKey);
//			if (configKey == null) return false;
//			String component = configKey.split("[.]")[0];
//			try {
//				configurationService.setConfigurationValue(component, configKey, value);
//			} catch (Exception e) {
//				saveSuccess = false;
//				e.printStackTrace();
//			}
//		}
		} catch (Exception e) {
			saveSuccess = false;
			e.printStackTrace();
		}
		
		return saveSuccess;
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> displayParameters = new HashMap<String, String>();

		ArrayList<String> agenciesExcludingScheduled = new ArrayList<>();
		//Get parameters from the file
		HashMap<String, ConfigurationServiceClientFileImpl.Agency> configParameters = (HashMap<String, ConfigurationServiceClientFileImpl.Agency>) configurationService.getConfigFromLocalFile();

		for(Map.Entry<String, ConfigurationServiceClientFileImpl.Agency> entry : configParameters.entrySet()) {
			if(entry.getValue().hideScheduleInfo){
				agenciesExcludingScheduled.add(entry.getValue().displayName);
			}
		}
		displayParameters.put("admin.agenciesExcludingScheduled", csl(agenciesExcludingScheduled));
		return displayParameters;
	}

	//Makes a Java list into a String that is a comma-separated list
	private String csl(ArrayList<String> input){
		String output = "";
		if (input.size() == 0){
			return "";
		}
		if (input.size() == 1){
			return input.get(0);
		}
		for(int i=0; i<input.size()-1;i++){
			output =  output + input.get(i) + ",";
		}
		output = output + input.get(input.size()-1);
		return output;
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

	public static class ConfigItem{
		public String component;
		public String key;
		public String value;

		public ConfigItem(String c, String k, String v){
			this.component = c;
			this.key = k;
			this.value = v;
		}
		public ConfigItem(){

		}
	}
	public static class ConfigFileStructure{
		public HashMap<String, String> oba;
		public HashMap<String, Agency> agencies;
		public ArrayList<ConfigItem> config;
	}
	public static class Agency{
		public String displayName;
		public boolean hideScheduleInfo;
	}




}
