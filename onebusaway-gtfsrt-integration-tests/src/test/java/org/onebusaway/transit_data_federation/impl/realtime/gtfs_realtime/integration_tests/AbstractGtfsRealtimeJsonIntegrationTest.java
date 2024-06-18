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
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AbstractGtfsRealtimeIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handling of json files that represent protocol buffer.
 */
public abstract class AbstractGtfsRealtimeJsonIntegrationTest extends AbstractGtfsRealtimeIntegrationTest {
  public void writeFeed(GtfsRealtime.FeedMessage feed, URL feedLocation) throws IOException {
    feed.writeTo(Files.newOutputStream(Path.of(feedLocation.getFile())));
  }

  public URL createFeedLocation() throws IOException {
    return File.createTempFile("trip_updates", "pb").toURI().toURL();
  }
}
