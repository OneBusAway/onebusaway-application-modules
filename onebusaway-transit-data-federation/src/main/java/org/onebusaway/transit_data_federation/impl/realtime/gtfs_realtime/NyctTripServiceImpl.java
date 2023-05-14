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
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support for MTA NYCT custom extensions.
 *
 * this logic borrowed from Kurt's NycTripId:
 * https://github.com/camsys/nyct-rt-proxy/blob/master/src/main/java/com/kurtraschke/nyctrtproxy/model/NyctTripId.java#L85
 */

public class NyctTripServiceImpl implements NyctTripService {

  private static final Pattern _rtTripPattern = Pattern.compile(
          "([A-Z0-9]+_)?(?<originDepartureTime>[0-9-]{6})_?(?<route>[A-Z0-9]+)\\.+(?<direction>[NS]?)(?<network>[A-Z0-9 -]*)$");


  public AddedTripInfo parse(GtfsRealtime.TripUpdate tu, GtfsRealtimeNYCT.NyctTripDescriptor nyctTripDescriptor) {
    String pathId, routeId, directionId, networkId;
    int originDepartureTime;
    String tripId = tu.getTrip().getTripId();
    Matcher matcher = _rtTripPattern.matcher(tripId);

    if (matcher.find()) {
      originDepartureTime = Integer.parseInt(matcher.group("originDepartureTime"), 10);
      pathId = StringUtils.rightPad(matcher.group("route"), 3, '.') + matcher.group("direction");
      routeId = matcher.group("route");
      directionId = matcher.group("direction");
      if (directionId.length() == 0)
        directionId = null;
      networkId = matcher.group("network");
      if (networkId.length() == 0)
        networkId = null;
      AddedTripInfo addedTrip = new AddedTripInfo();
      addedTrip.setTripStartTime(originDepartureTime);
      addedTrip.setRouteId(routeId);
      addedTrip.setTripId(tripId);
      addedTrip.setDirectionId(directionId);
      for (GtfsRealtime.TripUpdate.StopTimeUpdate stopTimeUpdate : tu.getStopTimeUpdateList()) {
        AddedStopInfo stopInfo = new AddedStopInfo();
        if (stopTimeUpdate.hasStopId()) {
          stopInfo.setStopId(stopTimeUpdate.getStopId());
        }
        if (stopTimeUpdate.hasArrival()) {
          // here we assume time not delay
          stopInfo.setArrivalTime(stopTimeUpdate.getArrival().getTime());
        }
        if (stopTimeUpdate.hasDeparture()) {
          // here we assume time not delay
          stopInfo.setDepartureTime(stopTimeUpdate.getDeparture().getTime());
        }
        if (stopTimeUpdate.hasExtension(GtfsRealtimeNYCT.nyctStopTimeUpdate)) {
          GtfsRealtimeNYCT.NyctStopTimeUpdate stopTimeUpdateExtension = stopTimeUpdate.getExtension(GtfsRealtimeNYCT.nyctStopTimeUpdate);
          if (stopTimeUpdateExtension.hasActualTrack()) {
            stopInfo.setActualTrack(stopTimeUpdateExtension.getActualTrack());
          }
          if (stopTimeUpdateExtension.hasScheduledTrack()) {
            stopInfo.setScheduledTrack(stopTimeUpdateExtension.getScheduledTrack());
          }
        }
        addedTrip.addStopTime(stopInfo);
      }

      return addedTrip;
    }
      return null;
  }
}
