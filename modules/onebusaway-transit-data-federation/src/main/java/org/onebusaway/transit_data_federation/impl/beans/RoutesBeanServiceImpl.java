package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.exceptions.InvalidArgumentServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.RoutesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.RouteCollectionSearchService;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.RoutesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;

import org.apache.lucene.queryParser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
class RoutesBeanServiceImpl implements RoutesBeanService {

  @Autowired
  private RouteCollectionSearchService _searchService;

  @Autowired
  private GeospatialBeanService _whereGeospatialService;

  @Autowired
  private RouteService _routeService;

  @Autowired
  private RouteBeanService _routeBeanService;

  @Autowired
  private StopBeanService _stopService;

  @Override
  public RoutesBean getRoutesForQuery(RoutesQueryBean query) {
    if (query.getQuery() != null)
      return getRoutesWithRouteNameQuery(query);
    else
      return getRoutesWithoutRouteNameQuery(query);
  }

  /****
   * Private Methods
   ****/

  private RoutesBean getRoutesWithoutRouteNameQuery(RoutesQueryBean query) {

    CoordinateBounds bounds = query.getBounds();

    List<AgencyAndId> stops = _whereGeospatialService.getStopsByBounds(
        bounds.getMinLat(), bounds.getMinLon(), bounds.getMaxLat(),
        bounds.getMaxLon());

    Set<RouteBean> routes = new HashSet<RouteBean>();
    for (AgencyAndId stopId : stops) {
      StopBean stop = _stopService.getStopForId(stopId);
      routes.addAll(stop.getRoutes());
    }

    List<RouteBean> routeBeans = new ArrayList<RouteBean>(routes);
    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(routeBeans,
        query.getMaxCount());
    return constructResult(routeBeans, limitExceeded);
  }

  private RoutesBean getRoutesWithRouteNameQuery(RoutesQueryBean query)
      throws ServiceException {

    SearchResult<AgencyAndId> result = searchForRoutes(query);

    List<RouteBean> routeBeans = new ArrayList<RouteBean>();
    CoordinateBounds bounds = query.getBounds();

    for (AgencyAndId id : result.getResults()) {
      Collection<AgencyAndId> stopIds = _routeService.getStopsForRouteCollection(id);
      for (AgencyAndId stopId : stopIds) {
        StopBean stop = _stopService.getStopForId(stopId);
        if (bounds.contains(stop.getLat(), stop.getLon())) {
          RouteBean routeBean = _routeBeanService.getRouteForId(id);
          routeBeans.add(routeBean);
          break;
        }
      }
    }

    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(routeBeans,
        query.getMaxCount());
    return constructResult(routeBeans, limitExceeded);
  }

  private SearchResult<AgencyAndId> searchForRoutes(RoutesQueryBean query)
      throws ServiceException, InvalidArgumentServiceException {

    try {
      return _searchService.searchForRoutesByShortName(query.getQuery(),
          query.getMaxCount() + 1);
    } catch (IOException e) {
      throw new ServiceException();
    } catch (ParseException e) {
      throw new InvalidArgumentServiceException("query", "queryParseError");
    }
  }

  private RoutesBean constructResult(List<RouteBean> routeBeans,
      boolean limitExceeded) {

    Collections.sort(routeBeans, new RouteBeanIdComparator());

    RoutesBean result = new RoutesBean();
    result.setRoutes(routeBeans);
    result.setLimitExceeded(limitExceeded);
    return result;
  }

}
