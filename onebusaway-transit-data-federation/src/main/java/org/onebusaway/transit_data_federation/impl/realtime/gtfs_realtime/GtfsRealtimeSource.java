/**
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ExtensionRegistry;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtimeConstants;
import com.google.transit.realtime.GtfsRealtimeOneBusAway;

public class GtfsRealtimeSource implements MonitoredDataSource {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeSource.class);

  private static final ExtensionRegistry _registry = ExtensionRegistry.newInstance();

  static {
    _registry.add(GtfsRealtimeOneBusAway.obaFeedEntity);
    _registry.add(GtfsRealtimeOneBusAway.obaTripUpdate);
  }

  private AgencyService _agencyService;

  private TransitGraphDao _transitGraphDao;

  private BlockCalendarService _blockCalendarService;

  private VehicleLocationListener _vehicleLocationListener;

  private ServiceAlertsService _serviceAlertService;

  private ScheduledExecutorService _scheduledExecutorService;

  private ScheduledFuture<?> _refreshTask;

  private URL _tripUpdatesUrl;

  private URL _vehiclePositionsUrl;

  private URL _alertsUrl;

  private int _refreshInterval = 30;
  
  private Map<String,String> _headersMap;
  
  private Map _alertAgencyIdMap;

  private List<String> _agencyIds = new ArrayList<String>();

  /**
   * We keep track of vehicle location updates, only pushing them to the
   * underling {@link VehicleLocationListener} when they've been updated, since
   * we'll often see the same trip updates and vehicle positions every time we
   * poll the GTFS-realtime feeds. We keep track of the timestamp of last update
   * for each vehicle id.
   */
  private Map<AgencyAndId, Date> _lastVehicleUpdate = new HashMap<AgencyAndId, Date>();

  /**
   * We keep track of alerts, only pushing them to the underlying
   * {@link ServiceAlertsService} when they've been updated, since we'll often
   * see the same alert every time we poll the alert URL
   */
  private Map<AgencyAndId, ServiceAlert> _alertsById = new HashMap<AgencyAndId, ServiceAlerts.ServiceAlert>();

  private GtfsRealtimeEntitySource _entitySource;

  private GtfsRealtimeTripLibrary _tripsLibrary;

  private GtfsRealtimeAlertLibrary _alertLibrary;
  
  private MonitoredResult _monitoredResult = new MonitoredResult();
  

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _agencyService = agencyService;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Autowired
  public void setVehicleLocationListener(
      VehicleLocationListener vehicleLocationListener) {
    _vehicleLocationListener = vehicleLocationListener;
  }

  @Autowired
  public void setServiceAlertService(ServiceAlertsService serviceAlertService) {
    _serviceAlertService = serviceAlertService;
  }

  @Autowired
  public void setScheduledExecutorService(
      ScheduledExecutorService scheduledExecutorService) {
    _scheduledExecutorService = scheduledExecutorService;
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

  public void setRefreshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }
  
  public void setHeadersMap(Map<String,String> headersMap) {
	_headersMap = headersMap;
  }
  
  public void setAlertAgencyIdMap(Map alertAgencyIdMap) {
	_alertAgencyIdMap = alertAgencyIdMap;
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
  
  public void setMonitoredResult(MonitoredResult result) {
    _monitoredResult = result;
  }
  
  public MonitoredResult getMonitoredResult() {
    return _monitoredResult;
  }
  
  @PostConstruct
  public void start() {
    if (_agencyIds.isEmpty()) {
      _log.info("no agency ids specified for GtfsRealtimeSource, so defaulting to full agency id set");
      List<String> agencyIds = _agencyService.getAllAgencyIds();
      _agencyIds.addAll(agencyIds);
      if (_agencyIds.size() > 3) {
        _log.warn("The default agency id set is quite large (n="
            + _agencyIds.size()
            + ").  You might consider specifying the applicable agencies for your GtfsRealtimeSource.");
      }
    }

    _entitySource = new GtfsRealtimeEntitySource();
    _entitySource.setAgencyIds(_agencyIds);
    _entitySource.setTransitGraphDao(_transitGraphDao);

    _tripsLibrary = new GtfsRealtimeTripLibrary();
    _tripsLibrary.setBlockCalendarService(_blockCalendarService);
    _tripsLibrary.setEntitySource(_entitySource);

    _alertLibrary = new GtfsRealtimeAlertLibrary();
    _alertLibrary.setEntitySource(_entitySource);

    if (_refreshInterval > 0) {
      _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(
          new RefreshTask(), 0, _refreshInterval, TimeUnit.SECONDS);
    }
  }

  @PreDestroy
  public void stop() {
    if (_refreshTask != null) {
      _refreshTask.cancel(true);
      _refreshTask = null;
    }
  }

  public void refresh() throws IOException {
    FeedMessage tripUpdates = readOrReturnDefault(_tripUpdatesUrl);
    FeedMessage vehiclePositions = readOrReturnDefault(_vehiclePositionsUrl);
    FeedMessage alerts = readOrReturnDefault(_alertsUrl);
    MonitoredResult result = new MonitoredResult();
    result.setAgencyIds(_agencyIds);
    handeUpdates(result, tripUpdates, vehiclePositions, alerts);
    // update reference in a thread safe manner
    _monitoredResult = result;
  }

  /****
   * Private Methods
   ****/

  /**
   * 
   * @param tripUpdates
   * @param vehiclePositions
   * @param alerts
   */
  private synchronized void handeUpdates(MonitoredResult result, FeedMessage tripUpdates,
      FeedMessage vehiclePositions, FeedMessage alerts) {

    List<CombinedTripUpdatesAndVehiclePosition> combinedUpdates = _tripsLibrary.groupTripUpdatesAndVehiclePositions(result,
        tripUpdates, vehiclePositions);
    result.setRecordsTotal(combinedUpdates.size());
    handleCombinedUpdates(result, combinedUpdates);
    handleAlerts(alerts);
  }

  private void handleCombinedUpdates(MonitoredResult result,
      List<CombinedTripUpdatesAndVehiclePosition> updates) {

    Set<AgencyAndId> seenVehicles = new HashSet<AgencyAndId>();

    for (CombinedTripUpdatesAndVehiclePosition update : updates) {
      VehicleLocationRecord record = _tripsLibrary.createVehicleLocationRecordForUpdate(result, update);
      if (record != null) {
        if (record.getTripId() != null) {
          result.addUnmatchedTripId(record.getTripId().toString());
        }
        AgencyAndId vehicleId = record.getVehicleId();
        seenVehicles.add(vehicleId);
        Date timestamp = new Date(record.getTimeOfRecord());
        Date prev = _lastVehicleUpdate.get(vehicleId);
        if (prev == null || prev.before(timestamp)) {
          _vehicleLocationListener.handleVehicleLocationRecord(record);
          _lastVehicleUpdate.put(vehicleId, timestamp);
        }
      }
    }

    Calendar c = Calendar.getInstance();
    c.add(Calendar.MINUTE, -15);
    Date staleRecordThreshold = c.getTime();

    Iterator<Map.Entry<AgencyAndId, Date>> it = _lastVehicleUpdate.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<AgencyAndId, Date> entry = it.next();
      AgencyAndId vehicleId = entry.getKey();
      Date lastUpdateTime = entry.getValue();
      if (!seenVehicles.contains(vehicleId)
          && lastUpdateTime.before(staleRecordThreshold)) {
        it.remove();
      }
    }
  }

  private void handleAlerts(FeedMessage alerts) {
    for (FeedEntity entity : alerts.getEntityList()) {
      Alert alert = entity.getAlert();
      if (alert == null) {
        _log.warn("epxected a FeedEntity with an Alert");
        continue;
      }

      AgencyAndId id = createId(entity.getId());

      if (entity.getIsDeleted()) {
        _alertsById.remove(id);
        _serviceAlertService.removeServiceAlert(id);
      } else {
        ServiceAlert.Builder serviceAlertBuilder = _alertLibrary.getAlertAsServiceAlert(
            id, alert, _alertAgencyIdMap);
        ServiceAlert serviceAlert = serviceAlertBuilder.build();
        ServiceAlert existingAlert = _alertsById.get(id);
        if (existingAlert == null || !existingAlert.equals(serviceAlert)) {
          _alertsById.put(id, serviceAlert);
          _serviceAlertService.createOrUpdateServiceAlert(serviceAlertBuilder,
              _agencyIds.get(0));
        }
      }
    }
  }

  private AgencyAndId createId(String id) {
    return new AgencyAndId(_agencyIds.get(0), id);
  }

  /**
   * 
   * @param url
   * @return a {@link FeedMessage} constructed from the protocol buffer conent
   *         of the specified url, or a default empty {@link FeedMessage} if the
   *         url is null
   * @throws IOException
   */
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
   URLConnection urlConnection = url.openConnection();
   setHeadersToUrlConnection(urlConnection);
   InputStream in = urlConnection.getInputStream();
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
/**
 * Set the headers to the urlConnection if any
 * @param urlConnection
 * @return, the urlConnection with the headers set
 */
  private void setHeadersToUrlConnection(URLConnection urlConnection) {
   if (_headersMap != null) {
	  for (Map.Entry<String, String> headerEntry : _headersMap.entrySet()) {
	    urlConnection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
	  }
	}
  }
  /****
   *
   ****/

  private class RefreshTask implements Runnable {

    @Override
    public void run() {
      try {
        refresh();
      } catch (Throwable ex) {
        _log.warn("Error updating from GTFS-realtime data sources", ex);
      }
    }
  }
}
