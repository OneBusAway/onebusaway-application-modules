package org.onebusaway.tripplanner.impl.state;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StopTimeBinarySearchTest {

  @Test
  public void testGetNextStopTime() {

    StopTimeBinarySearch search = new StopTimeBinarySearch();

    List<StopTime> stopTimes = getStopTimes(1, 5, 10, 10, 12, 13, 14, 16);
    List<StopTimeInstance> stis = search.getNextStopTime(0, stopTimes, 4000);
    assertStopTimeInstances(stis, 5000);

    stis = search.getNextStopTime(0, stopTimes, 0);
    assertStopTimeInstances(stis, 1000);

    stis = search.getNextStopTime(0, stopTimes, 1000);
    assertStopTimeInstances(stis, 1000);

    stis = search.getNextStopTime(0, stopTimes, 5000);
    assertStopTimeInstances(stis, 5000);

    stis = search.getNextStopTime(0, stopTimes, 6000);
    assertStopTimeInstances(stis, 10000, 10000);

    stis = search.getNextStopTime(0, stopTimes, 10000);
    assertStopTimeInstances(stis, 10000, 10000);

    stis = search.getNextStopTime(0, stopTimes, 11000);
    assertStopTimeInstances(stis, 12000);

    stis = search.getNextStopTime(0, stopTimes, 14500);
    assertStopTimeInstances(stis, 16000);

    stis = search.getNextStopTime(0, stopTimes, 16500);
    assertStopTimeInstances(stis);
  }

  @Test
  public void testEmptyStopTimes() {

    StopTimeBinarySearch search = new StopTimeBinarySearch();

    List<StopTime> stopTimes = getStopTimes();
    List<StopTimeInstance> stis = search.getNextStopTime(0, stopTimes, 4000);
    assertStopTimeInstances(stis);
  }

  @Test
  public void testOneStopTime() {

    StopTimeBinarySearch search = new StopTimeBinarySearch();

    List<StopTime> stopTimes = getStopTimes(1);
    List<StopTimeInstance> stis = search.getNextStopTime(0, stopTimes, 0);
    assertStopTimeInstances(stis, 1000);

    stis = search.getNextStopTime(0, stopTimes, 1000);
    assertStopTimeInstances(stis, 1000);

    stis = search.getNextStopTime(0, stopTimes, 2000);
    assertStopTimeInstances(stis);
  }

  @Test
  public void testTwoStopTimes() {

    StopTimeBinarySearch search = new StopTimeBinarySearch();

    List<StopTime> stopTimes = getStopTimes(1, 2);

    List<StopTimeInstance> stis = search.getNextStopTime(0, stopTimes, 0);
    assertStopTimeInstances(stis, 1000);

    stis = search.getNextStopTime(0, stopTimes, 1000);
    assertStopTimeInstances(stis, 1000);

    stis = search.getNextStopTime(0, stopTimes, 1500);
    assertStopTimeInstances(stis, 2000);

    stis = search.getNextStopTime(0, stopTimes, 2000);
    assertStopTimeInstances(stis, 2000);

    stis = search.getNextStopTime(0, stopTimes, 2500);
    assertStopTimeInstances(stis);
  }

  @Test
  public void testTwoStopTimesSame() {

    StopTimeBinarySearch search = new StopTimeBinarySearch();

    List<StopTime> stopTimes = getStopTimes(1, 1);

    List<StopTimeInstance> stis = search.getNextStopTime(0, stopTimes, 0);
    assertStopTimeInstances(stis, 1000, 1000);

    stis = search.getNextStopTime(0, stopTimes, 1000);
    assertStopTimeInstances(stis, 1000, 1000);

    stis = search.getNextStopTime(0, stopTimes, 1500);
    assertStopTimeInstances(stis);
  }

  @Test
  public void testSameStopTimesAsFrontAndBack() {

    StopTimeBinarySearch search = new StopTimeBinarySearch();

    List<StopTime> stopTimes = getStopTimes(1, 1, 2, 3, 3);

    List<StopTimeInstance> stis = search.getNextStopTime(0, stopTimes, 0);
    assertStopTimeInstances(stis, 1000, 1000);

    stis = search.getNextStopTime(0, stopTimes, 1000);
    assertStopTimeInstances(stis, 1000, 1000);

    stis = search.getNextStopTime(0, stopTimes, 1500);
    assertStopTimeInstances(stis, 2000);

    stis = search.getNextStopTime(0, stopTimes, 2000);
    assertStopTimeInstances(stis, 2000);

    stis = search.getNextStopTime(0, stopTimes, 2500);
    assertStopTimeInstances(stis, 3000, 3000);

    stis = search.getNextStopTime(0, stopTimes, 3000);
    assertStopTimeInstances(stis, 3000, 3000);

    stis = search.getNextStopTime(0, stopTimes, 3500);
    assertStopTimeInstances(stis);
  }

  private void assertStopTimeInstances(List<StopTimeInstance> stis,
      long... times) {
    assertEquals(times.length, stis.size());
    for (int i = 0; i < times.length; i++) {
      long time = times[i];
      StopTimeInstance sti = stis.get(i);
      assertEquals(time, sti.getDepartureTime().getTime());
    }
  }

  private List<StopTime> getStopTimes(int... times) {
    List<StopTime> stopTimes = new ArrayList<StopTime>(times.length);
    for (int time : times)
      stopTimes.add(getStopTime(time));
    Collections.sort(stopTimes);
    return stopTimes;
  }

  private StopTime getStopTime(int time) {
    StopTime st = new StopTime();
    st.setDepartureTime(time);
    return st;
  }
}
