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
package org.onebusaway.util.impl.configuration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class ConfigurationServiceClientFileImpl implements
		ConfigurationServiceClient {

	private static Logger _log = LoggerFactory
			.getLogger(ConfigurationServiceClientFileImpl.class);
	private HashMap<String, Object> _config = null;
	
	private boolean isLocal = true;
	
	private String configFile = "/var/lib/oba/config.json";

	public void setConfigFile(String file) {
		_log.info("config service override of configFile=" + file);
		configFile = file;
	}

	public ConfigurationServiceClientFileImpl() {
		if (System.getProperty("config.json") != null) {
			this.configFile = System.getProperty("config.json");
			_log.info("config service using system override configFile=" + this.configFile);
			return;
		}
		_log.info("using default configuration file=" + this.configFile);
	}
	
	public ConfigurationServiceClientFileImpl(String configFile) {
		if (System.getProperty("config.json") != null) {
			this.configFile = System.getProperty("config.json");
			_log.info("config service using system override configFile=" + this.configFile);
			return;
		}
		_log.info("config service using configFile=" + configFile);
		this.configFile = configFile;
	}
	
	@PostConstruct
	private void checkConfig(){
		if(getConfig() == null)
			isLocal = false;
	}
	
	
	@Override
	public URL buildUrl(String baseObject, String... params) throws Exception {
		// TODO Auto-generated method stub
		_log.info("empty buildUrl");
		return null;
	}

	@Override
	public void setConfigItem(String baseObject, String component, String key,
			String value) throws Exception {
		// TODO Auto-generated method stub
		_log.info("empty setConfigItem");
	}

	@Override
	public String log(String baseObject, String component, Integer priority,
			String message) {
		// TODO Auto-generated method stub
		_log.info("empty log");
		return null;
	}

	@Override
	public List<JsonObject> getItemsForRequest(String baseObject,
			String... params) throws Exception {
		// TODO Auto-generated method stub
		_log.info("empty getItemsForRequest");
		return null;
	}

	@Override
	public List<Map<String, String>> getItems(String baseObject,
			String... params) throws Exception {
		_log.debug("getItems(" + baseObject + ", " + params + ")");
		return (List<Map<String, String>>) getConfig().get("config");
	}

	@Override
	public String getItem(String component, String key) throws Exception {
		List<Map<String, String>> settings = getItems("config", "list");
		if (settings == null) return null;
		for (Map<String, String> setting : settings) {
		  if (component == null) {
		    if (setting.containsKey("key") && key.equals(setting.get("key"))) {
		      _log.debug("getItem(no-component)(" + component  + ", " + key + ")=" + setting.get("value"));
		      return setting.get("value");  
		    }
		  } else {
  			if ((setting.containsKey("component") && 
  					component.equals(setting.get("component"))) &&
  				setting.containsKey("key") && 
  				key.equals(setting.get("key"))) {
  			  _log.debug("getItem(" + component  + ", " + key + ")=" + setting.get("value"));
  				return setting.get("value");
  			}
		  }
		}
		return null;
	}

    private HashMap<String, Object> getConfig() {
		  if (_config != null) {
			  return _config;
		  }
		  
		    try {
		        String config = FileUtils.readFileToString(new File(configFile));
		        HashMap<String, Object> o = new ObjectMapper(new JsonFactory()).readValue(
		            config, new TypeReference<HashMap<String, Object>>() {
		            });
		        _config = o;
		      } catch (Exception e) {
		        _log.info("Failed to get configuration out of " + this.configFile + ", continuing without it.");

		      }
		      return _config;

	}

	public boolean isLocal() {
		return isLocal;
	}
	
}
