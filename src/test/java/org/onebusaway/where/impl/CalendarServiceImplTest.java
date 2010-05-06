package org.onebusaway.where.impl;

import org.junit.Test;
import org.onebusaway.BaseTest;
import org.onebusaway.gtdf.impl.CalendarServiceImpl;
import org.onebusaway.where.model.ServiceDate;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;

public class CalendarServiceImplTest extends BaseTest {

  private static DateFormat _format = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT);

  private CalendarServiceImpl _calendarService;

  @Autowired
  public void setCalendarService(CalendarServiceImpl calendarService) {
    _calendarService = calendarService;
  }

  @Test
  public void testGetServiceIdsWithinRange() throws ParseException {

    Date from = _format.parse("10/13/08 12:30 PM");
    Date to = _format.parse("10/13/08 12:45 PM");

    Set<ServiceDate> serviceDates = _calendarService.getServiceDatesWithinRange(
        from, to);
    System.out.println(serviceDates);
  }
}
