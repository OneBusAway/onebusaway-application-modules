package org.onebusaway.transit_data_federation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.addServiceDates;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.lsids;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.serviceIds;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.time;
import static org.onebusaway.transit_data_federation.testing.UnitTestingSupport.timeZone;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.ServiceIdActivation;
import org.onebusaway.transit_data_federation.testing.UnitTestingSupport;

public class ExtendedCalendarServiceImplTest {

  private ExtendedCalendarServiceImpl _service;

  private CalendarServiceImpl _calendarService;

  private ServiceInterval interval;

  @Before
  public void before() {

    _calendarService = new CalendarServiceImpl();

    CalendarServiceData data = new CalendarServiceData();
    _calendarService.setData(data);

    addServiceDates(data, "sA", new ServiceDate(2010, 9, 10), new ServiceDate(
        2010, 9, 11));
    addServiceDates(data, "sB", new ServiceDate(2010, 9, 11), new ServiceDate(
        2010, 9, 12));
    addServiceDates(data, "sC", new ServiceDate(2010, 9, 12), new ServiceDate(
        2010, 9, 13));
    addServiceDates(data, "sD", new ServiceDate(2010, 9, 13));

    interval = new ServiceInterval(time(9, 00), time(9, 05), time(10, 00),
        time(10, 05));

    _service = new ExtendedCalendarServiceImpl();
    _service.setCalendarService(_calendarService);
  }

  @Test
  public void testGetServiceDatesWithinRange01() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA"), lsids("sB"));

    Date from = UnitTestingSupport.date("2010-09-10 09:30");
    Date to = UnitTestingSupport.date("2010-09-10 10:30");

    Collection<Date> dates = _service.getServiceDatesWithinRange(serviceIds,
        interval, from, to);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 10).getAsDate(timeZone())));

    from = UnitTestingSupport.date("2010-09-11 09:30");
    to = UnitTestingSupport.date("2010-09-11 10:30");

    dates = _service.getServiceDatesWithinRange(serviceIds, interval, from, to);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetServiceDatesForServiceIds01() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA"), lsids("sB"));

    Set<Date> dates = _service.getDatesForServiceIds(serviceIds);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 10).getAsDate(timeZone())));
  }

  @Test
  public void testGetServiceDatesWithinRange02() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA", "sB"), lsids());
   
    Date from = UnitTestingSupport.date("2010-09-11 09:30");
    Date to = UnitTestingSupport.date("2010-09-11 10:30");

    Collection<Date> dates = _service.getServiceDatesWithinRange(serviceIds,
        interval, from, to);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 11).getAsDate(timeZone())));

    from = UnitTestingSupport.date("2010-09-10 09:30");
    to = UnitTestingSupport.date("2010-09-10 10:30");

    dates = _service.getServiceDatesWithinRange(serviceIds, interval, from, to);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetServiceDatesForServiceIds02() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA", "sB"), lsids());

    Set<Date> dates = _service.getDatesForServiceIds(serviceIds);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 11).getAsDate(timeZone())));
  }

  @Test
  public void testGetServiceDatesWithinRange03() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sC", "sD"), lsids());

    Date from = UnitTestingSupport.date("2010-09-13 09:30");
    Date to = UnitTestingSupport.date("2010-09-13 10:30");

    Collection<Date> dates = _service.getServiceDatesWithinRange(serviceIds,
        interval, from, to);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 13).getAsDate(timeZone())));

    from = UnitTestingSupport.date("2010-09-12 09:30");
    to = UnitTestingSupport.date("2010-09-12 10:30");

    dates = _service.getServiceDatesWithinRange(serviceIds, interval, from, to);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetServiceDatesForServiceIds03() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sC", "sD"), lsids());

    Set<Date> dates = _service.getDatesForServiceIds(serviceIds);

    assertEquals(1, dates.size());
    assertTrue(dates.contains(new ServiceDate(2010, 9, 13).getAsDate(timeZone())));
  }

  @Test
  public void testGetServiceDatesWithinRange04() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA", "sC"), lsids());

    Date from = UnitTestingSupport.date("2010-09-10 09:30");
    Date to = UnitTestingSupport.date("2010-09-10 10:30");

    Collection<Date> dates = _service.getServiceDatesWithinRange(serviceIds,
        interval, from, to);

    assertEquals(0, dates.size());

    from = UnitTestingSupport.date("2010-09-12 09:30");
    to = UnitTestingSupport.date("2010-09-12 10:30");

    dates = _service.getServiceDatesWithinRange(serviceIds, interval, from, to);

    assertEquals(0, dates.size());
  }

  @Test
  public void testGetServiceDatesForServiceIds04() {

    ServiceIdActivation serviceIds = serviceIds(lsids("sA", "sC"), lsids());

    Set<Date> dates = _service.getDatesForServiceIds(serviceIds);

    assertEquals(0, dates.size());
  }
}
