package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data_federation.model.RouteCollection;

/**
 * Service methods for retrieving lists of routes, usually with a search query.
 * Note that like {@like RouteService}, the underlying representation of a route
 * in a transit data federation bundle is a {@link RouteCollection}, not a
 * {@link Route}.
 * 
 * @author bdferris
 */
public interface RoutesBeanService {

  /**
   * TODO: Convert this to use {@list ListBean} at some point?
   * 
   * @param query the route search query
   * @return routes that match the specified query
   * @throws ServiceException
   */
  public RoutesBean getRoutesForQuery(SearchQueryBean query)
      throws ServiceException;

  /**
   * 
   * @param agencyId see {@link Agency#getId()}
   * @return the list of all routes for the specified agency
   */
  public ListBean<String> getRouteIdsForAgencyId(String agencyId);
  
  public ListBean<RouteBean> getRoutesForAgencyId(String agencyId);
}
