package org.onebusaway.gtfs.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.gtfs.impl.CalendarSupport.day;
import static org.onebusaway.gtfs.impl.CalendarSupport.hourToSec;
import static org.onebusaway.gtfs.impl.CalendarSupport.time;

import org.onebusaway.gtfs.impl.CalendarServiceImpl;
import org.onebusaway.gtfs.impl.ServiceCalendarData;
import org.onebusaway.gtfs.impl.ServiceIdCalendarData;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarServiceImplTest {

  private Date _d2 = day("1/2/2009");

  private Date _d3 = day("1/3/2009");

  private Date _d4 = day("1/4/2009");

  private Date _d5 = day("1/5/2009");

  private ServiceCalendarData _data;

  @Before
  public void prepareCalendarData() throws ParseException {

    List<Date> serviceDates = new ArrayList<Date>();

    serviceDates.add(_d2);
    serviceDates.add(_d3);
    serviceDates.add(_d4);
    serviceDates.add(_d5);

    ServiceIdCalendarData serviceIdData = new ServiceIdCalendarData(serviceDates, hourToSec(5), hourToSec(25),
        hourToSec(6), hourToSec(26));

    _data = new ServiceCalendarData();
    _data.putDataForServiceId("a", serviceIdData);
  }

  @Test
  public void testGetServiceDateDeparturesWithinRange() throws ParseException {

    CalendarServiceImpl calendar = new CalendarServiceImpl();
    calendar.setServiceCalendarData(_data);

    Set<String> serviceIds = new HashSet<String>();
    serviceIds.add("a");

    Map<String, List<Date>> results = calendar.getServiceDateDeparturesWithinRange(serviceIds,
        time("1/2/2009 4:30 am"), time("1/2/2009 5:30 am"));
    List<Date> serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/2/2009 4:30 am"),
        time("1/2/2009 6:00 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/2/2009 4:30 am"),
        time("1/2/2009 6:01 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/2/2009 4:30 am"),
        time("1/2/2009 10:00 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/2/2009 10:00 am"),
        time("1/2/2009 11:00 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/2/2009 4:00 am"),
        time("1/3/2009 4:00 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/3/2009 1:30 am"),
        time("1/3/2009 3:00 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/3/2009 2:00 am"),
        time("1/3/2009 3:00 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/2/2009 10:00 am"),
        time("1/3/2009 10:00 am"));
    serviceDates = results.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));
    assertTrue(serviceDates.contains(_d3));

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/3/2009 4:00 am"),
        time("1/3/2009 4:30 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/4/2009 5:00 pm"),
        time("1/5/2009 10:00 am"));
    serviceDates = results.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d4));
    assertTrue(serviceDates.contains(_d5));

    results = calendar.getServiceDateDeparturesWithinRange(serviceIds, time("1/7/2009 5:00 pm"),
        time("1/7/2009 6:00 pm"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());
  }

  @Test
  public void testGetServiceDateArrivalsWithinRange() throws ParseException {

    CalendarServiceImpl calendar = new CalendarServiceImpl();
    calendar.setServiceCalendarData(_data);

    Set<String> serviceIds = new HashSet<String>();
    serviceIds.add("a");

    Map<String, List<Date>> results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/2/2009 3:30 am"),
        time("1/2/2009 4:30 am"));
    List<Date> serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/2/2009 4:30 am"), time("1/2/2009 5:00 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/2/2009 4:30 am"), time("1/2/2009 5:01 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/2/2009 4:30 am"),
        time("1/2/2009 10:00 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/2/2009 10:00 am"),
        time("1/2/2009 11:00 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/2/2009 4:00 am"), time("1/3/2009 4:00 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/3/2009 1:00 am"), time("1/3/2009 3:00 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/3/2009 2:00 am"), time("1/3/2009 3:00 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/2/2009 10:00 am"),
        time("1/3/2009 10:00 am"));
    serviceDates = results.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));
    assertTrue(serviceDates.contains(_d3));

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/3/2009 4:00 am"), time("1/3/2009 4:30 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/4/2009 5:00 pm"),
        time("1/5/2009 10:00 am"));
    serviceDates = results.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d4));
    assertTrue(serviceDates.contains(_d5));

    results = calendar.getServiceDateArrivalsWithinRange(serviceIds, time("1/7/2009 7:00 am"), time("1/7/2009 8:00 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());
  }

  @Test
  public void testGetServiceDatesWithinRange() throws ParseException {

    CalendarServiceImpl calendar = new CalendarServiceImpl();
    calendar.setServiceCalendarData(_data);

    Set<String> serviceIds = new HashSet<String>();
    serviceIds.add("a");

    Map<String, List<Date>> results = calendar.getServiceDatesWithinRange(serviceIds,
        time("1/2/2009 3:30 am"), time("1/2/2009 4:30 am"));
    List<Date> serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/2/2009 4:30 am"),
        time("1/2/2009 5:00 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());

    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/2/2009 4:30 am"),
        time("1/2/2009 5:01 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));
    
    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/2/2009 5:30 am"),
        time("1/3/2009 1:30 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));
    
    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/2/2009 4:30 am"),
        time("1/3/2009 2:30 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));
    
    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/3/2009 1:30 am"),
        time("1/3/2009 2:30 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));
    
    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/3/2009 2:00 am"),
        time("1/3/2009 2:30 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());
    
    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/3/2009 4:00 am"),
        time("1/3/2009 5:30 am"));
    serviceDates = results.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d3));
    
    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/3/2009 1:30 am"),
        time("1/3/2009 5:30 am"));
    serviceDates = results.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));
    assertTrue(serviceDates.contains(_d3));
    
    results = calendar.getServiceDatesWithinRange(serviceIds, time("1/6/2009 2:30 am"),
        time("1/6/2009 5:30 am"));
    serviceDates = results.get("a");
    assertEquals(0, serviceDates.size());
  }

  @Test
  public void testGetDatesForServiceId() {

    List<Date> serviceDatesA = new ArrayList<Date>();
    serviceDatesA.add(_d2);
    serviceDatesA.add(_d3);

    List<Date> serviceDatesB = new ArrayList<Date>();
    serviceDatesB.add(_d2);
    serviceDatesB.add(_d4);

    ServiceCalendarData data = new ServiceCalendarData();
    data.putDataForServiceId("a", new ServiceIdCalendarData(serviceDatesA, hourToSec(5), hourToSec(25), hourToSec(6),
        hourToSec(26)));
    data.putDataForServiceId("b", new ServiceIdCalendarData(serviceDatesB, hourToSec(5), hourToSec(25), hourToSec(6),
        hourToSec(26)));

    CalendarServiceImpl calendar = new CalendarServiceImpl();
    calendar.setServiceCalendarData(data);

    Set<Date> dA = calendar.getDatesForServiceId("a");
    assertEquals(2, dA.size());
    assertTrue(dA.contains(_d2));
    assertTrue(dA.contains(_d3));

    Set<Date> dB = calendar.getDatesForServiceId("b");
    assertEquals(2, dB.size());
    assertTrue(dB.contains(_d2));
    assertTrue(dB.contains(_d4));

    Set<Date> dC = calendar.getDatesForServiceId("c");
    assertEquals(0, dC.size());
  }

  @Test
  public void testGetServiceIdsOnDate() {

    List<Date> serviceDatesA = new ArrayList<Date>();
    serviceDatesA.add(_d2);
    serviceDatesA.add(_d3);

    List<Date> serviceDatesB = new ArrayList<Date>();
    serviceDatesB.add(_d2);
    serviceDatesB.add(_d4);

    ServiceCalendarData data = new ServiceCalendarData();
    data.putDataForServiceId("a", new ServiceIdCalendarData(serviceDatesA, hourToSec(5), hourToSec(25), hourToSec(6),
        hourToSec(26)));
    data.putDataForServiceId("b", new ServiceIdCalendarData(serviceDatesB, hourToSec(5), hourToSec(25), hourToSec(6),
        hourToSec(26)));

    CalendarServiceImpl calendar = new CalendarServiceImpl();
    calendar.setServiceCalendarData(data);

    Set<String> d2 = calendar.getServiceIdsOnDate(_d2);
    assertEquals(2, d2.size());
    assertTrue(d2.contains("a"));
    assertTrue(d2.contains("b"));

    Set<String> d3 = calendar.getServiceIdsOnDate(_d3);
    assertEquals(1, d3.size());
    assertTrue(d3.contains("a"));

    Set<String> d4 = calendar.getServiceIdsOnDate(_d4);
    assertEquals(1, d4.size());
    assertTrue(d4.contains("b"));

    Set<String> d5 = calendar.getServiceIdsOnDate(_d5);
    assertEquals(0, d5.size());
  }

  @Test
  public void testGetNextDepartureServiceDates() {

    CalendarServiceImpl calendar = new CalendarServiceImpl();
    calendar.setServiceCalendarData(_data);

    Set<String> serviceIds = new HashSet<String>();
    serviceIds.add("a");

    Map<String, List<Date>> result = calendar.getNextDepartureServiceDates(serviceIds,
        time("1/2/2009 5:30 am").getTime());
    assertEquals(result.size(), 1);
    List<Date> serviceDates = result.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    result = calendar.getNextDepartureServiceDates(serviceIds, time("1/2/2009 6:00 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));

    result = calendar.getNextDepartureServiceDates(serviceIds, time("1/2/2009 6:30 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d2));
    assertTrue(serviceDates.contains(_d3));

    result = calendar.getNextDepartureServiceDates(serviceIds, time("1/3/2009 2:00 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d3));

    result = calendar.getNextDepartureServiceDates(serviceIds, time("1/3/2009 5:00 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d3));

    result = calendar.getNextDepartureServiceDates(serviceIds, time("1/3/2009 7:00 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d3));
    assertTrue(serviceDates.contains(_d4));

    result = calendar.getNextDepartureServiceDates(serviceIds, time("1/6/2009 7:00 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(0, serviceDates.size());
  }

  @Test
  public void testGetPreviousArrivalsServiceDates() {

    CalendarServiceImpl calendar = new CalendarServiceImpl();
    calendar.setServiceCalendarData(_data);

    Set<String> serviceIds = new HashSet<String>();
    serviceIds.add("a");

    Map<String, List<Date>> result = calendar.getPreviousArrivalServiceDates(serviceIds,
        time("1/6/2009 2:00 am").getTime());
    assertEquals(result.size(), 1);
    List<Date> serviceDates = result.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d5));

    result = calendar.getPreviousArrivalServiceDates(serviceIds, time("1/6/2009 1:00 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d5));

    result = calendar.getPreviousArrivalServiceDates(serviceIds, time("1/6/2009 12:30 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d5));
    assertTrue(serviceDates.contains(_d4));

    result = calendar.getPreviousArrivalServiceDates(serviceIds, time("1/5/2009 5:30 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d5));
    assertTrue(serviceDates.contains(_d4));

    result = calendar.getPreviousArrivalServiceDates(serviceIds, time("1/5/2009 5:00 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d4));

    result = calendar.getPreviousArrivalServiceDates(serviceIds, time("1/5/2009 1:30 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(1, serviceDates.size());
    assertTrue(serviceDates.contains(_d4));

    result = calendar.getPreviousArrivalServiceDates(serviceIds, time("1/5/2009 12:30 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(2, serviceDates.size());
    assertTrue(serviceDates.contains(_d4));
    assertTrue(serviceDates.contains(_d3));

    result = calendar.getPreviousArrivalServiceDates(serviceIds, time("1/2/2009 4:30 am").getTime());
    assertEquals(result.size(), 1);
    serviceDates = result.get("a");
    assertEquals(0, serviceDates.size());
  }
}
