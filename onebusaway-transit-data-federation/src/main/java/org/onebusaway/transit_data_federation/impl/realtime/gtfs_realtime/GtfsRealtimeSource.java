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

import java.io.File;
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

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.service_alerts.ECause;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertLocalizedString;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertRecord;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertSituationConsequenceClause;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertTimeRange;
import org.onebusaway.transit_data_federation.impl.service_alerts.ServiceAlertsSituationAffectsClause;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
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
import com.google.transit.realtime.GtfsRealtimeConstants;
import com.google.transit.realtime.GtfsRealtimeOneBusAway;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

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

  private BlockLocationService _blockLocationService;

  private VehicleLocationListener _vehicleLocationListener;

  private ServiceAlertsService _serviceAlertService;

  private ScheduledExecutorService _scheduledExecutorService;

  private ScheduledFuture<?> _refreshTask;

  private URL _tripUpdatesUrl;

  private String _sftpTripUpdatesUrl;

  private URL _vehiclePositionsUrl;

  private String _sftpVehiclePositionsUrl;

  private URL _alertsUrl;

  private String _sftpAlertsUrl;

  private int _refreshInterval = 30;

  private Integer _maxDeltaLocationMeters = null; // by default don't validate

  private boolean _showNegativeScheduledArrivals = true;

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
  
  private String _feedId = null;
  
  private StopModificationStrategy _stopModificationStrategy = null;
  
  private boolean _scheduleAdherenceFromLocation = false;

  private BlockGeospatialService _blockGeospatialService;
  
  private boolean _enabled = true;

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
  public void setBlockLocationService(BlockLocationService blockLocationService) {
    _blockLocationService = blockLocationService;
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
  
  @Autowired
  public void setBlockGeospatialService(BlockGeospatialService blockGeospatialService) {
    _blockGeospatialService = blockGeospatialService;
  }

  public void setStopModificationStrategy(StopModificationStrategy strategy) {
    _stopModificationStrategy = strategy;
  }


  public void setTripUpdatesUrl(URL tripUpdatesUrl) {
    _tripUpdatesUrl = tripUpdatesUrl;
  }
  
  public URL getTripUpdatesUrl() {
    return _tripUpdatesUrl;
  }

  public void setSftpTripUpdatesUrl(String sftpTripUpdatesUrl) {
    _sftpTripUpdatesUrl = sftpTripUpdatesUrl;
  }

  public void setVehiclePositionsUrl(URL vehiclePositionsUrl) {
    _vehiclePositionsUrl = vehiclePositionsUrl;
  }

  public URL getVehiclePositionsUrl() {
    return _vehiclePositionsUrl;
  }
  
  public void setSftpVehiclePositionsUrl(String sftpVehiclePositionsUrl) {
    _sftpVehiclePositionsUrl = sftpVehiclePositionsUrl;
  }

  public void setAlertsUrl(URL alertsUrl) {
    _alertsUrl = alertsUrl;
  }

  public URL getAlertsUrl() {
    return _alertsUrl;
  }
  
  public void setSftpAlertsUrl(String sftpAlertsUrl) {
    _sftpAlertsUrl = sftpAlertsUrl;
  }

  public void setRefreshInterval(int refreshInterval) {
    _refreshInterval = refreshInterval;
  }
  
  public int getRefreshInterval() {
    return _refreshInterval;
  }

  /**
   * add validation to drop record if difference between calculated and reported positions is greater
   * than max.  If null, validation is not applied.  Distance is in meters.
   * @param max
   */
  public void setMaxDeltaLocationMeters(Integer max) { _maxDeltaLocationMeters = max; }

  public Integer getMaxDeltaLocationMeters() { return _maxDeltaLocationMeters; }

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
  
  public void setShowNegativeScheduledArrivals(boolean _showNegativeScheduledArrivals) {
    this._showNegativeScheduledArrivals = _showNegativeScheduledArrivals;
  }
  
  public boolean getShowNegativeScheduledArrivals() {
    return _showNegativeScheduledArrivals;
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
  
  public String getFeedId() {
    return _feedId;
  }
  
  public void setFeedId(String id) {
    _feedId = id;
  }
  
  public void setScheduleAdherenceFromLocation(boolean scheduleAdherenceFromLocation) {
    _scheduleAdherenceFromLocation = scheduleAdherenceFromLocation;
  }
  
  public void setEnabled(boolean enabled) {
    this._enabled = enabled;
  }
  
  public boolean getEnabled() {
    return _enabled;
  }
  
  public GtfsRealtimeTripLibrary getGtfsRealtimeTripLibrary() {
    return _tripsLibrary;
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
    if (_stopModificationStrategy != null) {
      _tripsLibrary.setStopModificationStrategy(_stopModificationStrategy);
    }
    _tripsLibrary.setScheduleAdherenceFromLocation(_scheduleAdherenceFromLocation);
    _tripsLibrary.setBlockGeospatialService(_blockGeospatialService);
    
    _alertLibrary = new GtfsRealtimeAlertLibrary();
    _alertLibrary.setEntitySource(_entitySource);

    if (_refreshInterval > 0) {
      _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(
          new RefreshTask(), 0, _refreshInterval, TimeUnit.SECONDS);
    }
  }
  
  public void reset() {
    _lastVehicleUpdate.clear();
  }

  @PreDestroy
  public void stop() {
    if (_refreshTask != null) {
      _refreshTask.cancel(true);
      _refreshTask = null;
    }
  }

  public void refresh() throws IOException {
    FeedMessage tripUpdates = _sftpTripUpdatesUrl != null ?
        readOrReturnDefault(_sftpTripUpdatesUrl)
        : readOrReturnDefault(_tripUpdatesUrl);
    FeedMessage vehiclePositions = _sftpVehiclePositionsUrl != null ?
        readOrReturnDefault(_sftpVehiclePositionsUrl)
        : readOrReturnDefault(_vehiclePositionsUrl);
    FeedMessage alerts = _sftpAlertsUrl != null ?
        readOrReturnDefault(_sftpAlertsUrl)
        : readOrReturnDefault(_alertsUrl);
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
	  
	long time = tripUpdates.getHeader().getTimestamp() * 1000;
	_tripsLibrary.setCurrentTime(time);
	
    List<CombinedTripUpdatesAndVehiclePosition> combinedUpdates = _tripsLibrary.groupTripUpdatesAndVehiclePositions(result,
        tripUpdates, vehiclePositions);
    result.setRecordsTotal(combinedUpdates.size());
    handleCombinedUpdates(result, combinedUpdates);
    cacheVehicleLocations(vehiclePositions);
    handleAlerts(alerts);
  }

  private void cacheVehicleLocations(FeedMessage vehiclePositions) {


    for (FeedEntity entity : vehiclePositions.getEntityList()) {
      if (entity.hasVehicle()) {
        GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
        _vehicleLocationListener.handleRawPosition(
                new AgencyAndId(getAgencyIds().get(0), vehicle.getVehicle().getId()),
                vehicle.getPosition().getLatitude(),
                vehicle.getPosition().getLongitude(),
                vehicle.getTimestamp());
      }
    }
  }

  private void handleCombinedUpdates(MonitoredResult result,
      List<CombinedTripUpdatesAndVehiclePosition> updates) {

    Set<AgencyAndId> seenVehicles = new HashSet<AgencyAndId>();

    for (CombinedTripUpdatesAndVehiclePosition update : updates) {
      VehicleLocationRecord record = _tripsLibrary.createVehicleLocationRecordForUpdate(result, update);
      if (record != null) {
        if (record.getTripId() != null) {
          // tripId will be null if block was matched
          result.addUnmatchedTripId(record.getTripId().toString());
        }
        AgencyAndId vehicleId = record.getVehicleId();
        // here we try to get a more accurate count of updates
        // some providers re-send old data or future data cluttering the feed
        // the TDS will discard these
        if (blockNotActive(record)) {
          _log.debug("discarding v: " + vehicleId + " as block not active");
          continue;
        }
        if (!isValidLocation(record, update)) {
          _log.debug("discarding v: " + vehicleId + " as location is bad");
          continue;
        }
        seenVehicles.add(vehicleId);
        Date timestamp = new Date(record.getTimeOfRecord());
        Date prev = _lastVehicleUpdate.get(vehicleId);
        if (prev == null || prev.before(timestamp)) {
          _log.debug("matched vehicle " + vehicleId + " on block=" + record.getBlockId() + " with scheduleDeviation=" + record.getScheduleDeviation());
          _vehicleLocationListener.handleVehicleLocationRecord(record);
          _lastVehicleUpdate.put(vehicleId, timestamp);
        }
      }
    }

    Calendar c = Calendar.getInstance();
    c.add(Calendar.MINUTE, -15);
    Date staleRecordThreshold = c.getTime();
    long newestUpdate = 0; 
    Iterator<Map.Entry<AgencyAndId, Date>> it = _lastVehicleUpdate.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<AgencyAndId, Date> entry = it.next();
      AgencyAndId vehicleId = entry.getKey();
      Date lastUpdateTime = entry.getValue();
      if (lastUpdateTime != null && lastUpdateTime.getTime() > newestUpdate) {
        newestUpdate = lastUpdateTime.getTime();
      }
      if (!seenVehicles.contains(vehicleId)
          && lastUpdateTime.before(staleRecordThreshold)) {
        _log.debug("removing stale vehicleId=" + vehicleId);
        it.remove();
      }
    }
    // NOTE: this implies receiving stale updates is equivalent to not being updated at all
    result.setLastUpdate(newestUpdate);
    _log.info("Agency " + this.getAgencyIds().get(0) + " has active vehicles=" + seenVehicles.size() 
        + " for updates=" + updates.size() + " with most recent timestamp " + new Date(newestUpdate));
  }

  private boolean isValidLocation(VehicleLocationRecord record, CombinedTripUpdatesAndVehiclePosition update) {
    if (_maxDeltaLocationMeters == null) return true; // validation turned off
    CoordinatePoint reported = new CoordinatePoint(update.vehiclePosition.getPosition().getLatitude(),
            update.vehiclePosition.getPosition().getLongitude());

    BlockLocation blockLocation = _blockLocationService.getScheduledLocationForBlockInstance(update.block.getBlockInstance(), record.getTimeOfRecord());
      if (blockLocation == null) return true; // this record will be tossed for other reasons
    CoordinatePoint calculated = blockLocation.getLocation();
    double delta = SphericalGeometryLibrary.distanceFaster(reported.getLat(), reported.getLon(),
            calculated.getLat(), calculated.getLon());
    if (delta < _maxDeltaLocationMeters)
      return true;
    _log.info("dropped vehicle {} has distance of {} with deviation {} when limit is {}",
            record.getVehicleId(), delta, record.getScheduleDeviation(), _maxDeltaLocationMeters);
    return false;
  }

  private boolean blockNotActive(VehicleLocationRecord record) {
    if (record.isScheduleDeviationSet()) {
      if (Math.abs(record.getScheduleDeviation()) > 60 * 60) {
        // if schedule deviation is way off then ignore
        _log.debug("discarding v: " + record.getVehicleId() + " for schDev=" + record.getScheduleDeviation());
        return true;
      }
      
    }
    return false;
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

          ServiceAlertRecord serviceAlertRecord = new ServiceAlertRecord();
          serviceAlertRecord.setAgencyId(_agencyIds.get(0));
          serviceAlertRecord.setActiveWindows(new HashSet<ServiceAlertTimeRange>());
          if(serviceAlert.getActiveWindowList() != null){
            for(ServiceAlerts.TimeRange timeRange : serviceAlert.getActiveWindowList()){
              ServiceAlertTimeRange serviceAlertTimeRange = new ServiceAlertTimeRange();
              serviceAlertTimeRange.setFromValue(timeRange.getStart());
              serviceAlertTimeRange.setToValue(timeRange.getEnd());
                serviceAlertRecord.getActiveWindows().add(serviceAlertTimeRange);
            }
          }

          serviceAlertRecord.setAllAffects(new HashSet<ServiceAlertsSituationAffectsClause>());
          if(serviceAlert.getAffectsList() != null){
            for(ServiceAlerts.Affects affects : serviceAlertBuilder.getAffectsList()){
              ServiceAlertsSituationAffectsClause serviceAlertsSituationAffectsClause = new ServiceAlertsSituationAffectsClause();
              serviceAlertsSituationAffectsClause.setAgencyId(affects.getAgencyId());
              serviceAlertsSituationAffectsClause.setApplicationId(affects.getApplicationId());
              serviceAlertsSituationAffectsClause.setDirectionId(affects.getDirectionId());
              serviceAlertsSituationAffectsClause.setRouteId(affects.getRouteId().getId());
              serviceAlertsSituationAffectsClause.setStopId(affects.getStopId().getId());
              serviceAlertsSituationAffectsClause.setTripId(affects.getTripId().getId());
              serviceAlertRecord.getAllAffects().add(serviceAlertsSituationAffectsClause);
            }
          }

          serviceAlertRecord.setCause(getECause(serviceAlert.getCause()));
          serviceAlertRecord.setConsequences(new HashSet<ServiceAlertSituationConsequenceClause>());
          if(serviceAlert.getConsequenceList() != null){
            for(ServiceAlerts.Consequence consequence : serviceAlert.getConsequenceList()){
              ServiceAlertSituationConsequenceClause serviceAlertSituationConsequenceClause = new ServiceAlertSituationConsequenceClause();
              serviceAlertSituationConsequenceClause.setDetourPath(consequence.getDetourPath());
              serviceAlertSituationConsequenceClause.setDetourStopIds(new HashSet<String>());
              if(consequence.getDetourStopIdsList() != null){
                for(ServiceAlerts.Id stopId : consequence.getDetourStopIdsList()){
                  serviceAlertSituationConsequenceClause.getDetourStopIds().add(stopId.getId());
                }
              }
              serviceAlertRecord.getConsequences().add(serviceAlertSituationConsequenceClause);
            }
          }

          serviceAlertRecord.setCreationTime(serviceAlert.getCreationTime());
          serviceAlertRecord.setDescriptions(
              new HashSet<ServiceAlertLocalizedString>());
          if(serviceAlert.getDescription() != null){
            for(ServiceAlerts.TranslatedString.Translation translation : serviceAlert.getDescription().getTranslationList()){
              ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
              string.setValue(translation.getText());
              string.setLanguage(translation.getLanguage());
              serviceAlertRecord.getDescriptions().add(string);
            }
          }

          serviceAlertRecord.setModifiedTime(serviceAlert.getModifiedTime());
          serviceAlertRecord.setPublicationWindows(new HashSet<ServiceAlertTimeRange>());

          serviceAlertRecord.setServiceAlertId(serviceAlert.getId().getId());
          serviceAlertRecord.setSeverity(getESeverity(serviceAlert.getSeverity()));

          serviceAlertRecord.setSummaries(new HashSet<ServiceAlertLocalizedString>());
          if(serviceAlert.getSummary() != null){
            for(ServiceAlerts.TranslatedString.Translation translation : serviceAlert.getSummary().getTranslationList()){
              ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
              string.setValue(translation.getText());
              string.setLanguage(translation.getLanguage());
              serviceAlertRecord.getSummaries().add(string);
            }
          }

          serviceAlertRecord.setUrls(new HashSet<ServiceAlertLocalizedString>());
          if(serviceAlert.getUrl() != null){
            for(ServiceAlerts.TranslatedString.Translation translation : serviceAlert.getUrl().getTranslationList()){
              ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
              string.setValue(translation.getText());
              string.setLanguage(translation.getLanguage());
              serviceAlertRecord.getUrls().add(string);
            }
          }

          _serviceAlertService.createOrUpdateServiceAlert(serviceAlertRecord);
        }
      }
    }
  }

  private ESeverity getESeverity(ServiceAlert.Severity severity){
    if(severity == ServiceAlert.Severity.NO_IMPACT)
      return ESeverity.NO_IMPACT;
    if(severity == ServiceAlert.Severity.NORMAL)
      return ESeverity.NORMAL;
    if(severity == ServiceAlert.Severity.SEVERE)
      return ESeverity.SEVERE;
    if(severity == ServiceAlert.Severity.SLIGHT)
      return ESeverity.SLIGHT;
    if(severity == ServiceAlert.Severity.UNKNOWN)
      return ESeverity.UNKNOWN;
    if(severity == ServiceAlert.Severity.VERY_SEVERE)
      return ESeverity.VERY_SEVERE;
    if(severity == ServiceAlert.Severity.VERY_SLIGHT)
      return ESeverity.VERY_SLIGHT;
    return ESeverity.UNKNOWN;
  }

  private ECause getECause(ServiceAlert.Cause cause){
    if(cause == ServiceAlert.Cause.UNKNOWN_CAUSE){
      return ECause.UNKNOWN_CAUSE;
    }
    if(cause == ServiceAlert.Cause.OTHER_CAUSE){
      return ECause.OTHER_CAUSE;
    }
    if(cause == ServiceAlert.Cause.TECHNICAL_PROBLEM){
      return ECause.TECHNICAL_PROBLEM;
    }
    if(cause == ServiceAlert.Cause.STRIKE){
      return ECause.STRIKE;
    }
    if(cause == ServiceAlert.Cause.DEMONSTRATION){
      return ECause.DEMONSTRATION;
    }
    if(cause == ServiceAlert.Cause.ACCIDENT){
      return ECause.ACCIDENT;
    }
    if(cause == ServiceAlert.Cause.HOLIDAY){
      return ECause.HOLIDAY;
    }
    if(cause == ServiceAlert.Cause.WEATHER){
      return ECause.WEATHER;
    }
    if(cause == ServiceAlert.Cause.MAINTENANCE){
      return ECause.MAINTENANCE;
    }
    if(cause == ServiceAlert.Cause.CONSTRUCTION){
      return ECause.CONSTRUCTION;
    }
    if(cause == ServiceAlert.Cause.POLICE_ACTIVITY){
      return ECause.POLICE_ACTIVITY;
    }
    if(cause == ServiceAlert.Cause.MEDICAL_EMERGENCY){
      return ECause.MEDICAL_EMERGENCY;
    }
    return ECause.UNKNOWN_CAUSE;
  }

  private AgencyAndId createId(String id) {
    return new AgencyAndId(_agencyIds.get(0), id);
  }

  /**
   * 
   * @param url
   * @return a {@link FeedMessage} constructed from the protocol buffer content
   *         of the specified url, or a default empty {@link FeedMessage} if the
   *         url is null
   * @throws IOException
   */
  private FeedMessage readOrReturnDefault(URL url) throws IOException {
    if (url == null) {
      return getDefaultFeedMessage();
    }
    return readFeedFromUrl(url);
  }

  private FeedMessage readOrReturnDefault(String url) throws IOException {
    return readFeedFromUrl(url);
  }

  private FeedMessage getDefaultFeedMessage() {
    FeedMessage.Builder builder = FeedMessage.newBuilder();
    FeedHeader.Builder header = FeedHeader.newBuilder();
    header.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
    builder.setHeader(header);
    return builder.build();
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
   InputStream in = null;
   try {
     in = urlConnection.getInputStream();
     return FeedMessage.parseFrom(in, _registry);
   } catch (IOException ex) {
     _log.error("connection issue with url " + url + ", ex=" + ex);
     return getDefaultFeedMessage();
   } finally {
      try {
        if (in != null) in.close();
      } catch (IOException ex) {
        _log.error("error closing url stream " + url);
      }
    }
  }

  /**
   * This method was added to allow accessing an SFTP feed, since the Java URL
   * class does not yet support the SFTP protocol.
   *
   * @param url of the SFTP feed to read
   * @return a {@link FeedMessage} constructed from the protocol buffer content
   *         of the specified url
   * @throws IOException
   */
  private FeedMessage readFeedFromUrl(String url) throws IOException {
   Session session = null;
   Channel channel = null;
   ChannelSftp downloadChannelSftp = null;
   InputStream in = null;
   JSch jsch=new JSch();

   // Parse SFTP URL
   int idx = url.indexOf("//") + 2;
   int idx2 = url.indexOf(":", idx);
   String user = url.substring(idx, idx2);
   idx = idx2 + 1;
   idx2 = url.indexOf("@");
   String pw = url.substring(idx, idx2);
   url = url.substring(idx2 + 1);
   idx = url.indexOf(":");
   String host = url.substring(0, idx);
   String rdir = "";
   idx = url.indexOf("/") + 1;
   idx2 = url.lastIndexOf("/");
   if (idx2 > idx) {
     rdir = url.substring(idx, idx2);
   } else {
     idx2 = idx-1;
   }
   String rfile = url.substring(idx2+1);

   try {
     session=jsch.getSession(user, host, 22);
     session.setPassword(pw);
     session.setConfig("StrictHostKeyChecking", "no");
     session.connect(10000);  // Set timeout to 10 seconds
     channel = session.openChannel("sftp");
     channel.connect();
     downloadChannelSftp = (ChannelSftp) channel;
     downloadChannelSftp.cd(downloadChannelSftp.getHome() + "/" + rdir);
     File downloadFile = new File(downloadChannelSftp.getHome() + "/" + rfile);
     in = downloadChannelSftp.get(downloadFile.getName());

     return FeedMessage.parseFrom(in, _registry);
   } catch (JSchException ex) {
     _log.error("connection issue with sftp url " + url);
     return getDefaultFeedMessage();
   } catch (SftpException e) {
     _log.error("connection issue with sftp");
     e.printStackTrace();
    return getDefaultFeedMessage();
  } finally {
     try {
       if (channel != null) channel.disconnect();
       if (session != null) session.disconnect();
       if (in != null) in.close();
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
        if (_enabled) {
          refresh();
        }
      } catch (Throwable ex) {
        _log.warn("Error updating from GTFS-realtime data sources", ex);
      }
    }
  }
}
