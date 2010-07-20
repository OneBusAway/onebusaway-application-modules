package org.onebusaway.transit_data_federation.impl.realtime;

import static org.junit.Assert.assertEquals;

import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;
import org.onebusaway.gtfs.model.AgencyAndId;

public class TripPositionRecordCollectionTest {

  @Test
  public void test01() {

    SortedMap<Long, Integer> m = new TreeMap<Long, Integer>();
    m.put(250L, 10);
    m.put(500L, 18);
    m.put(750L, 15);

    TripPositionRecordCollection entry = new TripPositionRecordCollection(200,
        800, m);

    assertEquals(200, entry.getFromTime());
    assertEquals(800, entry.getToTime());

    assertEquals(10, entry.getScheduleDeviationForTargetTime(200));
    assertEquals(10, entry.getScheduleDeviationForTargetTime(250));
    assertEquals(12, entry.getScheduleDeviationForTargetTime(312));
    assertEquals(14, entry.getScheduleDeviationForTargetTime(375));
    assertEquals(16, entry.getScheduleDeviationForTargetTime(438));
    assertEquals(18, entry.getScheduleDeviationForTargetTime(500));
    assertEquals(17, entry.getScheduleDeviationForTargetTime(600));
    assertEquals(15, entry.getScheduleDeviationForTargetTime(750));
    assertEquals(15, entry.getScheduleDeviationForTargetTime(800));

    entry = entry.addRecord(record(600, 20), 300);

    assertEquals(400, entry.getFromTime());
    assertEquals(700, entry.getToTime());

    assertEquals(18, entry.getScheduleDeviationForTargetTime(400));
    assertEquals(18, entry.getScheduleDeviationForTargetTime(500));
    assertEquals(19, entry.getScheduleDeviationForTargetTime(550));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(600));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(650));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(700));

    entry = entry.addRecord(record(1000, 14), 400);

    assertEquals(600, entry.getFromTime());
    assertEquals(1000, entry.getToTime());

    assertEquals(20, entry.getScheduleDeviationForTargetTime(600));
    assertEquals(17, entry.getScheduleDeviationForTargetTime(800));
    assertEquals(14, entry.getScheduleDeviationForTargetTime(1000));
  }

  @Test
  public void test02() {

    SortedMap<Long, Integer> m = new TreeMap<Long, Integer>();

    m.put(1256014629194L, -1);
    m.put(1256014690365L, -1);
    m.put(1256014749281L, -1);
    m.put(1256014811083L, -1);
    m.put(1256014873182L, -1);
    m.put(1256014933139L, -1);
    m.put(1256014994241L, -1);
    m.put(1256015056226L, -1);
    m.put(1256015118150L, -1);
    m.put(1256015176376L, -1);
    m.put(1256015242309L, -1);
    m.put(1256015303789L, -1);
    m.put(1256015363324L, -1);
    m.put(1256015431407L, -1);
    m.put(1256015510312L, -1);
    m.put(1256015573500L, -1);
    m.put(1256015636443L, -1);

    TripPositionRecordCollection entry = new TripPositionRecordCollection(
        m.firstKey(), m.lastKey(), m);

    int deviation = entry.getScheduleDeviationForTargetTime(1256015757567L);
    assertEquals(-1, deviation);
  }

  private TripPositionRecord record(long recordTime, int scheduleDeviation) {
    AgencyAndId tripId = null;
    long serviceDate = 0;
    AgencyAndId vehicleId = null;
    return new TripPositionRecord(tripId, serviceDate, recordTime,
        scheduleDeviation, vehicleId);
  }
}
