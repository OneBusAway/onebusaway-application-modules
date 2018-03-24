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
package org.onebusaway.federations.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.onebusaway.federations.FederatedServiceRegistry;
import org.onebusaway.federations.FederatedServiceRegistryConstants;
import org.onebusaway.federations.FederatedServiceRegistryEntry;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic {@link FederatedServiceRegistry} implementation that implements
 * features such as
 * {@link FederatedServiceRegistryConstants#KEY_REGISTRATION_EXPIRES_AFTER}.
 * 
 * @author bdferris
 * @see FederatedServiceRegistry
 */
public class FederatedServiceRegistryImpl implements FederatedServiceRegistry {

  private static Logger _log = LoggerFactory.getLogger(FederatedServiceRegistryImpl.class);

  private ConcurrentMap<String, Boolean> _persistentStatusByUrl = new ConcurrentHashMap<String, Boolean>();

  private ConcurrentMap<String, FederatedServiceEntryImpl> _servicesByUrl = new ConcurrentHashMap<String, FederatedServiceEntryImpl>();

  private Map<String, Map<String, FederatedServiceEntryImpl>> _servicesByTypeAndUrl = new HashMap<String, Map<String, FederatedServiceEntryImpl>>();

  private ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  private int _updateFrequency = 60;

  public void setUpdateFrequency(int updateFrequencyInSeconds) {
    _updateFrequency = updateFrequencyInSeconds;
  }

  public void start() {
    _executor.scheduleAtFixedRate(new ServiceUpdateImpl(), 0, _updateFrequency,
        TimeUnit.SECONDS);
  }

  public void stop() {
    _executor.shutdown();
  }

  @Override
  public synchronized List<FederatedServiceRegistryEntry> getAllServices() {
    List<FederatedServiceRegistryEntry> entries = new ArrayList<FederatedServiceRegistryEntry>();
    for (FederatedServiceEntryImpl entry : _servicesByUrl.values())
      entries.add(entry.getAsEntry());
    return entries;
  }

  @Override
  public synchronized List<FederatedServiceRegistryEntry> getServices(
      String serviceClass, Map<String, String> properties) {

    List<FederatedServiceRegistryEntry> results = new ArrayList<FederatedServiceRegistryEntry>();
    Map<String, FederatedServiceEntryImpl> entries = _servicesByTypeAndUrl.get(serviceClass);
    if (entries != null) {
      for (FederatedServiceEntryImpl entry : entries.values()) {
        if (entry.isEnabled() && entry.isApplicable(properties)
            && !entry.isExpired())
          results.add(entry.getAsEntry());
      }
    }

    return results;
  }

  @Override
  public synchronized void addService(String url, String serviceClass,
      Map<String, String> properties) {

    FederatedServiceEntryImpl existing = _servicesByUrl.get(url);
    removeService(url);

    FederatedServiceEntryImpl entry = new FederatedServiceEntryImpl(url,
        serviceClass, properties);

    String expiresAfter = properties.get(FederatedServiceRegistryConstants.KEY_REGISTRATION_EXPIRES_AFTER);
    if (expiresAfter != null) {
      int epiresAfterMS = Integer.parseInt(expiresAfter) * 1000;
      entry.setExpiresAfter(SystemTime.currentTimeMillis() + epiresAfterMS);
    }

    boolean enabled = true;
    if (existing != null) {
      enabled = existing.isEnabled();
    } else if (_persistentStatusByUrl.containsKey(url)) {
      enabled = _persistentStatusByUrl.get(url);
    } else {
      String initiallyEnabledValue = properties.get(FederatedServiceRegistryConstants.KEY_INITIALLY_ENABLED);
      if (initiallyEnabledValue != null)
        enabled = Boolean.parseBoolean(initiallyEnabledValue);
    }
    entry.setEnabled(enabled);
    _persistentStatusByUrl.put(url, enabled);

    _servicesByUrl.put(url, entry);

    Map<String, FederatedServiceEntryImpl> entries = _servicesByTypeAndUrl.get(serviceClass);
    if (entries == null) {
      entries = new HashMap<String, FederatedServiceEntryImpl>();
      _servicesByTypeAndUrl.put(serviceClass, entries);
    }

    entries.put(url, entry);
  }

  @Override
  public synchronized void removeService(String url) {
    FederatedServiceEntryImpl entry = _servicesByUrl.remove(url);
    if (entry != null) {
      Map<String, FederatedServiceEntryImpl> byUrl = _servicesByTypeAndUrl.get(entry.getServiceClass());
      if (byUrl != null)
        byUrl.remove(url);
    }
  }

  @Override
  public synchronized void removeAllServices() {
    List<String> urls = new ArrayList<String>(_servicesByUrl.keySet());
    for (String url : urls)
      removeService(url);
  }

  @Override
  public synchronized void setServiceStatus(String url, boolean enabled) {
    FederatedServiceEntryImpl entry = _servicesByUrl.get(url);
    if (entry != null)
      entry.setEnabled(enabled);
    _persistentStatusByUrl.put(url, enabled);
  }

  private synchronized void pruneExpiredServices() {
    Set<String> expiredUrls = new HashSet<String>();
    for (FederatedServiceEntryImpl entry : _servicesByUrl.values()) {
      if (entry.isExpired())
        expiredUrls.add(entry.getServiceUrl());
    }
    for (String expiredUrl : expiredUrls) {
      _log.info("expiring service url: " + expiredUrl);
      removeService(expiredUrl);
    }
  }

  private class ServiceUpdateImpl implements Runnable {

    @Override
    public void run() {
      pruneExpiredServices();
    }
  }

}
