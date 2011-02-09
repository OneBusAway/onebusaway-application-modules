package org.onebusaway.presentation.services.configuration;

import java.util.Map;

public interface ConfigurationSource {
  public Map<String, Object> getConfiguration();
}
