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

import com.google.transit.realtime.*;
import org.apache.commons.lang.StringUtils;
import org.onebusaway.api.model.transit.realtime.GtfsRealtimeConstantsV2;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.realtime.api.VehicleOccupancyListener;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.service_alerts.ECause;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.alerts.impl.ServiceAlertLocalizedString;
import org.onebusaway.alerts.impl.ServiceAlertRecord;
import org.onebusaway.alerts.impl.ServiceAlertSituationConsequenceClause;
import org.onebusaway.alerts.impl.ServiceAlertTimeRange;
import org.onebusaway.alerts.impl.ServiceAlertsSituationAffectsClause;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.RouteReplacementServiceImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntriesFactory;
import org.onebusaway.transit_data_federation.services.*;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.DynamicBlockIndexService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.alerts.service.ServiceAlerts;
import org.onebusaway.alerts.service.ServiceAlerts.ServiceAlert;
import org.onebusaway.alerts.service.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ExtensionRegistry;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.springframework.beans.factory.annotation.Qualifier;

public class GtfsRealtimeSource implements MonitoredDataSource {

  public static final String GTFS_CONNECT_TIMEOUT = "gtfs.connect_timeout";
  public static final String GTFS_READ_TIMEOUT = "gtfs.read_timeout";
  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeSource.class);

  private static final ExtensionRegistry _registry = ExtensionRegistry.newInstance();

  static {
    _registry.add(GtfsRealtimeOneBusAway.obaFeedEntity);
    _registry.add(GtfsRealtimeOneBusAway.obaTripUpdate);
    _registry.add(GtfsRealtimeMTARR.mtaRailroadStopTimeUpdate); // track number
    _registry.add(GtfsRealtimeServiceStatus.mercuryAlert);
    // NYCT support for added trips
    _registry.add(GtfsRealtimeNYCT.nyctFeedHeader);
    _registry.add(GtfsRealtimeNYCT.nyctTripDescriptor);
    _registry.add(GtfsRealtimeNYCT.nyctStopTimeUpdate);
    // MTA Bus Time Crowding
    _registry.add(GtfsRealtimeCrowding.crowdingDescriptor);
    // Stroller support
    _registry.add(GtfsRealtimeOneBusAway.obaVehicleDescriptor);
  }

  private VehicleLocationListener _vehicleLocationListener;

  private VehicleOccupancyListener _vehicleOccupancyListener;

  private ServiceAlertsService _serviceAlertService;

  private ScheduledExecutorService _scheduledExecutorService;

  private ConsolidatedStopsService _consolidatedStopsService;

  private ScheduledFuture<?> _refreshTask;

  private DataSourceMonitor _monitor;

  private URL _tripUpdatesUrl;

  private String _sftpTripUpdatesUrl;

  private URL _vehiclePositionsUrl;

  private String _sftpVehiclePositionsUrl;

  private URL _alertsUrl;

  private URL _alertCollectionUrl;

  private String _sftpAlertsUrl;

  private int _refreshInterval = 30;

  private Integer _maxDeltaLocationMeters = null; // by default don't validate

  private boolean _showNegativeScheduledArrivals = true;

  private Map<String,String> _headersMap;

  private Map _alertAgencyIdMap;

  private List<String> _agencyIds = new ArrayList<String>();

  private List<String> _scheduleTripIdRegexs = null;

  private List<String> _realtimeTripIdRegexes = null;

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

  private GtfsRealtimeEntitySource _entitySource = new GtfsRealtimeEntitySource();

  private GtfsRealtimeServiceSource _serviceSource  = new GtfsRealtimeServiceSource();

  private GtfsRealtimeTripLibrary _tripsLibrary;

  private GtfsRealtimeAlertLibrary _alertLibrary;
  
  private MonitoredResult _monitoredResult = new MonitoredResult();
  
  private String _feedId = null;
  
  private StopModificationStrategy _stopModificationStrategy = null;
  
  private boolean _scheduleAdherenceFromLocation = false;

  private boolean _enabled = true;

  private boolean _useLabelAsId = false;

  private boolean _ignoreAlertTripId = false;

  private String _alertSourcePrefix = null;

  // this is a change from the default, but is much safer
  private boolean _validateCurrentTime = false;

  // a special case of some specific integration - drop unassigned trips
  private boolean _filterUnassigned = false;

  // allow trip_ids to match fuzzily instead of strictly
  private boolean _enableFuzzyMatching = false;

  private List<AgencyAndId> _routeIdsToCancel = null;

  private GtfsRealtimeCancelService _cancelService;

  // minimize operations while bundle is changing as state is inconsistent
  private boolean isBundleReady = false;

  @Autowired
  public void setAgencyService(AgencyService agencyService) {
    _serviceSource.setAgencyService(agencyService);
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _entitySource.setTransitGraphDao(transitGraphDao);
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _serviceSource.setBlockCalendarService(blockCalendarService);
  }

  @Autowired
  public void setBlockLocationService(BlockLocationService blockLocationService) {
    _serviceSource.setBlockLocationService(blockLocationService);
  }

  @Autowired
  public void setConsolidatedStopsService(ConsolidatedStopsService service) {
    _consolidatedStopsService = service;
  }

  @Autowired
  @Qualifier("dynamicBlockIndexServiceImpl")
  public void setDynamicBlockIndexService(DynamicBlockIndexService dynamicBlockIndexService) {
    _serviceSource.setDynamicBlockIndexService(dynamicBlockIndexService);
  }

  @Autowired
  public void setDataSourceMonitor(DataSourceMonitor monitor) {
    this._monitor = monitor;
  }
  @Autowired
  public void setNarrativeService(NarrativeService service) {
    _serviceSource.setNarrativeService(service);
  }

  @Autowired
  public void setVehicleLocationListener(
      VehicleLocationListener vehicleLocationListener) {
    _vehicleLocationListener = vehicleLocationListener;
  }

  @Autowired
  public void setVehicleOccupancyListener(VehicleOccupancyListener vehicleOccupancyListener) {
    _vehicleOccupancyListener = vehicleOccupancyListener;
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
   _serviceSource.setBlockGeospatialService(blockGeospatialService);
  }

  @Autowired
  public void setStopTimeEntriesFactory(StopTimeEntriesFactory stopTimeEntriesFactory) {
    _serviceSource.setStopTimeEntriesFactory(stopTimeEntriesFactory);
  }

  @Autowired
  public void setShapePointService(ShapePointService service) {
    _serviceSource.setShapePointService(service);
  }

  @Autowired
  public void setStopSwapService(StopSwapService service) {
    _serviceSource.setStopSwapServce(service);
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService service) {
    _serviceSource.setBlockIndexService(service);
  }

  @Autowired
  public void setCalendarService(ExtendedCalendarService service) {
    _serviceSource.setCalendarService(service);
  }

  public void bundleSwapListener() {
    _log.info("bundleSwapListener invoked");
    if (_entitySource != null && _entitySource.getRealtimeFuzzyMatcher() != null) {
      _entitySource.getRealtimeFuzzyMatcher().reset();
    }
    // force cache update
    if (_serviceSource.getBlockFinder() != null) {
      _serviceSource.getBlockFinder().reset();
    }
  }

  @Refreshable(dependsOn = RefreshableResources.MARK_START_BUNDLE_SWAP)
  public void markBundleNotReady() {
    isBundleReady = false;
  }
  @Refreshable(dependsOn = RefreshableResources.MARK_STOP_BUNDLE_SWAP)
  public void markBundleReady() {
    isBundleReady = true;
    bundleSwapListener();
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

  public void setAlertCollectionUrl(URL alertCollectionUrl) {
    _alertCollectionUrl = alertCollectionUrl;
  }

  public URL getAlertsUrl() {
    return _alertsUrl;
  }

  public URL getAlertCollectionUrl() { return _alertCollectionUrl; }
  
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

  public void setScheduleTripIdRegexes(List<String> regexes) {
    _scheduleTripIdRegexs = regexes;
  }

  public void setRealtimeTripIdRegexes(List<String> regexes) {
    _realtimeTripIdRegexes = regexes;
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
    if (_feedId == null)
      _feedId = _agencyIds.toString();
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

  /**
   * use the vehicle label as the id.
   * @param useLabelAsId
   */
  public void setUseLabelAsId(boolean useLabelAsId) {
    _useLabelAsId = useLabelAsId;
  }

  /**
   * if the alerts feed contains trip ids ignore them.  This helps
   * surface the alerts inside OBA.
   * @param ignore
   */
  public void setIgnoreAlertTripId(boolean ignore) {
    _ignoreAlertTripId = ignore;
  }

  public GtfsRealtimeTripLibrary getGtfsRealtimeTripLibrary() {
    return _tripsLibrary;
  }

  public String getAlertSourcePrefix() {
    return _alertSourcePrefix;
  }

  /**
   * if alerts come in via alternative means such as an RSS feed, set the name of that
   * source here to associate and merge them with this feed
   *
   * @param prefix
   */
  public void setAlertSourcePrefix(String prefix) {
    _alertSourcePrefix = prefix;
  }

  public void setRouteIdsToCancel(List<String> routeAgencyIds) {
    if (routeAgencyIds != null) {
      _routeIdsToCancel = new ArrayList<>();
      for (String routeAgencyId : routeAgencyIds) {
        try {
          _routeIdsToCancel.add(AgencyAndId.convertFromString(routeAgencyId));
        } catch (IllegalStateException ise) {
          _log.error("invalid routeId {}", ise);
        }
      }
    }
  }

  public void setFilterUnassigned(boolean flag) {
    _filterUnassigned = flag;
  }

  public void setEnableFuzzyMatching(boolean flag) {
    _enableFuzzyMatching = flag;
  }

  @Autowired
  public void setGtfsRealtimeCancelService(GtfsRealtimeCancelService service) {
    _cancelService = service;
  }
  @PostConstruct
  public void start() {
    if (_agencyIds.isEmpty()) {
      _log.info("no agency ids specified for GtfsRealtimeSource, so defaulting to full agency id set");
      List<String> agencyIds = _serviceSource.getAgencyService().getAllAgencyIds();
      _agencyIds.addAll(agencyIds);
      if (_agencyIds.size() > 3) {
        _log.warn("The default agency id set is quite large (n="
            + _agencyIds.size()
            + ").  You might consider specifying the applicable agencies for your GtfsRealtimeSource.");
      }
    }

    _entitySource.setAgencyIds(_agencyIds);
    _entitySource.setTripIdRegexs(_scheduleTripIdRegexs);
    _entitySource.setConsolidatedStopService(_consolidatedStopsService);

    if (_enableFuzzyMatching) {
      RealtimeFuzzyMatcher fuzzyMatcher = new RealtimeFuzzyMatcher(_entitySource.getTransitGraphDao(),
              _serviceSource.getCalendarService());
      fuzzyMatcher.setRealtimeTripIdRegexes(_realtimeTripIdRegexes);
      fuzzyMatcher.setScheduleTripIdRegexs(_scheduleTripIdRegexs);
      fuzzyMatcher.setAgencies(new HashSet<>(_agencyIds));

      _entitySource.setRealtimeFuzzyMatcher(fuzzyMatcher);
    }


    _tripsLibrary = new GtfsRealtimeTripLibrary();
    _tripsLibrary.setEntitySource(_entitySource);
    _tripsLibrary.setServiceSource(_serviceSource);
    if (_stopModificationStrategy != null) {
      _tripsLibrary.setStopModificationStrategy(_stopModificationStrategy);
    }
    _tripsLibrary.setScheduleAdherenceFromLocation(_scheduleAdherenceFromLocation);
    _tripsLibrary.setUseLabelAsVehicleId(_useLabelAsId);
    _tripsLibrary.setValidateCurrentTime(_validateCurrentTime);

    _tripsLibrary.setFilterUnassigned(_filterUnassigned);
    DuplicatedTripServiceImpl duplicatedTripService = new DuplicatedTripServiceImpl();
    duplicatedTripService.setGtfsRealtimeEntitySource(_entitySource);
    _serviceSource.setDuplicatedTripService(duplicatedTripService);
    DynamicTripBuilder tripBuilder = new DynamicTripBuilder();
    tripBuilder.setServiceSource(_serviceSource);
    tripBuilder.setEntityource(_entitySource);
    _serviceSource.setDynamicTripBuilder(tripBuilder);

    
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
    if (!graphReady()) {
      _log.warn("skipping update " + getAgencyIds() + ", bundle not ready");
      return;
    }
    FeedMessage tripUpdates = _sftpTripUpdatesUrl != null ?
        readOrReturnDefault(_sftpTripUpdatesUrl)
        : readOrReturnDefault(_tripUpdatesUrl);
    FeedMessage vehiclePositions = _sftpVehiclePositionsUrl != null ?
        readOrReturnDefault(_sftpVehiclePositionsUrl)
        : readOrReturnDefault(_vehiclePositionsUrl);
    FeedMessage alerts = _sftpAlertsUrl != null ?
        readOrReturnDefault(_sftpAlertsUrl)
        : readOrReturnDefault(_alertsUrl);
    ServiceAlerts.ServiceAlertsCollection alertCollection
            = readOrReturnDefaultCollection(_alertCollectionUrl);

    MonitoredResult result = new MonitoredResult();
    result.setAgencyIds(_agencyIds);
    result.setFeedId(getFeedId());

    if (_routeIdsToCancel != null) {
      long currentTime = _tripsLibrary.getCurrentTime();
      if (currentTime == 0) currentTime = System.currentTimeMillis();
      _cancelService.cancelServiceForRoutes(_routeIdsToCancel, currentTime);
    }

    handleUpdates(result, tripUpdates, vehiclePositions, alerts, alertCollection);
    // update reference in a thread safe manner
    _monitoredResult = result;
  }

  private ServiceAlerts.ServiceAlertsCollection readOrReturnDefaultCollection(URL alertCollectionUrl) throws IOException {
    return readAlertCollectionFromUrl(alertCollectionUrl);
  }

  /****
   * Private Methods
   ****/

  // test if the transit graph is ready
  protected boolean graphReady() {
    if (!isBundleReady) {
      return false;
    }
    return true;
  }

  /**
   * 
   * @param tripUpdates
   * @param vehiclePositions
   * @param alerts
   */
  private synchronized void handleUpdates(MonitoredResult result, FeedMessage tripUpdates,
                                          FeedMessage vehiclePositions, FeedMessage alerts,
                                          ServiceAlerts.ServiceAlertsCollection alertCollection) {
	  
	long time = tripUpdates.getHeader().getTimestamp() * 1000;
	_tripsLibrary.setCurrentTime(_tripsLibrary.ensureMillis(time));

    List<CombinedTripUpdatesAndVehiclePosition> combinedUpdates = _tripsLibrary.groupTripUpdatesAndVehiclePositions(result,
            tripUpdates, vehiclePositions);
    result.setRecordsTotal(combinedUpdates.size());
    handleCombinedUpdatesLogged(result, combinedUpdates);
    cacheVehicleLocations(vehiclePositions);
    handleAlerts(alerts);
    handleAlertCollection(alertCollection);
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

  void handleCombinedUpdatesLogged(MonitoredResult result,
                             List<CombinedTripUpdatesAndVehiclePosition> updates) {
    try {
      handleCombinedUpdates(result, updates);
    } catch (Throwable t) {
      _log.error("handleCombinedUpdates source-exception: {}", t, t);
    }
  }
  // package private for unit tests
   void handleCombinedUpdates(MonitoredResult result,
      List<CombinedTripUpdatesAndVehiclePosition> updates) {
    long methodStarTime = System.currentTimeMillis();
    // exit if we are configured in alerts mode
    if (_tripUpdatesUrl == null) return;

    Set<AgencyAndId> seenVehicles = new HashSet<AgencyAndId>();

    try {
      for (CombinedTripUpdatesAndVehiclePosition update : updates) {
        String metricTripId = null;
        if (update.getTripUpdates() != null && update.getTripUpdatesSize() > 0) {
          if (update.getTripUpdates().get(0).hasTrip()) {
            metricTripId = update.getTripUpdates().get(0).getTrip().getTripId();
          }
        }

        if (update.block == null) {
          _log.error("null block {} for agencies {}, bailing...", metricTripId, _agencyIds);
          result.addUnmatchedTripId(metricTripId);
          continue;
        }
        BlockDescriptor.ScheduleRelationship scheduleRelationship = update.block.getScheduleRelationship();
        if (scheduleRelationship == null) {
          _log.error("no schedule relationship for update {}", update);
          result.addUnmatchedTripId(metricTripId);
          continue;
        }
        boolean isDynamicTrip = TransitDataConstants.STATUS_ADDED.equals(scheduleRelationship.name())
                || TransitDataConstants.STATUS_DUPLICATED.equals(scheduleRelationship.name());

        VehicleLocationRecord record = _tripsLibrary.createVehicleLocationRecordForUpdate(result, update);
        if (record != null) {
          if (isDynamicTrip) {
            if (_monitoredResult.getLastUpdate() < record.getTimeOfRecord()) {
              _monitoredResult.setLastUpdate(record.getTimeOfRecord());
            }
            _serviceSource.getDynamicBlockIndexService().register(update.block.getBlockInstance(), record.getTimeOfRecord());
          }
          if (record.getTripId() != null) {
            // tripId will be null if block was matched
            result.addUnmatchedTripId(record.getTripId().toString());
          }

          AgencyAndId vehicleId = record.getVehicleId();
          // here we try to get a more accurate count of updates
          // some providers re-send old data or future data cluttering the feed
          // the TDS will discard these
          if (!isDynamicTrip && blockNotActive(record)) {
            _log.debug("discarding v: " + vehicleId + " as block not active");
            result.addUnmatchedTripId(metricTripId);
            continue;
          }
          if (!isDynamicTrip && !isValidLocation(record, update)) {
            _log.debug("discarding v: " + vehicleId + " as location is bad");
            result.addUnmatchedTripId(metricTripId);
            continue;
          }
          seenVehicles.add(vehicleId);
          VehicleOccupancyRecord vor = _tripsLibrary.createVehicleOccupancyRecordForUpdate(result, update);
          Date timestamp = new Date(getGtfsRealtimeTripLibrary().ensureMillis(record.getTimeOfRecord()));
          Date prev = _lastVehicleUpdate.get(vehicleId);
          if (prev == null || prev.before(timestamp)) {
            _log.debug("matched vehicle " + vehicleId + " on block=" + record.getBlockId() + " with scheduleDeviation=" + record.getScheduleDeviation());
            _vehicleLocationListener.handleVehicleLocationRecord(record);
            if (vor != null) {
              _vehicleOccupancyListener.handleVehicleOccupancyRecord(vor);
            }
            _lastVehicleUpdate.put(vehicleId, timestamp);
          } else {
            _log.debug("discarding: update for vehicle " + vehicleId
                    + " as timestamp in past (" + (timestamp.getTime()-prev.getTime()) + "ms)");
          }
        }
      }

    } catch (Throwable t) {
      _log.error("fatal exception {}", t, t);
    }
    Calendar c = Calendar.getInstance();
    if (getGtfsRealtimeTripLibrary() != null)
      c.setTime(new Date(getGtfsRealtimeTripLibrary().getCurrentTime()));
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
    if (_monitor != null) {
      _monitor.logUpdate(result);
    }
    long methodEndTime = System.currentTimeMillis();
    _log.info("Agency " + getFeedId() + " has active vehicles=" + seenVehicles.size()
            + ", matched=" + result.getMatchedTripIds().size() + " (" + result.getUnmatchedTripIds().size() + ")"
            + ", added=" + result.getAddedTripIds().size()
            + ", duplicated=" + result.getDuplicatedTripIds().size()
            + ", cancelled=" + result.getCancelledTripIds().size()
            + " for updates=" + updates.size() + " with most recent timestamp " + new Date(newestUpdate)
            + " in " + (methodEndTime-methodStarTime) + "ms");
  }

  private boolean isValidLocation(VehicleLocationRecord record, CombinedTripUpdatesAndVehiclePosition update) {
    if (_maxDeltaLocationMeters == null) return true; // validation turned off
    CoordinatePoint reported = new CoordinatePoint(update.vehiclePosition.getPosition().getLatitude(),
            update.vehiclePosition.getPosition().getLongitude());

    BlockLocation blockLocation = _serviceSource.getBlockLocationService().getScheduledLocationForBlockInstance(update.block.getBlockInstance(), record.getTimeOfRecord());
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

  private void handleAlertCollection(ServiceAlerts.ServiceAlertsCollection alertsCollection) {
    if (_alertCollectionUrl == null) {
      return;
    }

    if (alertsCollection == null) {
      // don't let a single connection issue wipe out the set of alerts
      _log.info("handleAlertCollection nothing to do");
      return;
    }

    Set<AgencyAndId> currentAlerts = new HashSet<AgencyAndId>();
    Set<ServiceAlertRecord> toAdd = new HashSet<>();
    Set<ServiceAlertRecord> toUpdate = new HashSet<>();

    long start = System.currentTimeMillis();
    _log.info("[" + getFeedId() + "] handleAlertCollection running....");

    ArrayList<AgencyAndId> idsInCollection = new ArrayList<>();
    for (ServiceAlerts.ServiceAlert alert : alertsCollection.getServiceAlertsList()) {

      AgencyAndId id = new AgencyAndId(alert.getId().getAgencyId(), alert.getId().getId());
      idsInCollection.add(id);
      handleSingleAlert(id, alert.toBuilder(), currentAlerts, toAdd, toUpdate, alert);
    }

    _serviceAlertService.createOrUpdateServiceAlerts(getAgencyIds().get(0), new ArrayList<ServiceAlertRecord>(toAdd));
    _serviceAlertService.createOrUpdateServiceAlerts(getAgencyIds().get(0), new ArrayList<ServiceAlertRecord>(toUpdate));

    Set<AgencyAndId> toBeDeleted = new HashSet<AgencyAndId>();
    for (ServiceAlertRecord sa : _serviceAlertService.getAllServiceAlerts()) {
      AgencyAndId testId = new AgencyAndId(sa.getAgencyId(), sa.getServiceAlertId());

      /* consider other feed sources that may be merged here as well */
      if (sa.getSource() != null
              && (sa.getSource().equals(getFeedId())
                || (_alertSourcePrefix != null && sa.getSource().contains(_alertSourcePrefix)))) {
        try {
          if (!currentAlerts.contains(testId)) {
            _log.debug("[" + getFeedId() + "] cleaning up alert id " + testId
                    + " with source=" + sa.getSource());
            toBeDeleted.add(testId);
          }
          else if (getAgencyIds().contains(testId.getAgencyId()) && !idsInCollection.contains(testId)) {
            //delete if the alert came from this feed but isn't there anymore
            toBeDeleted.add(testId);
          }
        } catch (Exception e) {
          _log.error("invalid AgencyAndId " + sa.getServiceAlertId());
        }
      }
      else if (getAgencyIds().contains(testId.getAgencyId()) && !idsInCollection.contains(testId)) {
        //delete if the alert came from this feed but isn't there anymore
        toBeDeleted.add(testId);
      }
    }

    _serviceAlertService.removeServiceAlerts(new ArrayList<AgencyAndId>(toBeDeleted));
    _serviceAlertService.cleanup();
    _log.info("[" + getFeedId() + "] handleAlertCollection complete with "
            + currentAlerts.size()
            + " active alerts and "
            + toBeDeleted.size()
            + " deleted in "
            + (System.currentTimeMillis() - start) + " ms");

  }

  private void handleSingleAlert(AgencyAndId id,
                                 ServiceAlert.Builder serviceAlertBuilder,
                                 Set<AgencyAndId> currentAlerts,
                                 Set<ServiceAlertRecord> toAdd,
                                 Set<ServiceAlertRecord> toUpdate,
                                 ServiceAlert alert) {

    ServiceAlert serviceAlert = alert;
    // cache value of alert
    ServiceAlert existingAlert = _alertsById.get(id);
    // data store value of service alert
    ServiceAlertRecord existingRecord = _serviceAlertService.getServiceAlertForId(new AgencyAndId(id.getAgencyId(), id.getId()));

    // don't update if there's nothing to do
    if ((existingAlert == null
            || !existingAlert.equals(serviceAlert))) {
      _alertsById.put(id, serviceAlert);

      ServiceAlertRecord serviceAlertRecord = new ServiceAlertRecord();
      // indicate this came from a feed so we can prune expired alerts
      if (serviceAlert.hasSource()) {
        serviceAlertRecord.setSource(serviceAlert.getSource());
      } else {
        serviceAlertRecord.setSource(getFeedId());
      }

      serviceAlertRecord.setConsequenceMessage(serviceAlert.getConsequenceMessage());

      serviceAlertRecord.setAgencyId(id.getAgencyId()); // AGENCY from feed configuration
      serviceAlertRecord.setServiceAlertId(id.getId()); // ID ONLY

      serviceAlertRecord.setActiveWindows(new HashSet<ServiceAlertTimeRange>());
      if (serviceAlert.getActiveWindowList() != null) {
        for (ServiceAlerts.TimeRange timeRange : serviceAlert.getActiveWindowList()) {
          ServiceAlertTimeRange serviceAlertTimeRange = new ServiceAlertTimeRange();
          if (timeRange.hasStart()) {
            if (timeRange.getStart() > 0) {
              serviceAlertTimeRange.setFromValue(timeRange.getStart());
            }
          }
          if (timeRange.hasEnd()) {
            if (timeRange.getEnd() > 0) {
              serviceAlertTimeRange.setToValue(timeRange.getEnd());
            }
          }
          serviceAlertRecord.getActiveWindows().add(serviceAlertTimeRange);
        }
      }

      serviceAlertRecord.setAllAffects(new HashSet<ServiceAlertsSituationAffectsClause>());
      if (serviceAlert.getAffectsList() != null) {
        for (ServiceAlerts.Affects affects : serviceAlertBuilder.getAffectsList()) {
          ServiceAlertsSituationAffectsClause serviceAlertsSituationAffectsClause = new ServiceAlertsSituationAffectsClause();

          /*
           * if the affects clause has empty but non-null fields the Affects...Factory references will break
           */
          if (!StringUtils.isBlank(affects.getAgencyId()))
            serviceAlertsSituationAffectsClause.setAgencyId(affects.getAgencyId());

          if (!StringUtils.isBlank(affects.getApplicationId()))
            serviceAlertsSituationAffectsClause.setApplicationId(affects.getApplicationId());

          if (!StringUtils.isBlank(affects.getDirectionId()))
            serviceAlertsSituationAffectsClause.setDirectionId(affects.getDirectionId());

          if (affects.getRouteId() != null && affects.getRouteId().hasId())
            serviceAlertsSituationAffectsClause.setRouteId(new AgencyAndId(affects.getRouteId().getAgencyId(), affects.getRouteId().getId()).toString());

          if (affects.getStopId().getId() != null && affects.getStopId().hasId()) {
            serviceAlertsSituationAffectsClause.setStopId(new AgencyAndId(affects.getStopId().getAgencyId(), affects.getStopId().getId()).toString());
          }

          if (!_ignoreAlertTripId && affects.getTripId() != null && affects.getTripId().hasId())
            serviceAlertsSituationAffectsClause.setTripId(new AgencyAndId(affects.getTripId().getAgencyId(), affects.getTripId().getId()).toString());
          serviceAlertRecord.getAllAffects().add(serviceAlertsSituationAffectsClause);
        }
      }

      serviceAlertRecord.setCause(getECause(serviceAlert.getCause()));
      serviceAlertRecord.setConsequences(new HashSet<ServiceAlertSituationConsequenceClause>());
      if (serviceAlert.getConsequenceList() != null) {
        for (ServiceAlerts.Consequence consequence : serviceAlert.getConsequenceList()) {
          ServiceAlertSituationConsequenceClause serviceAlertSituationConsequenceClause = new ServiceAlertSituationConsequenceClause();
          serviceAlertSituationConsequenceClause.setDetourPath(consequence.getDetourPath());
          serviceAlertSituationConsequenceClause.setDetourStopIds(new HashSet<String>());
          if (consequence.getDetourStopIdsList() != null) {
            for (ServiceAlerts.Id stopId : consequence.getDetourStopIdsList()) {
              serviceAlertSituationConsequenceClause.getDetourStopIds().add(stopId.getId());
            }
          }
          serviceAlertRecord.getConsequences().add(serviceAlertSituationConsequenceClause);
        }
      }

      serviceAlertRecord.setCreationTime(serviceAlert.getCreationTime());
      serviceAlertRecord.setDescriptions(
              new HashSet<ServiceAlertLocalizedString>());
      if (serviceAlert.getDescription() != null) {
        for (ServiceAlerts.TranslatedString.Translation translation : serviceAlert.getDescription().getTranslationList()) {
          ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
          string.setValue(translation.getText());
          string.setLanguage(translation.getLanguage());
          serviceAlertRecord.getDescriptions().add(string);
        }
      }

      serviceAlertRecord.setModifiedTime(serviceAlert.getModifiedTime());
      serviceAlertRecord.setPublicationWindows(new HashSet<ServiceAlertTimeRange>());

      serviceAlertRecord.setSeverity(getESeverity(serviceAlert.getSeverity()));

      serviceAlertRecord.setSummaries(new HashSet<ServiceAlertLocalizedString>());
      if (serviceAlert.getSummary() != null) {
        for (ServiceAlerts.TranslatedString.Translation translation : serviceAlert.getSummary().getTranslationList()) {
          ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
          string.setValue(translation.getText());
          string.setLanguage(translation.getLanguage());
          serviceAlertRecord.getSummaries().add(string);
        }
      }

      serviceAlertRecord.setUrls(new HashSet<ServiceAlertLocalizedString>());
      if (serviceAlert.getUrl() != null) {
        for (ServiceAlerts.TranslatedString.Translation translation : serviceAlert.getUrl().getTranslationList()) {
          ServiceAlertLocalizedString string = new ServiceAlertLocalizedString();
          string.setValue(translation.getText());
          string.setLanguage(translation.getLanguage());
          serviceAlertRecord.getUrls().add(string);
        }
      }

      if (existingAlert == null) {
        _log.debug("creating alert " + serviceAlertRecord.getAgencyId() + ":" + serviceAlertRecord.getServiceAlertId());
        toAdd.add(serviceAlertRecord);
      } else {
        _log.debug("updating alert " + serviceAlertRecord.getAgencyId() + ":" + serviceAlertRecord.getServiceAlertId());
        toUpdate.add(serviceAlertRecord);
      }
      currentAlerts.add(new AgencyAndId(serviceAlertRecord.getAgencyId(), serviceAlertRecord.getServiceAlertId()));
    } else {
      _log.debug("not updating alert " + id);
      currentAlerts.add(id);
    }

  }

  private void handleAlerts(FeedMessage alerts) {

    // exit if we are configured only in trip updates mode
    if (_alertsUrl == null) {
      return;
    }

    if (!alerts.hasHeader() || !alerts.getHeader().hasTimestamp()) {
      // don't let a single connection issue wipe out the set of alerts
      _log.error("missing alert header for " + getFeedId() + ", assuming connection issue and aborting");
      return;
    }

    Set<AgencyAndId> currentAlerts = new HashSet<AgencyAndId>();
    Set<ServiceAlertRecord> toAdd = new HashSet<>();
    Set<ServiceAlertRecord> toUpdate = new HashSet<>();

    long start = System.currentTimeMillis();
    _log.info("[" + getFeedId() + "] handleAlerts running....");
    for (FeedEntity entity : alerts.getEntityList()) {
      Alert alert = entity.getAlert();
      if (alert == null) {
        _log.warn("expected a FeedEntity with an Alert");
        continue;
      }

      // NOTE!! Here we default agencyID to be of feed name
      AgencyAndId id = createId(entity.getId());

      if (entity.getIsDeleted()) {
        _alertsById.remove(id);
        _serviceAlertService.removeServiceAlert(id);
      } else {
        ServiceAlert.Builder serviceAlertBuilder = _alertLibrary.getAlertAsServiceAlert(
                id, alert, _alertAgencyIdMap, _ignoreAlertTripId);
        ServiceAlert serviceAlert = serviceAlertBuilder.build();
        // cache value of alert
        ServiceAlert existingAlert = _alertsById.get(id);

        handleSingleAlert(id, serviceAlertBuilder, currentAlerts, toAdd, toUpdate, serviceAlert);

      }
    }

    _serviceAlertService.createOrUpdateServiceAlerts(getAgencyIds().get(0), new ArrayList<ServiceAlertRecord>(toAdd));
    _serviceAlertService.createOrUpdateServiceAlerts(getAgencyIds().get(0), new ArrayList<ServiceAlertRecord>(toUpdate));

    Set<AgencyAndId> toBeDeleted = new HashSet<AgencyAndId>();
    for (ServiceAlertRecord sa : _serviceAlertService.getAllServiceAlerts()) {
      if (sa.getSource() != null && sa.getSource().equals(getFeedId())) {
        try {
          AgencyAndId testId = new AgencyAndId(sa.getAgencyId(), sa.getServiceAlertId());
          /* consider other feed sources that may be merged here as well */
          if (!currentAlerts.contains(testId)
                  && (getFeedId().equals(sa.getSource())
                    || (_alertSourcePrefix != null && sa.getSource().contains(_alertSourcePrefix)))) {
            _log.debug("[" + getFeedId() + "] cleaning up alert id " + testId
                    + " with source=" + sa.getSource());
            toBeDeleted.add(testId);
          } else {
            _log.debug("[" + getFeedId() + "] appears to still be valid with id=" + testId + ", ("
                    + sa.getAllAffects().iterator().next().getRouteId() + ")");
          }
        } catch (Exception e) {
          _log.error("invalid AgencyAndId " + sa.getServiceAlertId());
        }
      }
    }

    _serviceAlertService.removeServiceAlerts(new ArrayList<AgencyAndId>(toBeDeleted));
    _serviceAlertService.cleanup();
    _log.info("[" + getFeedId() + "] handleAlerts complete with "
            + currentAlerts.size()
            + " active alerts and "
            + toBeDeleted.size()
            + " deleted in "
            + (System.currentTimeMillis() - start) + " ms");

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
    header.setGtfsRealtimeVersion(GtfsRealtimeConstantsV2.VERSION);
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
   if (System.getProperty(GTFS_CONNECT_TIMEOUT) != null) {
     urlConnection.setConnectTimeout(Integer.parseInt(System.getProperty(GTFS_CONNECT_TIMEOUT)));
   }
   if (System.getProperty(GTFS_READ_TIMEOUT) != null) {
     urlConnection.setReadTimeout(Integer.parseInt(System.getProperty(GTFS_READ_TIMEOUT)));
   }
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

  private ServiceAlerts.ServiceAlertsCollection readAlertCollectionFromUrl(URL url) throws IOException {
    if (url == null) return ServiceAlerts.ServiceAlertsCollection.newBuilder().build();

    URLConnection urlConnection = url.openConnection();
    if (System.getProperty(GTFS_CONNECT_TIMEOUT) != null) {
      urlConnection.setConnectTimeout(Integer.parseInt(System.getProperty(GTFS_CONNECT_TIMEOUT)));
    }
    if (System.getProperty(GTFS_READ_TIMEOUT) != null) {
      urlConnection.setReadTimeout(Integer.parseInt(System.getProperty(GTFS_READ_TIMEOUT)));
    }
    setHeadersToUrlConnection(urlConnection);
    InputStream in = null;
    try {
      in = urlConnection.getInputStream();
      return ServiceAlerts.ServiceAlertsCollection.parseFrom(in, _registry);
    } catch (IOException ex) {
      _log.error("connection issue with url " + url + ", ex=" + ex);
      return getDefaultServiceAlertsCollection();
    } finally {
      try {
        if (in != null) in.close();
      } catch (IOException ex) {
        _log.error("error closing url stream " + url);
      }
    }
  }

  private ServiceAlerts.ServiceAlertsCollection getDefaultServiceAlertsCollection() {
    ServiceAlerts.ServiceAlertsCollection.Builder builder = ServiceAlerts.ServiceAlertsCollection.newBuilder();
    return builder.build();
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

  public void setRouteRemap(Map<String, String> remaps) {
    RouteReplacementService routeReplacementService = new RouteReplacementServiceImpl();
    routeReplacementService.putAll(remaps);
    _entitySource.setRouteReplacementService(routeReplacementService);
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
        _log.warn("Error updating from GTFS-realtime data sources for config {}, {}", getFeedId(), ex, ex);
      }
    }
  }
}
