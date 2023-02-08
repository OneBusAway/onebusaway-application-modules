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

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Define this spring bean to cancel all active service for
 * a route for testing purposes.
 */
public class GtfsRealtimeCanceledHarness {

  private static Logger _log = LoggerFactory.getLogger(GtfsRealtimeCanceledHarness.class);

  // set this to be greater than zero to activate
  private int _refreshInterval = 0;
  private String _routeId = null;

  private ScheduledFuture<?> _refreshTask;
  private ScheduledExecutorService _scheduledExecutorService;
  private TransitGraphDao _transitGraphDao;
  private VehicleLocationListener _vehicleLocationListener;

  public void setRefreshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }

  @Autowired
  public void setScheduledExecutorService(
          ScheduledExecutorService scheduledExecutorService) {
    _scheduledExecutorService = scheduledExecutorService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setVehicleLocationListener(
          VehicleLocationListener vehicleLocationListener) {
    _vehicleLocationListener = vehicleLocationListener;
  }


  public void setRouteId(String routeId) {
    _routeId = routeId;
  }

  @PostConstruct
  public void start() {
    if (_refreshInterval > 0) {
      _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(
              new RefreshTask(), 0, _refreshInterval, TimeUnit.SECONDS);
    }
  }

  @PreDestroy
  public void shutdown() {
    if (_refreshTask != null) {
      _refreshTask.cancel(true);
      _refreshTask = null;
    }
  }

  public void refresh() {
    ArrayList<TripEntry> tripsToCancel = new ArrayList<>();
    for (TripEntry trip : _transitGraphDao.getAllTrips()) {
      if (select(trip.getRoute())) {
        if (isActive(trip)) {
          tripsToCancel.add(trip);
        }
      }
    }


    for (TripEntry trip : tripsToCancel) {
      VehicleLocationRecord record = createVehicleLocationRecord(trip);
      if (record != null) {
        _vehicleLocationListener.handleVehicleLocationRecord(record);
      }
    }
    _log.info("cancelled all service for route {}", _routeId);
  }

  private VehicleLocationRecord createVehicleLocationRecord(TripEntry trip) {
    VehicleLocationRecord record = new VehicleLocationRecord();
    record.setTimeOfRecord(System.currentTimeMillis());
    record.setBlockId(trip.getBlock().getId());
    record.setStatus(TransitDataConstants.STATUS_CANCELED);
    record.setTripId(trip.getId());
    record.setServiceDate(new ServiceDate().getAsDate().getTime());
    return record;
  }

  private boolean isActive(TripEntry trip) {
    return true; // todo: for now we don't care
  }

  private boolean select(RouteEntry route) {
    if (_routeId != null)
      return _routeId.equals(route.getId().getId());
    return false;
  }

  private class RefreshTask implements Runnable {
    @Override
    public void run() {
      try {
        refresh();
      } catch (Throwable t) {
        _log.error("refresh exception: ", t, t);
      }
    }
  }
}
