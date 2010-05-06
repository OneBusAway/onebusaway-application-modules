package org.onebusaway.gtfs.impl;

import static org.junit.Assert.assertEquals;

import org.onebusaway.gtfs.GtfsTestData;
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

import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

public class GtfsDaoImplTest {

  @Test
  public void testBart() throws IOException {
    
    GtfsDaoImpl dao = new GtfsDaoImpl();
    GtfsTestData.readGtfs(dao,GtfsTestData.getBartGtfs(),"BART");
    
    Collection<Agency> agencies = dao.getAllAgencies();
    assertEquals(2,agencies.size());

    Agency agency = dao.getAgencyForId("BART");
    assertEquals("BART",agency.getId());
    
    Collection<ServiceCalendarDate> calendarDates = dao.getAllCalendarDates();
    assertEquals(32,calendarDates.size());
    
    ServiceCalendarDate calendarDate = dao.getCalendarDateForId(1);
    assertEquals(new AgencyAndId("BART","SUN"),calendarDate.getServiceId());
    
    Collection<ServiceCalendar> calendars = dao.getAllCalendars();
    assertEquals(5,calendars.size());
    
    ServiceCalendar calendar = dao.getCalendarForId(1);
    assertEquals(new AgencyAndId("BART","WKDY"),calendar.getServiceId());
    
    Collection<FareAttribute> fareAttributes = dao.getAllFareAttributes();
    assertEquals(106,fareAttributes.size());
    
    FareAttribute fareAttribute = dao.getFareAttributeForId(new AgencyAndId("BART","30"));
    assertEquals(new AgencyAndId("BART","30"),fareAttribute.getId());
    
    Collection<FareRule> fareRules = dao.getAllFareRules();
    assertEquals(1849,fareRules.size());
    
    FareRule fareRule = dao.getFareRuleForId(1);
    assertEquals(new AgencyAndId("BART","98"),fareRule.getFare().getId());
    
    Collection<Frequency> frequencies = dao.getAllFrequencies();
    assertEquals(6,frequencies.size());
    
    Frequency frequency = dao.getFrequencyForId(1);
    assertEquals(new AgencyAndId("AirBART","M-FSAT1DN"),frequency.getTrip().getId());
    
    Collection<Route> routes = dao.getAllRoutes();
    assertEquals(11,routes.size());
    
    Route route = dao.getRouteForId(new AgencyAndId("BART","01"));
    assertEquals(new AgencyAndId("BART","01"),route.getId());
    
    Collection<ShapePoint> shapePoints = dao.getAllShapePoints();
    assertEquals(105,shapePoints.size());
    
    ShapePoint shapePoint = dao.getShapePointForId(1);
    assertEquals(new AgencyAndId("BART","airbart-dn.csv"),shapePoint.getShapeId());
    
    Collection<Stop> stops = dao.getAllStops();
    assertEquals(46,stops.size());
    
    Stop stop = dao.getStopForId(new AgencyAndId("BART","DBRK"));
    assertEquals("Downtown Berkeley BART",stop.getName());
    
    Collection<StopTime> stopTimes = dao.getAllStopTimes();
    assertEquals(33270,stopTimes.size());
    
    StopTime stopTime = dao.getStopTimeForId(1);
    assertEquals(18000,stopTime.getArrivalTime());
   
    Collection<Transfer> transfers = dao.getAllTransfers();
    assertEquals(4,transfers.size());
    
    Transfer transfer = dao.getTransferForId(1);
    assertEquals(1,transfer.getTransferType());
    
    Collection<Trip> trips = dao.getAllTrips();
    assertEquals(1620,trips.size());
    
    Trip trip = dao.getTripForId(new AgencyAndId("BART","15PB1"));
    assertEquals(new AgencyAndId("BART","WKDY"),trip.getServiceId());
  }
}
