package org.onebusaway.kcmetro_tcip.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.kcmetro.model.TimepointToStopMapping;
import org.onebusaway.kcmetro.model.TimepointToStopMappingInstance;
import org.onebusaway.kcmetro_tcip.model.TimepointPrediction;
import org.onebusaway.kcmetro_tcip.services.KCMetroTcipDao;
import org.onebusaway.tcip.model.CPTStoppointIden;
import org.onebusaway.tcip.model.CPTVehicleIden;
import org.onebusaway.tcip.model.PISchedAdherenceCountdown;
import org.onebusaway.tcip.model.SCHTripIden;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Min;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

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

public class TimepointPredictionToPiSchedAdherenceCountdownServiceImpl
    implements TimepointPredictionListener {

  private final Logger _log = LoggerFactory.getLogger(TimepointPredictionToPiSchedAdherenceCountdownServiceImpl.class);

  private List<PISchedAdherenceCountdownListener> _listeners = new ArrayList<PISchedAdherenceCountdownListener>();

  /**
   * First key: trackerTripId
   * 
   * Second key: timepointId
   * 
   * Value: list of TimepointToStopMappings
   * 
   * Recall that a timepoint can be visited multiple times in a trip, thus the
   * list of TimepointToStopMappings
   */
  private Cache _timepointToStopMappingsByTrackerTripId;

  private KCMetroTcipDao _dao;

  private CalendarService _calendarService;

  private int _predictionsCount = 0;

  private int _predictionsWithoutTrackingDataCount = 0;

  private int _predictionsWithNegativeTripIdCount = 0;

  private int _predictionsWithUnknownTripCount = 0;

  private int _predictionsWithUnknownTimepointCount = 0;

  private int _predictionWithNoTimepointToStopMappingCount = 0;

  private int _predictionsMappedToStopCount = 0;

  private int _unknownTripCount = 0;

  public void setListener(PISchedAdherenceCountdownListener listener) {
    addListener(listener);
  }

  public void addListener(PISchedAdherenceCountdownListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(PISchedAdherenceCountdownListener listener) {
    _listeners.remove(listener);
  }

  public void setTimepointToStopMappingsByTrackerTripIdCache(Cache cache) {
    _timepointToStopMappingsByTrackerTripId = cache;
  }

  @Autowired
  public void setKCMetroTcipDao(KCMetroTcipDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  public int getPredictionsCount() {
    return _predictionsCount;
  }

  public int getPredictionsMappedToStopCount() {
    return _predictionsMappedToStopCount;
  }

  public int getPredictionWithNoTimepointToStopMappingCount() {
    return _predictionWithNoTimepointToStopMappingCount;
  }

  public int getPredictionsWithoutTrackingDataCount() {
    return _predictionsWithoutTrackingDataCount;
  }

  public int getPredictionsWithNegativeTripIdCount() {
    return _predictionsWithNegativeTripIdCount;
  }

  public int getPredictionsWithUnknownTimepointCount() {
    return _predictionsWithUnknownTimepointCount;
  }

  public int getUnknownTripCount() {
    return _unknownTripCount;
  }

  /****
   * {@link TimepointPredictionListener} Interface
   ****/

  public void handleTimepointPredictions(List<TimepointPrediction> predictions) {

    List<PISchedAdherenceCountdown> countdowns = new ArrayList<PISchedAdherenceCountdown>();

    for (TimepointPrediction prediction : predictions) {
      _predictionsCount++;
      PISchedAdherenceCountdown countdown = getTimepointPredictionAsPiSchedAdherenceCountdown(prediction);
      if (countdown != null) {
        _predictionsMappedToStopCount++;
        countdowns.add(countdown);
      }
    }

    if (!countdowns.isEmpty()) {
      for (PISchedAdherenceCountdownListener listener : _listeners)
        listener.handle(countdowns);
    }
  }

  /****
   * Private Methods
   ****/

  /**
   * Given a {@link TimepointPrediction}, find the mapping to a
   * {@link PISchedAdherenceCountdown} object. A mapping will not be created if
   * there is no real-time arrival information attached to the
   * {@link TimepointPrediction}.
   */
  private PISchedAdherenceCountdown getTimepointPredictionAsPiSchedAdherenceCountdown(
      TimepointPrediction prediction) {

    TimepointToStopMappingInstance instance = getTimepointToStopMappingForTimepointPrediction(prediction);

    if (instance == null)
      return null;

    TimepointToStopMapping mapping = instance.getMapping();
    Date serviceDate = instance.getServiceDate();
    long countdown = (serviceDate.getTime() + prediction.getGoalTime() * 1000 - System.currentTimeMillis()) / 1000;

    PISchedAdherenceCountdown sac = new PISchedAdherenceCountdown();
    sac.setNextArrivalCountdown(countdown);
    sac.setStoppoint(convertStopId(mapping.getStopId()));
    sac.setTrip(convertTripId(mapping.getTripId()));
    sac.setVehicle(convertVehicleId(prediction));

    return sac;
  }

  private TimepointToStopMappingInstance getTimepointToStopMappingForTimepointPrediction(
      TimepointPrediction prediction) {

    if (prediction.getGoalTime() == -1) {
      _predictionsWithoutTrackingDataCount++;
      return null;
    }

    if (prediction.getTrackerTripId().startsWith("-")) {
      _predictionsWithNegativeTripIdCount++;
      return null;
    }

    Map<AgencyAndId, List<TimepointToStopMapping>> mappingsByTimepointId = getMappingsByTimepointForPrediction(prediction);

    if (mappingsByTimepointId.isEmpty()) {
      _predictionsWithUnknownTripCount++;
      return null;
    }

    AgencyAndId timepointId = new AgencyAndId(prediction.getAgencyId(),
        prediction.getTimepointId());

    // There can potentially be a lot of these because the MyBus AVL stream
    // automatically adds in all the timepoints that a particular trip passes
    // by, even if they aren't mentioned in the service pattern for that trip
    if (!mappingsByTimepointId.containsKey(timepointId)) {
      _predictionsWithUnknownTimepointCount++;
      _log.debug("no TimepointToStopMapping found: prediction=" + prediction
          + " mappingsByTimepointId=" + mappingsByTimepointId);
      return null;
    }

    List<TimepointToStopMapping> mappings = mappingsByTimepointId.get(timepointId);

    Set<AgencyAndId> serviceIds = new HashSet<AgencyAndId>();
    for (TimepointToStopMapping mapping : mappings)
      serviceIds.add(mapping.getServiceId());

    Calendar c = Calendar.getInstance();
    c.add(Calendar.MINUTE, -45);
    Date from = c.getTime();
    c.add(Calendar.MINUTE, 90);
    Date to = c.getTime();

    Map<AgencyAndId, List<Date>> serviceDates = _calendarService.getServiceDatesWithinRange(
        serviceIds, from, to);

    Min<TimepointToStopMappingInstance> hits = new Min<TimepointToStopMappingInstance>();

    for (TimepointToStopMapping mapping : mappings) {
      AgencyAndId serviceId = mapping.getServiceId();
      List<Date> dates = serviceDates.get(serviceId);
      if (dates == null)
        continue;
      for (Date date : dates) {

        Date scheduledArrival = new Date(date.getTime()
            + prediction.getScheduledTime() * 1000);
        Date predictedArrival = new Date(date.getTime()
            + prediction.getGoalTime() * 1000);

        boolean scheduledTimeInRange = from.getTime() <= scheduledArrival.getTime()
            && scheduledArrival.getTime() <= to.getTime();
        boolean predictedTimeInRange = from.getTime() <= predictedArrival.getTime()
            && scheduledArrival.getTime() <= to.getTime();

        if (scheduledTimeInRange || predictedTimeInRange) {
          int delta = Math.abs(prediction.getScheduledTime()
              - mapping.getTime());
          hits.add(delta, new TimepointToStopMappingInstance(mapping, date));
        }
      }
    }

    if (hits.isEmpty()) {
      _predictionWithNoTimepointToStopMappingCount++;
      _log.warn("no TimepointToStopMapping found: prediction=" + prediction
          + " mappingsByTimepointId=" + mappingsByTimepointId
          + " serviceDates=" + serviceDates);
      return null;
    }

    return hits.getMinElement();
  }

  /**
   * Given {@link TimepointPrediction}, finds the set of
   * {@link TimepointToStopMapping} (keyed by timepoint id) that correspond to
   * the tracker trip id ({@link TimepointPrediction#getTrackerTripId()}) for
   * the specified prediction. Recall that a timepoint can be visited multiple
   * times for a specific trip, so the returned map of
   * {@link TimepointToStopMapping} by timepoint id has a list of values, not
   * just a single value.
   * 
   * @param prediction
   * @return
   */
  @SuppressWarnings("unchecked")
  private Map<AgencyAndId, List<TimepointToStopMapping>> getMappingsByTimepointForPrediction(
      TimepointPrediction prediction) {

    AgencyAndId trackerTripId = new AgencyAndId(prediction.getAgencyId(),
        prediction.getTrackerTripId());

    Element element = _timepointToStopMappingsByTrackerTripId.get(trackerTripId);

    if (element == null) {

      List<TimepointToStopMapping> mappings = _dao.getTimepointToStopMappingsForTrackerTripId(trackerTripId);

      if (mappings.isEmpty()) {
        _log.debug("uknown trackerTripId=" + trackerTripId);
        _unknownTripCount++;
      }

      Map<AgencyAndId, List<TimepointToStopMapping>> mappingsByTimepointId = new FactoryMap<AgencyAndId, List<TimepointToStopMapping>>(
          new ArrayList<TimepointToStopMapping>());

      for (TimepointToStopMapping mapping : mappings)
        mappingsByTimepointId.get(mapping.getTimepointId()).add(mapping);

      element = new Element(trackerTripId, mappingsByTimepointId);
      _timepointToStopMappingsByTrackerTripId.put(element);
    }

    return (Map<AgencyAndId, List<TimepointToStopMapping>>) element.getValue();
  }

  private CPTStoppointIden convertStopId(AgencyAndId stopId) {
    CPTStoppointIden id = new CPTStoppointIden();
    id.setAgencyId(stopId.getAgencyId());
    id.setStoppointId(stopId.getId());
    return id;
  }

  private SCHTripIden convertTripId(AgencyAndId tripId) {
    SCHTripIden id = new SCHTripIden();
    id.setAgencyId(tripId.getAgencyId());
    id.setTripId(tripId.getId());
    return id;
  }

  private CPTVehicleIden convertVehicleId(TimepointPrediction prediction) {
    CPTVehicleIden id = new CPTVehicleIden();
    id.setAgencyId(prediction.getAgencyId());
    id.setVehicleId(Integer.toString(prediction.getVehicleId()));
    return id;
  }
}
