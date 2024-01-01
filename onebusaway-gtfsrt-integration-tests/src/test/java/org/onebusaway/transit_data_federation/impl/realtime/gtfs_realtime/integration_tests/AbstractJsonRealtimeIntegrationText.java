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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.integration_tests;

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.TestVehicleLocationListener;
import org.onebusaway.transit_data_federation.impl.realtime.VehicleStatusServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRtBuilder;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TimeZone;

public abstract class AbstractJsonRealtimeIntegrationText extends AbstractGtfsRealtimeIntegrationTest {

  public abstract String getTimezone();
  public abstract void runTestInTimezone() throws Exception;

  public void setupTest() throws Exception {
    TimeZone aDefault = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone(getTimezone()));
      runTestInTimezone();
    } finally {
      TimeZone.setDefault(aDefault);
    }
  }

  protected URL readJson(String jsonFilename) throws Exception {
    ClassPathResource gtfsRtResource = new ClassPathResource(jsonFilename);
    if (!gtfsRtResource.exists()) throw new RuntimeException(jsonFilename + " not found in classpath!");
    GtfsRtBuilder builder = new GtfsRtBuilder();
    GtfsRealtime.FeedMessage feed = builder.readJson(gtfsRtResource.getURL());

    URL tmpFeedLocation = createFeedLocation();
    writeFeed(feed, tmpFeedLocation);
    return tmpFeedLocation;
  }

  private void writeFeed(GtfsRealtime.FeedMessage feed, URL feedLocation) throws IOException {
    feed.writeTo(Files.newOutputStream(Path.of(feedLocation.getFile())));
  }

  private URL createFeedLocation() throws IOException {
    return File.createTempFile("trip_updates", "pb").toURI().toURL();
  }

  protected TestVehicleLocationListener setupTestListener(GtfsRealtimeSource source) {
    TestVehicleLocationListener listener = new TestVehicleLocationListener();
    VehicleLocationListener actualListener = getBundleLoader().getApplicationContext().getBean(VehicleStatusServiceImpl.class);
    listener.setVehicleLocationListener(actualListener);
    source.setVehicleLocationListener(listener);
    MonitoredResult testResult = new MonitoredResult();
    source.setMonitoredResult(testResult);
    return listener;
  }
}
