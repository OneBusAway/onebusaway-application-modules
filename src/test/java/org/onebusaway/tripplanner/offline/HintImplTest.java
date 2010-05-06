package org.onebusaway.tripplanner.offline;

import static junit.framework.Assert.assertEquals;

import org.onebusaway.tripplanner.services.StopTimeIndexContext;
import org.onebusaway.tripplanner.services.StopTimeIndexResult;
import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.where.model.ServiceDate;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.washington.cs.rse.collections.FactoryMap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HintImplTest {

  private Mockery _mockery = new Mockery();

  private static DateFormat _dayFormat = DateFormat.getDateInstance(DateFormat.SHORT);

  private static DateFormat _timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

  private static StopTimeInstanceProxyComparator _comparator = new StopTimeInstanceProxyComparator();

  private static StopProxyImpl _stop = new StopProxyImpl("stopA", null);

  private Map<String, List<StopTimeProxyImpl>> _timesByServiceIdAndArrival = new FactoryMap<String, List<StopTimeProxyImpl>>(
      new ArrayList<StopTimeProxyImpl>());

  private Map<String, List<StopTimeProxyImpl>> _timesByServiceIdAndDeparture = new FactoryMap<String, List<StopTimeProxyImpl>>(
      new ArrayList<StopTimeProxyImpl>());

  private StopTimeIndexContext _context;

  @Before
  public void setupStopTimes() {

    System.out.println("=== setup stop times ===");

    List<StopTimeProxyImpl> stopTimes = new ArrayList<StopTimeProxyImpl>();

    stopTimes.add(new StopTimeProxyImpl(hour(6), hour(7), "trip01", "serviceIdA", "routeA", _stop));
    stopTimes.add(new StopTimeProxyImpl(hour(8), hour(9), "trip02", "serviceIdA", "routeA", _stop));
    stopTimes.add(new StopTimeProxyImpl(hour(10), hour(11), "trip03", "serviceIdA", "routeA", _stop));

    // This trip has overlapping arrival and departure times with existing stop
    // times
    stopTimes.add(new StopTimeProxyImpl(hour(6), hour(9), "trip04", "serviceIdA", "routeB", _stop));

    // This trip wraps into the next day at 7 am, 8 am
    stopTimes.add(new StopTimeProxyImpl(hour(31), hour(32), "trip05", "serviceIdA", "routeB", _stop));

    // Different service id
    stopTimes.add(new StopTimeProxyImpl(hour(12), hour(13), "trip06", "serviceIdB", "routeC", _stop));

    for (StopTimeProxyImpl stopTime : stopTimes) {
      String serviceId = stopTime.getServiceId();
      _timesByServiceIdAndDeparture.get(serviceId).add(stopTime);
      _timesByServiceIdAndArrival.get(serviceId).add(stopTime);
    }

    for (List<StopTimeProxyImpl> sts : _timesByServiceIdAndDeparture.values())
      Collections.sort(sts, TimeOp.DEPARTURE);

    for (List<StopTimeProxyImpl> sts : _timesByServiceIdAndArrival.values())
      Collections.sort(sts, TimeOp.ARRIVAL);
  }

  @Before
  public void setupStopTimeIndexContext() throws ParseException {

    System.out.println("=== setup context ===");

    Date dateA = _dayFormat.parse("1/1/2009");
    Date dateB = _dayFormat.parse("1/2/2009");

    ServiceDate serviceDateA01 = new ServiceDate("serviceIdA", dateA, 0,0);
    ServiceDate serviceDateA02 = new ServiceDate("serviceIdA", dateB, 0,0);

    List<ServiceDate> serviceDatesA = new ArrayList<ServiceDate>();
    serviceDatesA.addFillRemaining(serviceDateA01);
    serviceDatesA.addFillRemaining(serviceDateA02);

    ServiceDate serviceDateB01 = new ServiceDate("serviceIdB", dateA, 0,0);
    ServiceDate serviceDateB02 = new ServiceDate("serviceIdB", dateB, 0,0);

    List<ServiceDate> serviceDatesB = new ArrayList<ServiceDate>();
    serviceDatesB.addFillRemaining(serviceDateB01);
    serviceDatesB.addFillRemaining(serviceDateB02);

    final Map<String, List<ServiceDate>> serviceDatesByServiceId = new HashMap<String, List<ServiceDate>>();
    serviceDatesByServiceId.put("serviceIdA", serviceDatesA);
    serviceDatesByServiceId.put("serviceIdB", serviceDatesB);

    _context = _mockery.mock(StopTimeIndexContext.class);
    _mockery.checking(new Expectations() {
      {
        allowing(_context);
        will(returnValue(serviceDatesByServiceId));
      }
    });
  }

  @Test
  public void testGetNextStopTime01() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.DEPARTURE);

    StopTimeIndexResult result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 6:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/2/2009 6:00 am"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 7:00 am"), sti.getDepartureTime());
    assertEquals(date("1/2/2009"), sti.getServiceDate());
    assertEquals("trip01", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 7:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    sti = stis.get(0);
    assertEquals(time("1/2/2009 6:00 am"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 7:00 am"), sti.getDepartureTime());
    assertEquals(date("1/2/2009"), sti.getServiceDate());
    assertEquals("trip01", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  @Test
  public void testGetNextStopTime02() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.DEPARTURE);

    StopTimeIndexResult result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 7:01 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/2/2009 7:00 am"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 8:00 am"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip05", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 8:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    sti = stis.get(0);
    assertEquals(time("1/2/2009 7:00 am"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 8:00 am"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip05", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  @Test
  public void testGetNextStopTime03() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.DEPARTURE);

    StopTimeIndexResult result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 8:01 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(2, stis.size());
    Collections.sort(stis, _comparator);

    StopTimeInstanceProxy stiA = stis.get(0);
    assertEquals(time("1/2/2009 8:00 am"), stiA.getArrivalTime());
    assertEquals(time("1/2/2009 9:00 am"), stiA.getDepartureTime());
    assertEquals(date("1/2/2009"), stiA.getServiceDate());
    assertEquals("trip02", stiA.getTripId());
    assertEquals(_stop, stiA.getStop());

    StopTimeInstanceProxy stiB = stis.get(1);
    assertEquals(time("1/2/2009 6:00 am"), stiB.getArrivalTime());
    assertEquals(time("1/2/2009 9:00 am"), stiB.getDepartureTime());
    assertEquals(date("1/2/2009"), stiB.getServiceDate());
    assertEquals("trip04", stiB.getTripId());
    assertEquals(_stop, stiB.getStop());

    result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 9:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(2, stis.size());
    Collections.sort(stis, _comparator);

    stiA = stis.get(0);
    assertEquals(time("1/2/2009 8:00 am"), stiA.getArrivalTime());
    assertEquals(time("1/2/2009 9:00 am"), stiA.getDepartureTime());
    assertEquals(date("1/2/2009"), stiA.getServiceDate());
    assertEquals("trip02", stiA.getTripId());
    assertEquals(_stop, stiA.getStop());

    stiB = stis.get(1);
    assertEquals(time("1/2/2009 6:00 am"), stiB.getArrivalTime());
    assertEquals(time("1/2/2009 9:00 am"), stiB.getDepartureTime());
    assertEquals(date("1/2/2009"), stiB.getServiceDate());
    assertEquals("trip04", stiB.getTripId());
    assertEquals(_stop, stiB.getStop());
  }

  @Test
  public void testGetNextStopTime04() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.DEPARTURE);

    StopTimeIndexResult result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 9:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/2/2009 10:00 am"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 11:00 am"), sti.getDepartureTime());
    assertEquals(date("1/2/2009"), sti.getServiceDate());
    assertEquals("trip03", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 11:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    sti = stis.get(0);
    assertEquals(time("1/2/2009 10:00 am"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 11:00 am"), sti.getDepartureTime());
    assertEquals(date("1/2/2009"), sti.getServiceDate());
    assertEquals("trip03", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  @Test
  public void testGetNextStopTime05() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.DEPARTURE);

    StopTimeIndexResult result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context,
        time("1/2/2009 11:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/2/2009 12:00 pm"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 1:00 pm"), sti.getDepartureTime());
    assertEquals(date("1/2/2009"), sti.getServiceDate());
    assertEquals("trip06", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 1:00 pm"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    sti = stis.get(0);
    assertEquals(time("1/2/2009 12:00 pm"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 1:00 pm"), sti.getDepartureTime());
    assertEquals(date("1/2/2009"), sti.getServiceDate());
    assertEquals("trip06", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  @Test
  public void testGetNextStopTime06() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.DEPARTURE);

    StopTimeIndexResult result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/2/2009 1:30 pm"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/3/2009 7:00 am"), sti.getArrivalTime());
    assertEquals(time("1/3/2009 8:00 am"), sti.getDepartureTime());
    assertEquals(date("1/2/2009"), sti.getServiceDate());
    assertEquals("trip05", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/3/2009 8:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    sti = stis.get(0);
    assertEquals(time("1/3/2009 7:00 am"), sti.getArrivalTime());
    assertEquals(time("1/3/2009 8:00 am"), sti.getDepartureTime());
    assertEquals(date("1/2/2009"), sti.getServiceDate());
    assertEquals("trip05", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  public void testGetNextStopTime07() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.DEPARTURE);

    StopTimeIndexResult result = hint.getNextStopTime(_timesByServiceIdAndDeparture, _context, time("1/3/2009 8:30 pm"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(0, stis.size());
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  @Test
  public void testGetPreviousStopTime01() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.ARRIVAL);

    StopTimeIndexResult result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context,
        time("1/1/2009 5:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(0, stis.size());
  }

  @Test
  public void testGetPreviousStopTime02() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.ARRIVAL);

    StopTimeIndexResult result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context,
        time("1/1/2009 6:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(2, stis.size());
    Collections.sort(stis, _comparator);

    StopTimeInstanceProxy stiA = stis.get(0);
    assertEquals(time("1/1/2009 6:00 am"), stiA.getArrivalTime());
    assertEquals(time("1/1/2009 7:00 am"), stiA.getDepartureTime());
    assertEquals(date("1/1/2009"), stiA.getServiceDate());
    assertEquals("trip01", stiA.getTripId());
    assertEquals(_stop, stiA.getStop());

    StopTimeInstanceProxy stiB = stis.get(1);
    assertEquals(time("1/1/2009 6:00 am"), stiB.getArrivalTime());
    assertEquals(time("1/1/2009 9:00 am"), stiB.getDepartureTime());
    assertEquals(date("1/1/2009"), stiB.getServiceDate());
    assertEquals("trip04", stiB.getTripId());
    assertEquals(_stop, stiB.getStop());

    result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context, time("1/1/2009 6:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(2, stis.size());
    Collections.sort(stis, _comparator);

    stiA = stis.get(0);
    assertEquals(time("1/1/2009 6:00 am"), stiA.getArrivalTime());
    assertEquals(time("1/1/2009 7:00 am"), stiA.getDepartureTime());
    assertEquals(date("1/1/2009"), stiA.getServiceDate());
    assertEquals("trip01", stiA.getTripId());
    assertEquals(_stop, stiA.getStop());

    stiB = stis.get(1);
    assertEquals(time("1/1/2009 6:00 am"), stiB.getArrivalTime());
    assertEquals(time("1/1/2009 9:00 am"), stiB.getDepartureTime());
    assertEquals(date("1/1/2009"), stiB.getServiceDate());
    assertEquals("trip04", stiB.getTripId());
    assertEquals(_stop, stiB.getStop());
  }

  @Test
  public void testGetPreviousStopTime03() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.ARRIVAL);

    StopTimeIndexResult result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context,
        time("1/1/2009 8:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());

    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/1/2009 8:00 am"), sti.getArrivalTime());
    assertEquals(time("1/1/2009 9:00 am"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip02", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context, time("1/1/2009 8:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());

    sti = stis.get(0);
    assertEquals(time("1/1/2009 8:00 am"), sti.getArrivalTime());
    assertEquals(time("1/1/2009 9:00 am"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip02", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  @Test
  public void testGetPreviousStopTime04() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.ARRIVAL);

    StopTimeIndexResult result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context,
        time("1/1/2009 10:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());

    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/1/2009 10:00 am"), sti.getArrivalTime());
    assertEquals(time("1/1/2009 11:00 am"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip03", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context, time("1/1/2009 10:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());

    sti = stis.get(0);
    assertEquals(time("1/1/2009 10:00 am"), sti.getArrivalTime());
    assertEquals(time("1/1/2009 11:00 am"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip03", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  @Test
  public void testGetPreviousStopTime05() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.ARRIVAL);

    StopTimeIndexResult result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context,
        time("1/1/2009 12:30 pm"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());

    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/1/2009 12:00 pm"), sti.getArrivalTime());
    assertEquals(time("1/1/2009 1:00 pm"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip06", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context, time("1/1/2009 12:00 pm"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());
    sti = stis.get(0);
    assertEquals(time("1/1/2009 12:00 pm"), sti.getArrivalTime());
    assertEquals(time("1/1/2009 1:00 pm"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip06", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  @Test
  public void testGetPreviousStopTime06() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.ARRIVAL);

    StopTimeIndexResult result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context,
        time("1/2/2009 6:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(2, stis.size());
    Collections.sort(stis, _comparator);

    StopTimeInstanceProxy stiA = stis.get(0);
    assertEquals(time("1/2/2009 6:00 am"), stiA.getArrivalTime());
    assertEquals(time("1/2/2009 7:00 am"), stiA.getDepartureTime());
    assertEquals(date("1/2/2009"), stiA.getServiceDate());
    assertEquals("trip01", stiA.getTripId());
    assertEquals(_stop, stiA.getStop());

    StopTimeInstanceProxy stiB = stis.get(1);
    assertEquals(time("1/2/2009 6:00 am"), stiB.getArrivalTime());
    assertEquals(time("1/2/2009 9:00 am"), stiB.getDepartureTime());
    assertEquals(date("1/2/2009"), stiB.getServiceDate());
    assertEquals("trip04", stiB.getTripId());
    assertEquals(_stop, stiB.getStop());

    result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context, time("1/2/2009 6:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(2, stis.size());
    Collections.sort(stis, _comparator);

    stiA = stis.get(0);
    assertEquals(time("1/2/2009 6:00 am"), stiA.getArrivalTime());
    assertEquals(time("1/2/2009 7:00 am"), stiA.getDepartureTime());
    assertEquals(date("1/2/2009"), stiA.getServiceDate());
    assertEquals("trip01", stiA.getTripId());
    assertEquals(_stop, stiA.getStop());

    stiB = stis.get(1);
    assertEquals(time("1/2/2009 6:00 am"), stiB.getArrivalTime());
    assertEquals(time("1/2/2009 9:00 am"), stiB.getDepartureTime());
    assertEquals(date("1/2/2009"), stiB.getServiceDate());
    assertEquals("trip04", stiB.getTripId());
    assertEquals(_stop, stiB.getStop());
  }

  @Test
  public void testGetPreviousStopTimeN() throws ParseException {

    HintImpl hint = new HintImpl(TimeOp.ARRIVAL);

    StopTimeIndexResult result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context,
        time("1/2/2009 7:30 am"));
    List<StopTimeInstanceProxy> stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());

    StopTimeInstanceProxy sti = stis.get(0);
    assertEquals(time("1/2/2009 7:00 am"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 8:00 am"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip05", sti.getTripId());
    assertEquals(_stop, sti.getStop());

    result = hint.getPreviousStopTime(_timesByServiceIdAndArrival, _context, time("1/2/2009 7:00 am"));
    stis = result.getStopTimeInstances();
    assertEquals(1, stis.size());

    sti = stis.get(0);
    assertEquals(time("1/2/2009 7:00 am"), sti.getArrivalTime());
    assertEquals(time("1/2/2009 8:00 am"), sti.getDepartureTime());
    assertEquals(date("1/1/2009"), sti.getServiceDate());
    assertEquals("trip05", sti.getTripId());
    assertEquals(_stop, sti.getStop());
  }

  private static final int hour(int hour) {
    return hour * 3600;
  }

  private static final long time(String spec) throws ParseException {
    return _timeFormat.parse(spec).getTime();
  }

  private static final long date(String spec) throws ParseException {
    return _dayFormat.parse(spec).getTime();
  }

  private static class StopTimeInstanceProxyComparator implements Comparator<StopTimeInstanceProxy> {

    public int compare(StopTimeInstanceProxy o1, StopTimeInstanceProxy o2) {
      return o1.getTripId().compareTo(o2.getTripId());
    }
  }
}
