/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DuplicatedTripServiceParserImpl implements DuplicatedTripServiceParser {

    private static final Logger _log = LoggerFactory.getLogger(DuplicatedTripServiceParserImpl.class);
    private GtfsRealtimeEntitySource _entitySource;
    public void setGtfsRealtimeEntitySource(GtfsRealtimeEntitySource source) {
        _entitySource = source;
    }
    @Override
    public AddedTripInfo parse(GtfsRealtime.TripUpdate tu) {
        AddedTripInfo duplicatedTrip = new AddedTripInfo();
        List<AddedStopInfo> stopInfos = new ArrayList<>();
        String tripId = tu.getTrip().getTripId();
        // this is an existing trip that we will change the start time of
        // therefor the tripId must be known!!!
        TripEntry tripEntry = _entitySource.getTrip(tripId);
        if (tripEntry == null) {
            _log.error("duplicated trip presented an invalid trip id {}", tripId);
            return null;
        }
        duplicatedTrip.setAgencyId(tripEntry.getId().getAgencyId());
        // producers interpret the spec different ways
        // CASE I:
        // start_time: "20230510"
        // start_date: not defined
        if (!tu.getTrip().getStartTime().contains(":")) {
            ServiceDate serviceDate = new ServiceDate(new Date(parseDate(tu.getTrip().getStartTime())));
            duplicatedTrip.setTripStartTime(getTimeOfFirstStop(tu.getStopTimeUpdateList(), serviceDate));
            duplicatedTrip.setServiceDate(serviceDate.getAsDate().getTime());
        } else {
            // CASE II:
            // start_time: "HH:MM:SS"
            // start_date: "YYmmDD"
            throw new UnsupportedOperationException("not implemented yet");
        }
        duplicatedTrip.setTripId(tripId);
        duplicatedTrip.setRouteId(tripEntry.getRoute().getId().getId());
        duplicatedTrip.setDirectionId(tripEntry.getDirectionId());
        duplicatedTrip.setShapeId(tripEntry.getShapeId());

        for(StopTimeEntry stopTimeEntry : tripEntry.getStopTimes() ){
            AddedStopInfo stopInfo = new AddedStopInfo();
            stopInfo.setStopId(String.valueOf(stopTimeEntry.getId()));
            stopInfo.setArrivalTime(stopTimeEntry.getArrivalTime());
            stopInfo.setDepartureTime(stopTimeEntry.getDepartureTime());
            stopInfos.add(stopInfo);
        }
        duplicatedTrip.setStops(stopInfos);
    return duplicatedTrip;
    }

    private int getTimeOfFirstStop(List<GtfsRealtime.TripUpdate.StopTimeUpdate> stopTimeUpdateList, ServiceDate serviceDate) {
        if (stopTimeUpdateList == null || stopTimeUpdateList.isEmpty())
        return -1;
        GtfsRealtime.TripUpdate.StopTimeUpdate update = stopTimeUpdateList.get(0);
        if (update.hasArrival()) {
            if (update.getArrival().hasTime()) {
                return Math.toIntExact(update.getArrival().getTime() * 1000 - serviceDate.getAsDate().getTime());
            }
        }
        if (update.hasDeparture()) {
            if (update.getDeparture().hasTime()) {
                return Math.toIntExact(update.getDeparture().getTime() * 1000 - serviceDate.getAsDate().getTime());
            }
        }
        throw new UnsupportedOperationException("received an update in an unexpected format " + update);
    }

    private long parseDate(String startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            Date date = sdf.parse(startTime);
            return date.getTime();
        } catch (ParseException e) {
            _log.error("unexpected date format for start_time {}", startTime);
            return -1;
        }
    }
}
