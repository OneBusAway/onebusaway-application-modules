/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.transit_data_federation.impl.realtime.BlockLocationRecord.Builder;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;

public class BlockLocationRecordCollectionTest {

  @Test
  public void test01() {

    BlockEntryImpl block = block("blockA");
    TripEntryImpl trip = trip("tripA", "serviceId");
    stopTime(0, null, trip, time(9, 00), 0);
    BlockConfigurationEntry blockConfig = linkBlockTrips(block, trip);
    BlockInstance blockInstance = new BlockInstance(blockConfig,
        System.currentTimeMillis());

    SortedMap<Long, BlockLocationRecord> records = new TreeMap<Long, BlockLocationRecord>();

    CoordinatePoint p1 = new CoordinatePoint(47.0, -122.0);
    CoordinatePoint p2 = new CoordinatePoint(47.1, -122.1);
    CoordinatePoint p3 = new CoordinatePoint(47.2, -122.2);

    EVehiclePhase inProgress = EVehiclePhase.IN_PROGRESS;
    EVehiclePhase layover = EVehiclePhase.LAYOVER_DURING;

    addRecord(records, record(t(4, 10), 10.0, 100.0, p1, 0.0, inProgress, "ok"));
    addRecord(records,
        record(t(8, 20), 18.0, 200.0, p2, 90.0, layover, "not ok"));
    addRecord(records,
        record(t(12, 30), 15.0, 300.0, p3, 45.0, inProgress, "ok"));

    BlockLocationRecordCollection entry = new BlockLocationRecordCollection(t(
        3, 20), t(13, 20), records);

    assertEquals(t(3, 20), entry.getFromTime());
    assertEquals(t(13, 20), entry.getToTime());

    assertEquals(10, entry.getScheduleDeviationForTargetTime(t(3, 20)), 0.0);
    assertEquals(10, entry.getScheduleDeviationForTargetTime(t(4, 10)), 0.0);
    assertEquals(12, entry.getScheduleDeviationForTargetTime(t(5, 12)), 0.0);
    assertEquals(14, entry.getScheduleDeviationForTargetTime(t(6, 15)), 0.0);
    assertEquals(16, entry.getScheduleDeviationForTargetTime(t(7, 18)), 0.0);
    assertEquals(18, entry.getScheduleDeviationForTargetTime(t(8, 20)), 0.0);
    assertEquals(17, entry.getScheduleDeviationForTargetTime(t(10, 0)), 0.0);
    assertEquals(15, entry.getScheduleDeviationForTargetTime(t(12, 30)), 0.0);
    assertEquals(15, entry.getScheduleDeviationForTargetTime(t(13, 20)), 0.0);

    assertEquals(80, entry.getDistanceAlongBlockForTargetTime(t(3, 20)), 0.0);
    assertEquals(100, entry.getDistanceAlongBlockForTargetTime(t(4, 10)), 0.0);
    assertEquals(124.8, entry.getDistanceAlongBlockForTargetTime(t(5, 12)), 0.0);
    assertEquals(150, entry.getDistanceAlongBlockForTargetTime(t(6, 15)), 0.0);
    assertEquals(175.2, entry.getDistanceAlongBlockForTargetTime(t(7, 18)), 0.0);
    assertEquals(200, entry.getDistanceAlongBlockForTargetTime(t(8, 20)), 0.0);
    assertEquals(240, entry.getDistanceAlongBlockForTargetTime(t(10, 0)), 0.0);
    assertEquals(300, entry.getDistanceAlongBlockForTargetTime(t(12, 30)), 0.0);
    assertEquals(320, entry.getDistanceAlongBlockForTargetTime(t(13, 20)), 0.0);

    assertNull(entry.getLastLocationForTargetTime(t(3, 20)));
    assertNull(entry.getLastLocationForTargetTime(t(4, 9)));
    assertEquals(p1, entry.getLastLocationForTargetTime(t(4, 10)));
    assertEquals(p1, entry.getLastLocationForTargetTime(t(5, 12)));
    assertEquals(p1, entry.getLastLocationForTargetTime(t(8, 19)));
    assertEquals(p2, entry.getLastLocationForTargetTime(t(8, 20)));
    assertEquals(p2, entry.getLastLocationForTargetTime(t(10, 0)));
    assertEquals(p2, entry.getLastLocationForTargetTime(t(12, 29)));
    assertEquals(p3, entry.getLastLocationForTargetTime(t(12, 30)));
    assertEquals(p3, entry.getLastLocationForTargetTime(t(16, 40)));

    assertTrue(Double.isNaN(entry.getLastOrientationForTargetTime(t(3, 20))));
    assertTrue(Double.isNaN(entry.getLastOrientationForTargetTime(t(4, 9))));
    assertEquals(0.0, entry.getLastOrientationForTargetTime(t(4, 10)), 0.0);
    assertEquals(0.0, entry.getLastOrientationForTargetTime(t(5, 12)), 0.0);
    assertEquals(0.0, entry.getLastOrientationForTargetTime(t(8, 19)), 0.0);
    assertEquals(90.0, entry.getLastOrientationForTargetTime(t(8, 20)), 0.0);
    assertEquals(90.0, entry.getLastOrientationForTargetTime(t(10, 0)), 0.0);
    assertEquals(90.0, entry.getLastOrientationForTargetTime(t(12, 29)), 0.0);
    assertEquals(45.0, entry.getLastOrientationForTargetTime(t(12, 30)), 0.0);
    assertEquals(45.0, entry.getLastOrientationForTargetTime(t(16, 40)), 0.0);

    assertNull(entry.getPhaseForTargetTime(t(3, 20)));
    assertNull(entry.getPhaseForTargetTime(t(4, 9)));
    assertEquals(inProgress, entry.getPhaseForTargetTime(t(4, 10)));
    assertEquals(inProgress, entry.getPhaseForTargetTime(t(5, 12)));
    assertEquals(inProgress, entry.getPhaseForTargetTime(t(8, 19)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(8, 20)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(10, 0)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(12, 29)));
    assertEquals(inProgress, entry.getPhaseForTargetTime(t(12, 30)));
    assertEquals(inProgress, entry.getPhaseForTargetTime(t(16, 40)));

    assertNull(entry.getStatusForTargetTime(t(3, 20)));
    assertNull(entry.getStatusForTargetTime(t(4, 9)));
    assertEquals("ok", entry.getStatusForTargetTime(t(4, 10)));
    assertEquals("ok", entry.getStatusForTargetTime(t(5, 12)));
    assertEquals("ok", entry.getStatusForTargetTime(t(8, 19)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(8, 20)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(10, 0)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(12, 29)));
    assertEquals("ok", entry.getStatusForTargetTime(t(12, 30)));
    assertEquals("ok", entry.getStatusForTargetTime(t(16, 40)));

    CoordinatePoint p4 = new CoordinatePoint(47.15, -122.15);
    entry = entry.addRecord(blockInstance,
        record(t(10, 0), 20, 220, p4, 270.0, layover, "not ok"), t(5, 0));

    assertEquals(t(6, 40), entry.getFromTime());
    assertEquals(t(11, 40), entry.getToTime());

    assertEquals(18, entry.getScheduleDeviationForTargetTime(t(6, 40)), 0.0);
    assertEquals(18, entry.getScheduleDeviationForTargetTime(t(8, 20)), 0.0);
    assertEquals(19, entry.getScheduleDeviationForTargetTime(t(9, 10)), 0.0);
    assertEquals(20, entry.getScheduleDeviationForTargetTime(t(10, 00)), 0.0);
    assertEquals(20, entry.getScheduleDeviationForTargetTime(t(10, 50)), 0.0);
    assertEquals(20, entry.getScheduleDeviationForTargetTime(t(11, 40)), 0.0);

    assertEquals(180, entry.getDistanceAlongBlockForTargetTime(t(6, 40)), 0.0);
    assertEquals(200, entry.getDistanceAlongBlockForTargetTime(t(8, 20)), 0.0);
    assertEquals(210, entry.getDistanceAlongBlockForTargetTime(t(9, 10)), 0.0);
    assertEquals(220, entry.getDistanceAlongBlockForTargetTime(t(10, 00)), 0.0);
    assertEquals(230, entry.getDistanceAlongBlockForTargetTime(t(10, 50)), 0.0);
    assertEquals(240, entry.getDistanceAlongBlockForTargetTime(t(11, 40)), 0.0);

    assertNull(entry.getLastLocationForTargetTime(t(6, 40)));
    assertEquals(p2, entry.getLastLocationForTargetTime(t(8, 20)));
    assertEquals(p2, entry.getLastLocationForTargetTime(t(9, 10)));
    assertEquals(p4, entry.getLastLocationForTargetTime(t(10, 00)));
    assertEquals(p4, entry.getLastLocationForTargetTime(t(10, 50)));

    assertTrue(Double.isNaN(entry.getLastOrientationForTargetTime(t(6, 40))));
    assertEquals(90.0, entry.getLastOrientationForTargetTime(t(8, 20)), 0.0);
    assertEquals(90.0, entry.getLastOrientationForTargetTime(t(9, 10)), 0.0);
    assertEquals(270.0, entry.getLastOrientationForTargetTime(t(10, 00)), 0.0);
    assertEquals(270.0, entry.getLastOrientationForTargetTime(t(10, 50)), 0.0);

    assertNull(entry.getPhaseForTargetTime(t(6, 40)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(8, 20)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(9, 10)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(10, 00)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(10, 50)));

    assertNull(entry.getStatusForTargetTime(t(6, 40)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(8, 20)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(9, 10)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(10, 00)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(10, 50)));

    CoordinatePoint p5 = new CoordinatePoint(47.4, -122.4);
    entry = entry.addRecord(blockInstance,
        record(t(16, 40), 14, 500, p5, 180.0, inProgress, "ok"), t(6, 40));

    assertEquals(t(10, 00), entry.getFromTime());
    assertEquals(t(16, 40), entry.getToTime());

    assertEquals(20, entry.getScheduleDeviationForTargetTime(t(9, 10)), 0.0);
    assertEquals(17, entry.getScheduleDeviationForTargetTime(t(13, 20)), 0.0);
    assertEquals(14, entry.getScheduleDeviationForTargetTime(t(16, 40)), 0.0);

    assertEquals(220, entry.getDistanceAlongBlockForTargetTime(t(10, 00)), 0.0);
    assertEquals(360, entry.getDistanceAlongBlockForTargetTime(t(13, 20)), 0.0);
    assertEquals(500, entry.getDistanceAlongBlockForTargetTime(t(16, 40)), 0.0);
    assertEquals(640, entry.getDistanceAlongBlockForTargetTime(t(20, 00)), 0.0);

    assertNull(entry.getLastLocationForTargetTime(t(6, 40)));
    assertEquals(p4, entry.getLastLocationForTargetTime(t(10, 00)));
    assertEquals(p4, entry.getLastLocationForTargetTime(t(13, 20)));
    assertEquals(p5, entry.getLastLocationForTargetTime(t(16, 40)));
    assertEquals(p5, entry.getLastLocationForTargetTime(t(20, 00)));

    assertTrue(Double.isNaN(entry.getLastOrientationForTargetTime(t(6, 40))));
    assertEquals(270.0, entry.getLastOrientationForTargetTime(t(10, 00)), 0.0);
    assertEquals(270.0, entry.getLastOrientationForTargetTime(t(13, 20)), 0.0);
    assertEquals(180.0, entry.getLastOrientationForTargetTime(t(16, 40)), 0.0);
    assertEquals(180.0, entry.getLastOrientationForTargetTime(t(20, 00)), 0.0);

    assertNull(entry.getPhaseForTargetTime(t(6, 40)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(10, 00)));
    assertEquals(layover, entry.getPhaseForTargetTime(t(13, 20)));
    assertEquals(inProgress, entry.getPhaseForTargetTime(t(16, 40)));
    assertEquals(inProgress, entry.getPhaseForTargetTime(t(20, 00)));

    assertNull(entry.getStatusForTargetTime(t(6, 40)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(10, 00)));
    assertEquals("not ok", entry.getStatusForTargetTime(t(13, 20)));
    assertEquals("ok", entry.getStatusForTargetTime(t(16, 40)));
    assertEquals("ok", entry.getStatusForTargetTime(t(20, 00)));
  }

  private BlockLocationRecord record(long time, double scheduleDeviation,
      double distanceAlongBlock, CoordinatePoint location, double orientation,
      EVehiclePhase phase, String status) {
    Builder builder = BlockLocationRecord.builder();
    builder.setTime(time);
    builder.setScheduleDeviation(scheduleDeviation);
    builder.setDistanceAlongBlock(distanceAlongBlock);
    builder.setLocation(location);
    builder.setOrientation(orientation);
    builder.setPhase(phase);
    builder.setStatus(status);
    return builder.create();
  }

  private void addRecord(SortedMap<Long, BlockLocationRecord> records,
      BlockLocationRecord record) {
    records.put(record.getTime(), record);
  }

  private long t(int minute, int sec) {
    return ((minute) * 60 + sec) * 1000;
  }
}
