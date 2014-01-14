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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.FederatedServiceRegistry;
import org.onebusaway.federations.FederatedServiceRegistryEntry;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.HessianConnectionException;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;

/**
 * A dynamic {@link FederatedServiceCollection} implementation. Service
 * instances are periodically queried from a {@link FederatedServiceRegistry}
 * instance given a target service interface + parameters and our service
 * collection is updated as is appropriate. It's assumed that all services
 * published by the {@link FederatedServiceRegistry} have been exported as a
 * Hessian rpc service, as we'll be using {@link HessianProxyFactory} to create
 * proxy service instances from the service urls.
 * 
 * @author bdferris
 */
public class DynamicFederatedServiceCollectionImpl extends
    AbstractFederatedServiceCollectionWrapperImpl {

  private static Logger _log = LoggerFactory.getLogger(DynamicFederatedServiceCollectionImpl.class);

  private HessianProxyFactory _proxyFactory = new HessianProxyFactory();

  private ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  /**
   * By default, we have an empty registry
   */
  private volatile FederatedServiceCollection _collection = new FederatedServiceCollectionImpl();

  private volatile Set<String> _activeUrls = new HashSet<String>();

  private FederatedServiceRegistry _registry;

  private int _updateFrequency = 60;

  private Class<?> _serviceInterface;

  private Map<String, String> _queryProperties = new HashMap<String, String>();

  public void setRegistry(FederatedServiceRegistry registry) {
    _registry = registry;
  }

  public void setUpdateFrequency(int updateFrequencyInSeconds) {
    _updateFrequency = updateFrequencyInSeconds;
  }

  public void setServiceInterface(Class<?> serviceInterface) {
    _serviceInterface = serviceInterface;
  }

  public void setQueryProperties(Map<String, String> queryProperties) {
    _queryProperties = queryProperties;
  }

  @PostConstruct
  public void start() {
    _log.debug("start");
    _executor.scheduleAtFixedRate(new ServiceUpdateImpl(), 0, _updateFrequency,
        TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    _log.debug("stop");
    _executor.shutdown();
  }

  @Override
  protected FederatedServiceCollection getCollection() {
    return _collection;
  }

  private class ServiceUpdateImpl implements Runnable {

    @Override
    public void run() {

      try {

        Set<String> activeUrls = new HashSet<String>();

        List<FederatedServiceRegistryEntry> entries = _registry.getServices(
            _serviceInterface.getName(), _queryProperties);

        Map<FederatedService, Map<String, List<CoordinateBounds>>> byProvider = new HashMap<FederatedService, Map<String, List<CoordinateBounds>>>();

        for (FederatedServiceRegistryEntry serviceEntry : entries) {

          String url = serviceEntry.getServiceUrl();

          _log.debug("querying url: {}", url);

          try {
            FederatedService service = (FederatedService) _proxyFactory.create(
                _serviceInterface, url);

            Map<String, List<CoordinateBounds>> agencyIdsWithCoverageArea = service.getAgencyIdsWithCoverageArea();

            boolean allGood = true;

            for (Map.Entry<String, List<CoordinateBounds>> entry : agencyIdsWithCoverageArea.entrySet()) {

              String agencyId = entry.getKey();
              List<CoordinateBounds> coverage = entry.getValue();

              boolean validService = FederatedServiceLibrary.checkAgencyAndCoverageAgainstExisting(
                  byProvider, agencyId, coverage, _serviceInterface, false);
              if (!validService) {
                allGood = false;
                _log.warn("error in agency coverage overlap: url=" + url
                    + " agencyId=" + agencyId);
              }
            }

            if (allGood) {
              _log.debug("adding service...");
              byProvider.put(service, agencyIdsWithCoverageArea);
              activeUrls.add(url);
            }
          } catch (HessianRuntimeException ex) {
            _log.warn("error querying service url: " + url);
          } catch (HessianConnectionException ex) {
            _log.warn("error connecting to service url: " + url);
          } catch (MalformedURLException e) {
            _log.warn("malformed service url: " + url);
          }
        }

        Set<String> allUrls = new HashSet<String>();
        allUrls.addAll(_activeUrls);
        allUrls.addAll(activeUrls);

        for (String url : allUrls) {
          boolean a = _activeUrls.contains(url);
          boolean b = activeUrls.contains(url);
          if (a && !b)
            _log.info("service removed: " + url);
          else if (!a && b)
            _log.info("service added: " + url);
        }

        _collection = new FederatedServiceCollectionImpl(byProvider);
        _activeUrls = activeUrls;
      } catch (Throwable ex) {
        _log.warn("error refreshing services", ex);
      }
    }
  }
}
