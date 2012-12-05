/**
 * Copyright (C) 2012 Kurt Raschke
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
package org.onebusaway.config_webapp.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.onebusaway.config_webapp.services.ConfigurationService;

/**
 *
 * @author kurt
 */
public class ConfigurationServiceImpl implements ConfigurationService {

    private ConcurrentMap<String, String> configurationMap = new ConcurrentHashMap<String, String>();

    @Override
    public String getConfigurationValueAsString(String configurationItemKey, String defaultValue) {
        if (configurationMap.containsKey(configurationItemKey)) {
            return configurationMap.get(configurationItemKey);
        } else {
            return defaultValue;
        }
    }

    @Override
    public Float getConfigurationValueAsFloat(String configurationItemKey, Float defaultValue) {
        if (configurationMap.containsKey(configurationItemKey)) {
            return new Float(configurationMap.get(configurationItemKey));
        } else {
            return new Float(defaultValue);
        }
    }

    @Override
    public Integer getConfigurationValueAsInteger(String configurationItemKey, Integer defaultValue) {
        if (configurationMap.containsKey(configurationItemKey)) {
            return new Integer(configurationMap.get(configurationItemKey));
        } else {
            return new Integer(defaultValue);
        }
    }

    @Override
    public void setConfigurationValue(String component, String configurationItemKey, String value) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getConfiguration() {
        return configurationMap;
    }

    public ConfigurationServiceImpl() {
    }
}
