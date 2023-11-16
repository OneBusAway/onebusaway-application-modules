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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
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
  private List<AgencyAndId> _routeIds = new ArrayList<>();

  private GtfsRealtimeCancelService _service;
  private ScheduledFuture<?> _refreshTask;
  private ScheduledExecutorService _scheduledExecutorService;

  @Autowired
  public void setGtfsRealtimeCancelService(GtfsRealtimeCancelService service) {
    _service = service;
  }

  public void setRefreshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }

  @Autowired
  public void setScheduledExecutorService(
          ScheduledExecutorService scheduledExecutorService) {
    _scheduledExecutorService = scheduledExecutorService;
  }

  public void setRouteIds(String routeIdAsCsv) {
    if (routeIdAsCsv != null)
      if (routeIdAsCsv.contains(",")) {
        for (String s : routeIdAsCsv.split(",")) {
          try {
            _routeIds.add(AgencyAndId.convertFromString(s));
          } catch (IllegalStateException ise) {
            _log.error("invalid route {}", s);
          }
        }
      } else {
        try {
          _routeIds.add(AgencyAndId.convertFromString(routeIdAsCsv)); // single element
        } catch (IllegalStateException ise) {
          _log.error("invalid route {}", routeIdAsCsv);
        }
      }
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
    if (_routeIds != null)
      _service.cancelServiceForRoutes(_routeIds, System.currentTimeMillis());
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
