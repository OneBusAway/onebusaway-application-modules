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
package org.onebusaway.util.services.configuration;

import java.net.URL;
import java.util.List;
import java.util.Map;

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
