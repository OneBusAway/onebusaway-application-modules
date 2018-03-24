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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import com.google.protobuf.ExtensionRegistry;
import com.google.transit.realtime.GtfsRealtimeConstants;
import com.google.transit.realtime.GtfsRealtimeOneBusAway;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

/**
 * 
 * Entry point for archiving GTFS-realtime. Configure one of these (via spring
 * configuration) for each of your GTFS realtime sources.
 */
public class GtfsRealtimeArchiverTask extends RealtimeArchiverTask {

  private static final ExtensionRegistry _registry = ExtensionRegistry.newInstance();

  static {
    _registry.add(GtfsRealtimeOneBusAway.obaFeedEntity);
    _registry.add(GtfsRealtimeOneBusAway.obaTripUpdate);
  }

  private TransitGraphDao _transitGraphDao;

  private URL _tripUpdatesUrl;

  private URL _vehiclePositionsUrl;

  private URL _alertsUrl;

  private List<String> _agencyIds = new ArrayList<String>();

  private GtfsRealtimeEntitySource _entitySource;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
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

  public void setAgencyId(String agencyId) {
    _agencyIds.add(agencyId);
  }

  public void setAgencyIds(List<String> agencyIds) {
    _agencyIds.addAll(agencyIds);
  }

  public List<String> getAgencyIds() {
    return _agencyIds;
  }

  protected void init() {
    while (!initialized) {
      _log.info("Still waiting for context initialization");
      try {
        TimeUnit.SECONDS.sleep(10);
      } catch (InterruptedException ex) {
        // don't handle exception
      }
    }
    if (_agencyIds.isEmpty()) {
      _log.info("no agency ids specified for GtfsRealtimeSource");

      for (AgencyEntry agency : _transitGraphDao.getAllAgencies()) {
        _agencyIds.add(agency.getId());
      }
      if (_agencyIds.size() > 3) {
        _log.warn("The default agency id set is quite large (n="
            + _agencyIds.size()
            + ").  You might consider specifying the applicable agencies for your GtfsRealtimeSource.");
      }
    }
    _log.info("Number of agencies: " + _agencyIds.size());
    for (String agency : _agencyIds) {
      _log.info("Agency id: " + agency);
    }

    _entitySource = new GtfsRealtimeEntitySource();
    _entitySource.setAgencyIds(_agencyIds);
    _entitySource.setTransitGraphDao(_transitGraphDao);

    if (_tripUpdatesUrl == null) {
      _log.warn(
          "no tripUpdatesUrl configured.  This is most likely a configuration issue");
    }
    if (_vehiclePositionsUrl == null) {
      _log.warn(
          "no vehiclePositionsUrl configured.  This is most likely a configuration issue");
    }
    if (_alertsUrl == null) {
      _log.warn(
          "no alertsUrl configured.  This is most likely a configuration issue");
    }
    if (_refreshInterval > 0) {
      _log.info("scheduling executor for refresh=" + _refreshInterval);
      _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(
          new UpdateTask(), 0, _refreshInterval, TimeUnit.SECONDS);
    }

  }

  public void update() throws IOException {
    FeedMessage tripUpdates = readOrReturnDefault(_tripUpdatesUrl);
    FeedMessage vehiclePositions = readOrReturnDefault(_vehiclePositionsUrl);
    FeedMessage alerts = readOrReturnDefault(_alertsUrl);

    _feedService.readTripUpdates(tripUpdates, _entitySource);
    _feedService.readVehiclePositions(vehiclePositions, _entitySource);
    _feedService.readAlerts(alerts, _entitySource);
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

  protected class UpdateTask implements Runnable {

    @Override
    public void run() {
      try {
        update();
      } catch (Throwable ex) {
        _log.warn("Error updating from GTFS-realtime data sources for url= " + _tripUpdatesUrl, ex);
      }
    }
  }

}
