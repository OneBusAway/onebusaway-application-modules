package org.onebusaway.transit_data_federation.impl.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.DateSupport;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

public class BlockCalendarServiceTest {

  private BlockCalendarService _service;

  private TransitGraphDao _graphDao;

  private CalendarServiceImpl _calendarService;

  private CalendarServiceData _calendarData;

  @Before
  public void setup() {

    _service = new BlockCalendarService();

    _calendarService = new CalendarServiceImpl();
    _service.setCalendarService(_calendarService);

    _calendarData = new CalendarServiceData();
    _calendarService.setData(_calendarData);

    _graphDao = Mockito.mock(TransitGraphDao.class);
    _service.setTransitGraphDao(_graphDao);
  }

  @Test
  public void test() {

    AgencyAndId blockId = new AgencyAndId("agency", "block");

    AgencyAndId serviceIdA = new AgencyAndId("agency", "serviceIdA");
    AgencyAndId serviceIdB = new AgencyAndId("agency", "serviceIdB");

    LocalizedServiceId lsidA = new LocalizedServiceId(serviceIdA,
        DateSupport.getTimeZone());
    LocalizedServiceId lsidB = new LocalizedServiceId(serviceIdB,
        DateSupport.getTimeZone());

    StopTimeEntryImpl stopTimeA = new StopTimeEntryImpl();
    stopTimeA.setArrivalTime(DateSupport.hourToSec(8.5));
    stopTimeA.setDepartureTime(DateSupport.hourToSec(8.5));

    TripEntryImpl tripA = new TripEntryImpl();
    tripA.setId(new AgencyAndId("agency", "tripA"));
    tripA.setServiceId(serviceIdA);
    tripA.setStopTimes(Arrays.asList((StopTimeEntry) stopTimeA));

    StopTimeEntryImpl stopTimeB = new StopTimeEntryImpl();
    stopTimeB.setArrivalTime(DateSupport.hourToSec(8.5));
    stopTimeB.setDepartureTime(DateSupport.hourToSec(8.5));

    TripEntryImpl tripB = new TripEntryImpl();
    tripB.setId(new AgencyAndId("agency", "tripB"));
    tripB.setServiceId(serviceIdB);
    tripB.setStopTimes(Arrays.asList((StopTimeEntry) stopTimeB));

    List<TripEntry> trips = Arrays.asList((TripEntry) tripA, tripB);

    Mockito.when(_graphDao.getTripsForBlockId(blockId)).thenReturn(trips);

    _calendarData.putTimeZoneForAgencyId("agency", DateSupport.getTimeZone());

    Date d1 = DateSupport.date("2010-03-16 00:00");
    Date d2 = DateSupport.date("2010-03-17 00:00");
    Date d3 = DateSupport.date("2010-03-18 00:00");

    _calendarData.putDatesForLocalizedServiceId(lsidA, Arrays.asList(d1, d2));
    _calendarData.putDatesForLocalizedServiceId(lsidB, Arrays.asList(d2, d3));

    Date from = DateSupport.date("2010-03-16 8:00");
    Date to = DateSupport.date("2010-03-16 9:00");

    List<Date> dates = _service.getServiceDatesWithinRangeForBlockId(blockId,
        from, to);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d1));

    from = DateSupport.date("2010-03-17 8:00");
    to = DateSupport.date("2010-03-17 9:00");

    dates = _service.getServiceDatesWithinRangeForBlockId(blockId, from, to);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d2));

    from = DateSupport.date("2010-03-18 8:00");
    to = DateSupport.date("2010-03-18 9:00");

    dates = _service.getServiceDatesWithinRangeForBlockId(blockId, from, to);
    assertEquals(1, dates.size());
    assertTrue(dates.contains(d3));
  }
}
