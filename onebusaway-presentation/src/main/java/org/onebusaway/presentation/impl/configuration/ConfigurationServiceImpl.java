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
      @CacheableArgument(cacheRefreshIndicator = true) boolean forceRefresh, String contextPath) {

    Map<String, Object> config = new HashMap<String, Object>();

    for (ConfigurationSource source : _sources) {
      Map<String, Object> sourceConfig = source.getConfiguration(contextPath);
      config.putAll(sourceConfig);
    }

    return config;
  }

}
