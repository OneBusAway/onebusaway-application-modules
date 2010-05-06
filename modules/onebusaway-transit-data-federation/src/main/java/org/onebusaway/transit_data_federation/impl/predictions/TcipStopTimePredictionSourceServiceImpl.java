package org.onebusaway.transit_data_federation.impl.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.tcip.model.CPTStoppointIden;
import org.onebusaway.tcip.model.CPTSubscriptionHeader;
import org.onebusaway.tcip.model.CPTSubscriptionType;
import org.onebusaway.tcip.model.PISchedAdherenceCountdown;
import org.onebusaway.tcip.model.PiStopPointETA;
import org.onebusaway.tcip.model.PiStopPointETASub;
import org.onebusaway.tcip.model.SCHTripIden;
import org.onebusaway.tcip.services.TcipClient;
import org.onebusaway.tcip.services.TcipFutureListener;
import org.onebusaway.tcip.services.TcipServlet;
import org.onebusaway.tcip.services.TcipServletMapping;
import org.onebusaway.tcip.services.TcipServletRequest;
import org.onebusaway.tcip.services.TcipServletResponse;
import org.onebusaway.transit_data_federation.model.predictions.StopTimePrediction;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionSourceService;
import org.onebusaway.transit_data_federation.services.predictions.StopTimePredictionSourceServiceListener;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TcipStopTimePredictionSourceServiceImpl implements
    StopTimePredictionSourceService, TcipFutureListener<TcipClient> {

  private final Logger _log = LoggerFactory.getLogger(TcipStopTimePredictionSourceServiceImpl.class);

  private List<StopTimePredictionSourceServiceListener> _listeners = new ArrayList<StopTimePredictionSourceServiceListener>();

  private CalendarService _calendarService;

  private String _hostname;

  private int _port;

  private TcipClient _client;

  private TripPlannerGraph _graph;

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  public void setPort(int port) {
    _port = port;
  }

  public void addListener(StopTimePredictionSourceServiceListener listener) {
    _listeners.add(listener);
  }

  public void start() throws Throwable {
    _client = new TcipClient();
    _client.setHostname(_hostname);
    _client.setPort(_port);
    _client.addServletMapping(new TcipServletMapping(PiStopPointETA.class,
        new PiStopPointETAServlet()));
    _client.addConnectionListener(this);
    _client.start();
  }

  public void stop() throws InterruptedException {
    _log.info("stopping tcip stop time prediction source client...");
    _client.stop();
  }

  public void operationCompleted(TcipClient operation) {

    PiStopPointETASub sub = new PiStopPointETASub();
    CPTSubscriptionHeader header = new CPTSubscriptionHeader();
    header.setRequestedType(CPTSubscriptionType.TYPE_EVENT);
    header.setRequestIdentifier(0);
    sub.setSubscriptionInfo(header);

    _client.writeMessage(sub);
  }

  /****
   * Private Methods
   ****/

  private class PiStopPointETAServlet extends TcipServlet {

    @Override
    public void service(TcipServletRequest request, TcipServletResponse response) {

      PiStopPointETA message = (PiStopPointETA) request.getMessage();
      List<PISchedAdherenceCountdown> arrivalEstimates = message.getArrivalEstimates();

      if (arrivalEstimates.isEmpty())
        return;

      Map<AgencyAndId, List<PISchedAdherenceCountdown>> estimatesByTripId = new FactoryMap<AgencyAndId, List<PISchedAdherenceCountdown>>(
          new ArrayList<PISchedAdherenceCountdown>());

      for (PISchedAdherenceCountdown arrivalEstimate : arrivalEstimates) {
        SCHTripIden trip = arrivalEstimate.getTrip();
        AgencyAndId tripId = new AgencyAndId(trip.getAgencyId(),
            trip.getTripId());
        estimatesByTripId.get(tripId).add(arrivalEstimate);
      }

      List<StopTimePrediction> predictions = new ArrayList<StopTimePrediction>();

      for (Map.Entry<AgencyAndId, List<PISchedAdherenceCountdown>> entry : estimatesByTripId.entrySet()) {

        AgencyAndId tripId = entry.getKey();

        TripEntry tripEntry = _graph.getTripEntryForId(tripId);
        
        if( tripEntry == null) {
          _log.warn("unknown tripId=" + tripId);
          continue;
        }

        Map<AgencyAndId, List<StopTimeEntry>> stopTimesByStopId = CollectionsLibrary.mapToValueList(
            tripEntry.getStopTimes(), "stop.id", AgencyAndId.class);

        for (PISchedAdherenceCountdown arrivalEstimate : entry.getValue()) {

          CPTStoppointIden stoppoint = arrivalEstimate.getStoppoint();
          AgencyAndId stopId = new AgencyAndId(stoppoint.getAgencyId(),
              stoppoint.getStoppointId());
          List<StopTimeEntry> stopTimesAtStop = stopTimesByStopId.get(stopId);

          if (stopTimesAtStop == null || stopTimesAtStop.isEmpty()) {
            _log.warn("Stop prediction not found for trip: stop=" + stopId
                + " trip=" + tripId);
            continue;
          }

          StopTimeInstanceProxy sti = applyBestStopTime(tripEntry,stopTimesAtStop, message,
              arrivalEstimate);

          if (sti != null) {
            StopTimePrediction prediction = new StopTimePrediction();
            prediction.setTripId(tripId);
            prediction.setStopId(stopId);
            prediction.setServiceDate(sti.getServiceDate());
            prediction.setPredictionTime(System.currentTimeMillis());
            prediction.setScheduledArrivalTime(sti.getStopTime().getArrivalTime());
            prediction.setScheduledDepartureTime(sti.getStopTime().getDepartureTime());
            prediction.setPredictedArrivalOffset(sti.getPredictedArrivalOffset());
            prediction.setPredictedDepartureOffset(sti.getPredictedDepartureOffset());
            predictions.add(prediction);
          }
        }
      }

      if (predictions.isEmpty())
        return;

      for (StopTimePredictionSourceServiceListener listener : _listeners)
        listener.handleStopTimePredictions(predictions);
    }

    private StopTimeInstanceProxy applyBestStopTime(TripEntry tripEntry, List<StopTimeEntry> stopTimesAtStop,
        PiStopPointETA message, PISchedAdherenceCountdown arrivalEstimate) {

      long estimatedArrivalTime = message.getCreated().getTime()
          + arrivalEstimate.getNextArrivalCountdown() * 1000;

      AgencyAndId serviceId = tripEntry.getServiceId();

      Set<AgencyAndId> serviceIds = new HashSet<AgencyAndId>();
      serviceIds.add(serviceId);

      Calendar c = Calendar.getInstance();
      c.setTimeInMillis(estimatedArrivalTime);
      c.add(Calendar.MINUTE, -60);
      Date from = c.getTime();
      c.add(Calendar.MINUTE, 120);
      Date to = c.getTime();

      Map<AgencyAndId, List<Date>> serviceDatesByServiceId = _calendarService.getServiceDatesWithinRange(
          serviceIds, from, to);
      List<Date> serviceDates = serviceDatesByServiceId.get(serviceId);

      if (serviceDates == null || serviceDates.isEmpty()) {
        _log.warn("No service dates found for trip=" + tripEntry.getId()
            + " serviceId=" + serviceId);
        return null;
      }

      Min<StopTimeInstanceProxy> closestStopTimeInstance = new Min<StopTimeInstanceProxy>();

      for (Date serviceDate : serviceDates) {
        for (StopTimeEntry stopTime : stopTimesAtStop) {
          StopTimeInstanceProxy sti = new StopTimeInstanceProxy(stopTime,serviceDate);
          double diff = Math.abs(sti.getArrivalTime()
              - estimatedArrivalTime);
          closestStopTimeInstance.add(diff, sti);
        }
      }

      if (closestStopTimeInstance.isEmpty()) {
        _log.warn("No stop time instance found for trip=" + tripEntry.getId()
            + " serviceId=" + serviceId);
        return null;
      }

      StopTimeInstanceProxy best = closestStopTimeInstance.getMinElement();
      int predictedArrivalOffset = (int) ((estimatedArrivalTime - best.getArrivalTime()) / 1000);
      int predictedDepartureOffset = (int) ((estimatedArrivalTime - best.getDepartureTime()) / 1000);
      best.setPredictedArrivalOffset(predictedArrivalOffset);
      best.setPredictedDepartureOffset(predictedDepartureOffset);
      return best;
    }
  }

}
