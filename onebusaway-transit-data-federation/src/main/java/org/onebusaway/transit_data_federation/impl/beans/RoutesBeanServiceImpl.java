package org.onebusaway.transit_data_federation.impl.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.lucene.queryParser.ParseException;
import org.onebusaway.exceptions.InvalidArgumentServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.model.SearchResult;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.RouteCollectionSearchService;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.beans.GeospatialBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.RoutesBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.strtree.STRtree;

@Component
class RoutesBeanServiceImpl implements RoutesBeanService {

  private static Logger _log = LoggerFactory.getLogger(RoutesBeanServiceImpl.class);

  private static final double MIN_SEARCH_SCORE = 1.0;

  @Autowired
  private RouteCollectionSearchService _searchService;

  @Autowired
  private GeospatialBeanService _whereGeospatialService;

  @Autowired
  private RouteBeanService _routeBeanService;

  @Autowired
  private StopBeanService _stopService;

  @Autowired
  private ExtendedGtfsRelationalDao _dao;

  @Autowired
  private TransitGraphDao _graphDao;

  private Map<AgencyAndId, STRtree> _stopTreesByRouteId = new HashMap<AgencyAndId, STRtree>();

  @PostConstruct
  public void setup() {
    
    for (StopEntry stop : _graphDao.getAllStops()) {
      Set<AgencyAndId> routeIds = _graphDao.getRouteCollectionIdsForStop(stop.getId());
      for (AgencyAndId routeId : routeIds) {
        STRtree tree = _stopTreesByRouteId.get(routeId);
        if (tree == null) {
          tree = new STRtree();
          _stopTreesByRouteId.put(routeId, tree);
        }
        double x = stop.getStopLon();
        double y = stop.getStopLat();
        Envelope env = new Envelope(x, x, y, y);
        tree.insert(env,routeId);
      }
    }
    
    for( STRtree tree : _stopTreesByRouteId.values() )
      tree.build();
  }

  @Override
  public RoutesBean getRoutesForQuery(SearchQueryBean query)
      throws ServiceException {
    if (query.getQuery() != null)
      return getRoutesWithRouteNameQuery(query);
    else
      return getRoutesWithoutRouteNameQuery(query);
  }

  @Override
  public ListBean<String> getRouteIdsForAgencyId(String agencyId) {
    List<AgencyAndId> routeIds = _dao.getRouteIdsForAgencyId(agencyId);
    List<String> ids = new ArrayList<String>();
    for (AgencyAndId id : routeIds)
      ids.add(AgencyAndIdLibrary.convertToString(id));
    return new ListBean<String>(ids, false);
  }

  /****
   * Private Methods
   ****/

  private RoutesBean getRoutesWithoutRouteNameQuery(SearchQueryBean query) {

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

  private RoutesBean getRoutesWithRouteNameQuery(SearchQueryBean query)
      throws ServiceException {

    SearchResult<AgencyAndId> result = searchForRoutes(query);

    List<RouteBean> routeBeans = new ArrayList<RouteBean>();
    CoordinateBounds bounds = query.getBounds();

    for (AgencyAndId id : result.getResults()) {
      STRtree tree = _stopTreesByRouteId.get(id);
      if( tree == null) {
        _log.warn("stop tree not found for routeId=" + id);
        continue;
      }
      Envelope env = new Envelope(bounds.getMinLon(),bounds.getMaxLon(),bounds.getMinLat(),bounds.getMaxLat());
      HasItemsVisitor v = new HasItemsVisitor();
      tree.query(env, v);
      
      if( v.hasItems() ) {
        RouteBean routeBean = _routeBeanService.getRouteForId(id);
        routeBeans.add(routeBean);
      }
    }

    boolean limitExceeded = BeanServiceSupport.checkLimitExceeded(routeBeans,
        query.getMaxCount());
    
    return constructResult(routeBeans, limitExceeded);
  }

  private SearchResult<AgencyAndId> searchForRoutes(SearchQueryBean query)
      throws ServiceException, InvalidArgumentServiceException {

    try {
      return _searchService.searchForRoutesByShortName(query.getQuery(),
          query.getMaxCount() + 1, MIN_SEARCH_SCORE);
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
  
  private static class HasItemsVisitor implements ItemVisitor {

    private boolean _hasItems = false;
    
    public boolean hasItems() {
      return _hasItems;
    }
    
    @Override
    public void visitItem(Object arg0) {
      _hasItems = true;
    }    
  }

}
