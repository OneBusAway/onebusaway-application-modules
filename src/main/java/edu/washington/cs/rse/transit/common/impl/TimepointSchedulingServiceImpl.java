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

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.text.TextLibrary;
import edu.washington.cs.rse.transit.common.NameNormalizationStrategy;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.ServicePatternKey;
import edu.washington.cs.rse.transit.common.model.StopTime;
import edu.washington.cs.rse.transit.common.model.Timepoint;
import edu.washington.cs.rse.transit.common.model.aggregate.BusArrivalEstimateBean;
import edu.washington.cs.rse.transit.common.model.aggregate.ScheduledArrivalTime;
import edu.washington.cs.rse.transit.common.services.MyBusService;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;
import edu.washington.cs.rse.transit.common.services.TimepointSchedulingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TimepointSchedulingServiceImpl extends AbstractSchedulingService implements TimepointSchedulingService {

    private static Logger _log = Logger.getLogger(TimepointSchedulingServiceImpl.class.getName());

    /***************************************************************************
     * Private Members
     **************************************************************************/

    private NameNormalizationStrategy _nameNormalization;

    private MyBusService _mybus;

    private long _timeOffset = 0;

    private boolean _hasCheckedTimeOffset = false;

    @Autowired
    public void setNameNormalizationStrategy(NameNormalizationStrategy strategy) {
        _nameNormalization = strategy;
    }

    @Autowired
    public void setMyBusService(MyBusService mybus) {
        _mybus = mybus;
    }

    /***************************************************************************
     * {@link TimepointSchedulingService} Interface
     **************************************************************************/

    public int getPreWindow() {
        return _mybus.getMyBusWindowPreInMinutes();
    }

    public int getPostWindow() {
        return _mybus.getMyBusWindowPostInMinutes();
    }

    /***************************************************************************
     * 
     **************************************************************************/

    public List<ScheduledArrivalTime> getPredictedArrivalsByTimepointId(int timepointId) throws NoSuchStopException {
        return getPredictedArrivalsByTimepointId(timepointId, System.currentTimeMillis());
    }

    public List<ScheduledArrivalTime> getPredictedArrivalsByTimepointId(int timepointId, long now)
            throws NoSuchStopException {

        checkMetroTimeOffset();

        TimingBean timing = getTiming(now);

        List<StopTime> interpolatedStops = _dao.getActiveStopTimesByTimepointAndTimeRange(timepointId, timing
                .getTimeOfWeekInMinutes(), MAX_WINDOW);
        return getPredictedArrivals(timepointId, interpolatedStops, timing);
    }

    public List<ScheduledArrivalTime> getPredictedArrivalsByTimepointIds(Set<Integer> ids) {
        return getPredictedArrivalsByTimepointIds(ids, System.currentTimeMillis());
    }

    /***************************************************************************
     * 
     **************************************************************************/

    public List<ScheduledArrivalTime> getPredictedArrivalsByTimepointIds(Set<Integer> ids, long time) {
        checkMetroTimeOffset();

        TimingBean timing = getTiming(time);

        Map<Timepoint, Set<StopTime>> interpolatedStops = _dao.getActiveStopTimesByTimepointsAndTimeRange(ids, timing
                .getTimeOfWeekInMinutes(), MAX_WINDOW);

        List<ScheduledArrivalTime> arrivals = new ArrayList<ScheduledArrivalTime>();

        for (Map.Entry<Timepoint, Set<StopTime>> entry : interpolatedStops.entrySet()) {
            Timepoint timepoint = entry.getKey();
            Set<StopTime> stopTimes = entry.getValue();
            List<ScheduledArrivalTime> pats = getPredictedArrivals(timepoint.getId(), stopTimes, timing);
            arrivals.addAll(pats);
        }

        return arrivals;
    }

    /***************************************************************************
     * 
     **************************************************************************/

    public List<ScheduledArrivalTime> getPredictedArrivalsByServicePatterns(Set<ServicePatternKey> ids,
            TimingBean timing) {

        checkMetroTimeOffset();

        int minutes = timing.getTimeOfWeekInMinutes();

        List<StopTime> sts = _dao.getStopTimesByServicePatternsAndTimeRange(ids, minutes, MAX_WINDOW);

        Map<Timepoint, List<StopTime>> stByTimepoint = new HashMap<Timepoint, List<StopTime>>();

        for (StopTime st : sts) {
            Timepoint timepoint = st.getTimepoint();
            List<StopTime> sts2 = stByTimepoint.get(timepoint);
            if (sts2 == null) {
                sts2 = new ArrayList<StopTime>();
                stByTimepoint.put(timepoint, sts2);
            }
            sts2.add(st);
        }

        List<ScheduledArrivalTime> arrivals = new ArrayList<ScheduledArrivalTime>();

        for (Map.Entry<Timepoint, List<StopTime>> entry : stByTimepoint.entrySet()) {
            Timepoint timepoint = entry.getKey();
            List<StopTime> stopTimes = entry.getValue();

            List<ScheduledArrivalTime> pats = getPredictedArrivals(timepoint.getId(), stopTimes, timing);
            arrivals.addAll(pats);
        }

        return arrivals;
    }

    /***************************************************************************
     * Private Methods
     **************************************************************************/

    private void checkMetroTimeOffset() {

        if (_hasCheckedTimeOffset)
            return;

        _hasCheckedTimeOffset = true;

        try {
            long before = System.currentTimeMillis();
            long metro = _mybus.getMetroTime();
            long after = System.currentTimeMillis();
            _timeOffset = metro - (before + after) / 2;

            _log.info("Time Offset = " + _timeOffset);

        } catch (IOException ex) {
            _log.log(Level.WARNING, "Error query metro kc time", ex);
        }
    }

    private List<ScheduledArrivalTime> getPredictedArrivals(int timepointId, Collection<StopTime> stopTimes,
            TimingBean timing) {

        // Retreive the scheduled arrivals
        List<ScheduledArrivalTime> retro = getStopTimesAsScheduledArrivalTimes(stopTimes, timing);
        Map<RouteAndTimeKey, Set<ScheduledArrivalTime>> scheduledArrivals = getArrivalEstimatesByKey(retro);

        Map<RouteAndTimeKey, Set<BusArrivalEstimateBean>> estimates = getArrivalEstimatesByKey(timepointId, timing);

        // The RouteAndTimeKey allow us to quickly match ScheduledArrivalTime
        // objects and BusArrivalEstimateBeans
        for (RouteAndTimeKey key : scheduledArrivals.keySet()) {

            Set<ScheduledArrivalTime> sats = scheduledArrivals.get(key);
            Set<BusArrivalEstimateBean> beats = estimates.get(key);

            if (beats == null)
                continue;

            if (sats.size() == 1 && beats.size() == 1) {
                ScheduledArrivalTime sat = sats.iterator().next();
                BusArrivalEstimateBean beat = beats.iterator().next();
                if (beat.hasPredictedArrivalTime())
                    setPredictedArrivalTime(sat, beat);
                continue;
            }

            Map<BusArrivalEstimateBean, Set<ScheduledArrivalTime>> go = new FactoryMap<BusArrivalEstimateBean, Set<ScheduledArrivalTime>>(
                    new HashSet<ScheduledArrivalTime>());

            for (ScheduledArrivalTime sat : sats) {

                ServicePattern sp = sat.getServicePattern();
                String name = _nameNormalization.getNormalizedName(sp.getTimepointDestination());

                Min<BusArrivalEstimateBean> closestMatch = new Min<BusArrivalEstimateBean>();

                for (BusArrivalEstimateBean bean : beats) {
                    String dest = _nameNormalization.getNormalizedName(bean.getDestination());
                    int distance = TextLibrary.getEditDistance(dest, name);
                    closestMatch.add(distance, bean);
                }

                BusArrivalEstimateBean bean = closestMatch.getMinElement();
                go.get(bean).add(sat);
            }

            for (BusArrivalEstimateBean beat : go.keySet()) {

                Set<ScheduledArrivalTime> seats = go.get(beat);

                if (seats.size() == 1) {
                    ScheduledArrivalTime sat = seats.iterator().next();
                    if (beat.hasPredictedArrivalTime())
                        setPredictedArrivalTime(sat, beat);
                } else {

                    // If we don't have a prediction, we don't really care
                    if (!beat.hasPredictedArrivalTime())
                        continue;

                    Min<ScheduledArrivalTime> closestMatch = new Min<ScheduledArrivalTime>();
                    String dest = _nameNormalization.getNormalizedName(beat.getDestination());

                    for (ScheduledArrivalTime sat : seats) {
                        ServicePattern sp = sat.getServicePattern();
                        String name = _nameNormalization.getNormalizedName(sp.getTimepointDestination());
                        int distance = TextLibrary.getEditDistance(dest, name);
                        closestMatch.add(distance, sat);
                    }

                    for (ScheduledArrivalTime sat : closestMatch.getMinElements()) {
                        setPredictedArrivalTime(sat, beat);
                    }
                }
            }
        }

        return retro;
    }

    /**
     * Organize the scheduled arrivals {@link ScheduledArrivalTime} into sets
     * based on their route+scheduled arrival time {@link RouteAndTimeKey}.
     * 
     * @param sats
     * @return
     */
    private Map<RouteAndTimeKey, Set<ScheduledArrivalTime>> getArrivalEstimatesByKey(
            Collection<ScheduledArrivalTime> sats) {

        Map<RouteAndTimeKey, Set<ScheduledArrivalTime>> arrivals = new HashMap<RouteAndTimeKey, Set<ScheduledArrivalTime>>();

        for (ScheduledArrivalTime sat : sats) {
            RouteAndTimeKey key = new RouteAndTimeKey(sat.getRoute().getNumber(), sat.getScheduledTime());
            Set<ScheduledArrivalTime> satsByKey = arrivals.get(key);
            if (satsByKey == null) {
                satsByKey = new HashSet<ScheduledArrivalTime>();
                arrivals.put(key, satsByKey);
            }
            satsByKey.add(sat);
        }

        return arrivals;
    }

    /**
     * Retreive bus arrival estimates {@link BusArrivalEstimateBean} for the
     * specified timepoint and return them organized by route+scheduled arrival
     * time
     * 
     * @param timepointId
     * @param timing
     * @return
     */
    private Map<RouteAndTimeKey, Set<BusArrivalEstimateBean>> getArrivalEstimatesByKey(int timepointId,
            TimingBean timing) {

        try {
            List<BusArrivalEstimateBean> beans = _mybus.getSchedule(timepointId);
            return getEstimatedArrivalsByKey(beans, timing);
        } catch (NoSuchStopException ex) {
            _log.warning("No such timepoint=" + timepointId);
        } catch (Exception ex) {
            _log.log(Level.WARNING, "error retreiving predicted timepoint arrivals", ex);
        }

        return new HashMap<RouteAndTimeKey, Set<BusArrivalEstimateBean>>();
    }

    /**
     * Organize the {@link BusArrivalEstimateBean} beans into sets based on
     * their route+scheduled arrival time {@link RouteAndTimeKey}.
     * 
     * @param beans
     * @param timing
     * @return
     */
    private Map<RouteAndTimeKey, Set<BusArrivalEstimateBean>> getEstimatedArrivalsByKey(
            List<BusArrivalEstimateBean> beans, TimingBean timing) {

        Map<RouteAndTimeKey, Set<BusArrivalEstimateBean>> estimates = new HashMap<RouteAndTimeKey, Set<BusArrivalEstimateBean>>();
        for (BusArrivalEstimateBean bean : beans) {

            RouteAndTimeKey key = getArrivalEstimateAsKey(bean, timing);
            Set<BusArrivalEstimateBean> baebs = estimates.get(key);
            if (baebs == null) {
                baebs = new HashSet<BusArrivalEstimateBean>();
                estimates.put(key, baebs);
            }
            baebs.add(bean);
        }
        return estimates;
    }

    /**
     * Generate a route+scheduled arrival time key for a given
     * {@link BusArrivalEstimateBean}
     * 
     * @param bean
     * @param timing
     * @return
     */
    private RouteAndTimeKey getArrivalEstimateAsKey(BusArrivalEstimateBean bean, TimingBean timing) {
        long timeOfDay = (long) Math.round(bean.getSchedTime() / 60.0) * 60 * 1000;
        long time = getClosestTime(timing, timeOfDay);
        return new RouteAndTimeKey(bean.getRoute(), time);
    }

    private void setPredictedArrivalTime(ScheduledArrivalTime sat, BusArrivalEstimateBean eat) {
        long predicted = sat.getScheduledTime() + eat.getGoalDeviation() * 1000;
        sat.setPredictedTime(predicted);
    }

    /***************************************************************************
     * Internal Classes
     **************************************************************************/

    private static class RouteAndTimeKey {

        private int _route;

        private long _time;

        public RouteAndTimeKey(int route, long time) {
            _route = route;
            _time = time;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof RouteAndTimeKey))
                return false;
            RouteAndTimeKey key = (RouteAndTimeKey) obj;
            return _route == key._route && _time == key._time;
        }

        @Override
        public int hashCode() {
            return (int) (_route * 7 + _time);
        }
    }
}
