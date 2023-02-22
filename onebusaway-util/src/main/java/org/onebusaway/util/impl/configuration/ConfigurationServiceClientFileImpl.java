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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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

	private static long CACHE_TIME_MILLIS = 5 * 60 * 1000; // 5 mins
	private long lastCacheTime = 0;

	private HashMap<String, Object> cachedMergeConfig = null;

	// when populated merge values in from admin config service
	private String adminApiUrl;
	public void setAdminApiUrl(String url) {
		this.adminApiUrl = url;
	}

	private HashMap<String, Object> _config = null;
	// for unit tests
	public void setConfig(HashMap<String, Object> config) {
		_config = config;
	}
	
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
	public void setConfigItem(String baseObject, String component, String configurationKey,
			String value) throws Exception {
		String key = configurationKey.substring(configurationKey.indexOf(".")+1);

		try {
			ObjectMapper mapper = new ObjectMapper();
			ConfigFileStructure cfs = mapper.readValue(new File(configFile), ConfigFileStructure.class);
			boolean found = false;
			for (ConfigItem item : cfs.config) {
				if (item.component.equals(component) && item.key.equals(key)) {
					item.value = value;
					found = true;

				}
			}
			if(!found){
				_log.warn("Could not find an existing configuration item for " + component + ":" + key + " so one will be created");
				cfs.config.add(new ConfigItem(component, key, value));
			}
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(configFile), cfs);
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cfs));
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}

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
			  // setting is a map of keys "component", "key", and "value"
			  // corresponding to {"component": "admin", "key": "useTdm", "value": "false"},
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
			  return mergeConfig(_config);
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
		      return mergeConfig(_config);

	}
	// if configured, merge the staticConfig with dynamicConfig.  DynamicConfig
	// takes priority
	synchronized HashMap<String, Object> mergeConfig(HashMap<String, Object> staticConfig) {
		if (cachedMergeConfig == null || cacheExpired()) {
			HashMap<String, Object> dynamicContent = this.getConfigFromApi();
			if (dynamicContent == null || dynamicContent.isEmpty()) return staticConfig;
			cachedMergeConfig = mergeConfig(staticConfig, dynamicContent);
			lastCacheTime = System.currentTimeMillis();
		}
		return cachedMergeConfig;
	}

	private boolean cacheExpired() {
		if (System.currentTimeMillis() - lastCacheTime > CACHE_TIME_MILLIS) {
			return true;
		}
		return false;
	}

	HashMap<String, Object> mergeConfig(HashMap<String, Object> staticConfig,
																			HashMap<String, Object> dynamicConfig) {

		ArrayList<HashMap> mergedItems = new ArrayList<>();
		// only merge config elements
		ArrayList<HashMap> staticItems = (ArrayList<HashMap>) staticConfig.get("config");
		ArrayList<HashMap> dynamicItems = (ArrayList<HashMap>) dynamicConfig.get("config");
		for (HashMap<String, String> staticItem : staticItems) {
			HashMap<String, String> dynamicItem = getConfigItem(getItemKey(staticItem), dynamicItems);
			mergedItems.add(mergeComponent(staticItem, dynamicItem));
		}
		for (HashMap dynamicItem : dynamicItems) {
			mergedItems.add(mergeComponent(dynamicItem, null));
		}

		staticConfig.put("config", mergedItems);
		return staticConfig;
	}

	private HashMap<String, String> getConfigItem(String searchItemKey, ArrayList<HashMap> configItems) {
		if (configItems == null) return new HashMap<>();
		for (HashMap<String, String> configItem : configItems) {
			String itemKey = getItemKey(configItem);
			if (itemKey.equals(searchItemKey))
				return configItem;
		}
		return null;
	}

	String getItemKey(HashMap<String, String> configItem) {
		if (configItem == null) return null;
		return configItem.get("component") + "." + configItem.get("key");
	}

	private HashMap<String, String> mergeComponent(HashMap<String, String> staticMap, HashMap<String, String> dynamicMap) {
		if (dynamicMap == null) return staticMap;
		if (staticMap == null) return dynamicMap;

		String component = dynamicMap.get("component");
		String key = dynamicMap.get("key");
		String value = dynamicMap.get("value");

		staticMap.put("component", component);
		staticMap.put("key", key);
		staticMap.put("value", value);
		return staticMap;
	}

	@Override
	public HashMap<String, String> getConfigFromLocalFile() {
		try {
		ObjectMapper mapper = new ObjectMapper();
		ConfigFileStructure cfs = mapper.readValue(new File(configFile), ConfigFileStructure.class);
		return getConfigFromCFS(cfs);
		} catch (IOException e) {
			_log.error(e.getMessage());
			_log.error("problem converting file config file contents to config map");
		}
		return new HashMap<>();
	}

	private HashMap<String, String> getConfigFromCFS(ConfigFileStructure cfs){
			HashMap<String, String> config = new HashMap<>();
			for(ConfigItem item : cfs.config){
				config.put(item.component + "." + item.key, item.value);
			}
			return config;
	}

	public HashMap<String, Object> getConfigFromApi() {
		if (adminApiUrl == null || adminApiUrl.length() == 0)
			return null;

		InputStream in = null;

		URL url = null;
		String urlString = adminApiUrl;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		try {
			URLConnection urlConnection = url.openConnection();
			in = null;

			in = urlConnection.getInputStream();
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, Object> config = mapper.readValue(in, new TypeReference<HashMap<String, Object>>() {
			});
			return config;


		} catch (IOException ex) {
			_log.error("connection issue with url " + url + ", ex=" + ex);
			return new HashMap<>();
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException ex) {
				_log.error("error closing url stream " + url);
			}
		}

	}

	@Override
	public boolean isLocal() {
		return isLocal;
	}

	private static class ConfigItem{
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
	private static class ConfigFileStructure{
		public HashMap<String, String> oba;
		public ArrayList<ConfigurationServiceClientFileImpl.ConfigItem> config;
	}

}
