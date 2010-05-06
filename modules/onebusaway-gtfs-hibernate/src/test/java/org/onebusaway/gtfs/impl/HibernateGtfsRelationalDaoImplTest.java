package org.onebusaway.gtfs.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.onebusaway.gtfs.GtfsHibernateTest;
import org.onebusaway.gtfs.GtfsHibernateTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.testing.DbUnitTestConfiguration;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.filter.IFilter;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@DbUnitTestConfiguration(location = GtfsHibernateTestData.CALTRAIN_DATABASE)
public class HibernateGtfsRelationalDaoImplTest extends GtfsHibernateTest {

  private static DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");

  private static final String _agencyId = "Caltrain";

  @Autowired
  private HibernateGtfsRelationalDaoImpl _dao;

  /****
   * {@link Agency} Methods
   ****/

  @Test
  public void testGetAllAgencies() {
    List<Agency> agencies = _dao.getAllAgencies();
    assertEquals(1, agencies.size());
  }

  @Test
  public void testGetAgencyForId() {
    Agency agency = _dao.getAgencyForId(_agencyId);
    assertNotNull(agency);
    assertEquals(_agencyId, agency.getId());
    assertEquals("Caltrain", agency.getName());
    assertEquals("http://www.caltrain.com", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());
    assertNull(agency.getLang());
    assertNull(agency.getPhone());
  }

  /****
   * {@link ServiceCalendar} and {@link ServiceCalendarDate} Methods
   ****/

  @Test
  public void testGetAllCalendarDates() throws ParseException {

    List<ServiceCalendarDate> calendarDates = _dao.getAllCalendarDates();
    assertEquals(10, calendarDates.size());

    List<ServiceCalendarDate> weekdays = CollectionsLibrary.grep(calendarDates,
        new IFilter<ServiceCalendarDate>() {
          public boolean isEnabled(ServiceCalendarDate element) {
            return element.getServiceId().equals(aid("WD01272009"));
          }
        });

    assertEquals(4, weekdays.size());

    final Date date = _format.parse("2009-05-25");

    List<ServiceCalendarDate> onDate = CollectionsLibrary.grep(weekdays, "date.time",
        date.getTime());
    assertEquals(1, onDate.size());

    ServiceCalendarDate cd = onDate.get(0);
    assertEquals(2, cd.getExceptionType());
  }

  @Test
  public void testGetAllCalendars() throws ParseException {

    List<ServiceCalendar> calendars = _dao.getAllCalendars();
    assertEquals(6, calendars.size());

    List<ServiceCalendar> weekdays = CollectionsLibrary.grep(calendars,
        "serviceId", aid("WD"));

    assertEquals(1, weekdays.size());
    ServiceCalendar weekday = weekdays.get(0);

    assertEquals(_format.parse("2009-01-01"), weekday.getStartDate());
    assertEquals(_format.parse("2009-03-01"), weekday.getEndDate());
    assertEquals(1, weekday.getMonday());
    assertEquals(1, weekday.getTuesday());
    assertEquals(1, weekday.getWednesday());
    assertEquals(1, weekday.getThursday());
    assertEquals(1, weekday.getFriday());
    assertEquals(0, weekday.getSaturday());
    assertEquals(0, weekday.getSunday());
  }

  @Test
  public void testGetArrivalTimeIntervalForServiceId() {
    int[] interval = _dao.getArrivalTimeIntervalForServiceId(aid("WD"));
    assertEquals(16200, interval[0]);
    assertEquals(91920, interval[1]);
  }

  @Test
  public void testGetDepartureTimeIntervalForServiceId() {
    int[] interval = _dao.getDepartureTimeIntervalForServiceId(aid("WD"));
    assertEquals(16200, interval[0]);
    assertEquals(91920, interval[1]);
  }

  /****
   * {@link Route} Methods
   ****/

  @Test
  public void testGetAllRoutes() {
    List<Route> routes = _dao.getAllRoutes();
    assertEquals(3, routes.size());
  }

  @Test
  public void testGetRouteById() {
    Route route = _dao.getRouteForId(aid("ct_bullet"));
    assertEquals(aid("ct_bullet"), route.getId());
    assertEquals("Bullet", route.getLongName());
    assertEquals(2,route.getType());
    assertEquals(null,route.getColor());
    assertEquals("ff0000",route.getTextColor());
    assertEquals(null,route.getUrl());
  }

  /****
   * {@link Stop} Methods
   ****/

  @Test
  public void testGetAllStops() {
    List<Stop> stops = _dao.getAllStops();
    assertEquals(31, stops.size());
  }

  @Test
  public void testGetStopById() {
    AgencyAndId id = aid("Gilroy Caltrain");
    Stop stop = _dao.getStopForId(id);
    assertEquals(id, stop.getId());
    assertNull(stop.getCode());
    assertEquals("7150 Monterey Street, Gilroy", stop.getDesc());
    assertEquals(37.003084, stop.getLat(), 0.000001);
    assertEquals(-121.567091, stop.getLon(), 0.000001);
    assertEquals(0, stop.getLocationType());
    assertEquals("Gilroy Caltrain", stop.getName());
    assertEquals("6", stop.getZoneId());
    assertNull(stop.getUrl());
    assertNull(stop.getParentStation());
  }

  /****
   * {@link Trip} Methods
   ****/

  @Test
  public void testGetAllTrips() {
    List<Trip> trips = _dao.getAllTrips();
    assertEquals(260, trips.size());
  }

  @Test
  public void testGetTripById() {
    Route route = _dao.getRouteForId(aid("ct_local"));

    Trip trip = _dao.getTripForId(aid("10101272009"));
    assertEquals(aid("10101272009"), trip.getId());
    assertNull(trip.getBlockId());
    assertEquals("0", trip.getDirectionId());
    assertEquals(route, trip.getRoute());
    assertNull(trip.getRouteShortName());
    assertEquals(aid("WD01272009"), trip.getServiceId());
    assertEquals(aid("cal_sj_sf"), trip.getShapeId());
    assertEquals("101", trip.getTripShortName());
    assertEquals("San Jose to San Francisco", trip.getTripHeadsign());
  }

  @Test
  public void testGetTripsForRoute() {
    Route route = _dao.getRouteForId(aid("ct_local"));
    List<Trip> tripsForRoute = _dao.getTripsForRoute(route);
    assertEquals(120, tripsForRoute.size());
  }

  /****
   * {@link StopTime} Methods
   ****/

  @Test
  public void testGetAllStopTimes() {
    List<StopTime> stopTimes = _dao.getAllStopTimes();
    assertEquals(4712, stopTimes.size());
  }

  @Test
  public void testGetStopTimesForId() {

    StopTime first = _dao.getStopTimeForId(1);

    assertEquals(21120, first.getArrivalTime());
    assertEquals(21120, first.getDepartureTime());
    assertEquals(0, first.getDropOffType());
    assertEquals(0, first.getPickupType());
    assertEquals(-1.0, first.getShapeDistTraveled(), 0.0);
    assertEquals(21, first.getStopSequence());
    assertEquals(aid("22nd Street Caltrain"), first.getStop().getId());
    assertEquals(aid("10101272009"), first.getTrip().getId());

    StopTime second = _dao.getStopTimeForId(193);

    assertEquals(41220, second.getArrivalTime());
    assertEquals(41220, second.getDepartureTime());
    assertEquals(0, second.getDropOffType());
    assertEquals(0, second.getPickupType());
    assertEquals(-1.0, second.getShapeDistTraveled(), 0.0);
    assertEquals(5, second.getStopSequence());
    assertEquals(aid("San Bruno Caltrain"), second.getStop().getId());
    assertEquals(aid("14201272009"), second.getTrip().getId());
  }

  @Test
  public void testGetStopTimesByTrip() {
    Trip trip = _dao.getTripForId(aid("10101272009"));
    List<StopTime> stopTimes = _dao.getStopTimesForTrip(trip);
    assertEquals(22, stopTimes.size());
  }

  /****
   * {@link ShapePoint} Methods
   ****/

  @Test
  public void testGetShapePointsByShapeId() {
    List<ShapePoint> shapePoints = _dao.getShapePointsForShapeId(aid("cal_sf_gil"));
    assertEquals(556, shapePoints.size());
  }

  /****
   * Private Methods
   ****/

  private AgencyAndId aid(String id) {
    return new AgencyAndId(_agencyId, id);
  }
}
