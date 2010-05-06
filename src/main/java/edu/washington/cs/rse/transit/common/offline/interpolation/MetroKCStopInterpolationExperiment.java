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
package edu.washington.cs.rse.transit.common.offline.interpolation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.PatternTimepoints;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.StopTime;
import edu.washington.cs.rse.transit.common.model.Trip;

public class MetroKCStopInterpolationExperiment {

    public static void main(String[] args) {
        ApplicationContext ctx = MetroKCApplicationContext.getApplicationContext();
        MetroKCStopInterpolationExperiment m = new MetroKCStopInterpolationExperiment();
        ctx.getAutowireCapableBeanFactory().autowireBean(m);
        m.run();
    }

    private MetroKCDAO _dao;

    @Autowired
    public void setMetroKCDAO(MetroKCDAO dao) {
        _dao = dao;
    }

    public void run() {

        List<ServicePattern> servicePatterns = _dao.getAllServicePatterns();
        System.out.println("servicePatterns=" + servicePatterns.size());

        for (ServicePattern servicePattern : servicePatterns) {

            System.out.println("  servicePattern=" + servicePattern.getId() + " schedPatternId="
                    + servicePattern.getSchedulePatternId());

            List<PatternTimepoints> patternTimepoints = _dao.getPatternTimepointsByServicePattern(servicePattern);

            List<Integer> timepointIds = new ArrayList<Integer>();

            for (PatternTimepoints pt : patternTimepoints)
                timepointIds.add(pt.getTimepoint().getId());

            List<StopTime> stopTimes = _dao.getStopTimesByServicePattern(servicePattern);
            Map<Trip, List<StopTime>> byTrip = new HashMap<Trip, List<StopTime>>();
            for (StopTime stopTime : stopTimes) {
                Trip trip = stopTime.getTrip();
                List<StopTime> sts = byTrip.get(trip);
                if (sts == null) {
                    sts = new ArrayList<StopTime>();
                    byTrip.put(trip, sts);
                }
                sts.add(stopTime);
            }

            boolean first = true;
            List<Integer> tids0 = null;
            List<Integer> sids0 = null;
            List<Integer> pids0 = null;

            for (Map.Entry<Trip, List<StopTime>> entry : byTrip.entrySet()) {

                Trip trip = entry.getKey();
                List<StopTime> sts = entry.getValue();

                List<Integer> tids = new ArrayList<Integer>();
                List<Integer> sids = new ArrayList<Integer>();
                List<Integer> pids = new ArrayList<Integer>();

                for (StopTime st : sts) {
                    tids.add(st.getTimepoint().getId());
                    sids.add(st.getStopTimePosition());
                    pids.add(st.getPatternTimepointPosition());
                }

                if (first) {
                    first = false;
                    tids0 = tids;
                    sids0 = sids;
                    pids0 = pids;
                } else {
                    if (!tids0.equals(tids))
                        System.out.println("  trip=" + trip.getId() + " tids");
                    if (!sids0.equals(sids))
                        System.out.println("  trip=" + trip.getId() + " sids");
                    if (!pids0.equals(pids))
                        System.out.println("  trip=" + trip.getId() + " pids");

                }

            }

        }

    }
}
