package org.onebusaway.presentation.services.configuration;

import java.util.Map;

public interface ConfigurationService {
  public Map<String, Object> getConfiguration(boolean forceRefresh);
}
