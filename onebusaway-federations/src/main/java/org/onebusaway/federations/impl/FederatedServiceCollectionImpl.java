package org.onebusaway.federations.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.exceptions.MultipleServiceAreasServiceException;
import org.onebusaway.exceptions.NoSuchAgencyServiceException;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * A basic {@link FederatedServiceCollection} implementation that provides
 * methods for query a set of {@link FederatedService} instances. See the static
 * factory method {@link #create(List, Class)} for a convenient way of
 * constructing a {@link FederatedServiceCollectionImpl} from a set of
 * {@link FederatedService} instances of a particular type.
 * 
 * @author bdferris
 * @see FederatedService
 * @see FederatedServiceCollection
 */
public class FederatedServiceCollectionImpl implements
    FederatedServiceCollection {

  private Set<FederatedService> _services;

  private Map<String, FederatedService> _servicesByAgencyId = new HashMap<String, FederatedService>();

  private STRtree _tree;

  public static <T extends FederatedService> FederatedServiceCollectionImpl create(
      List<T> serviceProviders, Class<T> serviceInterface) {
    Map<FederatedService, Map<String, List<CoordinateBounds>>> map = FederatedServiceLibrary.getFederatedServiceAgencyCoverage(
        serviceProviders, serviceInterface);
    return new FederatedServiceCollectionImpl(map);
  }

  public FederatedServiceCollectionImpl(
      Map<FederatedService, Map<String, List<CoordinateBounds>>> services) {

    _services = Collections.unmodifiableSet(services.keySet());

    _tree = new STRtree();

    for (Map.Entry<FederatedService, Map<String, List<CoordinateBounds>>> entry : services.entrySet()) {
      FederatedService service = entry.getKey();
      Map<String, List<CoordinateBounds>> agencyIdsAndCoverage = entry.getValue();
      for (Map.Entry<String, List<CoordinateBounds>> acEntry : agencyIdsAndCoverage.entrySet()) {

        String agencyId = acEntry.getKey();
        List<CoordinateBounds> coverage = acEntry.getValue();

        _servicesByAgencyId.put(agencyId, service);

        for (CoordinateBounds rc : coverage) {
          Envelope env = new Envelope(rc.getMinLon(), rc.getMaxLon(),
              rc.getMinLat(), rc.getMaxLat());
          _tree.insert(env, service);
        }
      }
    }

    _tree.build();
  }

  public FederatedServiceCollectionImpl() {
    this(new HashMap<FederatedService, Map<String, List<CoordinateBounds>>>());
  }

  /****
   * {@link FederatedServiceCollection} Interface
   ****/

  @Override
  public Set<FederatedService> getAllServices() {
    return _services;
  }

  @Override
  public FederatedService getServiceForAgencyId(String agencyId)
      throws ServiceAreaServiceException {
    FederatedService provider = _servicesByAgencyId.get(agencyId);
    if (provider == null)
      throw new NoSuchAgencyServiceException(agencyId);
    return provider;
  }

  @Override
  public FederatedService getServiceForAgencyIds(Iterable<String> agencyIds)
      throws ServiceAreaServiceException {
    Set<FederatedService> providers = new HashSet<FederatedService>();
    for (String id : agencyIds) {
      FederatedService provider = getServiceForAgencyId(id);
      if (provider == null)
        throw new NoSuchAgencyServiceException(id);
      providers.add(provider);
    }
    return getProviderFromProviders(providers);
  }

  @Override
  public FederatedService getServiceForBounds(CoordinateBounds bounds)
      throws ServiceAreaServiceException {
    return getServiceForBounds(bounds.getMinLat(), bounds.getMinLon(),
        bounds.getMaxLat(), bounds.getMaxLon());
  }

  @Override
  public FederatedService getServiceForBounds(double lat1, double lon1,
      double lat2, double lon2) throws ServiceAreaServiceException {
    Envelope rectangle = new Envelope(lon1, lon2, lat1, lat2);
    return getProviderForRectangle(rectangle);
  }

  @Override
  public FederatedService getServiceForLocation(double lat, double lon)
      throws ServiceAreaServiceException {
    return getServiceForBounds(lat, lon, lat, lon);
  }

  @Override
  public FederatedService getServiceForLocations(List<CoordinatePoint> points)
      throws ServiceAreaServiceException {

    FederatedService service = null;

    for (CoordinatePoint point : points) {

      FederatedService provider = getServiceForLocation(point.getLat(),
          point.getLon());

      if (service == null) {
        service = provider;
      } else if (service != provider) {
        throw new MultipleServiceAreasServiceException();
      }
    }

    if (service == null)
      throw new OutOfServiceAreaServiceException();

    return service;
  }

  /****
   * Private Methods
   ****/

  private FederatedService getProviderForRectangle(Envelope env)
      throws ServiceAreaServiceException {
    ProviderCollector collector = new ProviderCollector();
    if (_tree.size() != 0)
      _tree.query(env, collector);
    Set<FederatedService> providers = collector.getProviders();
    return getProviderFromProviders(providers);
  }

  private FederatedService getProviderFromProviders(
      Set<FederatedService> providers) throws OutOfServiceAreaServiceException,
      MultipleServiceAreasServiceException {
    if (providers.size() == 1)
      return providers.iterator().next();
    if (providers.size() == 0)
      throw new OutOfServiceAreaServiceException();
    throw new MultipleServiceAreasServiceException();
  }

  private class ProviderCollector implements ItemVisitor {

    private Set<FederatedService> _providers = new HashSet<FederatedService>();

    public Set<FederatedService> getProviders() {
      return _providers;
    }

    @Override
    public void visitItem(Object item) {
      _providers.add((FederatedService) item);
    }
  }
}
