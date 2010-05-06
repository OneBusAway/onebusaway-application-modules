package org.onebusaway.gtfs.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

public class GtfsReaderTest {

  private static DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");

  @Test
  public void testIslandTransit() throws IOException {

    String agencyId = "26";
    GtfsDao entityStore = processFeed(GtfsTestData.getIslandGtfs(),agencyId);

    Collection<Agency> agencies = entityStore.getAllAgencies();
    assertEquals(1, agencies.size());

    Agency agency = entityStore.getAgencyForId(agencyId);
    assertNotNull(agency);
    assertEquals("Island Transit", agency.getName());
    assertEquals("http://www.islandtransit.org/", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());
    assertEquals("(360) 678-7771", agency.getPhone());
    assertNull(agency.getLang());

    Collection<Stop> stops = entityStore.getAllStops();
    assertEquals(410, stops.size());

    AgencyAndId stopAId = new AgencyAndId(agencyId, "2");
    Stop stopA = entityStore.getStopForId(stopAId);
    assertEquals(stopAId, stopA.getId());
    assertNull(stopA.getCode());
    assertEquals("blank", stopA.getDesc());
    assertEquals(48.108303, stopA.getLat(), 0.000001);
    assertEquals(-122.580446, stopA.getLon(), 0.000001);
    assertEquals(0, stopA.getLocationType());
    assertEquals("Greenbank Farm: SR 525 at Smugglers Cove Rd", stopA.getName());
    assertEquals("1", stopA.getZoneId());
    assertEquals("http://islandtransit.org/stops/2", stopA.getUrl());

    AgencyAndId stopBId = new AgencyAndId(agencyId, "1178");
    Stop stopB = entityStore.getStopForId(stopBId);
    assertEquals(stopBId, stopB.getId());
    assertNull(stopB.getCode());
    assertEquals("blank", stopB.getDesc());
    assertEquals(48.018190, stopB.getLat(), 0.000001);
    assertEquals(-122.544122, stopB.getLon(), 0.000001);
    assertEquals(0, stopB.getLocationType());
    assertEquals("Bercot at Honeymoon Bay Shipshaven", stopB.getName());
    assertEquals("1", stopB.getZoneId());
    assertEquals("http://islandtransit.org/stops/1178", stopB.getUrl());

    Collection<FareAttribute> fares = entityStore.getAllFareAttributes();
    assertEquals(1, fares.size());
    FareAttribute fare = fares.iterator().next();
    assertEquals(new AgencyAndId(agencyId, "1"), fare.getId());
    assertEquals(0.0, fare.getPrice(), 0.0);
    assertEquals("USD", fare.getCurrencyType());
    assertEquals(0, fare.getPaymentMethod());
    assertEquals(-1, fare.getTransfers());
    assertEquals(-1, fare.getTransferDuration());

    Collection<FareRule> fareRules = entityStore.getAllFareRules();
    assertEquals(1, fareRules.size());
    FareRule fareRule = fareRules.iterator().next();
    assertEquals(fare, fareRule.getFare());
    assertNull(fareRule.getRoute());
    assertNull(fareRule.getContainsId());
    assertNull(fareRule.getDestinationId());
    assertNull(fareRule.getOriginId());

    Collection<Transfer> transfers = entityStore.getAllTransfers();
    assertEquals(1, transfers.size());
    Transfer transfer = transfers.iterator().next();
    assertEquals(new AgencyAndId(agencyId, "878"),
        transfer.getFromStop().getId());
    assertEquals(new AgencyAndId(agencyId, "1167"),
        transfer.getToStop().getId());
    assertEquals(1, transfer.getTransferType());
    assertEquals(-1, transfer.getMinTransferTime());
  }

  @Test
  public void testCaltrain() throws IOException, ParseException {

    File resourcePath = GtfsTestData.getCaltrainGtfs();
    String agencyId = "Caltrain";
    GtfsDao entityStore = processFeed(resourcePath, agencyId);

    Collection<Agency> agencies = entityStore.getAllAgencies();
    assertEquals(1, agencies.size());
    
    Agency agency = entityStore.getAgencyForId(agencyId);
    assertNotNull(agency);
    assertEquals("Caltrain", agency.getName());
    assertEquals("http://www.caltrain.com", agency.getUrl());
    assertEquals("America/Los_Angeles", agency.getTimezone());
    assertNull(agency.getPhone());
    assertNull(agency.getLang());

    Collection<Route> routes = entityStore.getAllRoutes();
    assertEquals(3, routes.size());

    AgencyAndId routeBulletId = new AgencyAndId(agencyId, "ct_bullet");
    Route routeBullet = entityStore.getRouteForId(routeBulletId);
    assertEquals(routeBulletId, routeBullet.getId());
    assertEquals(agency, routeBullet.getAgency());
    assertNull(routeBullet.getShortName());
    assertEquals("Bullet", routeBullet.getLongName());
    assertNull(routeBullet.getDesc());
    assertEquals(2, routeBullet.getType());
    assertNull(routeBullet.getUrl());
    assertNull(routeBullet.getColor());
    assertEquals("ff0000", routeBullet.getTextColor());

    Route routeLocal = entityStore.getRouteForId(new AgencyAndId(agencyId, "ct_local"));

    Collection<Stop> stops = entityStore.getAllStops();
    assertEquals(31, stops.size());

    AgencyAndId stopAId = new AgencyAndId(agencyId, "San Francisco Caltrain");
    Stop stopA = entityStore.getStopForId(stopAId);
    assertEquals(stopAId, stopA.getId());
    assertNull(stopA.getCode());
    assertEquals("700 4th Street, San Francisco", stopA.getDesc());
    assertEquals(37.7764393371, stopA.getLat(), 0.000001);
    assertEquals(-122.394322993, stopA.getLon(), 0.000001);
    assertEquals(0, stopA.getLocationType());
    assertEquals("San Francisco Caltrain", stopA.getName());
    assertEquals("1", stopA.getZoneId());
    assertNull(stopA.getUrl());

    AgencyAndId stopBId = new AgencyAndId(agencyId, "Gilroy Caltrain");
    Stop stopB = entityStore.getStopForId(stopBId);
    assertEquals(stopBId, stopB.getId());
    assertNull(stopB.getCode());
    assertEquals("7150 Monterey Street, Gilroy", stopB.getDesc());
    assertEquals(37.003084, stopB.getLat(), 0.000001);
    assertEquals(-121.567091, stopB.getLon(), 0.000001);
    assertEquals(0, stopB.getLocationType());
    assertEquals("Gilroy Caltrain", stopB.getName());
    assertEquals("6", stopB.getZoneId());
    assertNull(stopB.getUrl());

    Collection<Trip> trips = entityStore.getAllTrips();
    assertEquals(260, trips.size());

    AgencyAndId tripAId = new AgencyAndId(agencyId, "10101272009");
    Trip tripA = entityStore.getTripForId(tripAId);
    assertEquals(tripAId, tripA.getId());
    assertNull(tripA.getBlockId());
    assertEquals("0", tripA.getDirectionId());
    assertEquals(routeLocal, tripA.getRoute());
    assertEquals(new AgencyAndId(agencyId, "WD01272009"), tripA.getServiceId());
    assertEquals(new AgencyAndId(agencyId, "cal_sj_sf"), tripA.getShapeId());
    assertEquals("San Jose to San Francisco", tripA.getTripHeadsign());

    Collection<StopTime> stopTimes = entityStore.getAllStopTimes();
    assertEquals(4712, stopTimes.size());

    StopTime stopTimeA = entityStore.getStopTimeForId(new Integer(1));
    assertEquals(new Integer(1), stopTimeA.getId());
    assertEquals(entityStore.getTripForId(new AgencyAndId(agencyId, "10101272009")),
        stopTimeA.getTrip());
    assertEquals(21120, stopTimeA.getArrivalTime());
    assertEquals(21120, stopTimeA.getDepartureTime());
    assertEquals(entityStore.getStopForId(new AgencyAndId(agencyId, "22nd Street Caltrain")),
        stopTimeA.getStop());
    assertEquals(21, stopTimeA.getStopSequence());
    assertNull(stopTimeA.getStopHeadsign());
    assertEquals(0, stopTimeA.getPickupType());
    assertEquals(0, stopTimeA.getDropOffType());
    assertEquals(-1.0, stopTimeA.getShapeDistTraveled(), 0.0);

    Collection<ShapePoint> shapePoints = entityStore.getAllShapePoints();
    assertEquals(2677, shapePoints.size());

    AgencyAndId shapeId = new AgencyAndId(agencyId, "cal_sf_gil");
    ShapePoint shapePointA = getShapePoint(shapePoints, shapeId, 1);
    assertEquals(shapeId, shapePointA.getShapeId());
    assertEquals(1, shapePointA.getSequence());
    assertEquals(37.776439059278346, shapePointA.getLat(), 0.0);
    assertEquals(-122.39441156387329, shapePointA.getLon(), 0.0);
    assertEquals(0.0, shapePointA.getDistTraveled(), 0.0);

    Collection<ServiceCalendar> calendars = entityStore.getAllCalendars();
    assertEquals(6, calendars.size());

    ServiceCalendar calendarA = entityStore.getCalendarForId(new Integer(1));
    assertEquals(new Integer(1), calendarA.getId());
    assertEquals(new AgencyAndId(agencyId, "SN01272009"),
        calendarA.getServiceId());
    assertEquals(_format.parse("2009-03-02"), calendarA.getStartDate());
    assertEquals(_format.parse("2019-03-02"), calendarA.getEndDate());
    assertEquals(0, calendarA.getMonday());
    assertEquals(0, calendarA.getTuesday());
    assertEquals(0, calendarA.getWednesday());
    assertEquals(0, calendarA.getThursday());
    assertEquals(0, calendarA.getFriday());
    assertEquals(1, calendarA.getSaturday());
    assertEquals(1, calendarA.getSunday());

    Collection<ServiceCalendarDate> calendarDates = entityStore.getAllCalendarDates();
    assertEquals(10, calendarDates.size());
    ServiceCalendarDate cd = entityStore.getCalendarDateForId(new Integer(1));
    assertEquals(new Integer(1), cd.getId());
    assertEquals(new AgencyAndId(agencyId, "SN01272009"), cd.getServiceId());
    assertEquals(_format.parse("2009-05-25"), cd.getDate());
    assertEquals(1, cd.getExceptionType());

    Collection<FareAttribute> fareAttributes = entityStore.getAllFareAttributes();
    assertEquals(6, fareAttributes.size());

    AgencyAndId fareId = new AgencyAndId(agencyId, "OW_1");
    FareAttribute fareAttribute = entityStore.getFareAttributeForId(fareId);
    assertEquals(fareId, fareAttribute.getId());
    assertEquals(2.50, fareAttribute.getPrice(), 0.0);
    assertEquals("USD", fareAttribute.getCurrencyType());
    assertEquals(1, fareAttribute.getPaymentMethod());
    assertEquals(-1, fareAttribute.getTransfers());
    assertEquals(-1, fareAttribute.getTransferDuration());

    Collection<FareRule> fareRules = entityStore.getAllFareRules();
    assertEquals(36, fareRules.size());

    List<FareRule> fareRuleMatches = GtfsTestData.grep(fareRules,"fare",fareAttribute);
    assertEquals(6, fareRuleMatches.size());
    
    fareRuleMatches = GtfsTestData.grep(fareRuleMatches,"originId","1");
    assertEquals(1,fareRuleMatches.size());
    
    FareRule fareRule = fareRuleMatches.get(0);
    assertEquals(fareAttribute,fareRule.getFare());
    assertEquals("1",fareRule.getOriginId());
    assertEquals("1",fareRule.getDestinationId());
    assertNull(fareRule.getRoute());
    assertNull(fareRule.getContainsId());
  }

  @Test
  public void testBar() throws IOException, ParseException {

    File resourcePath = GtfsTestData.getBartGtfs();
    String agencyId = "BART";
    GtfsDao entityStore = processFeed(resourcePath,agencyId);

    Collection<Frequency> frequencies = entityStore.getAllFrequencies();
    assertEquals(6, frequencies.size());
    
    List<Frequency> frequenciesForTrip = GtfsTestData.grep(frequencies,"trip.id",new AgencyAndId("AirBART","M-FSAT1DN"));
    assertEquals(1,frequenciesForTrip.size());
    Frequency frequencyForTrip = frequenciesForTrip.get(0);
    assertEquals(18000,frequencyForTrip.getStartTime());
    assertEquals(21600,frequencyForTrip.getEndTime());
    assertEquals(1200,frequencyForTrip.getHeadwaySecs());
    
    Collection<Transfer> transfers = entityStore.getAllTransfers();
    assertEquals(4, transfers.size());
  }

  private GtfsDao processFeed(File resourcePath, String agencyId) throws IOException {
    
    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId(agencyId);
    
    reader.setInputLocation(resourcePath);

    GtfsDaoImpl entityStore = new GtfsDaoImpl();
    entityStore.setGenerateIds(true);
    reader.setEntityStore(entityStore);

    reader.run();
    return entityStore;
  }

  private ShapePoint getShapePoint(Iterable<ShapePoint> shapePoints,
      AgencyAndId shapeId, int sequence) {
    for (ShapePoint shapePoint : shapePoints) {
      if (shapePoint.getShapeId().equals(shapeId)
          && shapePoint.getSequence() == sequence)
        return shapePoint;
    }
    return null;
  }
}
