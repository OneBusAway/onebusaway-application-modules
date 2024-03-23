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

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.transit_data_federation.impl.realtime.DynamicBlockIndexServiceImpl;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Support for loading bundles in integration tests.
 */
public class BundleLoader {
  protected static Logger _log = LoggerFactory.getLogger(BundleLoader.class);

  protected BundleContext _bundleContext;
  protected ConfigurableApplicationContext _context;
  public ConfigurableApplicationContext getApplicationContext() {
    return _context;
  }
  protected GtfsRealtimeSource _source;

  public BundleLoader(BundleContext bundleContext) {
    this._bundleContext = bundleContext;
  }

  public GtfsRealtimeSource getSource() {
    return _source;
  }

  public void create(String[] paths) {
    setupEnvironment();
    _context = ContainerLibrary.createContext(Arrays.asList(paths));
  }

  public void load() throws Exception{
    AgencyService agencyService = _context.getBean(AgencyService.class);
    TransitGraphDao currentTransitGraphDao = _context.getBean(TransitGraphDao.class);
    _source = new GtfsRealtimeSource();
    _source.setRefreshInterval(0); // don't refresh

    // set the bundle _source
    _source.setTransitGraphDao(currentTransitGraphDao);
    // manual dependency injection....
    _source.setAgencyService(agencyService);
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
      if (i % 10 == 0) _log.info("waiting on bundle.....");
    }
    // wait an extra bit just in case
    for (int j = 0; j < 10; j++) {
      Thread.sleep(1000);
    }
    _source.setFilterUnassigned(false);  // let time disqualify those trips
    _source.start(); // initialize

  }

  private void setupEnvironment() {
    System.setProperty("bundle.root", _bundleContext._bundleRootDir);
    System.setProperty("bundlePath", _bundleContext._bundleRootDir);
    System.setProperty("bundle.remote.source", _bundleContext._bundleIndexURI);
  }

  public void close() {
    _context.stop();
    _context.close();

  }

}
