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

import org.onebusaway.cloud.api.ExternalServices;
import org.onebusaway.cloud.api.ExternalServicesBridgeFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitoring Implementation of Data Source metrics.
 */
@Component
public class DataSourceMonitorImpl implements DataSourceMonitor {

  private boolean _initialized = false;
  private long _refreshIntervalMillis = 60 * 1000;
  private ExternalServices _externalServices = null;
  private Map<String, Long> _lastUpdateByFeedId = new HashMap<>();

  @Override
  public void logUpdate(MonitoredResult result) {
    ExternalServices es = new ExternalServicesBridgeFactory().getExternalServices();
    if (es != null) {
      logUpdate(es, result);
    }
  }

  private void logUpdate(ExternalServices es, MonitoredResult result) {
    long lastUpdate = getLastUpdate(result);
    if (hasUpdateExpired(lastUpdate)) {
      publishMetric(es, result);
      markUpdated(result);
    }
  }

  private void markUpdated(MonitoredResult result) {
    String feedId = result.getFeedId();
    _lastUpdateByFeedId.put(feedId, System.currentTimeMillis());
  }

  private void publishMetric(ExternalServices es, MonitoredResult result) {
    String feedId = result.getFeedId();
    String env = System.getProperty("oba.cloud.env");
    if (env == null) env = "test";
    String bypass = System.getProperty("oba.gtfsrt.skip_monitor");
    if ("true".equals(bypass)) {
      return;
    }

    es.publishMetric(env, "Matched", "feed", feedId, result.getMatchedTripIds().size());
    es.publishMetric(env, "Added", "feed", feedId, result.getAddedTripIds().size());
    es.publishMetric(env, "Duplicated", "feed", feedId, result.getDuplicatedTripIds().size());
    es.publishMetric(env, "Canceled", "feed", feedId, result.getCancelledTripIds().size());
    es.publishMetric(env, "Unmatched", "feed", feedId, result.getUnmatchedTripIds().size());
    es.publishMetric(env, "Total", "feed", feedId,
            result.getMatchedTripIds().size()
            + result.getAddedTripIds().size()
            + result.getDuplicatedTripIds().size()
            + result.getCancelledTripIds().size()
            + result.getUnmatchedTripIds().size());
  }

  private boolean hasUpdateExpired(long lastUpdate) {
    return lastUpdate + _refreshIntervalMillis < System.currentTimeMillis();
  }

  private long getLastUpdate(MonitoredResult result) {
    String feedId = result.getFeedId();
    if (_lastUpdateByFeedId.containsKey(feedId))
      return _lastUpdateByFeedId.get(feedId);
    return 0l;
  }

  private ExternalServices getExternalServices() {
    if (!_initialized) {
      // only try once to set this up
      try {
        _externalServices = new ExternalServicesBridgeFactory().getExternalServices();
      } finally {
        _initialized = true;
      }
    }
    return _externalServices;
  }
}
