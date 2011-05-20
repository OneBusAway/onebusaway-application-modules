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
  public Map<String, Object> getConfiguration() {

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
