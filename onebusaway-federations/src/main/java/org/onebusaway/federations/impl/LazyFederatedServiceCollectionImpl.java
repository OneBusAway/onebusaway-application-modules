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
