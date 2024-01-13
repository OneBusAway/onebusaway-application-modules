/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl;

import org.apache.logging.log4j.util.Strings;
import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.transit_graph.CanonicalRoutesEntryImpl;
import org.onebusaway.transit_data_federation.services.CanonicalRoutesService;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.transit_graph.CanonicalRoutesEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteStopCollectionEntry;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Implementation of service interface for canonical (ideal) route representations.
 */
@Component
public class CanonicalRoutesServiceImpl implements CanonicalRoutesService {

  private static Logger _log = LoggerFactory.getLogger(CanonicalRoutesServiceImpl.class);

  @Autowired
  private FederatedTransitDataBundle _bundle;

  @Autowired
  public TransitDataService _transitDataService;
  public void setTransitDataService(TransitDataService tds) {
    _transitDataService = tds;
  }

  private CanonicalRoutesEntry _entry;
  // package private for unit tests
  void setData(CanonicalRoutesEntry data) {
    this._entry = data;
  }

  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void setup() throws IOException, ClassNotFoundException {
    _log.info("bundle path=" + _bundle.getPath());
    File path = _bundle.getCanonicalRoutePath();
    if (path.exists()) {
      _log.info("loading optional canonical routes and shapes at {}", path);
      try {
        _entry = ObjectSerializationLibrary.readObject(path);
      } catch (Throwable t) {
        // this is optional, don't let it fail the load
        _entry = new CanonicalRoutesEntryImpl();
        _log.error("failed to load option canonical routes with exception {}", t, t);
        return;
      }
      _log.info("loading canonical routes and shapes...done");
    } else {
      // this index is optional, do not fail if not found
      _log.info("failed CanonicalRoutesServiceImpl load, path not found of " + path);
      _entry = new CanonicalRoutesEntryImpl();
    }
  }

  /**
   * For the given service date and routeId, return typical "direction" based representation of
   * route if present in the GTFS as well as "canonical" ideal representation of route, again is present
   * in the GTFS.  If no "canonical" information, return "heuristic" type that is a synthetic respresentation
   * of the ideal.
   * @param serviceInterval
   * @param routeId
   * @return
   */
  @Override
  @Cacheable
  public ListBean<RouteGroupingBean> getCanonicalOrMergedRoute(AgencyServiceInterval serviceInterval, AgencyAndId routeId) {
    StopsForRouteBean stopsForRoute;
    if (serviceInterval != null) {
      stopsForRoute = copy(_transitDataService.getStopsForRouteForServiceInterval(AgencyAndIdLibrary.convertToString(routeId), serviceInterval));
    } else {
      stopsForRoute = copy(_transitDataService.getStopsForRoute(AgencyAndIdLibrary.convertToString(routeId)));
    }
    if (stopsForRoute == null || stopsForRoute.getStopGroupings() == null
        || stopsForRoute.getStopGroupings().isEmpty()) {
     // this is an ideal route only, with no physical schedule
     // return what little we know about it from the canonical index
      return addReferences(createRouteDirectionBean(routeId, serviceInterval), serviceInterval);

    }

    // else create merged
    return addReferences(merge(routeId, stopsForRoute, createRouteDirectionBean(routeId, serviceInterval)), serviceInterval);
  }

  private ListBean<RouteGroupingBean> addReferences(ListBean<RouteGroupingBean> bean, AgencyServiceInterval serviceInterval) {
    if (bean == null || bean.getList() == null) return bean;

    for (RouteGroupingBean routeGroupingBean : bean.getList()) {
      addReferences(routeGroupingBean, serviceInterval);
    }
    return bean;
  }

  private void addReferences(RouteGroupingBean bean, AgencyServiceInterval serviceInterval) {
    if (bean == null) return;
    Set<AgencyAndId> visitedRoutes = new HashSet<>();
    Set<AgencyAndId> visitedStops = new HashSet<>();
    if (bean.getRouteId() != null)
      visitedRoutes.add(bean.getRouteId());
    if (bean.getStopGroupings() == null) {
      loadReferences(bean, visitedRoutes, visitedStops, serviceInterval);
      return;
    }
    for (StopGroupingBean stopGrouping : bean.getStopGroupings()) {
      for (StopGroupBean stopGroup : stopGrouping.getStopGroups()) {
        for (String stopId : stopGroup.getStopIds()) {
          try {
            AgencyAndId stop = AgencyAndIdLibrary.convertFromString(stopId);
            if (stop != null)
              visitedStops.add(stop);
          } catch (IllegalStateException ise) {
            // bury
          }
        }
      }
    }
    loadReferences(bean, visitedRoutes, visitedStops, serviceInterval);

  }

  private void loadReferences(RouteGroupingBean bean, Set<AgencyAndId> visitedRoutes,
                              Set<AgencyAndId> visitedStops, AgencyServiceInterval serviceInterval) {
    for (AgencyAndId visitedRoute : visitedRoutes) {
      RouteBean route = _transitDataService.getRouteForId(AgencyAndIdLibrary.convertToString(visitedRoute));
      if (route != null)
        bean.getRoutes().add(route);
    }

    for (AgencyAndId visitedStop : visitedStops) {
      StopBean stop = _transitDataService.getStopForServiceDate(AgencyAndIdLibrary.convertToString(visitedStop),
              serviceInterval);
      if (stop != null)
        bean.getStops().add(stop);
    }

  }

  private ListBean<RouteGroupingBean> merge(AgencyAndId routeId, StopsForRouteBean stopsForRoute, ListBean<RouteGroupingBean> beans) {
    RouteGroupingBean bean = null;
    List<RouteGroupingBean> list;
    if (beans == null || beans.getList() == null) {
      list = new ArrayList<>();
    } else {
      list = beans.getList();
    }

    if (list.isEmpty()) {
      bean = new RouteGroupingBean();
      bean.setRouteId(routeId);
      bean.setStopGroupings(new ArrayList<>());
      list.add(bean);
    } else {
      // todo here we are assuming a trivial merge
      // we may have to be smarter
      bean = list.get(0);
    }
    bean.getStopGroupings().addAll(stopsForRoute.getStopGroupings());
    // sort the subgroups
    for (StopGroupingBean stopGrouping : bean.getStopGroupings()) {
      Collections.sort(stopGrouping.getStopGroups(), new Comparator<StopGroupBean>() {
        @Override
        public int compare(StopGroupBean o1, StopGroupBean o2) {
          if (o1.getId() == null && o2.getId() == null)
            return 0;
          if (o1.getId() == null) return -1;
          if (o2.getId() == null) return 1;
          return o1.getId().compareTo(o2.getId());
        }
      });
    }

    return beans;
  }

  private ListBean<RouteGroupingBean> createRouteDirectionBean(AgencyAndId routeId, AgencyServiceInterval serviceInterval) {
    List<RouteGroupingBean> results = new ArrayList<>();
    RouteGroupingBean bean = null;
    List<RouteStopCollectionEntry> rscs = _entry.getRouteStopCollectionEntries(routeId);
    if (rscs != null) {
      // we have data via GTFS extensions
      bean = createBeanFromBundle(routeId, rscs);
    }
    if (bean == null) {
      // we don't have data so generate via heuristic
      bean = createBeanFromHeuristic(routeId, serviceInterval);
    }

    if (bean == null) {
      bean = createBeanWithCanonicalShapes(routeId);
    } else {
      addCanonicalShapes(routeId, bean);
    }

    if (bean != null) {
      results.add(bean);
    }
    ListBean<RouteGroupingBean> result = new ListBean<>(results, false);
    return result;
  }

  private boolean addCanonicalShapes(AgencyAndId routeId, RouteGroupingBean bean) {
    // find or create the appropriate bean and add each shape to it
    Map<String, String> directionToShapeMap = _entry.getDirectionToShapeMap(routeId);
    if (directionToShapeMap == null) {
      // nothing to do
      return false;
    }
    for (String directionId : directionToShapeMap.keySet()) {
      StopGroupBean sg = findStopGroup(bean, routeId, directionId, CANONICAL_TYPE);
      if (sg == null) {
        sg = createStopGroup(bean, routeId, directionId, CANONICAL_TYPE);
      }
      // now add shape to bean
      EncodedPolylineBean epl = new EncodedPolylineBean();
      epl.setPoints(directionToShapeMap.get(directionId));
      if (sg.getPolylines() == null) sg.setPolylines(new ArrayList<>());
      sg.getPolylines().add(epl);
    }

    return true;
  }

  private StopGroupBean createStopGroup(RouteGroupingBean bean, AgencyAndId routeId, String directionId, String canonicalType) {
    StopGroupBean stopGroupBean = new StopGroupBean();
    stopGroupBean.setId(directionId);
    boolean found = false;
    for (StopGroupingBean stopGrouping : bean.getStopGroupings()) {
      if (canonicalType.equals(stopGrouping.getType())) {
        if (stopGrouping.getStopGroups() == null) stopGrouping.setStopGroups(new ArrayList<>());
        stopGrouping.getStopGroups().add(stopGroupBean);
        found = true;
      }
    }

    if (!found) {
      StopGroupingBean stopGroupingBean = new StopGroupingBean();
      stopGroupingBean.setType(canonicalType);
      stopGroupingBean.setOrdered(false);
      stopGroupingBean.setStopGroups(new ArrayList<>());
      stopGroupingBean.getStopGroups().add(stopGroupBean);
      bean.getStopGroupings().add(stopGroupingBean);
    }
    return stopGroupBean;
  }

  private StopGroupBean findStopGroup(RouteGroupingBean bean, AgencyAndId routeId, String directionId, String canonicalType) {
    for (StopGroupingBean stopGrouping : bean.getStopGroupings()) {
      if (canonicalType.equals(stopGrouping.getType())) {
        if (stopGrouping.getStopGroups() == null) stopGrouping.setStopGroups(new ArrayList<>());
        for (StopGroupBean stopGroupBean : stopGrouping.getStopGroups()) {
          if (directionId.equals(stopGroupBean.getId())) {
            return stopGroupBean;
          }
        }
      }
    }
    return null;
  }

  private RouteGroupingBean createBeanWithCanonicalShapes(AgencyAndId routeId) {
    RouteGroupingBean bean = new RouteGroupingBean();
    bean.setStopGroupings(new ArrayList<>());
    boolean foundShapes = addCanonicalShapes(routeId, bean);
    if (!foundShapes) return null;
    return bean;
  }

  private RouteGroupingBean createBeanFromHeuristic(AgencyAndId routeId, AgencyServiceInterval serviceInterval) {
    StopsForRouteBean stops = null;
    if (serviceInterval != null) {
      // look up how strip map works and re-use that logic via stops-on-route-for-direction
      stops = _transitDataService.getStopsForRouteForServiceInterval(AgencyAndIdLibrary.convertToString(routeId), serviceInterval);
    } else {
      stops = _transitDataService.getStopsForRoute(AgencyAndIdLibrary.convertToString(routeId));
    }

    if (stops == null || stops.getStopGroupings() == null
        || stops.getStopGroupings().isEmpty()) {
      // no data, nothing to do
      return null;
    }

    for (StopGroupingBean stopGrouping : stops.getStopGroupings()) {
      stopGrouping.setType(HEURISTIC_TYPE);
    }

    RouteGroupingBean bean = new RouteGroupingBean();
    bean.setRouteId(routeId);
    bean.setStopGroupings(stops.getStopGroupings());
    return bean;
  }

  private RouteGroupingBean createBeanFromBundle(AgencyAndId routeId, List<RouteStopCollectionEntry> rscs) {
    RouteGroupingBean bean = new RouteGroupingBean();
    bean.setStopGroupings(new ArrayList<>());
    if (rscs == null) return null;

    for (RouteStopCollectionEntry rsc : rscs) {
      if (rsc != null) {
        bean.setRouteId(rsc.getRouteId());

        List<StopGroupingBean> stopGroupings = createStopGroupings(rsc);
        if (stopGroupings != null && !stopGroupings.isEmpty()) {
          bean.getStopGroupings().addAll(stopGroupings);
        }
      }
    }
    if (bean.getStopGroupings().isEmpty())
      return null;
    return bean;
  }

  private List<StopGroupingBean> createStopGroupings(RouteStopCollectionEntry rsc) {
    List<StopGroupingBean> results = new ArrayList<>();
    if (rsc.getRouteStops() == null) return results;

    StopGroupingBean stopGroupingBean = new StopGroupingBean();

    stopGroupingBean.setStopGroups(new ArrayList<>());
    stopGroupingBean.setType(CANONICAL_TYPE);
    StopGroupBean stopGroupBean = new StopGroupBean();
    stopGroupBean.setId(nullSafeGet(rsc.getId()));

    stopGroupBean.setName(new NameBean("name", rsc.getName()));
    stopGroupBean.setStopIds(new ArrayList<>());
    stopGroupingBean.getStopGroups().add(stopGroupBean);
    results.add(stopGroupingBean);

    for (RouteStopCollectionEntry.StopAndSequence routeStop : rsc.getRouteStops()) {
      stopGroupBean.getStopIds().add(AgencyAndIdLibrary.convertToString(routeStop.getStopId()));
    }

    return results;
  }


  private StopsForRouteBean copy(StopsForRouteBean input) {
    if (input == null) return null;
    StopsForRouteBean output = new StopsForRouteBean();
    output.setRoute(input.getRoute());
    output.setStops(input.getStops());
    output.setPolylines(input.getPolylines());
    output.setStopGroupings(copyStopGroupingList(input.getStopGroupings()));
    return output;
  }

  private List<StopGroupingBean> copyStopGroupingList(List<StopGroupingBean> stopGroupings) {
    if (stopGroupings == null)
      return null;
    List<StopGroupingBean> output = new ArrayList<>();
    for (StopGroupingBean input : stopGroupings) {
      output.add(copy(input));
    }
    return output;
  }

  private StopGroupingBean copy(StopGroupingBean input) {
    if (input == null) return null;
    StopGroupingBean output = new StopGroupingBean();
    output.setType(input.getType());
    output.setOrdered(input.isOrdered());
    output.setStopGroups(copy(input.getStopGroups()));
    return output;
  }

  private List<StopGroupBean> copy(List<StopGroupBean> inputs) {
    if (inputs == null) return null;
    List<StopGroupBean> output = new ArrayList<>();
    for (StopGroupBean input : inputs) {
      output.add(copy(input));
    }
    return output;
  }

  private StopGroupBean copy(StopGroupBean input) {
    if (input == null) return null;
    StopGroupBean output = new StopGroupBean();
    output.setId(input.getId());
    output.setName(input.getName());
    output.setPolylines(input.getPolylines());
    output.setStopIds(input.getStopIds());
    output.setSubGroups(copy(input.getSubGroups()));
    return output;
  }
    private String nullSafeGet(String id) {
    if (id == null) return Strings.EMPTY;
    return id;
  }

}
