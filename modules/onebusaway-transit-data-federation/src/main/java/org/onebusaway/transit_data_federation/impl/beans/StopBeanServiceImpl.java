package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.services.WhereGraphDao;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.utility.text.NaturalStringOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
class StopBeanServiceImpl implements StopBeanService {

  private static RouteBeanComparator _routeBeanComparator = new RouteBeanComparator();

  private GtfsRelationalDao _gtfsDao;

  private RouteBeanService _routeBeanService;

  private WhereGraphDao _whereGraphDao;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setRouteBeanService(RouteBeanService routeBeanService) {
    _routeBeanService = routeBeanService;
  }

  @Autowired
  public void setWhereGraphDao(WhereGraphDao dao) {
    _whereGraphDao = dao;
  }

  @Cacheable
  public StopBean getStopForId(AgencyAndId id) {

    Stop stop = _gtfsDao.getStopForId(id);

    if (stop == null)
      return null;

    StopBean sb = new StopBean();
    fillStopBean(stop, sb);
    fillRoutesForStopBean(stop, sb);
    return sb;
  }

  private void fillRoutesForStopBean(Stop stop, StopBean sb) {

    Set<AgencyAndId> routeCollectionIds = _whereGraphDao.getRouteCollectionIdsForStop(stop.getId());

    List<RouteBean> routeBeans = new ArrayList<RouteBean>(
        routeCollectionIds.size());

    for (AgencyAndId routeCollectionId : routeCollectionIds) {
      RouteBean bean = _routeBeanService.getRouteForId(routeCollectionId);
      routeBeans.add(bean);
    }

    Collections.sort(routeBeans, _routeBeanComparator);

    sb.setRoutes(routeBeans);
  }

  private StopBean fillStopBean(Stop stop, StopBean bean) {
    
    bean.setId(ApplicationBeanLibrary.getId(stop.getId()));
    bean.setLat(stop.getLat());
    bean.setLon(stop.getLon());
    
    if (stop.getDirection() != null)
      bean.setDirection(ApplicationBeanLibrary.getStopDirection(stop));
    
    bean.setName(stop.getName());
    bean.setCode(ApplicationBeanLibrary.getBestName(stop.getCode(),stop.getId().getId()));
    bean.setLocationType(stop.getLocationType());
    
    return bean;
  }

  private static String getRouteBeanName(RouteBean bean) {
    return bean.getShortName() == null ? bean.getLongName()
        : bean.getShortName();
  }

  private static class RouteBeanComparator implements Comparator<RouteBean> {
    public int compare(RouteBean o1, RouteBean o2) {
      String name1 = getRouteBeanName(o1);
      String name2 = getRouteBeanName(o2);
      return NaturalStringOrder.compareNatural(name1, name2);
    }
  }
}
