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

import java.util.Map;

/**
 * Service interface for getting configuration parameters from a centrally-distributed
 * configuration service.
 * 
 * @author jmaki
 */
public interface ConfigurationService {

  /**
   * Get a value for the given configuration key as a string.
   * 
   * @param configurationItemKey The configuration item key.
   * @param defaultValue The value to return if a value for the configurationItemKey 
   * 		  is not found.
   */
  public String getConfigurationValueAsString(String configurationItemKey,
      String defaultValue);

  public Float getConfigurationValueAsFloat(String configurationItemKey,
      Float defaultValue);
  
  public Integer getConfigurationValueAsInteger(String configurationItemKey,
	      Integer defaultValue);

  public Boolean getConfigurationValueAsBoolean(String configurationItemKey,
                                                Boolean defaultValue);

  public Double getConfigurationValueAsDouble(String configurationItemKey, 
		  Double defaultValue);
  
  /**
   * Set a value for the given configuration key as a string.
   * 
   * @param component The component to which this key value pair belongs
   * @param configurationItemKey The configuration item key.
   * @param value The value to set the configuration param to.
   */  
  public void setConfigurationValue(String component, String configurationItemKey, 
		  String value) throws Exception;
  
  
  
  /**
   * Get a collection of all key value pairs stored in tdm_config.xml on TDM server
   * @return collection of all config key value pairs
   */
  public Map<String, String> getConfiguration();
}
