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
package org.onebusaway.admin.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Translates keys used in TDM configuration to the keys used in admin parameters UI and vice versa.
 * This is required to hide the actual keys used in tdm configuration from client.
 * @author abelsare
 *
 */
public class ConfigurationKeyTranslator {

	private BiMap<String, String> configToUIKeys;
	private BiMap<String, String> uiToConfigKeys;
	
	public ConfigurationKeyTranslator() {
		//Holds mappings of config keys to ui keys. This map needs to be updated for each new key
		//introduced in tdm.config.xml if it needs to be displayed in admin parameters UI.
		configToUIKeys = HashBiMap.create();
		configToUIKeys.put("agency_1.hideScheduleInfo", "agency_1.hideScheduleInfo");
		configToUIKeys.put("admin.showVehicleStatus", "admin.showVehicleStatus");
		configToUIKeys.put("admin.agenciesExcludingScheduled", "admin.agenciesExcludingScheduled");

		//Create another map with inverse mapping that is ui to config keys
		uiToConfigKeys = configToUIKeys.inverse();
		
	}
	
	public String getUIKey(String configKey) {
		return configToUIKeys.get(configKey);
	}
	
	public String getConfigKey(String uiKey) {
		return uiToConfigKeys.get(uiKey);
	}

}
