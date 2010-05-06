package org.onebusaway.gtfs.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.GtfsTestData;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class GtfsRelationalDaoImplTest {

  @Test
  public void testBart() throws IOException {
    
    GtfsRelationalDaoImpl dao = new GtfsRelationalDaoImpl();
    GtfsTestData.readGtfs(dao,GtfsTestData.getBartGtfs(),"BART");
    
    int[] arrivalTimeInterval = dao.getArrivalTimeIntervalForServiceId(new AgencyAndId("BART","WKDY"));
    assertEquals(13860,arrivalTimeInterval[0]);
    assertEquals(92040,arrivalTimeInterval[1]);
    
    int[] departureTimeInterval = dao.getDepartureTimeIntervalForServiceId(new AgencyAndId("BART","WKDY"));
    assertEquals(13860,departureTimeInterval[0]);
    assertEquals(92040,departureTimeInterval[1]);

    Agency agency = dao.getAgencyForId("BART");
    List<Route> routes = dao.getRoutesForAgency(agency);
    assertEquals(10,routes.size());
    
    agency = dao.getAgencyForId("AirBART");
    routes = dao.getRoutesForAgency(agency);
    assertEquals(1,routes.size());
    
    Route route = dao.getRouteForId(new AgencyAndId("BART","01"));
    List<Trip> trips = dao.getTripsForRoute(route);
    assertEquals(225,trips.size());
    
    Trip trip = dao.getTripForId(new AgencyAndId("BART","15PB1"));
    List<StopTime> stopTimes = dao.getStopTimesForTrip(trip);
    assertEquals(12,stopTimes.size());

    // Ensure the stopTimes are in stop sequence order
    for( int i=0; i<stopTimes.size() - 1; i++)
      assertTrue(stopTimes.get(i).getStopSequence() < stopTimes.get(i+1).getStopSequence());

    List<ShapePoint> shapePoints = dao.getShapePointsForShapeId(new AgencyAndId("BART","airbart-dn.csv"));
    assertEquals(50,shapePoints.size());
    
    for( int i=0; i<shapePoints.size()-1;i++)
      assertTrue(shapePoints.get(i).getSequence() < shapePoints.get(i+1).getSequence());
   }
}
