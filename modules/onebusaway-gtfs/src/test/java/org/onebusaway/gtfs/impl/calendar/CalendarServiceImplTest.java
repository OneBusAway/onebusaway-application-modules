package org.onebusaway.gtfs.impl.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarServiceData;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarServiceImplTest {
  
  private static final DateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private static final DateFormat _timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private static CalendarServiceData _data;

  private static CalendarServiceImpl _service;

  @BeforeClass
  public static void setup() throws IOException {

    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getIslandGtfs(), "26");

    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(dao);

    _data = factory.createServiceCalendarData();

    _service = new CalendarServiceImpl();
    _service.setServiceCalendarData(_data);
  }

  @Test
  public void testGetDatesForServiceId() {

    Date from = date("2008-10-27");
    Date to = date("2009-09-27");

    Set<Date> toExclude = new HashSet<Date>();
    toExclude.add(date("2009-01-01"));

    // 23,1,1,1,1,1,0,0,20081027,20090927
    Set<Date> dates = _service.getServiceDatesForServiceId(new AgencyAndId(
        "26", "23"));
    assertEquals(239, dates.size());

    Calendar c = Calendar.getInstance();
    c.setTime(from);

    while (c.getTime().compareTo(to) <= 0) {
      Date day = c.getTime();
      int dow = c.get(Calendar.DAY_OF_WEEK);

      if (!(dow == Calendar.SATURDAY || dow == Calendar.SUNDAY || toExclude.contains(day))) {
        assertTrue(dates.contains(day));
      }

      c.add(Calendar.DAY_OF_YEAR, 1);
    }
  }

  @Test
  public void testGetServiceDatesWithinRange01() {

    Date from = dateAndTime("2009-02-04 11:00");
    Date to = dateAndTime("2009-02-04 12:00");

    Map<AgencyAndId, List<Date>> serviceIdsAndDates = _service.getServiceDatesWithinRange(
        from, to);
    assertEquals(2, serviceIdsAndDates.size());

    List<Date> target = Arrays.asList(date("2009-02-04"));
    List<Date> dates = serviceIdsAndDates.get(new AgencyAndId("26", "23"));
    assertEquals(target, dates);
  }

  @Test
  public void testGetServiceDatesWithinRange02() {

    Date from = dateAndTime("2009-02-04 03:00");
    Date to = dateAndTime("2009-02-04 4:00");

    Map<AgencyAndId, List<Date>> serviceIdsAndDates = _service.getServiceDatesWithinRange(
        from, to);
    assertEquals(1, serviceIdsAndDates.size());

    List<Date> target = Arrays.asList(date("2009-02-04"));
    List<Date> dates = serviceIdsAndDates.get(new AgencyAndId("26", "23"));
    assertEquals(target, dates);
  }

  @Test
  public void testGetServiceDatesWithinRange03() {

    Date from = dateAndTime("2009-02-04 03:00");
    Date to = dateAndTime("2009-02-04 5:00");

    Map<AgencyAndId, List<Date>> serviceIdsAndDates = _service.getServiceDatesWithinRange(
        from, to);
    assertEquals(2, serviceIdsAndDates.size());

    List<Date> target = Arrays.asList(date("2009-02-04"));

    List<Date> dates = serviceIdsAndDates.get(new AgencyAndId("26", "23"));
    assertEquals(target, dates);

    dates = serviceIdsAndDates.get(new AgencyAndId("26", "25"));
    assertEquals(target, dates);
  }

  @Test
  public void testGetServiceIdsOnDate01() {

    Date date = dateAndTime("2009-02-04 11:00");
    Set<AgencyAndId> ids = _service.getServiceIdsOnDate(date);

    assertEquals(2, ids.size());
    assertTrue(ids.contains(new AgencyAndId("26", "23")));
    assertTrue(ids.contains(new AgencyAndId("26", "25")));
  }

  @Test
  public void testGetServiceIdsOnDate02() {

    Date date = dateAndTime("2009-02-07 11:00");
    Set<AgencyAndId> ids = _service.getServiceIdsOnDate(date);

    assertEquals(2, ids.size());

    assertTrue(ids.contains(new AgencyAndId("26", "24")));
    assertTrue(ids.contains(new AgencyAndId("26", "26")));
  }
  
  /****
   * Private Methods
   ****/
  

  private static final Date date(String spec) {
    try {
      return _dateFormat.parse(spec);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static final Date dateAndTime(String spec) {
    try {
      return _timeFormat.parse(spec);
    } catch (ParseException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
