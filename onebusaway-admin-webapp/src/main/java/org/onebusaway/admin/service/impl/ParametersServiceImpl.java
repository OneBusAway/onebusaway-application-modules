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
import java.util.*;

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

	//TODO wire this instead of hard-coding
	private String configFile = "/opt/wmata/oba/config.json";
	
	@Override
	public boolean saveParameters(Map<String, String> parameters) {
		boolean saveSuccess = true;

		HashSet agenciesExcludingScheduled = new HashSet(List.of(parameters.get("admin.agenciesExcludingScheduled").split(",")));

		ObjectMapper mapper = new ObjectMapper();
		try {
			ConfigFileStructure cfs = mapper.readValue(new File(configFile), ConfigFileStructure.class);
			for (Map.Entry<String,Agency> agency : cfs.agencies.entrySet()){
				if(agenciesExcludingScheduled.contains(agency.getValue().displayName)){
					agency.getValue().hideScheduleInfo = true;
				}else{
					agency.getValue().hideScheduleInfo = false;
				}

			}
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(configFile), cfs);
			configurationService.setConfigurationValue("admin", "agenciesExcludingScheduled", parameters.get("admin.agenciesExcludingScheduled"));

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
		try {
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, Agency> configParameters = mapper.readValue(new File(configFile), ConfigFileStructure.class).agencies;

			for (Map.Entry<String, Agency> entry : configParameters.entrySet()) {
				if (entry.getValue().hideScheduleInfo) {
					agenciesExcludingScheduled.add(entry.getValue().displayName);
				}
			}
			displayParameters.put("admin.agenciesExcludingScheduled", csl(agenciesExcludingScheduled));
			return displayParameters;
		}catch(Exception e){
			e.printStackTrace();
			//TODO better error reporting here. ParametersService could use a logger
			return null;
		}
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
