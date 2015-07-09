package org.onebusaway.util.services.configuration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.util.rest.RestApiLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface ConfigurationServiceClient {


  URL buildUrl(String baseObject, String... params) throws Exception;
    
  void setConfigItem(String baseObject, String component, String key, String value) 
		  throws Exception;
  
  String log(String baseObject, String component, Integer priority, String message);
  
  List<JsonObject> getItemsForRequest(String baseObject, String... params) throws Exception;
  
  /**
   * Convenience method. Note this assumes all values coming back from the service are strings.
   */
  List<Map<String, String>> getItems(String baseObject, String... params) throws Exception;
  
  String getItem(String baseObject, String key) throws Exception;
  
  boolean isLocal();

}
