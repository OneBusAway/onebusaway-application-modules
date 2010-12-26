package org.onebusaway.webapp.services;

import java.util.Map;

public interface ConfigurationService {
  public Map<String, Object> getConfiguration(boolean forceRefresh);
}
