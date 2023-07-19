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
import com.google.transit.realtime.GtfsRealtimeNYCT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AddedTripInfo.getStartOfDay;

/**
 * Implementation of GTFS-RT added trip support.
 */
public class AddedTripServiceImpl implements AddedTripService {

  private static final Logger _log = LoggerFactory.getLogger(AddedTripServiceImpl.class);
  private NyctTripService nycService = new NyctTripServiceImpl();
  @Override
  public AddedTripInfo handleNyctDescriptor(GtfsRealtime.TripUpdate tu, GtfsRealtimeNYCT.NyctTripDescriptor nyctTripDescriptor,
                                            long currentTime) {
    AddedTripInfo info = nycService.parse(tu,nyctTripDescriptor, currentTime);
    return info;
  }

  @Override
  public AddedTripInfo handleAddedDescriptor(String agencyId, GtfsRealtime.TripUpdate tu, long currentTime) {
    AddedTripInfo addedTrip = new AddedTripInfo();
    if (!tu.hasTrip()) return null;
    GtfsRealtime.TripDescriptor trip = tu.getTrip();

    addedTrip.setAgencyId(agencyId);
    addedTrip.setTripStartTime(parseTripStartTime(trip.getStartTime()));
    addedTrip.setServiceDate(getStartOfDay(new Date(currentTime)).getTime());
    addedTrip.setScheduleRelationshipValue(trip.getScheduleRelationship().toString());
    addedTrip.setRouteId(trip.getRouteId());
    addedTrip.setDirectionId(String.valueOf(trip.getDirectionId()));
    for (GtfsRealtime.TripUpdate.StopTimeUpdate stopTimeUpdate : tu.getStopTimeUpdateList()) {
      AddedStopInfo stopInfo = new AddedStopInfo();
      if (stopTimeUpdate.hasStopId()) {
        stopInfo.setStopId(stopTimeUpdate.getStopId());
      }
      if (stopTimeUpdate.hasArrival() && stopTimeUpdate.getArrival().getTime() > 0) {
        stopInfo.setArrivalTime(stopTimeUpdate.getArrival().getTime()*1000);
      }
      if (stopTimeUpdate.hasDeparture() && stopTimeUpdate.getDeparture().getTime() > 0) {
        stopInfo.setDepartureTime(stopTimeUpdate.getDeparture().getTime()*1000);
      }
      if (stopInfo.getArrivalTime() > 0 || stopInfo.getDepartureTime() > 0) {
        addedTrip.addStopTime(stopInfo);
      }
    }

    return addedTrip;
  }

  private int parseTripStartTime(String startTime) {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    Date date = null;
    try {
      date = sdf.parse(startTime);
    } catch (ParseException e) {
      return -1;
    }
    return Math.toIntExact(date.getTime() / 1000);
  }

}
