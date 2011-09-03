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

import java.util.List;
import java.util.Map;

import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LazyFederatedServiceCollectionImpl extends
    AbstractFederatedServiceCollectionWrapperImpl {

  private Logger _logger = LoggerFactory.getLogger(LazyFederatedServiceCollectionImpl.class);

  private List<? extends FederatedService> _serviceProviders;

  private Class<? extends FederatedService> _serviceInterface;

  private volatile FederatedServiceCollection _registry = null;

  public void setServiceProviders(List<? extends FederatedService> serviceProviders) {
    _serviceProviders = serviceProviders;
  }

  public void setServiceInterface(Class<? extends FederatedService> serviceInterface) {
    _serviceInterface = serviceInterface;
  }

  protected FederatedServiceCollection getCollection() {

    if (_registry == null) {
      synchronized (this) {
        if (_registry == null) {
          _logger.info("instantiating FederatedServiceRegistry");
          Map<FederatedService, Map<String, List<CoordinateBounds>>> coverage = FederatedServiceLibrary.getFederatedServiceAgencyCoverage(
              _serviceProviders, _serviceInterface);
          _registry = new FederatedServiceCollectionImpl(coverage);
        }
      }
    }

    return _registry;
  }
}
