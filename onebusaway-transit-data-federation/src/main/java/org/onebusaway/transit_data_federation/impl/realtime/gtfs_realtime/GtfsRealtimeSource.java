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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertLibrary;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Affects;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Consequence;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.Id;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtimeConstants;

public class GtfsRealtimeSource {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeSource.class);

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

  private List<String> _agencyIds = new ArrayList<String>();

  /**
   * We keep track of alerts, only pushing them to the underlying
   * {@link ServiceAlertsService} when they've been updated, since we'll often
   * see the same alert every time we poll the alert URL
   */
  private Map<AgencyAndId, ServiceAlert> _alertsById = new HashMap<AgencyAndId, ServiceAlerts.ServiceAlert>();

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

  public void setRefeshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }

  public void setAgencyId(String agencyId) {
    _agencyIds.add(agencyId);
  }

  public void setAgencyIds(List<String> agencyIds) {
    _agencyIds.addAll(agencyIds);
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
    _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(
        new RefreshTask(), 0, _refreshInterval, TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    if (_refreshTask != null) {
      _refreshTask.cancel(true);
      _refreshTask = null;
    }
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
  private synchronized void handeUpdates(FeedMessage tripUpdates,
      FeedMessage vehiclePositions, FeedMessage alerts) {

    handleTripUpdates(tripUpdates);
    handleAlerts(alerts);
  }

  private void handleTripUpdates(FeedMessage tripUpdates) {

    long t = System.currentTimeMillis();
    long timeFrom = t - 30 * 60 * 1000;
    long timeTo = t + 30 * 60 * 1000;

    for (FeedEntity entity : tripUpdates.getEntityList()) {

      TripUpdate tripUpdate = entity.getTripUpdate();
      if (tripUpdate == null) {
        _log.warn("epxected a FeedEntity with a TripUpdate");
        continue;
      }
      TripDescriptor tripDescriptor = tripUpdate.getTrip();
      if (!tripDescriptor.hasTripId()) {
        continue;
      }

      if (tripUpdate.getStopTimeUpdateCount() == 0)
        continue;

      StopTimeUpdate stopTimeUpdate = tripUpdate.getStopTimeUpdate(0);
      if (!(stopTimeUpdate.hasArrival() || stopTimeUpdate.hasDeparture()))
        continue;

      int delay = 0;
      boolean hasDelay = false;

      if (stopTimeUpdate.hasDeparture()) {
        StopTimeEvent departure = stopTimeUpdate.getDeparture();
        if (departure.hasDelay()) {
          delay = departure.getDelay();
          hasDelay = true;
        }
      }
      if (stopTimeUpdate.hasArrival()) {
        StopTimeEvent arrival = stopTimeUpdate.getArrival();
        if (arrival.hasDelay()) {
          delay = arrival.getDelay();
          hasDelay = true;
        }
      }

      if (!hasDelay)
        continue;

      TripEntry trip = getTrip(tripDescriptor.getTripId());
      if (trip == null) {
        _log.warn("no trip found with id=" + tripDescriptor.getTripId());
        continue;
      }

      BlockEntry block = trip.getBlock();

      List<BlockInstance> instances = _blockCalendarService.getActiveBlocks(
          block.getId(), timeFrom, timeTo);
      if (instances.isEmpty()) {
        _log.warn("could not find any active schedules instance for the specified trip="
            + trip.getId());
        continue;
      }

      BlockInstance instance = instances.get(0);
      VehicleLocationRecord record = new VehicleLocationRecord();
      record.setBlockId(block.getId());
      record.setScheduleDeviation(delay);
      record.setServiceDate(instance.getServiceDate());
      record.setTimeOfRecord(System.currentTimeMillis());
      // HACK - we assume we have access to a vehicle id to uniquely identify
      // records. But what if we don't?
      record.setVehicleId(block.getId());
      _vehicleLocationListener.handleVehicleLocationRecord(record);
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
        ServiceAlert.Builder serviceAlertBuilder = getAlertAsServiceAlert(id,
            alert);
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

  private ServiceAlert.Builder getAlertAsServiceAlert(AgencyAndId id,
      Alert alert) {
    ServiceAlert.Builder b = ServiceAlert.newBuilder();
    b.setCreationTime(System.currentTimeMillis());
    b.setModifiedTime(System.currentTimeMillis());
    b.setId(ServiceAlertLibrary.id(id));
    for (GtfsRealtime.TimeRange range : alert.getActivePeriodList()) {
      ServiceAlerts.TimeRange.Builder rangeBuilder = ServiceAlerts.TimeRange.newBuilder();
      if (range.hasStart())
        rangeBuilder.setStart(range.getStart());
      if (range.hasEnd())
        rangeBuilder.setEnd(range.getEnd());
      b.addActiveWindow(rangeBuilder);
    }
    if (alert.hasCause())
      b.setCause(convertCause(alert.getCause()));
    if (alert.hasHeaderText())
      b.setSummary(convertTranslatedString(alert.getHeaderText()));
    if (alert.hasDescriptionText())
      b.setDescription(convertTranslatedString(alert.getDescriptionText()));
    if (alert.hasEffect()) {
      Consequence.Builder consequence = Consequence.newBuilder();
      consequence.setEffect(convertEffect(alert.getEffect()));
      b.addConsequence(consequence);
    }
    for (EntitySelector selector : alert.getInformedEntityList()) {
      Affects.Builder affects = getEntitySelectorAsAffects(selector);
      b.addAffects(affects);
    }
    if (alert.hasUrl())
      b.setUrl(convertTranslatedString(alert.getUrl()));
    return b;
  }

  private Affects.Builder getEntitySelectorAsAffects(EntitySelector selector) {
    Affects.Builder affects = Affects.newBuilder();
    if (selector.hasAgencyId())
      affects.setAgencyId(selector.getAgencyId());
    if (selector.hasRouteId())
      affects.setRouteId(getRouteId(selector.getRouteId()));
    if (selector.hasStopId())
      affects.setTripId(getStopId(selector.getStopId()));
    if (selector.hasTrip()) {
      TripDescriptor trip = selector.getTrip();
      if (trip.hasTripId())
        affects.setTripId(getTripId(trip.getTripId()));
      else if (trip.hasRouteId())
        affects.setRouteId(getRouteId(trip.getRouteId()));
    }
    return affects;
  }

  private Id getRouteId(String routeId) {

    for (String agencyId : _agencyIds) {
      AgencyAndId id = new AgencyAndId(agencyId, routeId);
      RouteEntry route = _transitGraphDao.getRouteForId(id);
      if (route != null)
        return ServiceAlertLibrary.id(route.getParent().getId());
    }

    try {
      AgencyAndId id = AgencyAndId.convertFromString(routeId);
      RouteEntry route = _transitGraphDao.getRouteForId(id);
      if (route != null)
        return ServiceAlertLibrary.id(route.getParent().getId());
    } catch (IllegalArgumentException ex) {

    }

    _log.warn("route not found with id \"{}\"", routeId);

    AgencyAndId id = new AgencyAndId(_agencyIds.get(0), routeId);
    return ServiceAlertLibrary.id(id);
  }

  private Id getTripId(String tripId) {

    TripEntry trip = getTrip(tripId);
    if (trip != null)
      return ServiceAlertLibrary.id(trip.getId());

    _log.warn("trip not found with id \"{}\"", tripId);

    AgencyAndId id = new AgencyAndId(_agencyIds.get(0), tripId);
    return ServiceAlertLibrary.id(id);
  }

  private TripEntry getTrip(String tripId) {

    for (String agencyId : _agencyIds) {
      AgencyAndId id = new AgencyAndId(agencyId, tripId);
      TripEntry trip = _transitGraphDao.getTripEntryForId(id);
      if (trip != null)
        return trip;
    }

    try {
      AgencyAndId id = AgencyAndId.convertFromString(tripId);
      TripEntry trip = _transitGraphDao.getTripEntryForId(id);
      if (trip != null)
        return trip;
    } catch (IllegalArgumentException ex) {

    }
    return null;
  }

  private Id getStopId(String stopId) {

    for (String agencyId : _agencyIds) {
      AgencyAndId id = new AgencyAndId(agencyId, stopId);
      StopEntry stop = _transitGraphDao.getStopEntryForId(id);
      if (stop != null)
        return ServiceAlertLibrary.id(id);
    }

    try {
      AgencyAndId id = AgencyAndId.convertFromString(stopId);
      StopEntry stop = _transitGraphDao.getStopEntryForId(id);
      if (stop != null)
        return ServiceAlertLibrary.id(id);
    } catch (IllegalArgumentException ex) {

    }

    _log.warn("stop not found with id \"{}\"", stopId);

    AgencyAndId id = new AgencyAndId(_agencyIds.get(0), stopId);
    return ServiceAlertLibrary.id(id);
  }

  private ServiceAlert.Cause convertCause(Alert.Cause cause) {
    switch (cause) {
      case ACCIDENT:
        return ServiceAlert.Cause.ACCIDENT;
      case CONSTRUCTION:
        return ServiceAlert.Cause.CONSTRUCTION;
      case DEMONSTRATION:
        return ServiceAlert.Cause.DEMONSTRATION;
      case HOLIDAY:
        return ServiceAlert.Cause.HOLIDAY;
      case MAINTENANCE:
        return ServiceAlert.Cause.MAINTENANCE;
      case MEDICAL_EMERGENCY:
        return ServiceAlert.Cause.MEDICAL_EMERGENCY;
      case OTHER_CAUSE:
        return ServiceAlert.Cause.OTHER_CAUSE;
      case POLICE_ACTIVITY:
        return ServiceAlert.Cause.POLICE_ACTIVITY;
      case STRIKE:
        return ServiceAlert.Cause.STRIKE;
      case TECHNICAL_PROBLEM:
        return ServiceAlert.Cause.TECHNICAL_PROBLEM;
      case UNKNOWN_CAUSE:
        return ServiceAlert.Cause.UNKNOWN_CAUSE;
      case WEATHER:
        return ServiceAlert.Cause.WEATHER;
      default:
        _log.warn("unknown GtfsRealtime.Alert.Cause " + cause);
        return ServiceAlert.Cause.UNKNOWN_CAUSE;
    }
  }

  private Consequence.Effect convertEffect(Alert.Effect effect) {
    switch (effect) {
      case ADDITIONAL_SERVICE:
        return Consequence.Effect.ADDITIONAL_SERVICE;
      case DETOUR:
        return Consequence.Effect.DETOUR;
      case MODIFIED_SERVICE:
        return Consequence.Effect.MODIFIED_SERVICE;
      case NO_SERVICE:
        return Consequence.Effect.NO_SERVICE;
      case OTHER_EFFECT:
        return Consequence.Effect.OTHER_EFFECT;
      case REDUCED_SERVICE:
        return Consequence.Effect.REDUCED_SERVICE;
      case SIGNIFICANT_DELAYS:
        return Consequence.Effect.SIGNIFICANT_DELAYS;
      case STOP_MOVED:
        return Consequence.Effect.STOP_MOVED;
      case UNKNOWN_EFFECT:
        return Consequence.Effect.UNKNOWN_EFFECT;
      default:
        _log.warn("unknown GtfsRealtime.Alert.Effect " + effect);
        return Consequence.Effect.UNKNOWN_EFFECT;

    }
  }

  private ServiceAlerts.TranslatedString convertTranslatedString(
      GtfsRealtime.TranslatedString string) {
    ServiceAlerts.TranslatedString.Builder b = ServiceAlerts.TranslatedString.newBuilder();
    for (GtfsRealtime.TranslatedString.Translation translation : string.getTranslationList()) {
      ServiceAlerts.TranslatedString.Translation.Builder tb = ServiceAlerts.TranslatedString.Translation.newBuilder();
      if (translation.hasLanguage())
        tb.setLanguage(translation.getLanguage());
      tb.setText(translation.getText());
      b.addTranslation(tb);
    }
    return b.build();
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
    InputStream in = url.openStream();
    try {
      return FeedMessage.parseFrom(in);
    } finally {
      try {
        in.close();
      } catch (IOException ex) {
        _log.error("error closing url stream " + url);
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
        FeedMessage tripUpdates = readOrReturnDefault(_tripUpdatesUrl);
        FeedMessage vehiclePositions = readOrReturnDefault(_vehiclePositionsUrl);
        FeedMessage alerts = readOrReturnDefault(_alertsUrl);
        handeUpdates(tripUpdates, vehiclePositions, alerts);
      } catch (Throwable ex) {
        _log.warn("Error updating from GTFS-realtime data sources", ex);
      }
    }

  }

}
