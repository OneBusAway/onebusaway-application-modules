package org.onebusaway.presentation.impl.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.cache.CacheableArgument;
import org.onebusaway.presentation.services.configuration.ConfigurationService;
import org.onebusaway.presentation.services.configuration.ConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ConfigurationServiceImpl implements ConfigurationService {

  private List<ConfigurationSource> _sources;

  @Autowired
  public void setSources(List<ConfigurationSource> sources) {
    _sources = sources;
  }

  @Override
  @Cacheable
  public Map<String, Object> getConfiguration(
      @CacheableArgument(cacheRefreshIndicator = true) boolean forceRefresh) {

    Map<String, Object> config = new HashMap<String, Object>();

    for (ConfigurationSource source : _sources) {
      Map<String, Object> sourceConfig = source.getConfiguration();
      config.putAll(sourceConfig);
    }

    return config;
  }

}
