/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.impl.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.onebusaway.nextbus.service.cache.CacheService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.StopBean;
import org.springframework.stereotype.Component;

@Component
public class CacheServiceImpl implements CacheService {
  
  private ScheduledExecutorService _executor;
  

  private Map<String, AgencyBean> _validAgencyCache = new ConcurrentHashMap<String, AgencyBean>();
  private Map<String, StopBean> _validStopCache = new ConcurrentHashMap<String, StopBean>();
  private Map<String, Boolean> _invalidStopCache = new ConcurrentHashMap<String, Boolean>();
  private Map<String, Boolean> _validRouteCache = new ConcurrentHashMap<String, Boolean>();
  
  @PostConstruct
  public void start() throws Exception {
      _executor = Executors.newSingleThreadScheduledExecutor();
      // re-build internal route cache once a day
      _executor.scheduleAtFixedRate(new RefreshDataTask(), 0, 24, TimeUnit.HOURS);
  }
  
  public void clearCache() {
    _validAgencyCache.clear();
    _validStopCache.clear();
    _invalidStopCache.clear();
    _validRouteCache.clear();
  }
  
  
  private class RefreshDataTask implements Runnable {

    @Override
    public void run() {
      clearCache();
    }
  }


  @Override
  public AgencyBean getAgency(String key) {
    return _validAgencyCache.get(key);
  }

  @Override
  public void putAgency(String key, AgencyBean value) {
    _validAgencyCache.put(key, value);
  }

  @Override
  public Boolean getRoute(String key) {
    return _validRouteCache.get(key);
  }

  @Override
  public void putRoute(String key, Boolean value) {
    _validRouteCache.put(key, value);
  }

  @Override
  public StopBean getStop(String key) {
    return _validStopCache.get(key);
  }

  @Override
  public void putStop(String key, StopBean value) {
    _validStopCache.put(key, value);
  }
  
  @Override 
  public boolean isInvalidStop(String key) {
    return Boolean.TRUE.equals(_invalidStopCache.get(key));
  }
  
  @Override 
  public void setInvalidStop(String key) {
    _invalidStopCache.put(key, Boolean.TRUE);
  }
}
