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

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleConventionMain;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.ConsolidatedStopsService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.bundle.BundleManagementService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Build and load a bundle to match to gtfs-rt.  Then run comparisons against
 * the reults of the gtfs-rt.
 */
public class GtfsRealtimeIntegrationTest {

  private static Logger _log = LoggerFactory.getLogger(GtfsRealtimeIntegrationTest.class);
  private GtfsRealtimeSource _source;
  private String _bundleGzipURI;
  private String _bundleRootDir;
  private String _bundleIndexURI;

  @Before
  public void setup() throws Exception {
    _bundleGzipURI = System.getProperty("bundle.index.json");
    if (!exists(_bundleGzipURI)) {
      _log.info("building bundle...");
      // integration test support:  build bundle
      ClassPathResource bundleResource = new ClassPathResource("org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime");
      String bundleInputDir = bundleResource.getURL().getFile();
      _bundleRootDir =
              System.getProperty("java.io.tmpdir")
                      + "bundle" + System.currentTimeMillis();
      makeTempDirectory(_bundleRootDir, System.getProperty("bundle.keep"));
      String bundleName = "v" + System.currentTimeMillis();
      _bundleGzipURI = buildBundle(bundleInputDir, _bundleRootDir, bundleName);
      _bundleIndexURI = _bundleRootDir + File.separator + "index.json";
      createIndexJson(_bundleGzipURI,  _bundleRootDir);
    } else {
      // re-use the bundle if It's still present
      _log.info("bundle exists at {}, loading", _bundleGzipURI);
      _bundleRootDir = parsePath(_bundleGzipURI);
      _bundleIndexURI = _bundleRootDir + File.separator + "index.json";
    }

  }


  @Test
  public void testAddedViaExtension() throws Exception {
    System.setProperty("bundle.root", _bundleRootDir);
    System.setProperty("bundlePath", _bundleRootDir);
    System.setProperty("bundle.remote.source", _bundleIndexURI);
    String[] paths = {"test-data-sources.xml"};
    ConfigurableApplicationContext context = ContainerLibrary.createContext(Arrays.asList(paths));

    AgencyService agencyService = context.getBean(AgencyService.class);
    TransitGraphDao currentTransitGraphDao = context.getBean(TransitGraphDao.class);

    _source = new GtfsRealtimeSource();
    _source.setAgencyId("MTASBWY");

    // set the bundle _source
    _source.setTransitGraphDao(currentTransitGraphDao);
    // manual dependency injection....
    _source.setAgencyService(agencyService);
    _source.setBlockCalendarService(context.getBean(BlockCalendarService.class));
    _source.setBlockLocationService(context.getBean(BlockLocationService.class));
    _source.setScheduledExecutorService(context.getBean(ScheduledExecutorService.class));
    VehicleLocationListener listener = new TestVehicleLocationListener();
    _source.setVehicleLocationListener(listener);
    _source.setConsolidatedStopsService(context.getBean(ConsolidatedStopsService.class));
    MonitoredResult testResult = new MonitoredResult();
    _source.setMonitoredResult(testResult);

    BundleManagementService bundleManagementService = context.getBean(BundleManagementService.class);
    int i = 0;
    while (!bundleManagementService.bundleIsReady()) {
      Thread.sleep(1000);
      if (i % 10 == 0) _log.info("waiting on bundle.....");
    }
    // wait an extra bit just in case
    for (int j = 0; j < 10; j++) {
      Thread.sleep(1000);
    }

    _source.start(); // initialize

    // this is the gtfs-rt protocol-buffer file to match to the loaded bundle
    String gtfsrtFilename = "org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/nyct_subways_gtfs_rt.pb";
    ClassPathResource gtfsRtResource = new ClassPathResource(gtfsrtFilename);
    if (!gtfsRtResource.exists()) throw new RuntimeException(gtfsrtFilename + " not found in classpath!");
    _source.setTripUpdatesUrl(gtfsRtResource.getURL());
    _source.refresh(); // launch

    // check TestVehicleLocationListenr for expected service
    // check MonitoredResult for expected number of results
    assertFalse(testResult.getAddedTripIds().isEmpty()); // this doesn't work yet!
  }


  private String createIndexJson(String bundleGzipURI, String baseLocation) throws Exception {
    String indexJson = "{\"latest\":\"" + bundleGzipURI + "\"}";
    String location = baseLocation + File.separator + "index.json";
    FileWriter fw = new FileWriter(location);
    fw.write(indexJson);
    fw.close();
    _log.info("wrote {} to {}", bundleGzipURI, baseLocation);
    return location;
  }

  private File makeTempDirectory(String tmpDirStr, String keepIfSet) {
    File tmpDir = new File(tmpDirStr);
    tmpDir.mkdirs();
    if (keepIfSet == null) {
      DeleteTempDirectoryOnExitRunnable r = new DeleteTempDirectoryOnExitRunnable(
              tmpDir);
      Runtime.getRuntime().addShutdownHook(new Thread(r));
    }
    return tmpDir;
  }

  private String buildBundle(String bundleInputDir, String bundleOutputDir, String bundleName) throws IOException {
    dirCheck(bundleInputDir);
    dirCheck(bundleOutputDir);
    _log.info("buliding {} from {} to {}", bundleName, bundleInputDir, bundleOutputDir);
    FederatedTransitDataBundleConventionMain main = new FederatedTransitDataBundleConventionMain();
    String[] args = {bundleInputDir, bundleOutputDir, bundleName};
    return main.run(args);
  }

  private boolean exists(String checkFileName) {
    if (checkFileName == null) return false;
    File checkFile = new File(checkFileName);
    return checkFile.exists() && checkFile.isFile();
  }

  private void dirCheck(String checkFileName) throws FileNotFoundException {
    if (checkFileName == null || checkFileName.length() == 0) {
      throw new IllegalStateException("null/empty filename provided");
    }
    File check = new File(checkFileName);
    if (!check.exists()) {
      throw new FileNotFoundException("expecting file/dir at " + checkFileName);
    }
  }

  private String parsePath(String fileName) {
    File file = new File(fileName);
    return file.getParent();
  }

  /**
   * This runner should clean up all the extracted wars from the filesystem when
   * we finish up.
   *
   * (This was copied over from legacy quickstart app.)
   *
   * @author bdferris
   *
   */
  private static class DeleteTempDirectoryOnExitRunnable implements Runnable {

    private File _path;

    public DeleteTempDirectoryOnExitRunnable(File path) {
      _path = path;
    }

    @Override
    public void run() {
      deleteFiles(_path);
    }

    private void deleteFiles(File file) {
      if (file.isDirectory()) {
        for (File child : file.listFiles())
          deleteFiles(child);
      }
      file.delete();
    }
  };


}
