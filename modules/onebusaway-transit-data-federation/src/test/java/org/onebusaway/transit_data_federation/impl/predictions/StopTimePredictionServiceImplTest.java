package org.onebusaway.transit_data_federation.impl.predictions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.TransitDataFederationBaseTest;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.predictions.StopTimePrediction;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import edu.washington.cs.rse.text.DateLibrary;

import net.sf.ehcache.Cache;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Date;

public class StopTimePredictionServiceImplTest {

  @Test
  public void test() {

    Date today = DateLibrary.getTimeAsDay(new Date());

    StopTimePrediction predictionA = new StopTimePrediction();
    predictionA.setPredictedArrivalOffset(34);
    predictionA.setPredictedDepartureOffset(43);
    predictionA.setPredictionTime(System.currentTimeMillis());
    predictionA.setScheduledArrivalTime(27000);
    predictionA.setScheduledDepartureTime(27200);
    predictionA.setStopId(new AgencyAndId("1", "stopIdA"));
    predictionA.setTripId(new AgencyAndId("1", "tripId"));
    predictionA.setServiceDate(today.getTime());

    StopTimePrediction predictionB = new StopTimePrediction();
    predictionB.setPredictedArrivalOffset(36);
    predictionB.setPredictedDepartureOffset(45);
    predictionB.setPredictionTime(System.currentTimeMillis());
    predictionB.setScheduledArrivalTime(28800);
    predictionB.setScheduledDepartureTime(29000);
    predictionB.setStopId(new AgencyAndId("1", "stopIdB"));
    predictionB.setTripId(new AgencyAndId("1", "tripId"));
    predictionB.setServiceDate(today.getTime());

    Cache cache = TransitDataFederationBaseTest.createCache();

    StopTimePredictionServiceImpl service = new StopTimePredictionServiceImpl();
    service.setTripStopTimePredictionsCache(cache);
    service.handleStopTimePredictions(Arrays.asList(predictionA, predictionB));

    TripEntryImpl trip = new TripEntryImpl();
    trip.setId(new AgencyAndId("1", "tripId"));

    StopTimeEntryImpl stopTimeA = new StopTimeEntryImpl();
    stopTimeA.setArrivalTime(27600);
    stopTimeA.setDepartureTime(27800);
    stopTimeA.setTrip(trip);

    StopTimeInstanceProxy stiA = new StopTimeInstanceProxy(stopTimeA, today);

    service.applyPredictions(Arrays.asList(stiA));

    assertTrue(stiA.hasPredictedArrivalOffset());
    assertEquals(41, stiA.getPredictedArrivalOffset());
    assertTrue(stiA.hasPredictedDepartureOffset());
    assertEquals(40, stiA.getPredictedDepartureOffset());

    StopTimeEntryImpl stopTimeB = new StopTimeEntryImpl();
    stopTimeB.setArrivalTime(26000);
    stopTimeB.setDepartureTime(26200);
    stopTimeB.setTrip(trip);

    StopTimeInstanceProxy stiB = new StopTimeInstanceProxy(stopTimeB, today);

    service.applyPredictions(Arrays.asList(stiB));

    assertTrue(stiB.hasPredictedArrivalOffset());
    assertEquals(34, stiB.getPredictedArrivalOffset());
    assertTrue(stiB.hasPredictedDepartureOffset());
    assertEquals(34, stiB.getPredictedDepartureOffset());

    StopTimeEntryImpl stopTimeC = new StopTimeEntryImpl();
    stopTimeC.setArrivalTime(30000);
    stopTimeC.setDepartureTime(30200);
    stopTimeC.setTrip(trip);

    StopTimeInstanceProxy stiC = new StopTimeInstanceProxy(stopTimeC, today);

    service.applyPredictions(Arrays.asList(stiC));

    assertTrue(stiC.hasPredictedArrivalOffset());
    assertEquals(45, stiC.getPredictedArrivalOffset());
    assertTrue(stiC.hasPredictedDepartureOffset());
    assertEquals(45, stiC.getPredictedDepartureOffset());
  }

  @Test
  public void testTripTimePredictionGeneration() {

    AgencyAndId tripId = new AgencyAndId("1", "tripId");
    long serviceDate = DateLibrary.getTimeAsDay(new Date()).getTime();

    long predictionTime = serviceDate + 28000 * 1000;
    
    StopTimePrediction predictionA = new StopTimePrediction();
    predictionA.setPredictedArrivalOffset(34);
    predictionA.setPredictedDepartureOffset(44);
    predictionA.setPredictionTime(predictionTime);
    predictionA.setScheduledArrivalTime(27000);
    predictionA.setScheduledDepartureTime(27200);
    predictionA.setStopId(new AgencyAndId("1", "stopIdA"));
    predictionA.setTripId(tripId);
    predictionA.setServiceDate(serviceDate);

    StopTimePrediction predictionB = new StopTimePrediction();
    predictionB.setPredictedArrivalOffset(36);
    predictionB.setPredictedDepartureOffset(45);
    predictionB.setPredictionTime(predictionTime);
    predictionB.setScheduledArrivalTime(28800);
    predictionB.setScheduledDepartureTime(29000);
    predictionB.setStopId(new AgencyAndId("1", "stopIdB"));
    predictionB.setTripId(tripId);
    predictionB.setServiceDate(serviceDate);

    Cache cache = TransitDataFederationBaseTest.createCache();

    StopTimePredictionServiceImpl service = new StopTimePredictionServiceImpl();
    service.setTripStopTimePredictionsCache(cache);

    TripTimePredictionService tripTimePredictionService = Mockito.mock(TripTimePredictionService.class);
    service.setTripTimePredictionService(tripTimePredictionService);

    service.handleStopTimePredictions(Arrays.asList(predictionA, predictionB));

    Mockito.verify(tripTimePredictionService).putScheduleDeviationPrediction(
        tripId, serviceDate, predictionTime, 40);
  }
}
