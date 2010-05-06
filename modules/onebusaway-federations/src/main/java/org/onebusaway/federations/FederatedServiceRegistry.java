package org.onebusaway.federations;

import org.onebusaway.exceptions.MultipleServiceAreasServiceException;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FederatedServiceRegistry {

  private Set<FederatedService> _services;

  private Map<String, FederatedService> _servicesByAgencyId = new HashMap<String, FederatedService>();

  private Map<Integer, FederatedService> _servicesByNodeIndex = new HashMap<Integer, FederatedService>();

  private RTree _tree;

  public FederatedServiceRegistry(
      Map<FederatedService, Map<String, List<CoordinateBounds>>> services) {

    _services = Collections.unmodifiableSet(services.keySet());

    _tree = new RTree();
    _tree.init(new Properties());

    for (Map.Entry<FederatedService, Map<String, List<CoordinateBounds>>> entry : services.entrySet()) {
      FederatedService service = entry.getKey();
      Map<String, List<CoordinateBounds>> agencyIdsAndCoverage = entry.getValue();
      for (Map.Entry<String, List<CoordinateBounds>> acEntry : agencyIdsAndCoverage.entrySet()) {

        String agencyId = acEntry.getKey();
        List<CoordinateBounds> coverage = acEntry.getValue();

        _servicesByAgencyId.put(agencyId, service);

        for (CoordinateBounds rc : coverage) {
          Rectangle r = new Rectangle((float) rc.getMinLon(),
              (float) rc.getMinLat(), (float) rc.getMaxLon(),
              (float) rc.getMaxLat());
          int index = _servicesByNodeIndex.size();

          _tree.add(r, index);
          _servicesByNodeIndex.put(index, service);
        }
      }
    }
  }

  public Set<FederatedService> getAllServices() {
    return _services;
  }

  public FederatedService getServiceForAgencyId(String agencyId)
      throws ServiceAreaServiceException {
    FederatedService provider = _servicesByAgencyId.get(agencyId);
    if (provider == null)
      throw new OutOfServiceAreaServiceException();
    return provider;
  }

  public FederatedService getServiceForAgencyIds(Iterable<String> agencyIds)
      throws ServiceAreaServiceException {
    Set<FederatedService> providers = new HashSet<FederatedService>();
    for (String id : agencyIds) {
      FederatedService provider = getServiceForAgencyId(id);
      providers.add(provider);
    }
    return getProviderFromProviders(providers);
  }

  public FederatedService getServiceForBounds(CoordinateBounds bounds)
      throws ServiceAreaServiceException {
    return getServiceForBounds(bounds.getMinLat(), bounds.getMinLon(),
        bounds.getMaxLat(), bounds.getMaxLon());
  }
  
  public FederatedService getServiceForBounds(double lat1, double lon1,
      double lat2, double lon2) throws ServiceAreaServiceException {
    Rectangle rectangle = new Rectangle((float) lon1, (float) lat1,
        (float) lon2, (float) lat2);
    return getProviderForRectangle(rectangle);
  }

  public FederatedService getServiceForLocation(double lat, double lon)
      throws ServiceAreaServiceException {
    return getServiceForBounds(lat, lon, lat, lon);
  }

  /****
   * Private Methods
   ****/

  private FederatedService getProviderForRectangle(Rectangle rectangle)
      throws ServiceAreaServiceException {
    ProviderCollector collector = new ProviderCollector();
    _tree.intersects(rectangle, collector);
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

  private class ProviderCollector implements IntProcedure {

    private Set<FederatedService> _providers = new HashSet<FederatedService>();

    public Set<FederatedService> getProviders() {
      return _providers;
    }

    public boolean execute(int index) {
      _providers.add(_servicesByNodeIndex.get(index));
      return true;
    }
  }
}
