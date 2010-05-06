package org.onebusaway.transit_data_federation.impl.predictions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.tcip.model.CPTStoppointIden;
import org.onebusaway.tcip.model.PISchedAdherenceCountdown;
import org.onebusaway.tcip.model.PiStopPointETA;
import org.onebusaway.tcip.model.PiStopPointETASub;
import org.onebusaway.tcip.model.SCHTripIden;
import org.onebusaway.tcip.services.TcipServer;
import org.onebusaway.tcip.services.TcipServlet;
import org.onebusaway.tcip.services.TcipServletMapping;
import org.onebusaway.tcip.services.TcipServletRequest;
import org.onebusaway.tcip.services.TcipServletResponse;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeIndexImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.predictions.StopTimePrediction;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionSourceServiceListener;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;

import edu.washington.cs.rse.text.DateLibrary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TcipStopTimePredictionSourceServiceImplTest {

  private TcipServer _server;

  private TcipServletImpl _servlet;

  @Before
  public void setup() throws IOException {
    _server = new TcipServer();
    _server.setPort(8080);
    _server.setSourceApp("app");

    _servlet = new TcipServletImpl();
    _server.setServletMappings(Arrays.asList(new TcipServletMapping(
        PiStopPointETASub.class, _servlet)));
    _server.start();
  }

  @After
  public void teardown() {
    _server.stop();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test() throws Throwable {

    int arrivalInXSeconds = 120;
    int scheduledArrivalInXSeconds = 100;

    PISchedAdherenceCountdown estimate = new PISchedAdherenceCountdown();
    estimate.setStoppoint(new CPTStoppointIden("1", "stopIdA"));
    estimate.setTrip(new SCHTripIden("1", "tripIdA"));
    estimate.setNextArrivalCountdown(arrivalInXSeconds);
    _servlet.setArrivalEstimate(estimate);

    TcipStopTimePredictionSourceServiceImpl service = new TcipStopTimePredictionSourceServiceImpl();
    service.setHostname("localhost");
    service.setPort(8080);

    CalendarService calendarService = Mockito.mock(CalendarService.class);
    service.setCalendarService(calendarService);

    TripPlannerGraph tripPlannerGraph = Mockito.mock(TripPlannerGraph.class);
    service.setTripPlannerGraph(tripPlannerGraph);

    Date now = new Date();
    Date day = DateLibrary.getTimeAsDay(now);
    int arrivalTime = (int) ((now.getTime() - day.getTime()) / 1000 + scheduledArrivalInXSeconds);

    StopTimeEntryImpl stopTime = new StopTimeEntryImpl();
    TripEntryImpl trip = new TripEntryImpl();
    StopEntryImpl stop = new StopEntryImpl(new AgencyAndId("1", "stopIdA"), 0,
        0, new StopTimeIndexImpl());

    stopTime.setTrip(trip);
    stopTime.setArrivalTime(arrivalTime);
    stopTime.setDepartureTime(arrivalTime);
    stopTime.setStop(stop);

    trip.setId(new AgencyAndId("1", "tripIdA"));
    trip.setServiceId(new AgencyAndId("1", "serviceIdA"));
    trip.setStopTimes(Arrays.asList((StopTimeEntry) stopTime));

    Mockito.when(
        tripPlannerGraph.getTripEntryForId(new AgencyAndId("1", "tripIdA"))).thenReturn(
        trip);

    Map<AgencyAndId, List<Date>> serviceDates = new HashMap<AgencyAndId, List<Date>>();
    serviceDates.put(new AgencyAndId("1", "serviceIdA"), Arrays.asList(day));
    Mockito.when(
        calendarService.getServiceDatesWithinRange(Mockito.any(Set.class),
            Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(
        serviceDates);

    Go handler = new Go();
    service.addListener(handler);

    service.start();

    Thread.sleep(2000);

    service.stop();

    List<StopTimePrediction> predictions = handler.getPredictions();
    assertNotNull(predictions);
    assertEquals(1, predictions.size());

    StopTimePrediction prediction = predictions.get(0);
    assertEquals(new AgencyAndId("1", "stopIdA"), prediction.getStopId());
    assertEquals(new AgencyAndId("1", "tripIdA"), prediction.getTripId());
    assertTrue(Math.abs(prediction.getPredictionTime()
        - now.getTime()) < 500);
    assertEquals(arrivalTime, prediction.getScheduledArrivalTime());
    assertEquals(arrivalTime, prediction.getScheduledDepartureTime());
    long diff = prediction.getPredictedArrivalOffset() / 1000 - 20;
    assertTrue(Math.abs(diff) < 2);
    assertTrue(Math.abs(diff) < 2);
  }

  private class TcipServletImpl extends TcipServlet {

    private PISchedAdherenceCountdown _arrivalEstimate;

    public void setArrivalEstimate(PISchedAdherenceCountdown arrivalEstimate) {
      _arrivalEstimate = arrivalEstimate;
    }

    @Override
    public void service(TcipServletRequest request, TcipServletResponse response) {
      PiStopPointETA message = new PiStopPointETA();

      message.setArrivalEstimates(Arrays.asList(_arrivalEstimate));
      response.writeMessage(message);
    }

  }

  private class Go implements StopTimePredictionSourceServiceListener {

    private List<StopTimePrediction> _predictions = null;

    public List<StopTimePrediction> getPredictions() {
      return _predictions;
    }

    public void handleStopTimePredictions(List<StopTimePrediction> predictions) {
      if (_predictions != null)
        throw new IllegalStateException("called twice?");
      _predictions = predictions;
    }
  }
}
