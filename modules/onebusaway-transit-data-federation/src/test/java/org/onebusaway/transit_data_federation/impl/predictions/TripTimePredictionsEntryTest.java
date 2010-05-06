package org.onebusaway.transit_data_federation.impl.predictions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    
    TripTimePredictionsEntry entry = new TripTimePredictionsEntry(200,800,m);
    
    assertEquals(200,entry.getFromTime());
    assertEquals(800,entry.getToTime());
    
    assertEquals(10,entry.getScheduleDeviationForTargetTime(200));
    assertEquals(10,entry.getScheduleDeviationForTargetTime(250));
    assertEquals(12,entry.getScheduleDeviationForTargetTime(312));
    assertEquals(14,entry.getScheduleDeviationForTargetTime(375));
    assertEquals(16,entry.getScheduleDeviationForTargetTime(438));
    assertEquals(18,entry.getScheduleDeviationForTargetTime(500));
    assertEquals(17,entry.getScheduleDeviationForTargetTime(600));
    assertEquals(15,entry.getScheduleDeviationForTargetTime(750));
    assertEquals(15,entry.getScheduleDeviationForTargetTime(800));
    
    entry = entry.addPrediction(600, 20, 300);
    
    assertEquals(400,entry.getFromTime());
    assertEquals(700,entry.getToTime());
    
    assertEquals(18,entry.getScheduleDeviationForTargetTime(400));
    assertEquals(18,entry.getScheduleDeviationForTargetTime(500));
    assertEquals(19,entry.getScheduleDeviationForTargetTime(550));
    assertEquals(20,entry.getScheduleDeviationForTargetTime(600));
    assertEquals(20,entry.getScheduleDeviationForTargetTime(650));    
    assertEquals(20,entry.getScheduleDeviationForTargetTime(700));  
    
    entry = entry.addPrediction(1000, 14, 400);
    
    assertEquals(600,entry.getFromTime());
    assertEquals(1000,entry.getToTime());
    
    assertEquals(20,entry.getScheduleDeviationForTargetTime(600));
    assertEquals(17,entry.getScheduleDeviationForTargetTime(800));    
    assertEquals(14,entry.getScheduleDeviationForTargetTime(1000));
  }
}
