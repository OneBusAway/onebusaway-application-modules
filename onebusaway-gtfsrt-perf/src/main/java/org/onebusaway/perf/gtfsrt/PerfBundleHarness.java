/**
 * Copyright (C) 2026 Open Transit Software Foundation
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
package org.onebusaway.perf.gtfsrt;

import org.onebusaway.alerts.impl.ServiceAlertRecord;
import org.onebusaway.alerts.service.ServiceAlertsService;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data_federation.impl.realtime.DynamicBlockIndexServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeCancelServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntriesFactory;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.ConsolidatedStopsService;
import org.onebusaway.transit_data_federation.services.StopSwapService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stands up a real Spring federation context and a fully-wired
 * {@link GtfsRealtimeSource}, replicating the wiring used by
 * {@code BundleLoader} and {@code AbstractGtfsRealtimeIntegrationTest} in
 * onebusaway-gtfsrt-integration-tests, so that {@code source.reset()} and
 * {@code source.refresh()} can be driven synchronously in a perf loop.
 */
public class PerfBundleHarness {

  private final String _bundleRootDir;
  private final String _tripUpdatesUrl;
  private final String _vehiclePositionsUrl;
  private final String _alertsUrl;

  private ConfigurableApplicationContext _context;
  private GtfsRealtimeSource _source;
  private final AtomicInteger _lastCount = new AtomicInteger();

  public PerfBundleHarness(String bundleRootDir, String tripUpdatesUrl,
                           String vehiclePositionsUrl, String alertsUrl) {
    _bundleRootDir = bundleRootDir;
    _tripUpdatesUrl = tripUpdatesUrl;
    _vehiclePositionsUrl = vehiclePositionsUrl;
    _alertsUrl = alertsUrl;
  }

  public void open() throws Exception {
    System.setProperty("bundle.root", _bundleRootDir);
    System.setProperty("bundlePath", _bundleRootDir);
    System.setProperty("bundle.remote.source", _bundleRootDir + File.separator + "index.json");

    _context = ContainerLibrary.createContext(Collections.singletonList("perf-data-sources.xml"));

    _source = new GtfsRealtimeSource();
    _source.setRefreshInterval(0); // no background scheduler; we drive refresh() synchronously
    _source.setTransitGraphDao(_context.getBean(TransitGraphDao.class));
    _source.setAgencyService(_context.getBean(AgencyService.class));
    _source.setBlockCalendarService(_context.getBean(BlockCalendarService.class));
    _source.setBlockLocationService(_context.getBean(BlockLocationService.class));
    _source.setScheduledExecutorService(_context.getBean(ScheduledExecutorService.class));
    _source.setConsolidatedStopsService(_context.getBean(ConsolidatedStopsService.class));
    _source.setDynamicBlockIndexService(_context.getBean(DynamicBlockIndexServiceImpl.class));
    _source.setStopTimeEntriesFactory(_context.getBean(StopTimeEntriesFactory.class));
    _source.setNarrativeService(_context.getBean(NarrativeService.class));
    _source.setShapePointService(_context.getBean(ShapePointService.class));
    _source.setStopSwapService(_context.getBean(StopSwapService.class));
    _source.setBlockIndexService(_context.getBean(BlockIndexService.class));

    BundleManagementService bundleManagementService = _context.getBean(BundleManagementService.class);
    int i = 0;
    while (!bundleManagementService.bundleIsReady()) {
      Thread.sleep(1000);
      if (i++ % 10 == 0) System.out.println("waiting on bundle...");
    }
    _source.markBundleReady();
    _source.setFilterUnassigned(false);

    // Pin the feed to specific agency ids, exactly as production does
    // (GTFS_RT_FEEDS "agencyIds":["1"]). GtfsRealtimeSource.start() otherwise
    // defaults _agencyIds to EVERY agency in the graph (see start(), line ~519),
    // which against a multi-agency bundle would resolve trip ids against all
    // agencies and distort both matching and the "null block ... bailing" flood.
    // Comma-separated via -Dperf.agencyIds=1 (default: leave empty = all agencies).
    String agencyIds = System.getProperty("perf.agencyIds");
    if (agencyIds != null && !agencyIds.trim().isEmpty()) {
      for (String id : agencyIds.split(",")) {
        String trimmed = id.trim();
        if (!trimmed.isEmpty()) _source.setAgencyId(trimmed);
      }
      System.out.println("pinned feed agencyIds=" + _source.getAgencyIds());
    }

    // Capturing listener that delegates to the real block-location write path.
    VehicleStatusServiceImpl real = _context.getBean(VehicleStatusServiceImpl.class);
    _source.setVehicleLocationListener(new CountingListener(real, _lastCount));
    // Same bean also implements VehicleOccupancyListener; GtfsRealtimeSource requires
    // both to be non-null (they're @Autowired in production Spring wiring, but this
    // harness constructs GtfsRealtimeSource by hand).
    _source.setVehicleOccupancyListener(real);
    // ServiceAlertsService has no bean in this harness's minimal context
    // (onebusaway-alerts-persistence's DB-backed impl isn't wired here); a
    // simple in-memory implementation is enough to keep GtfsRealtimeSource's
    // alert-handling path from NPEing on every refresh.
    _source.setServiceAlertService(new InMemoryServiceAlertsService());
    _source.setGtfsRealtimeCancelService(_context.getBean(GtfsRealtimeCancelServiceImpl.class));

    _source.setTripUpdatesUrl(new java.net.URL(_tripUpdatesUrl));
    if (_vehiclePositionsUrl != null)
      _source.setVehiclePositionsUrl(new java.net.URL(_vehiclePositionsUrl));
    if (_alertsUrl != null)
      _source.setAlertsUrl(new java.net.URL(_alertsUrl));

    _source.start();
  }

  public void refreshOnce() throws Exception {
    _lastCount.set(0);
    _source.reset();      // clear _lastVehicleUpdate so BOTH stages run this iteration
    _source.refresh();
  }

  public int lastRecordCount() { return _lastCount.get(); }
  public GtfsRealtimeSource getSource() { return _source; }
  public org.springframework.context.ApplicationContext getContext() { return _context; }

  public void close() {
    if (_context != null) { _context.stop(); _context.close(); }
  }

  private static final class CountingListener implements VehicleLocationListener {
    private final VehicleLocationListener _delegate;
    private final AtomicInteger _counter;
    CountingListener(VehicleLocationListener delegate, AtomicInteger counter) {
      _delegate = delegate; _counter = counter;
    }
    @Override public void handleVehicleLocationRecord(VehicleLocationRecord record) {
      _counter.incrementAndGet();
      _delegate.handleVehicleLocationRecord(record);
    }
    @Override public void handleVehicleLocationRecords(List<VehicleLocationRecord> records) {
      _counter.addAndGet(records.size());
      _delegate.handleVehicleLocationRecords(records);
    }
    @Override public void resetVehicleLocation(AgencyAndId vehicleId) { _delegate.resetVehicleLocation(vehicleId); }
    @Override public void handleRawPosition(AgencyAndId vehicleId, double lat, double lon, long timestamp) { }
  }

  /**
   * Minimal in-memory {@link ServiceAlertsService} so {@code GtfsRealtimeSource}'s
   * alert-handling path has somewhere to read/write. This harness isn't measuring
   * alert persistence, so a real DB-backed implementation (which would require
   * pulling in onebusaway-alerts-persistence's Hibernate wiring) isn't warranted;
   * this just needs to satisfy the read-your-writes contract the source relies on.
   */
  private static final class InMemoryServiceAlertsService implements ServiceAlertsService {
    private final Map<String, ServiceAlertRecord> _byId = new ConcurrentHashMap<>();

    private static String key(String agencyId, String serviceAlertId) {
      return agencyId + "_" + serviceAlertId;
    }

    @Override public void cleanup() { }
    @Override public void loadServiceAlerts() { }

    @Override public ServiceAlertRecord createOrUpdateServiceAlert(ServiceAlertRecord record) {
      _byId.put(key(record.getAgencyId(), record.getServiceAlertId()), record);
      return record;
    }

    @Override public void removeServiceAlert(AgencyAndId serviceAlertId) {
      _byId.remove(key(serviceAlertId.getAgencyId(), serviceAlertId.getId()));
    }

    @Override public ServiceAlertRecord copyServiceAlert(ServiceAlertRecord record) { return record; }

    @Override public void removeServiceAlerts(List<AgencyAndId> serviceAlertIds) {
      for (AgencyAndId id : serviceAlertIds) removeServiceAlert(id);
    }

    @Override public void removeAllServiceAlertsForFederatedAgencyId(String agencyId) {
      _byId.values().removeIf(r -> agencyId.equals(r.getAgencyId()));
    }

    @Override public ServiceAlertRecord getServiceAlertForId(AgencyAndId serviceAlertId) {
      return _byId.get(key(serviceAlertId.getAgencyId(), serviceAlertId.getId()));
    }

    @Override public List<ServiceAlertRecord> getAllServiceAlerts() {
      return new ArrayList<>(_byId.values());
    }

    @Override public List<ServiceAlertRecord> getServiceAlertsForFederatedAgencyId(String agencyId) {
      List<ServiceAlertRecord> result = new ArrayList<>();
      for (ServiceAlertRecord r : _byId.values())
        if (agencyId.equals(r.getAgencyId())) result.add(r);
      return result;
    }

    @Override public List<ServiceAlertRecord> getServiceAlertsForAgencyId(long time, String agencyId) {
      return Collections.emptyList();
    }

    @Override public List<ServiceAlertRecord> getServiceAlertsForStopId(long time, AgencyAndId stopId) {
      return Collections.emptyList();
    }

    @Override public List<ServiceAlertRecord> getServiceAlertsForRouteId(long time, AgencyAndId routeId) {
      return Collections.emptyList();
    }

    @Override public List<ServiceAlertRecord> getServiceAlertsForRouteAndStopId(long time, AgencyAndId routeId, AgencyAndId stopId) {
      return Collections.emptyList();
    }

    @Override public List<ServiceAlertRecord> getServiceAlertsForTripAndStopId(long time, AgencyAndId tripId, AgencyAndId stopId) {
      return Collections.emptyList();
    }

    @Override public List<ServiceAlertRecord> getServiceAlertsForRouteAndDirection(long time, AgencyAndId routeId, AgencyAndId stopId, String directionId) {
      return Collections.emptyList();
    }

    @Override public List<ServiceAlertRecord> getServiceAlerts(SituationQueryBean query) {
      return Collections.emptyList();
    }

    @Override public List<ServiceAlertRecord> createOrUpdateServiceAlerts(String agencyId, List<ServiceAlertRecord> records) {
      for (ServiceAlertRecord r : records) createOrUpdateServiceAlert(r);
      return records;
    }
  }
}
