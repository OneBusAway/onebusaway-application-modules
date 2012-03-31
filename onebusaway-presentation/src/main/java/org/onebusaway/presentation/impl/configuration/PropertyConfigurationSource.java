/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.presentation.impl.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.onebusaway.presentation.services.configuration.ConfigurationSource;

public class PropertyConfigurationSource implements ConfigurationSource {

  private Properties _properties;

  public void setProperties(Properties properties) {
    _properties = properties;
  }

  @Override
  public Map<String, Object> getConfiguration(String contextPath) {

    if (_properties == null || _properties.isEmpty())
      return Collections.emptyMap();

    Map<String, Object> config = new HashMap<String, Object>();

    for (Map.Entry<Object, Object> entry : _properties.entrySet()) {
      String key = entry.getKey().toString();
      Object value = entry.getValue();
      config.put(key, value);
    }

    return config;
  }
}
