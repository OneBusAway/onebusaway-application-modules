/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.ConsolidatedStopsService;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.utility.text.NaturalStringOrder;
import org.onebusaway.utility.text.StringLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopBeanServiceImpl implements StopBeanService {

  private static RouteBeanComparator _routeBeanComparator = new RouteBeanComparator();

  private TransitGraphDao _transitGraphDao;

  private RouteService _routeService;

  private RouteBeanService _routeBeanService;

  private NarrativeService _narrativeService;

  private ConsolidatedStopsService _consolidatedStopsService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setRouteService(RouteService routeService) {
    _routeService = routeService;
  }

  @Autowired
  public void setRouteBeanService(RouteBeanService routeBeanService) {
    _routeBeanService = routeBeanService;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setConsolidatedStopsService(ConsolidatedStopsService consolidatedStopsService) {
    _consolidatedStopsService = consolidatedStopsService;
  }

  @Cacheable
  /** serviceDate can be null.
   *  If included, the routes returned in the stopBean will be filtered by service date
  **/
  public StopBean getStopForId(AgencyAndId id, AgencyServiceInterval serviceInterval) {
      return getStopForIdForServiceDate(id, serviceInterval);
  }

  public boolean matchesRouteTypeFilter(RouteBean route, List<Integer> routeTypesFilter) {
    if (routeTypesFilter == null  || routeTypesFilter.isEmpty())
      return true; // no filter, everything matches

    for (Integer routeType : routeTypesFilter) {
      if (routeType == route.getType())
        return true;
    }

    return false;
  }

  @Cacheable
  public StopBean getStopForIdForServiceDate(AgencyAndId id, AgencyServiceInterval serviceInterval) {

    StopEntry stop = _transitGraphDao.getStopEntryForId(id);
    StopNarrative narrative = _narrativeService.getStopForId(id);

    if (stop == null) {
      // try looking up consolidated id
      AgencyAndId consolidatedId = _consolidatedStopsService.getConsolidatedStopIdForHiddenStopId(id);
      if (consolidatedId != null)
        return getStopForId(consolidatedId, serviceInterval);
      throw new NoSuchStopServiceException(
              AgencyAndIdLibrary.convertToString(id));
    }

    StopBean sb = new StopBean();
    fillStopBean(stop, narrative, sb);
    if (stop.getParent() != null) {
      AgencyAndId parentId = stop.getParent();
      StopBean parentBean = new StopBean();
      StopNarrative parentNarrative = _narrativeService.getStopForId(parentId);
      StopEntry parent = _transitGraphDao.getStopEntryForId(parentId);
      fillStopBean(parent, parentNarrative, parentBean);
      fillRoutesForStopBean(parent, parentBean, serviceInterval);
      sb.setParent(parentBean);
    }
    fillRoutesForStopBean(stop, sb, serviceInterval);
    fillTransfersForStopBean(stop, narrative, sb);
    return sb;
  }

  private void fillTransfersForStopBean(StopEntry stop, StopNarrative narrative, StopBean bean) {
    List<AgencyAndId> staticRoutes = _narrativeService.getStaticRoutes(stop.getId());
    if (staticRoutes == null) {
      // we don't have any defaults, use the standard set of routes
      bean.setStaticRoutes(bean.getRoutes());
      return;
    }
    List<RouteBean> routeBeans = new ArrayList<>(staticRoutes.size());
    for (AgencyAndId routeId : staticRoutes) {
      RouteBean staticRouteBean = _routeBeanService.getRouteForId(routeId);
      routeBeans.add(staticRouteBean);
    }
    bean.setStaticRoutes(routeBeans);
  }


  private void fillRoutesForStopBean(StopEntry stop, StopBean sb, AgencyServiceInterval serviceInterval) {

    Set<AgencyAndId> routeCollectionIds;
    if (serviceInterval != null)
      routeCollectionIds = _routeService.getRouteCollectionIdsForStopForServiceDate(stop.getId(), serviceInterval);
    else
      routeCollectionIds = _routeService.getRouteCollectionIdsForStop(stop.getId());

    List<RouteBean> routeBeans = new ArrayList<RouteBean>(
        routeCollectionIds.size());

    for (AgencyAndId routeCollectionId : routeCollectionIds) {
      RouteBean bean = _routeBeanService.getRouteForId(routeCollectionId);
      routeBeans.add(bean);
    }

    Collections.sort(routeBeans, _routeBeanComparator);

    sb.setRoutes(routeBeans);
  }

  private void fillStopBean(StopEntry stop, StopNarrative narrative,
      StopBean bean) {

    bean.setId(ApplicationBeanLibrary.getId(stop.getId()));
    bean.setLat(stop.getStopLat());
    bean.setLon(stop.getStopLon());
    bean.setName(narrative.getName());
    bean.setCode(StringLibrary.getBestName(narrative.getCode(),
        stop.getId().getId()));
    bean.setLocationType(narrative.getLocationType());
    bean.setDirection(narrative.getDirection());
    bean.setWheelchairBoarding(stop.getWheelchairBoarding());
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
