package org.onebusaway.transit_data_federation.impl.time;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations.getStopTimeEntriesInRange;
import static org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations.getStopTimeInstancesInRange;
import static org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations.searchForStopTime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeIndexImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import edu.washington.cs.rse.text.DateLibrary;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;

public class StopTimeSearchOperationsTest {

  private static final StopTimeInstanceProxyComparator _comparator = new StopTimeInstanceProxyComparator();

  @Test
  public void testGetStopTimeInstancesInRange() {

    Date today = DateLibrary.getTimeAsDay(new Date());
    Date yesterday = shiftDate(today, -1);
    Date tomorrow = shiftDate(today, 1);

    List<StopTimeEntry> stopTimesA = new ArrayList<StopTimeEntry>();
    stopTimesA.add(entry(1, 8, 00));
    stopTimesA.add(entry(2, 8, 30));
    stopTimesA.add(entry(3, 9, 00));
    stopTimesA.add(entry(4, 9, 30));
    stopTimesA.add(entry(5, 10, 00));

    List<StopTimeEntry> stopTimesB = new ArrayList<StopTimeEntry>();
    stopTimesB.add(entry(6, 2, 00));
    stopTimesB.add(entry(7, 12, 00));
    stopTimesB.add(entry(8, 25, 00));

    AgencyAndId serviceIdA = new AgencyAndId("1", "serviceIdA");
    AgencyAndId serviceIdB = new AgencyAndId("1", "serviceIdB");

    StopTimeIndexImpl index = new StopTimeIndexImpl();

    for (StopTimeEntry stopTime : stopTimesA)
      index.addStopTime(stopTime, serviceIdA);

    for (StopTimeEntry stopTime : stopTimesB)
      index.addStopTime(stopTime, serviceIdB);

    Map<AgencyAndId, List<Date>> serviceIdsAndDates = new HashMap<AgencyAndId, List<Date>>();
    serviceIdsAndDates.put(serviceIdA,
        Arrays.asList(yesterday, today, tomorrow));
    serviceIdsAndDates.put(serviceIdB,
        Arrays.asList(yesterday, today, tomorrow));

    long timeFrom = time(today, 0, 30);
    long timeTo = time(today, 2, 30);

    List<StopTimeInstanceProxy> instances = getStopTimeInstancesInRange(index,
        timeFrom, timeTo, StopTimeOp.ARRIVAL, serviceIdsAndDates);

    Collections.sort(instances, _comparator);

    assertEquals(2, instances.size());

    StopTimeInstanceProxy proxy = instances.get(0);
    assertEquals(yesterday.getTime(), proxy.getServiceDate());
    assertEquals(time(yesterday, 25, 00), proxy.getArrivalTime());

    proxy = instances.get(1);
    assertEquals(today.getTime(), proxy.getServiceDate());
    assertEquals(time(today, 2, 00), proxy.getArrivalTime());

    timeFrom = time(today, 9, 30);
    timeTo = time(today, 12, 30);

    instances = getStopTimeInstancesInRange(index, timeFrom, timeTo,
        StopTimeOp.ARRIVAL, serviceIdsAndDates);

    Collections.sort(instances, _comparator);

    assertEquals(3, instances.size());

    proxy = instances.get(0);
    assertEquals(today.getTime(), proxy.getServiceDate());
    assertEquals(time(today, 9, 30), proxy.getArrivalTime());

    proxy = instances.get(1);
    assertEquals(today.getTime(), proxy.getServiceDate());
    assertEquals(time(today, 10, 00), proxy.getArrivalTime());

    proxy = instances.get(2);
    assertEquals(today.getTime(), proxy.getServiceDate());
    assertEquals(time(today, 12, 00), proxy.getArrivalTime());
  }

  private Date shiftDate(Date today, int shift) {
    Calendar c = Calendar.getInstance();
    c.setTime(today);
    c.add(Calendar.DAY_OF_YEAR, shift);
    Date yesterday = c.getTime();
    return yesterday;
  }

  @Test
  public void testGetStopTimeEntriesInRange() {
    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    stopTimes.add(entry(1, 10, 00));
    stopTimes.add(entry(2, 10, 30));
    stopTimes.add(entry(3, 11, 00));
    stopTimes.add(entry(4, 11, 30));
    stopTimes.add(entry(5, 12, 00));

    List<StopTimeEntry> result = getStopTimeEntriesInRange(stopTimes,
        StopTimeOp.ARRIVAL, time(9, 45), time(10, 15));
    assertEquals(1, result.size());

    StopTimeEntry entry = result.get(0);
    assertEquals(time(10, 00), entry.getArrivalTime());
    
    result = getStopTimeEntriesInRange(stopTimes,
        StopTimeOp.ARRIVAL, time(10, 00), time(10, 30));
    assertEquals(1, result.size());

    entry = result.get(0);
    assertEquals(time(10, 00), entry.getArrivalTime());
    
    result = getStopTimeEntriesInRange(stopTimes,
        StopTimeOp.ARRIVAL, time(10, 00), time(10, 45));
    assertEquals(2, result.size());

    entry = result.get(0);
    assertEquals(time(10, 00), entry.getArrivalTime());
    
    entry = result.get(1);
    assertEquals(time(10, 30), entry.getArrivalTime());
  }

  @Test
  public void testSearchForStopTime() {

    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    stopTimes.add(entry(1, 10, 00));
    stopTimes.add(entry(2, 10, 30));
    stopTimes.add(entry(3, 11, 00));
    stopTimes.add(entry(4, 11, 30));
    stopTimes.add(entry(5, 12, 00));

    assertEquals(0, searchForStopTime(stopTimes, time(9,45), StopTimeOp.ARRIVAL));
    assertEquals(0, searchForStopTime(stopTimes, time(10,00), StopTimeOp.ARRIVAL));
    assertEquals(1, searchForStopTime(stopTimes, time(10,15), StopTimeOp.ARRIVAL));
    assertEquals(1, searchForStopTime(stopTimes, time(10,30), StopTimeOp.ARRIVAL));
    assertEquals(2, searchForStopTime(stopTimes, time(10,45), StopTimeOp.ARRIVAL));
    assertEquals(2, searchForStopTime(stopTimes, time(11,00), StopTimeOp.ARRIVAL));
    assertEquals(3, searchForStopTime(stopTimes, time(11,15), StopTimeOp.ARRIVAL));
    assertEquals(3, searchForStopTime(stopTimes, time(11,30), StopTimeOp.ARRIVAL));
    assertEquals(4, searchForStopTime(stopTimes, time(11,45), StopTimeOp.ARRIVAL));
    assertEquals(4, searchForStopTime(stopTimes, time(12,00), StopTimeOp.ARRIVAL));
    assertEquals(5, searchForStopTime(stopTimes, time(12,15), StopTimeOp.ARRIVAL));
  }

  private static int time(int hour, int min) {
    return (hour * 60 + min) * 60;
  }

  private static long time(Date serviceDate, int hour, int min) {
    return serviceDate.getTime() + time(hour, min) * 1000;
  }

  private StopTimeEntry entry(int id, int hour, int min) {
    int t = time(hour, min);
    StopTimeEntryImpl stopTime = new StopTimeEntryImpl();
    stopTime.setId(id);
    stopTime.setArrivalTime(t);
    stopTime.setDepartureTime(t);
    return stopTime;
  }

  private static class StopTimeInstanceProxyComparator implements
      Comparator<StopTimeInstanceProxy> {

    @Override
    public int compare(StopTimeInstanceProxy o1, StopTimeInstanceProxy o2) {

      int rc = new Long(o1.getServiceDate()).compareTo(new Long(
          o2.getServiceDate()));

      if (rc == 0)
        rc = new Long(o1.getArrivalTime()).compareTo(new Long(
            o2.getArrivalTime()));

      return rc;
    }
  }
}
