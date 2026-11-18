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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

/**
 * A source's last update time feeds the metrics endpoint's
 * timeSinceLastRealtimeUpdate, which monitoring alerts on. A refresh that
 * yields no vehicles (a failed or timed-out fetch, or an empty feed) must not
 * reset it to zero: zero reads as "never updated", which monitoring discards,
 * so a feed that stops updating would vanish instead of going stale.
 */
public class GtfsRealtimeSourceLastUpdateTest {

  private static final long PREVIOUS_UPDATE = 1_795_000_000_000L;

  private GtfsRealtimeSource _source;

  @Before
  public void setup() throws Exception {
    _source = new GtfsRealtimeSource();
    _source.setAgencyIds(Collections.singletonList("1"));
    _source.setFeedId("feed");
    _source.setTripUpdatesUrl(new URL("http://localhost/trip-updates.pb"));
  }

  @Test
  public void refreshWithNoVehiclesKeepsThePreviousLastUpdate() {
    MonitoredResult previous = new MonitoredResult();
    previous.setLastUpdate(PREVIOUS_UPDATE);
    _source.setMonitoredResult(previous);

    MonitoredResult result = new MonitoredResult();
    _source.handleCombinedUpdates(result,
        Collections.<CombinedTripUpdatesAndVehiclePosition>emptyList());

    assertEquals(PREVIOUS_UPDATE, result.getLastUpdate());
  }

  @Test
  public void sourceThatNeverUpdatedStaysAtZero() {
    MonitoredResult result = new MonitoredResult();
    _source.handleCombinedUpdates(result,
        Collections.<CombinedTripUpdatesAndVehiclePosition>emptyList());

    assertEquals(0, result.getLastUpdate());
  }
}
