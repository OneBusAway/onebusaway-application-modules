/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.impl;

import edu.washington.cs.rse.collections.adapter.AdapterLibrary;
import edu.washington.cs.rse.collections.adapter.IAdapter;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.ServicePatternKey;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.StopTime;
import edu.washington.cs.rse.transit.common.model.Trip;
import edu.washington.cs.rse.transit.common.model.aggregate.ICommonStopTime;
import edu.washington.cs.rse.transit.common.model.aggregate.InterpolatedStopTime;
import edu.washington.cs.rse.transit.common.model.aggregate.ScheduledArrivalTime;
import edu.washington.cs.rse.transit.common.model.aggregate.StopTimepointInterpolation;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;
import edu.washington.cs.rse.transit.common.services.StopSchedulingService;
import edu.washington.cs.rse.transit.common.services.TimepointSchedulingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
class StopSchedulingServiceImpl extends AbstractSchedulingService implements
    StopSchedulingService {

  private static final ScheduledArrivalTimeSort SCHEDULED_ARRIVAL_TIME_SORT = new ScheduledArrivalTimeSort();

  private TimepointSchedulingService _timepointSchedulingService;

  @Autowired
  public void stopTimepointSchedulingService(
      TimepointSchedulingService timepointSchedulingService) {
    _timepointSchedulingService = timepointSchedulingService;
  }

  /*****************************************************************************
   * {@link StopSchedulingService}
   ****************************************************************************/

  public List<ScheduledArrivalTime> getPredictedArrivalsByStopId(int stopId)
      throws NoSuchStopException {

    TimingBean timing = getTiming();

    List<InterpolatedStopTime> interpolatedStops = _dao.getActiveInterpolatedStopTimesByStopAndTimeRange(
        stopId, timing.getTimeOfWeekInMinutes(), MAX_WINDOW);

    Map<InterpolatedStopTime, ScheduledArrivalTime> istToSad = getInterpolatedStopTimesAsScheduledArrivals(
        interpolatedStops, timing);

    Set<Integer> timepointIds = getTimepointIdsForInterpolatedStops(istToSad.keySet());
    Map<TimepointAndTripKey, ScheduledArrivalTime> timepointPredictions = new HashMap<TimepointAndTripKey, ScheduledArrivalTime>();

    List<ScheduledArrivalTime> arrivals = _timepointSchedulingService.getPredictedArrivalsByTimepointIds(timepointIds);

    /**
     * We sort so that when StopTimes with the same service pattern, timepoint,
     * sequence, and trip are used, the most recent is the one that is kept
     */
    Collections.sort(arrivals, SCHEDULED_ARRIVAL_TIME_SORT);

    for (ScheduledArrivalTime sat : arrivals) {

      int tripId = sat.getTrip().getId();
      StopTime st = (StopTime) sat.getStopTime();
      int timepointId = st.getTimepoint().getId();
      int sequence = st.getPatternTimepointPosition();

      TimepointAndTripKey key = new TimepointAndTripKey(timepointId, tripId,
          sequence);
      timepointPredictions.put(key, sat);
    }

    List<ScheduledArrivalTime> stopPredictions = new ArrayList<ScheduledArrivalTime>();

    long from = timing.getNow() - _timepointSchedulingService.getPreWindow()
        * 60 * 1000;
    long to = timing.getNow() + _timepointSchedulingService.getPostWindow()
        * 60 * 1000;

    for (Map.Entry<InterpolatedStopTime, ScheduledArrivalTime> entry : istToSad.entrySet()) {

      InterpolatedStopTime ist = entry.getKey();
      ScheduledArrivalTime sad = entry.getValue();

      setPredictedTime(timepointPredictions, ist.getInterpolation(), sad);

      if (from <= sad.getMaxTime() && sad.getMinTime() <= to)
        stopPredictions.add(sad);
    }

    return stopPredictions;
  }

  public List<ScheduledArrivalTime> getArrivalsByServicePatterns(
      Set<ServicePatternKey> ids) {
    return getArrivalsByServicePatterns(ids, System.currentTimeMillis());
  }

  public List<ScheduledArrivalTime> getArrivalsByServicePatterns(
      Set<ServicePatternKey> ids, long now) {

    TimingBean timing = getTiming(now);

    List<StopTimepointInterpolation> stis = _dao.getStopTimeInterpolationByServicePatterns(ids);

    List<ScheduledArrivalTime> arrivals = _timepointSchedulingService.getPredictedArrivalsByServicePatterns(
        ids, timing);
    /**
     * We sort so that when StopTimes with the same service pattern, timepoint,
     * sequence, and trip are used, the most recent is the one that is kept
     */
    Collections.sort(arrivals, SCHEDULED_ARRIVAL_TIME_SORT);

    Map<ServicePatternTimepointSequenceKey, Map<Trip, ScheduledArrivalTime>> arrivalsByKey = getArrivalsByKey(arrivals);

    long minTime = timing.getNow() - _timepointSchedulingService.getPreWindow()
        * 60 * 1000;
    long maxTime = timing.getNow()
        + _timepointSchedulingService.getPostWindow() * 60 * 1000;

    List<ScheduledArrivalTime> retro = new ArrayList<ScheduledArrivalTime>();

    for (StopTimepointInterpolation sti : stis) {

      Pair<ServicePatternTimepointSequenceKey> pair = getStopTimeInterpolationKeys(sti);
      ServicePatternTimepointSequenceKey fromKey = pair.getFirst();
      ServicePatternTimepointSequenceKey toKey = pair.getSecond();

      Map<Trip, ScheduledArrivalTime> from = arrivalsByKey.get(fromKey);
      Map<Trip, ScheduledArrivalTime> to = arrivalsByKey.get(toKey);

      if (from == null || to == null)
        continue;

      Set<Trip> common = new HashSet<Trip>(from.keySet());
      common.retainAll(to.keySet());

      for (Trip trip : common) {

        ScheduledArrivalTime satFrom = from.get(trip);
        ScheduledArrivalTime satTo = to.get(trip);

        ICommonStopTime stFrom = satFrom.getStopTime();
        ICommonStopTime stTo = satFrom.getStopTime();

        double t1 = stFrom.getPassingTime();
        double t2 = stTo.getPassingTime();

        double passingTime = (t1 + (t2 - t1) * sti.getRatio());
        long scheduledTime = (long) (satFrom.getScheduledTime() + (satTo.getScheduledTime() - satFrom.getScheduledTime())
            * sti.getRatio());

        InterpolatedStopTime ist = new InterpolatedStopTime(sti, trip,
            passingTime);
        ScheduledArrivalTime sat = new ScheduledArrivalTime(ist, scheduledTime);

        setPredictedArrivalTime(sti, sat, satFrom, satTo);

        if (minTime <= sat.getMaxTime() && sat.getMinTime() <= maxTime)
          retro.add(sat);
      }
    }

    return retro;
  }

  public List<InterpolatedStopTime> getInterpolatedStopTimes(
      List<StopTimepointInterpolation> stis, List<StopTime> stopTimes) {

    List<InterpolatedStopTime> results = new ArrayList<InterpolatedStopTime>();

    Map<ServicePatternTimepointSequenceKey, Map<Trip, StopTime>> stopTimesByKey = getStopTimesByKey(stopTimes);

    for (StopTimepointInterpolation sti : stis) {
      Pair<ServicePatternTimepointSequenceKey> pair = getStopTimeInterpolationKeys(sti);
      ServicePatternTimepointSequenceKey fromKey = pair.getFirst();
      ServicePatternTimepointSequenceKey toKey = pair.getSecond();

      Map<Trip, StopTime> from = stopTimesByKey.get(fromKey);
      Map<Trip, StopTime> to = stopTimesByKey.get(toKey);

      if (from == null || to == null)
        continue;

      Set<Trip> common = new HashSet<Trip>(from.keySet());
      common.retainAll(to.keySet());

      for (Trip trip : common) {

        StopTime stFrom = from.get(trip);
        StopTime stTo = to.get(trip);

        double t1 = stFrom.getPassingTime();
        double t2 = stTo.getPassingTime();
        double passingTime = (t1 + (t2 - t1) * sti.getRatio());
        InterpolatedStopTime ist = new InterpolatedStopTime(sti, trip,
            passingTime);
        results.add(ist);
      }
    }

    return results;
  }

  public ScheduledArrivalTime getScheduledArrivalTime(ServicePattern pattern,
      Trip trip, StopLocation stop, int stopIndex, long target) {
    InterpolatedStopTime ist = _dao.getInterpolatedStopTime(trip, pattern,
        stop, stopIndex);
    TimingBean timing = getTiming(target);
    return getStopTimeAsScheduledArrivalTime(ist, timing);
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private Set<Integer> getTimepointIdsForInterpolatedStops(
      Set<InterpolatedStopTime> ists) {
    Set<Integer> timepointIds = new HashSet<Integer>();

    for (InterpolatedStopTime ist : ists) {
      StopTimepointInterpolation interpolation = ist.getInterpolation();
      timepointIds.add(interpolation.getFromTimepoint().getId());
      timepointIds.add(interpolation.getToTimepoint().getId());
    }
    return timepointIds;
  }

  private Map<InterpolatedStopTime, ScheduledArrivalTime> getInterpolatedStopTimesAsScheduledArrivals(
      List<InterpolatedStopTime> interpolatedStops, TimingBean timing) {

    Map<InterpolatedStopTime, ScheduledArrivalTime> istToSad = new HashMap<InterpolatedStopTime, ScheduledArrivalTime>();

    for (InterpolatedStopTime ist : interpolatedStops) {

      ScheduledArrivalTime sat = getStopTimeAsScheduledArrivalTime(ist, timing);
      if (sat != null)
        istToSad.put(ist, sat);
    }

    return istToSad;
  }

  private void setPredictedTime(
      Map<TimepointAndTripKey, ScheduledArrivalTime> predictions,
      StopTimepointInterpolation sti, ScheduledArrivalTime sad) {

    int fromTimepointId = sti.getFromTimepoint().getId();
    int fromTimepointSequence = sti.getFromTimepointSequence();
    int toTimepointId = sti.getToTimepoint().getId();
    int toTimepointSequence = sti.getToTimepointSequence();
    int tripId = sad.getStopTime().getTrip().getId();

    ScheduledArrivalTime padFrom = predictions.get(new TimepointAndTripKey(
        fromTimepointId, tripId, fromTimepointSequence));

    ScheduledArrivalTime padTo = predictions.get(new TimepointAndTripKey(
        toTimepointId, tripId, toTimepointSequence));

    setPredictedArrivalTime(sti, sad, padFrom, padTo);
  }

  private void setPredictedArrivalTime(StopTimepointInterpolation sti,
      ScheduledArrivalTime sat, ScheduledArrivalTime satFrom,
      ScheduledArrivalTime satTo) {

    long predicted = -1;

    if (satFrom != null && !satFrom.hasPredictedTime())
      satFrom = null;

    if (satTo != null && !satTo.hasPredictedTime())
      satTo = null;

    if (satFrom != null && satTo != null) {
      predicted = (long) (satFrom.getPredictedTime() + sti.getRatio()
          * (satTo.getPredictedTime() - satFrom.getPredictedTime()));
    } else if (satFrom != null) {

      predicted = sat.getScheduledTime()
          + (satFrom.getPredictedTime() - satFrom.getScheduledTime());
    } else if (satTo != null) {
      predicted = sat.getScheduledTime()
          + (satTo.getPredictedTime() - satTo.getScheduledTime());
    }

    if (predicted != -1)
      sat.setPredictedTime(predicted);
  }

  private Map<ServicePatternTimepointSequenceKey, Map<Trip, StopTime>> getStopTimesByKey(
      List<StopTime> arrivals) {
    return getArrivalsByKey(arrivals,
        AdapterLibrary.getIdentityAdapter(StopTime.class));
  }

  private Map<ServicePatternTimepointSequenceKey, Map<Trip, ScheduledArrivalTime>> getArrivalsByKey(
      List<ScheduledArrivalTime> arrivals) {
    return getArrivalsByKey(arrivals,
        new IAdapter<ScheduledArrivalTime, StopTime>() {
          public StopTime adapt(ScheduledArrivalTime source) {
            return (StopTime) source.getStopTime();
          }
        });
  }

  private <T> Map<ServicePatternTimepointSequenceKey, Map<Trip, T>> getArrivalsByKey(
      List<T> arrivals, IAdapter<T, StopTime> adapter) {

    Map<ServicePatternTimepointSequenceKey, Map<Trip, T>> m = new HashMap<ServicePatternTimepointSequenceKey, Map<Trip, T>>();

    for (T sat : arrivals) {

      StopTime st = adapter.adapt(sat);
      ServicePatternKey spKey = st.getServicePattern().getId();
      int timepointId = st.getTimepoint().getId();
      int sequence = st.getPatternTimepointPosition();

      ServicePatternTimepointSequenceKey key = new ServicePatternTimepointSequenceKey(
          spKey, timepointId, sequence);
      Map<Trip, T> m2 = m.get(key);
      if (m2 == null) {
        m2 = new HashMap<Trip, T>();
        m.put(key, m2);
      }
      m2.put(st.getTrip(), sat);
    }

    return m;
  }

  private Pair<ServicePatternTimepointSequenceKey> getStopTimeInterpolationKeys(
      StopTimepointInterpolation sti) {
    ServicePatternKey spKey = sti.getServicePattern().getId();

    int fromTimepointId = sti.getFromTimepoint().getId();
    int fromTimepointSequence = sti.getFromTimepointSequence();
    ServicePatternTimepointSequenceKey fromKey = new ServicePatternTimepointSequenceKey(
        spKey, fromTimepointId, fromTimepointSequence);

    int toTimepointId = sti.getToTimepoint().getId();
    int toTimepointSequence = sti.getToTimepointSequence();
    ServicePatternTimepointSequenceKey toKey = new ServicePatternTimepointSequenceKey(
        spKey, toTimepointId, toTimepointSequence);

    return Pair.createPair(fromKey, toKey);
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private class TimepointAndTripKey {

    private int _timepointId;

    private int _tripId;

    private int _sequence;

    public TimepointAndTripKey(int timepointId, int tripId, int sequence) {
      _timepointId = timepointId;
      _tripId = tripId;
      _sequence = sequence;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof TimepointAndTripKey))
        return false;
      TimepointAndTripKey key = (TimepointAndTripKey) obj;
      return _tripId == key._tripId && _timepointId == key._timepointId
          && _sequence == key._sequence;
    }

    @Override
    public int hashCode() {
      return _timepointId + _tripId * 3 + _sequence * 5;
    }
  }

  private static class ScheduledArrivalTimeSort implements
      Comparator<ScheduledArrivalTime> {

    public int compare(ScheduledArrivalTime o1, ScheduledArrivalTime o2) {
      ServicePatternKey keyA = o1.getServicePattern().getId();
      ServicePatternKey keyB = o2.getServicePattern().getId();
      int rc = keyA.compareTo(keyB);
      if (rc != 0)
        return rc;
      rc = o1.getTrip().compareTo(o2.getTrip());
      if (rc != 0)
        return rc;
      double t1 = o1.getStopTime().getPassingTime();
      double t2 = o2.getStopTime().getPassingTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }
}
