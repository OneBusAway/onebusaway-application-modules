package org.onebusaway.transit_data_federation.impl.time;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.search;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class GenericBinarySearchTest {

  @Test
  public void testSearchForStopTime() {

    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    stopTimes.add(entry(1, 10, 00));
    stopTimes.add(entry(2, 10, 30));
    stopTimes.add(entry(3, 11, 00));
    stopTimes.add(entry(4, 11, 30));
    stopTimes.add(entry(5, 12, 00));

    assertEquals(0, search(stopTimes, time(9, 45), StopTimeOp.ARRIVAL));
    assertEquals(0, search(stopTimes, time(10, 00), StopTimeOp.ARRIVAL));
    assertEquals(1, search(stopTimes, time(10, 15), StopTimeOp.ARRIVAL));
    assertEquals(1, search(stopTimes, time(10, 30), StopTimeOp.ARRIVAL));
    assertEquals(2, search(stopTimes, time(10, 45), StopTimeOp.ARRIVAL));
    assertEquals(2, search(stopTimes, time(11, 00), StopTimeOp.ARRIVAL));
    assertEquals(3, search(stopTimes, time(11, 15), StopTimeOp.ARRIVAL));
    assertEquals(3, search(stopTimes, time(11, 30), StopTimeOp.ARRIVAL));
    assertEquals(4, search(stopTimes, time(11, 45), StopTimeOp.ARRIVAL));
    assertEquals(4, search(stopTimes, time(12, 00), StopTimeOp.ARRIVAL));
    assertEquals(5, search(stopTimes, time(12, 15), StopTimeOp.ARRIVAL));
  }

  private static int time(int hour, int min) {
    return (hour * 60 + min) * 60;
  }

  private StopTimeEntry entry(int id, int hour, int min) {
    int t = time(hour, min);
    StopTimeEntryImpl stopTime = new StopTimeEntryImpl();
    stopTime.setId(id);
    stopTime.setArrivalTime(t);
    stopTime.setDepartureTime(t);
    return stopTime;
  }
}
