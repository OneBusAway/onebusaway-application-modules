package org.onebusaway.transit_data_federation.impl.predictions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

public class TripTimePredictionsEntryTest {

  @Test
  public void test() {

    SortedMap<Long, Integer> m = new TreeMap<Long, Integer>();
    m.put(250L, 10);
    m.put(500L, 18);
    m.put(750L, 15);

    TripTimePredictionsEntry entry = new TripTimePredictionsEntry(200, 800, m, null);

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

    entry = entry.addPrediction(600, 20, 300);

    assertEquals(400, entry.getFromTime());
    assertEquals(700, entry.getToTime());

    assertEquals(18, entry.getScheduleDeviationForTargetTime(400));
    assertEquals(18, entry.getScheduleDeviationForTargetTime(500));
    assertEquals(19, entry.getScheduleDeviationForTargetTime(550));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(600));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(650));
    assertEquals(20, entry.getScheduleDeviationForTargetTime(700));

    entry = entry.addPrediction(1000, 14, 400);

    assertEquals(600, entry.getFromTime());
    assertEquals(1000, entry.getToTime());

    assertEquals(20, entry.getScheduleDeviationForTargetTime(600));
    assertEquals(17, entry.getScheduleDeviationForTargetTime(800));
    assertEquals(14, entry.getScheduleDeviationForTargetTime(1000));
  }

  @Test
  public void go() {

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

    TripTimePredictionsEntry entry = new TripTimePredictionsEntry(m.firstKey(), m.lastKey(), m, null);
    
    int deviation = entry.getScheduleDeviationForTargetTime(1256015757567L);
    assertEquals(-1,deviation);
  }
}
