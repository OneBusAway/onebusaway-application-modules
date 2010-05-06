package org.onebusaway.where.impl;

import org.junit.Test;
import org.onebusaway.BaseTest;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.StopTimeInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class StopTimeServiceImplTest extends BaseTest {

  private static DateFormat _dateFormat = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT);

  private static DateFormat _timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

  private GtfsDao _dao;

  private StopTimeServiceImpl _stopTimeService;

  @Autowired
  public void setDao(GtfsDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setCalendarService(StopTimeServiceImpl stopTimeService) {
    _stopTimeService = stopTimeService;
  }

  @Test
  public void testGetServiceIdsWithinRange() throws ParseException {

    Stop stop = _dao.getStopById("75414");
    Date from = _dateFormat.parse("10/13/08 12:15 PM");
    Date to = _dateFormat.parse("10/13/08 12:45 PM");

    List<StopTimeInstance> stis = _stopTimeService.getStopTimeInstancesInTimeRange(
        stop, from, to);

    for (StopTimeInstance sti : stis) {
      StopTime st = sti.getStopTime();
      Trip trip = st.getTrip();
      Route route = trip.getRoute();
      System.out.println(route.getShortName() + " "
          + _timeFormat.format(sti.getDepartureTime()));
    }
  }
}
