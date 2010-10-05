package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.block;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.linkBlockTrips;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.stopTime;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.trip;

import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;

public class BlockLocationRecordCollectionTest {

  @Test
  public void test01() {

    BlockEntryImpl block = block("blockA");
    TripEntryImpl trip = trip("tripA", "serviceId");
    stopTime(0, null, trip, time(9, 00), 0);
    BlockConfigurationEntry blockConfig = linkBlockTrips(block, trip);
    BlockInstance blockInstance = new BlockInstance(blockConfig,
        System.currentTimeMillis());

    SortedMap<Long, Double> m = new TreeMap<Long, Double>();
    m.put(t(4, 10), 10.0);
    m.put(t(8, 20), 18.0);
    m.put(t(12, 30), 15.0);

    SortedMap<Integer, Double> sd = new TreeMap<Integer, Double>();
    sd.put(240, 10.0);
    sd.put(482, 18.0);
    sd.put(735, 15.0);

    SortedMap<Long, Double> d = new TreeMap<Long, Double>();
    d.put(t(4, 10), 100.0);
    d.put(t(8, 20), 200.0);
    d.put(t(12, 30), 300.0);

    SortedMap<Long, CoordinatePoint> p = new TreeMap<Long, CoordinatePoint>();
    CoordinatePoint p1 = new CoordinatePoint(47.0, -122.0);
    CoordinatePoint p2 = new CoordinatePoint(47.1, -122.1);
    CoordinatePoint p3 = new CoordinatePoint(47.2, -122.2);
    p.put(t(4, 10), p1);
    p.put(t(8, 20), p2);
    p.put(t(12, 30), p3);

    BlockLocationRecordCollection entry = new BlockLocationRecordCollection(t(
        3, 20), t(13, 20), m, sd, d, p);

    assertEquals(t(3, 20), entry.getFromTime());
    assertEquals(t(13, 20), entry.getToTime());

    assertEquals(10, entry.getScheduleDeviationForTargetTime(t(3, 20)));
    assertEquals(10, entry.getScheduleDeviationForTargetTime(t(4, 10)));
    assertEquals(12, entry.getScheduleDeviationForTargetTime(t(5, 12)));
    assertEquals(14, entry.getScheduleDeviationForTargetTime(t(6, 15)));
    assertEquals(16, entry.getScheduleDeviationForTargetTime(t(7, 18)));
    assertEquals(18, entry.getScheduleDeviationForTargetTime(t(8, 20)));
    assertEquals(17, entry.getScheduleDeviationForTargetTime(t(10, 0)));
    assertEquals(15, entry.getScheduleDeviationForTargetTime(t(12, 30)));
    assertEquals(15, entry.getScheduleDeviationForTargetTime(t(13, 20)));

    assertEquals(10, entry.getScheduleDeviationForScheduleTime(200));
    assertEquals(10, entry.getScheduleDeviationForScheduleTime(240));
    assertEquals(12, entry.getScheduleDeviationForScheduleTime(300));
    assertEquals(15, entry.getScheduleDeviationForScheduleTime(400));
    assertEquals(18, entry.getScheduleDeviationForScheduleTime(482));
    assertEquals(16, entry.getScheduleDeviationForScheduleTime(650));
    assertEquals(15, entry.getScheduleDeviationForScheduleTime(735));
    assertEquals(15, entry.getScheduleDeviationForScheduleTime(800));

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

    CoordinatePoint p4 = new CoordinatePoint(47.15, -122.15);
    entry = entry.addRecord(blockInstance, record(t(10, 0), 20, 220, p4),
        t(5, 0));

    assertEquals(t(6, 40), entry.getFromTime());
    assertEquals(t(11, 40), entry.getToTime());

    assertEquals(18, entry.getScheduleDeviationForTargetTime(t(6, 40)));
    assertEquals(18, entry.getScheduleDeviationForTargetTime(t(8, 20)));
    assertEquals(19, entry.getScheduleDeviationForTargetTime(t(9, 10)));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(t(10, 00)));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(t(10, 50)));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(t(11, 40)));

    assertEquals(18, entry.getScheduleDeviationForScheduleTime(400));
    assertEquals(18, entry.getScheduleDeviationForScheduleTime(482));
    assertEquals(18, entry.getScheduleDeviationForScheduleTime(500));
    assertEquals(19, entry.getScheduleDeviationForScheduleTime(555));
    assertEquals(20, entry.getScheduleDeviationForScheduleTime(556));
    assertEquals(20, entry.getScheduleDeviationForScheduleTime(580));
    assertEquals(20, entry.getScheduleDeviationForScheduleTime(700));

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

    CoordinatePoint p5 = new CoordinatePoint(47.4, -122.4);
    entry = entry.addRecord(blockInstance, record(t(16, 40), 14, 500, p5),
        t(6, 40));

    assertEquals(t(10, 00), entry.getFromTime());
    assertEquals(t(16, 40), entry.getToTime());

    assertEquals(20, entry.getScheduleDeviationForTargetTime(t(9, 10)));
    assertEquals(17, entry.getScheduleDeviationForTargetTime(t(13, 20)));
    assertEquals(14, entry.getScheduleDeviationForTargetTime(t(16, 40)));

    assertEquals(20, entry.getScheduleDeviationForScheduleTime(400));
    assertEquals(20, entry.getScheduleDeviationForScheduleTime(580));
    assertEquals(18, entry.getScheduleDeviationForScheduleTime(700));
    assertEquals(17, entry.getScheduleDeviationForScheduleTime(800));
    assertEquals(14, entry.getScheduleDeviationForScheduleTime(986));
    assertEquals(14, entry.getScheduleDeviationForScheduleTime(1000));

    assertEquals(220, entry.getDistanceAlongBlockForTargetTime(t(10, 00)), 0.0);
    assertEquals(360, entry.getDistanceAlongBlockForTargetTime(t(13, 20)), 0.0);
    assertEquals(500, entry.getDistanceAlongBlockForTargetTime(t(16, 40)), 0.0);
    assertEquals(640, entry.getDistanceAlongBlockForTargetTime(t(20, 00)), 0.0);

    assertNull(entry.getLastLocationForTargetTime(t(6, 40)));
    assertEquals(p4, entry.getLastLocationForTargetTime(t(10, 00)));
    assertEquals(p4, entry.getLastLocationForTargetTime(t(13, 20)));
    assertEquals(p5, entry.getLastLocationForTargetTime(t(16, 40)));
    assertEquals(p5, entry.getLastLocationForTargetTime(t(20, 00)));
  }

  private BlockLocationRecord record(long recordTime, int scheduleDeviation,
      double distanceAlongBlock, CoordinatePoint location) {
    BlockLocationRecord.Builder builder = BlockLocationRecord.builder();
    builder.setTime(recordTime);
    builder.setScheduleDeviation(scheduleDeviation);
    builder.setDistanceAlongBlock(distanceAlongBlock);
    builder.setLocation(location);
    return builder.create();
  }

  private long t(int minute, int sec) {
    return ((minute) * 60 + sec) * 1000;
  }
}
