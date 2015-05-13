/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.listener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.gtfs_realtime.archiver.model.EntitySelectorModel;
import org.onebusaway.gtfs_realtime.archiver.model.TripUpdateModel;
import org.onebusaway.gtfs_realtime.archiver.service.FeedService;
import org.onebusaway.gtfs_realtime.archiver.service.GtfsPersistor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ExtensionRegistry;
import com.google.transit.realtime.GtfsRealtimeConstants;
import com.google.transit.realtime.GtfsRealtimeOneBusAway;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

/**
 * 
 * Entry point for archiving GTFS-realtime.  Configure one of these (via spring configuration) for
 * each of your GTFS realtime sources.
 */
public class GtfsRealtimeArchiverTask {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeArchiverTask.class);
  
  private static final ExtensionRegistry _registry = ExtensionRegistry.newInstance();

  
  static {
    _registry.add(GtfsRealtimeOneBusAway.obaFeedEntity);
    _registry.add(GtfsRealtimeOneBusAway.obaTripUpdate);
  }
  
  private ScheduledExecutorService _scheduledExecutorService;

  private ScheduledFuture<?> _refreshTask;
  
  private FeedService _feedService;
  
  private URL _tripUpdatesUrl;

  private URL _vehiclePositionsUrl;

  private URL _alertsUrl;

  private int _refreshInterval = 30;
  
  
  @Autowired
  public void setScheduledExecutorService(
      ScheduledExecutorService scheduledExecutorService) {
    _log.info("executor=" + scheduledExecutorService);
    _scheduledExecutorService = scheduledExecutorService;
  }

  @Autowired
  public void setFeedService(FeedService feedService) {
    _feedService = feedService;
  }
  
  public void setTripUpdatesUrl(URL tripUpdatesUrl) {
    _tripUpdatesUrl = tripUpdatesUrl;
  }

  public void setVehiclePositionsUrl(URL vehiclePositionsUrl) {
    _vehiclePositionsUrl = vehiclePositionsUrl;
  }

  public void setAlertsUrl(URL alertsUrl) {
    _alertsUrl = alertsUrl;
  }

  @Autowired
  public void setRefreshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }

  @PostConstruct
  public void start() {
    _log.info("starting");
    if (_tripUpdatesUrl == null) {
      _log.warn("no tripUpdatesUrl configured.  This is most likely a configuration issue");
    }
    if (_vehiclePositionsUrl == null) {
      _log.warn("no vehiclePositionsUrl configured.  This is most likely a configuration issue");
    }
    if (_alertsUrl == null) {
      _log.warn("no alertsUrl configured.  This is most likely a configuration issue");
    }
    if (_refreshInterval > 0) {
      _log.info("scheduling executor for refresh=" + _refreshInterval);
      _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(
          new UpdateTask(), 0, _refreshInterval, TimeUnit.SECONDS);
    }

  }
  
  @PreDestroy
  public void stop() {
    _log.info("stopping");
    if (_refreshTask != null) {
      _refreshTask.cancel(true);
      _refreshTask = null;
    }  
  }
  
  public void update() throws IOException {
    FeedMessage tripUpdates = readOrReturnDefault(_tripUpdatesUrl);
    FeedMessage vehiclePositions = readOrReturnDefault(_vehiclePositionsUrl); // todo
    FeedMessage alerts = readOrReturnDefault(_alertsUrl); // todo
    
    _feedService.readTripUpdates(tripUpdates);
    _feedService.readVehiclePositions(vehiclePositions);
    _feedService.readAlerts(alerts);
  }
  
  private FeedMessage readOrReturnDefault(URL url) throws IOException {
    if (url == null) {
      FeedMessage.Builder builder = FeedMessage.newBuilder();
      FeedHeader.Builder header = FeedHeader.newBuilder();
      header.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
      builder.setHeader(header);
      return builder.build();
    }
    return readFeedFromUrl(url);
  }

  /**
   * 
   * @param url the {@link URL} to read from
   * @return a {@link FeedMessage} constructed from the protocol buffer content
   *         of the specified url
   * @throws IOException
   */
  private FeedMessage readFeedFromUrl(URL url) throws IOException {
    InputStream in = url.openStream();
    try {
      return FeedMessage.parseFrom(in, _registry);
    } finally {
      try {
        in.close();
      } catch (IOException ex) {
        _log.error("error closing url stream " + url);
      }
    }
  }
  
  
  private class UpdateTask implements Runnable {

    @Override
    public void run() {
      try {
        update();
      } catch (Throwable ex) {
        _log.warn("Error updating from GTFS-realtime data sources", ex);
      }
    }
  }
}
