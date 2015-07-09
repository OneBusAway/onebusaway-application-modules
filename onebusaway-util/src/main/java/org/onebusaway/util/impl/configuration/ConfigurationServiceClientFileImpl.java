package org.onebusaway.util.impl.configuration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
	
	private String configFile = "/var/lib/obanyc/config.json";
	
	public ConfigurationServiceClientFileImpl() {}
	
	public ConfigurationServiceClientFileImpl(String configFile) {
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
		      _log.info("getItem(no-component)(" + component  + ", " + key + ")=" + setting.get("value"));
		      return setting.get("value");  
		    }
		  } else {
  			if ((setting.containsKey("component") && 
  					component.equals(setting.get("component"))) &&
  				setting.containsKey("key") && 
  				key.equals(setting.get("key"))) {
  			  _log.info("getItem(" + component  + ", " + key + ")=" + setting.get("value"));
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
		        _log.info("Failed to get an environment out of /var/lib/obanyc/config.json, continuing without it.");
		        
		      }
		      return _config;

	}

	public boolean isLocal() {
		return isLocal;
	}
	
}
