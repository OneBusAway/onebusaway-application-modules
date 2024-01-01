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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.DynamicHelper;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
/**
 * Canceling service when GTFS-RT input is replacing the existing service.
 */
public class GtfsRealtimeCancelServiceImpl implements GtfsRealtimeCancelService {

  private static Logger _log = LoggerFactory.getLogger(GtfsRealtimeCancelServiceImpl.class);
  private TransitGraphDao _transitGraphDao;
  private VehicleLocationListener _vehicleLocationListener;

  private TransitDataService _tds;

  private DynamicHelper helper = new DynamicHelper();

  private Map<List<AgencyAndId>, Set<RouteEntry>> _cache = new HashMap();

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setVehicleLocationListener(
          VehicleLocationListener vehicleLocationListener) {
    _vehicleLocationListener = vehicleLocationListener;
  }

  @Autowired
  public void setTransitDataService(TransitDataService tds) {
    _tds = tds;
  }

  @Override
  public Set<RouteEntry> findRoutesForIds(List<AgencyAndId> ids) {
    Set<RouteEntry> routeSet = new HashSet<>();
    List<RouteEntry> routes = _transitGraphDao.getAllRoutes();
    if (routes == null) return null;
    for (RouteEntry routeEntry : routes) {
      if (routeSet == null) {
        routeSet = new HashSet<>();
      }
      for (AgencyAndId routeId : ids) {
        if (routeId.equals(routeEntry.getId())) {
          routeSet.add(routeEntry);
        }
      }
    }
    return routeSet;
  }

  public List<TripDetailsBean> findActiveTripsForRoutes(Set<RouteEntry> routes, long timestamp) {
    List<TripDetailsBean> beans = new ArrayList<>();
    if (routes == null) return beans;
    for (RouteEntry route : routes) {
      beans.addAll(findActiveTripsForRoute(route, timestamp));
    }
    return beans;
  }
  @Override
  public List<TripDetailsBean> findActiveTripsForRoute(RouteEntry route, long timestamp) {
    List<TripDetailsBean> activeTrips = new ArrayList<>();
    TripsForRouteQueryBean query = new TripsForRouteQueryBean();
    query.setRouteId(AgencyAndId.convertToString(route.getId()));
    query.setMaxCount(5000);
    query.setTime(timestamp);
    TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean(true, false, true);
    query.setInclusion(inclusion);
    ListBean<TripDetailsBean> tripsForRoute = _tds.getTripsForRoute(query);
    if (tripsForRoute != null) {
      for (TripDetailsBean bean :tripsForRoute.getList()) {
        if (!isDynamic(bean))
        activeTrips.add(bean);
      }
    }
    return activeTrips;
  }

  private boolean isDynamic(TripDetailsBean bean) {
    if (bean == null || bean.getTrip() == null || bean.getTrip().getServiceId() == null)
      return false;
    return helper.isServiceIdDynamic(bean.getTrip().getServiceId());
  }

  @Override
  public void cancel(List<TripDetailsBean> tripsToCancel) {
    for (TripDetailsBean trip : tripsToCancel) {
      VehicleLocationRecord record = createVehicleLocationRecord(trip);
      if (record != null) {
        _vehicleLocationListener.handleVehicleLocationRecord(record);
      }
    }
  }


  @Override
  public void cancelServiceForRoutes(List<AgencyAndId> routeIdsToCancel, long timestamp) {
    Set<RouteEntry> routeEntries = _cache.get(routeIdsToCancel);
    if (routeEntries == null) {
      routeEntries = findRoutesForIds(routeIdsToCancel);
      if (routeEntries == null) return;
      _cache.put(routeIdsToCancel, routeEntries);
    }
    cancel(findActiveTripsForRoutes(routeEntries, timestamp));
    _log.info("canceled service for {}", routeIdsToCancel);
  }

  private VehicleLocationRecord createVehicleLocationRecord(TripDetailsBean trip) {
    VehicleLocationRecord record = new VehicleLocationRecord();
    record.setTimeOfRecord(System.currentTimeMillis());
    if (trip == null || trip.getTrip() == null || trip.getTrip().getBlockId() == null) {
      return null;
    }
    record.setBlockId(AgencyAndId.convertFromString(trip.getTrip().getBlockId()));
    record.setStatus(TransitDataConstants.STATUS_CANCELED);
    record.setTripId(AgencyAndId.convertFromString(trip.getTripId()));
    record.setServiceDate(new ServiceDate().getAsDate().getTime());
    return record;
  }

}
