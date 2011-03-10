package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeIndexImpl;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import edu.washington.cs.rse.text.DateLibrary;

public class StopTimeServiceImplTest {

  private DateFormat _format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private static final TimeZone _timeZone = TimeZone.getTimeZone("America/Los_Angeles");

  private StopTimeServiceImpl _service;

  private CalendarService _calendarService;

  private AgencyAndId _stopId;

  private StopTimeIndexImpl _index;

  @Before
  public void setup() {

    _stopId = new AgencyAndId("agency", "stopId");

    StopEntry stopEntry = Mockito.mock(StopEntry.class);

    _index = new StopTimeIndexImpl();
    Mockito.when(stopEntry.getStopTimes()).thenReturn(_index);

    TransitGraphDao graph = Mockito.mock(TransitGraphDao.class);
    Mockito.when(graph.getStopEntryForId(_stopId)).thenReturn(stopEntry);

    _calendarService = Mockito.mock(CalendarService.class);

    _service = new StopTimeServiceImpl();
    _service.setTransitGraphDao(graph);
    _service.setCalendarService(_calendarService);
  }

  @Test
  public void test01() {

    Date from = time("2009-09-01 10:00");
    Date to = time("2009-09-01 10:30");
    Date day = DateLibrary.getTimeAsDay(from);

    LocalizedServiceId serviceId = new LocalizedServiceId(new AgencyAndId(
        "agency", "serviceId"), _timeZone);

    List<Date> serviceDates = Arrays.asList(day);

    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    stopTimes.add(stopTime(0, 9, 50));
    stopTimes.add(stopTime(1, 10, 10));
    stopTimes.add(stopTime(2, 10, 20));
    stopTimes.add(stopTime(3, 10, 40));

    setupStopTimes(serviceId, stopTimes, serviceDates);

    List<StopTimeInstanceProxy> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    StopTimeInstanceProxy sti = results.get(0);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:10").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:10").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:20").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:20").getTime(), sti.getDepartureTime());

    from = time("2009-09-01 10:15");
    to = time("2009-09-01 10:25");

    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:20").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:20").getTime(), sti.getDepartureTime());

    from = time("2009-09-01 10:21");
    to = time("2009-09-01 10:25");

    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    assertEquals(0, results.size());
  }

  @Test
  public void test02() {

    Date day = DateLibrary.getTimeAsDay(time("2009-09-01 00:00"));

    LocalizedServiceId serviceId = new LocalizedServiceId(new AgencyAndId(
        "agency", "serviceId"), _timeZone);

    List<Date> serviceDates = Arrays.asList(day);

    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    stopTimes.add(stopTime(0, 9, 50, 9, 55));
    stopTimes.add(stopTime(1, 10, 00, 10, 30));
    stopTimes.add(stopTime(2, 10, 20, 10, 25));
    stopTimes.add(stopTime(3, 10, 40, 10, 50));

    setupStopTimes(serviceId, stopTimes, serviceDates);

    Date from = time("2009-09-01 10:09");
    Date to = time("2009-09-01 10:21");
    List<StopTimeInstanceProxy> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    StopTimeInstanceProxy sti = results.get(0);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:20").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:25").getTime(), sti.getDepartureTime());

    from = time("2009-09-01 9:52");
    to = time("2009-09-01 10:05");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    sti = results.get(0);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 09:50").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 09:55").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:30").getTime(), sti.getDepartureTime());

    from = time("2009-09-01 10:27");
    to = time("2009-09-01 10:41");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    sti = results.get(0);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(day.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:40").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:50").getTime(), sti.getDepartureTime());
  }

  @Test
  public void test03() {

    LocalizedServiceId serviceId = new LocalizedServiceId(new AgencyAndId(
        "agency", "serviceId"), _timeZone);

    Date dayA = DateLibrary.getTimeAsDay(time("2009-09-01 00:00"));
    Date dayB = DateLibrary.getTimeAsDay(time("2009-09-02 00:00"));

    List<Date> serviceDates = Arrays.asList(dayA, dayB);

    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    stopTimes.add(stopTime(1, 10, 00, 10, 30)); // 10:00am, 10:30am
    stopTimes.add(stopTime(2, 25, 00, 25, 30)); // 01:00am, 01:30am (both on
    // next day)

    setupStopTimes(serviceId, stopTimes, serviceDates);

    /****
     * 
     ****/

    Date from = time("2009-09-01 10:10");
    Date to = time("2009-09-01 10:40");
    List<StopTimeInstanceProxy> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    StopTimeInstanceProxy sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = time("2009-09-02 10:10");
    to = time("2009-09-02 10:40");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = time("2009-09-01 10:10");
    to = time("2009-09-02 10:40");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(3, results.size());

    sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 01:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 01:30").getTime(), sti.getDepartureTime());

    sti = results.get(2);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = time("2009-09-01 12:00");
    to = time("2009-09-02 12:00");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 01:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 01:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = time("2009-09-02 12:00");
    to = time("2009-09-03 12:00");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-03 01:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-03 01:30").getTime(), sti.getDepartureTime());
  }

  @Test
  public void test04() {

    LocalizedServiceId serviceIdA = new LocalizedServiceId(new AgencyAndId(
        "agency", "serviceIdA"), _timeZone);
    LocalizedServiceId serviceIdB = new LocalizedServiceId(new AgencyAndId(
        "agency", "serviceIdB"), _timeZone);

    Date dayA = DateLibrary.getTimeAsDay(time("2009-09-01 00:00"));
    Date dayB = DateLibrary.getTimeAsDay(time("2009-09-02 00:00"));

    Map<LocalizedServiceId, List<Date>> serviceDates = new HashMap<LocalizedServiceId, List<Date>>();
    serviceDates.put(serviceIdA, Arrays.asList(dayA));
    serviceDates.put(serviceIdB, Arrays.asList(dayB));

    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    stopTimes.add(stopTime(1, 10, 00, 10, 30));
    stopTimes.add(stopTime(2, 25, 00, 25, 30));

    Map<LocalizedServiceId, List<StopTimeEntry>> stopTimesByServiceId = new HashMap<LocalizedServiceId, List<StopTimeEntry>>();
    stopTimesByServiceId.put(serviceIdA, stopTimes);
    stopTimesByServiceId.put(serviceIdB, stopTimes);

    setupStopTimes(stopTimesByServiceId, serviceDates);

    /****
     * 
     ****/

    Date from = time("2009-09-01 10:10");
    Date to = time("2009-09-01 10:40");
    List<StopTimeInstanceProxy> results = _service.getStopTimeInstancesInTimeRange(
        _stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    StopTimeInstanceProxy sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = time("2009-09-02 10:10");
    to = time("2009-09-02 10:40");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = time("2009-09-01 10:10");
    to = time("2009-09-02 10:40");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(3, results.size());

    sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-01 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-01 10:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 01:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 01:30").getTime(), sti.getDepartureTime());

    sti = results.get(2);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = time("2009-09-01 12:00");
    to = time("2009-09-02 12:00");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(2, results.size());

    sti = results.get(0);
    assertEquals(dayA.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 01:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 01:30").getTime(), sti.getDepartureTime());

    sti = results.get(1);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-02 10:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-02 10:30").getTime(), sti.getDepartureTime());

    /****
     * 
     ****/

    from = time("2009-09-02 12:00");
    to = time("2009-09-03 12:00");
    results = _service.getStopTimeInstancesInTimeRange(_stopId, from, to);

    sort(results);

    assertEquals(1, results.size());

    sti = results.get(0);
    assertEquals(dayB.getTime(), sti.getServiceDate());
    assertEquals(time("2009-09-03 01:00").getTime(), sti.getArrivalTime());
    assertEquals(time("2009-09-03 01:30").getTime(), sti.getDepartureTime());
  }

  /****
   * Private Methods
   ****/

  private void setupStopTimes(LocalizedServiceId serviceId,
      List<StopTimeEntry> stopTimes, List<Date> serviceDates) {
    Map<LocalizedServiceId, List<StopTimeEntry>> stopTimesByServiceId = new HashMap<LocalizedServiceId, List<StopTimeEntry>>();
    Map<LocalizedServiceId, List<Date>> serviceDatesByServiceId = new HashMap<LocalizedServiceId, List<Date>>();
    stopTimesByServiceId.put(serviceId, stopTimes);
    serviceDatesByServiceId.put(serviceId, serviceDates);
    setupStopTimes(stopTimesByServiceId, serviceDatesByServiceId);
  }

  private void setupStopTimes(
      Map<LocalizedServiceId, List<StopTimeEntry>> stopTimesByServiceId,
      Map<LocalizedServiceId, List<Date>> serviceDatesByServiceId) {

    CalendarServiceHelper answer = new CalendarServiceHelper();

    for (Map.Entry<LocalizedServiceId, List<StopTimeEntry>> entry : stopTimesByServiceId.entrySet()) {

      LocalizedServiceId serviceId = entry.getKey();

      List<StopTimeEntry> stopTimes = entry.getValue();
      List<Date> serviceDates = serviceDatesByServiceId.get(serviceId);
      
      for( StopTimeEntry stopTime : stopTimes)
        _index.addStopTime(stopTime, serviceId);

      answer.addServiceDates(serviceId, serviceDates);
    }
    
    _index.sort();

    Mockito.when(
        _calendarService.getServiceDatesWithinRange(
            Mockito.any(ServiceIdIntervals.class), Mockito.any(Date.class),
            Mockito.any(Date.class))).thenAnswer(answer);
  }

  private StopTimeEntry stopTime(int id, int hours, int minutes) {
    return stopTime(id, hours, minutes, hours, minutes);
  }

  private StopTimeEntry stopTime(int id, int arrivalHours, int arrivalMinutes,
      int departureHours, int departureMinutes) {

    int arrival = (int) ((arrivalHours * 60) + arrivalMinutes) * 60;
    int departure = (int) ((departureHours * 60) + departureMinutes) * 60;
    StopTimeEntryImpl proxy = new StopTimeEntryImpl();
    proxy.setId(id);
    proxy.setArrivalTime(arrival);
    proxy.setDepartureTime(departure);
    return proxy;
  }

  private Date time(String time) {
    try {
      return _format.parse(time);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private void sort(List<StopTimeInstanceProxy> stopTimes) {
    Collections.sort(stopTimes, new StopTimeInstanceComparator());
  }

  private static class CalendarServiceHelper implements
      Answer<Map<LocalizedServiceId, List<Date>>> {

    private Map<LocalizedServiceId, List<Date>> _serviceDates = new HashMap<LocalizedServiceId, List<Date>>();

    public void addServiceDates(LocalizedServiceId serviceId,
        List<Date> serviceDates) {
      _serviceDates.put(serviceId, serviceDates);
    }

    public Map<LocalizedServiceId, List<Date>> answer(
        InvocationOnMock invocation) throws Throwable {

      Object[] arguments = invocation.getArguments();
      ServiceIdIntervals intervals = (ServiceIdIntervals) arguments[0];
      Date from = (Date) arguments[1];
      Date to = (Date) arguments[2];

      Map<LocalizedServiceId, List<Date>> result = new HashMap<LocalizedServiceId, List<Date>>();

      for (Map.Entry<LocalizedServiceId, ServiceInterval> entry : intervals) {
        LocalizedServiceId serviceId = entry.getKey();
        ServiceInterval interval = entry.getValue();
        List<Date> serviceDates = _serviceDates.get(serviceId);
        if (serviceDates == null)
          continue;
        List<Date> hits = new ArrayList<Date>();
        for (Date serviceDate : serviceDates) {
          Date serviceFrom = new Date(serviceDate.getTime()
              + interval.getMinArrival() * 1000);
          Date serviceTo = new Date(serviceDate.getTime()
              + interval.getMaxArrival() * 1000);
          if (!(from.after(serviceTo) || to.before(serviceFrom)))
            hits.add(serviceDate);
        }
        if (!hits.isEmpty())
          result.put(serviceId, hits);
      }

      return result;
    }
  }

  private static class StopTimeInstanceComparator implements
      Comparator<StopTimeInstanceProxy> {
    public int compare(StopTimeInstanceProxy o1, StopTimeInstanceProxy o2) {
      return (int) (o1.getArrivalTime() - o2.getArrivalTime());
    }
  }
}
