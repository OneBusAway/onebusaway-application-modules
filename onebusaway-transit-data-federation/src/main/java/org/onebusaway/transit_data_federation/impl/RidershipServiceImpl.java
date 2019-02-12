/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data.OccupancyStatusBean;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.RidershipService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manage HistoricalRidership Bundle Operations.
 */
@Component
public class RidershipServiceImpl implements RidershipService {
    private static Logger _log = LoggerFactory.getLogger(RidershipServiceImpl.class);

    private FederatedTransitDataBundle _bundle;
    private List<HistoricalRidership> _riderships;
    private Map<AgencyAndId, List<HistoricalRidership>> _tripRiderships;
    private Map<AgencyAndId, List<HistoricalRidership>> _routeRiderships;
    private Map<AgencyAndId, List<HistoricalRidership>> _stopRiderships;
    private Map<String, List<HistoricalRidership>> _tuppleRiderships;

    @Autowired
    public void setBundle(FederatedTransitDataBundle bundle) { _bundle = bundle; }

    @PostConstruct
    @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
    public void setup() throws IOException, ClassNotFoundException {
        File path = _bundle.getHistoricalRidershipPath();
        if (path.exists()) {
            _log.info("loading historical riderships...");
            _riderships = ObjectSerializationLibrary.readObject(path);
            _log.info("loading historical riderships...done");
        } else {
            _log.info("historical riderships not found at " + path);
            _riderships = new ArrayList<>();
        }
        _tripRiderships = new HashMap<>();
        _routeRiderships = new HashMap<>();
        _stopRiderships = new HashMap<>();
        _tuppleRiderships = new HashMap<>();

        _log.info("generating historical ridership indexes");
        createIndexes();
        _log.info("complete with " + _riderships.size() + " entries");
    }

    private void createIndexes() {
        for (HistoricalRidership hr : _riderships) {
            AgencyAndId routeId = hr.getRouteId();
            AgencyAndId tripId = hr.getTripId();
            AgencyAndId stopId = hr.getStopId();
            String tupple = tupple = hash(routeId, tripId, stopId);
            if (_routeRiderships.containsKey(routeId)) {
                _routeRiderships.get(routeId).add(hr);
            } else {
                _routeRiderships.put(routeId, createList(hr));
            }

            if (_tripRiderships.containsKey(tripId)) {
                _tripRiderships.get(tripId).add(hr);
            } else {
                _tripRiderships.put(tripId, createList(hr));
            }
            if (_stopRiderships.containsKey(stopId)) {
                _stopRiderships.get(stopId).add(hr);
            } else {
                _stopRiderships.put(stopId, createList(hr));
            }
            if (tupple != null) {
                if ( _tuppleRiderships.containsKey(tupple)) {
                    _tuppleRiderships.get(tupple).add(hr);
                } else {
                    _tuppleRiderships.put(tupple, createList(hr));
                }
            }
        }
    }

    private List<HistoricalRidership> createList(HistoricalRidership hr) {
        List<HistoricalRidership> list = new ArrayList<>();
        list.add(hr);
        return list;
    }

    private String hash(AgencyAndId a1, AgencyAndId a2, AgencyAndId a3) {
        if (a1 != null && a2 != null && a3 != null)
            return a1.toString() + "." + a2.toString() + "." + a3.toString();
        return null;
    }
    @Override
    public List<HistoricalRidership> getAllHistoricalRiderships(long serviceDate) {
        return filterByServiceDate(_riderships, serviceDate);
    }

    @Override
    public List<HistoricalRidership> getHistoricalRidershipsForTrip(AgencyAndId tripId, long serviceDate) {
        return filterByServiceDate(_tripRiderships.get(tripId), serviceDate);
    }

    @Override
    public List<HistoricalRidership> getHistoricalRidershipsForStop(AgencyAndId stopId, long serviceDate) {
        return filterByServiceDate(_stopRiderships.get(stopId), serviceDate);
    }

    @Override
    public List<HistoricalRidership> getHistoricalRidershipsForRoute(AgencyAndId routeId, long serviceDate) {
        return filterByServiceDate(_routeRiderships.get(routeId), serviceDate);
    }

    @Override
    public List<HistoricalRidership> getHistoricalRiderships(AgencyAndId routeId, AgencyAndId tripId, AgencyAndId stopId, long serviceDate) {
        String hash = hash(routeId, tripId, stopId);
        if (hash == null)
            return  new ArrayList<>();
        return filterByServiceDate(_tuppleRiderships.get(hash), serviceDate);
    }
    @Override
    public List<OccupancyStatusBean> convertToOccupancyStatusBeans(List<HistoricalRidership> hrs) {
        List<OccupancyStatusBean> beans = new ArrayList<>();
        if (hrs != null) {
            for (HistoricalRidership hr : hrs) {
                OccupancyStatusBean bean = new OccupancyStatusBean();
                bean.setStopId(hr.getStopId());
                bean.setTripId(hr.getTripId());
                bean.setRouteId(hr.getRouteId());
                bean.setOccpancyStatus(OccupancyStatus.toEnum(hr.getLoadFactor()));
                beans.add(bean);
            }
        }
        return beans;
    }


    private List<HistoricalRidership> filterByServiceDate(List<HistoricalRidership> input, long sd) {
        if (sd == 0 || input == null) {
            return input;
        }
        List<HistoricalRidership> results = new ArrayList<>();
        for (HistoricalRidership hr : input) {
            if (isWeekday(sd)) {
                if(hr.getCalendarType() == HistoricalRidership.CalendarType.WEEKDAY) {
                    results.add(hr);
                }
            } else if (hr.getCalendarType() == HistoricalRidership.CalendarType.SATURDAY || hr.getCalendarType() == HistoricalRidership.CalendarType.SUNDAY) {
                results.add(hr);
            }
        }
        return results;
    }
    private boolean isWeekday(long sd) {
        Calendar cal = new ServiceDate(new Date(sd)).getAsCalendar(TimeZone.getDefault());
        return !(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
    }

}
