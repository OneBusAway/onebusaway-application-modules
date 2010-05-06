package org.onebusaway.federations;

import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;

import java.util.Set;

public interface FederatedServiceCollection {

  public abstract Set<FederatedService> getAllServices();

  public abstract FederatedService getServiceForAgencyId(String agencyId)
      throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForAgencyIds(
      Iterable<String> agencyIds) throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForBounds(CoordinateBounds bounds)
      throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForBounds(double lat1,
      double lon1, double lat2, double lon2) throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForLocation(double lat, double lon)
      throws ServiceAreaServiceException;

}