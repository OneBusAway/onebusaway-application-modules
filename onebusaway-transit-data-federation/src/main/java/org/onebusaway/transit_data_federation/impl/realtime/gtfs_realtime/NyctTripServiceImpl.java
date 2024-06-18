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
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.AddedTripInfo.getStartOfDay;

/**
 * Support for MTA NYCT custom extensions.
 *
 * this logic borrowed from Kurt's NycTripId:
 * https://github.com/camsys/nyct-rt-proxy/blob/master/src/main/java/com/kurtraschke/nyctrtproxy/model/NyctTripId.java#L85
 */

public class NyctTripServiceImpl implements NyctTripService {

  private static Logger _log = LoggerFactory.getLogger(NyctTripServiceImpl.class);

  private static final String DEFAULT_AGENCY_ID = "MTASBWY";
  private String _defaultAgencyId = DEFAULT_AGENCY_ID;

  private static final Pattern _rtTripPattern = Pattern.compile(
          "([A-Z0-9]+_)?(?<originDepartureTime>[0-9-]{6})_?(?<route>[A-Z0-9]+)\\.+(?<direction>[NS]?)(?<network>[A-Z0-9 -]*)$");


  public AddedTripInfo parse(GtfsRealtime.TripUpdate tu, GtfsRealtimeNYCT.NyctTripDescriptor nyctTripDescriptor, long currentTime) {
    String pathId, routeId, directionId, networkId;
    int originDepartureTime;
    String tripId = tu.getTrip().getTripId();
    Matcher matcher = _rtTripPattern.matcher(tripId);

    if (matcher.find()) {
      originDepartureTime = Integer.parseInt(matcher.group("originDepartureTime"), 10) * 60 /100;
      pathId = StringUtils.rightPad(matcher.group("route"), 3, '.') + matcher.group("direction");
      routeId = routeFromTripUpdate(tu);
      if (routeId == null) {
        routeId = matcher.group("route");
      }
      directionId = matcher.group("direction");
      if (directionId.length() == 0)
        directionId = null;
      networkId = matcher.group("network");
      if (networkId.length() == 0)
        networkId = null;
      AddedTripInfo addedTrip = new AddedTripInfo();
      addedTrip.setScheduleRelationshipValue(TransitDataConstants.STATUS_ADDED);
      addedTrip.setAgencyId(getDefaultAgency());
      addedTrip.setTripStartTime(originDepartureTime); // 100ths
      addedTrip.setServiceDate(parseTimeFromUpdate(tu, currentTime));
      addedTrip.setRouteId(routeId);
      addedTrip.setTripId(tripId);
      addedTrip.setDirectionId(directionId);
      if (nyctTripDescriptor.hasTrainId()) {
        addedTrip.setVehicleId(nyctTripDescriptor.getTrainId());
      }
      int index = -1;
      for (GtfsRealtime.TripUpdate.StopTimeUpdate stopTimeUpdate : tu.getStopTimeUpdateList()) {
        index++;
        AddedStopInfo stopInfo = new AddedStopInfo();
        if (stopTimeUpdate.hasStopId()) {
          stopInfo.setStopId(stopTimeUpdate.getStopId());
        }
        if (stopTimeUpdate.hasArrival() && stopTimeUpdate.getArrival().getTime() > 0) {
          // here we assume time not delay
          stopInfo.setArrivalTime(stopTimeUpdate.getArrival().getTime()*1000);
          if (index == tu.getStopTimeUpdateCount()-1) {
            // default the departure to the arrival if the last stop on the trip
            stopInfo.setDepartureTime(stopTimeUpdate.getArrival().getTime()*1000);
          }
        }
        if (stopTimeUpdate.hasDeparture() && stopTimeUpdate.getDeparture().getTime() > 0) {
          // here we assume time not delay
          stopInfo.setDepartureTime(stopTimeUpdate.getDeparture().getTime()*1000);
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
        if (stopInfo.getArrivalTime() <= 0 && stopInfo.getDepartureTime() <= 0) {
          _log.error("invalid stop for update {}", stopTimeUpdate);
        } else {
          addedTrip.addStopTime(stopInfo);
        }
      }

      return addedTrip;
    }
      return null;
  }

  private long parseTimeFromUpdate(GtfsRealtime.TripUpdate tu, long currentTime) {
    String startDate = tu.getTrip().getStartDate();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    try {
      return sdf.parse(startDate).getTime();
    } catch (ParseException e) {
      _log.error("invalid date {}", startDate);
      return getStartOfDay(new Date(currentTime)).getTime();
    }
  }

  private String routeFromTripUpdate(GtfsRealtime.TripUpdate tu) {
    if (tu.hasTrip()) {
      if (tu.getTrip().hasRouteId()) {
        return tu.getTrip().getRouteId();
      }
    }
    return null;
  }

  private String getDefaultAgency() {
    return _defaultAgencyId;
  }

  public void setDefaultAgencyId(String defaultAgencyId) {
    this._defaultAgencyId = defaultAgencyId;
  }
}
