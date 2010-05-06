package org.onebusaway.gtdf.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.where.model.ServiceDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

@ContextConfiguration(locations = {
    "/data-sources-common.xml",
    "/org/onebusaway/application-context-common.xml",
    "/org/onebusaway/gtdf/impl/application-context-test.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class CalendarServiceDataFactoryBeanTest {

  private static DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");

  @Autowired
  private ApplicationContext _context;

  @Test
  public void test() throws ParseException {
    CalendarServiceData data = (CalendarServiceData) _context.getBean("calendarServiceData");

    Date date = _format.parse("2008-11-10");
    Set<ServiceDate> serviceDates = data.getServiceDatesForDate(date);
    for (ServiceDate serviceDate : serviceDates) {
      System.out.println("date=" + serviceDate.getServiceDate() + " "
          + serviceDate.getServiceId());
    }
  }

}
