package org.onebusaway.gtfs.impl.calendar;

import static org.junit.Assert.assertEquals;

import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarServiceData;
import org.onebusaway.gtfs.services.calendar.ServiceIdCalendarServiceData;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class CalendarServiceDataFactoryImplTest {

  @Test
  public void go() throws IOException {
    
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao, GtfsTestData.getIslandGtfs(), "26");
    
    CalendarServiceDataFactoryImpl factory = new CalendarServiceDataFactoryImpl();
    factory.setGtfsDao(dao);

    CalendarServiceData data = factory.createServiceCalendarData();

    ServiceIdCalendarServiceData idData = data.getDataForServiceId(new AgencyAndId(
        "26", "23"));
    assertEquals(13500, idData.getFirstArrival());
    assertEquals(74100, idData.getLastArrival());
    assertEquals(13500, idData.getFirstDeparture());
    assertEquals(74100, idData.getLastDeparture());
    List<Date> dates = idData.getServiceDates();
    assertEquals(239, dates.size());
    
    
  }

}
