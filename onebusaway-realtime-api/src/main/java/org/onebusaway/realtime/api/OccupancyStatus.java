/**
 * Copyright (C) 2017 Metropolitan Transportation Authority
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
package org.onebusaway.realtime.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * this mirrors GTFS-RT's occupancyStatus:
 * https://developers.google.com/transit/gtfs-realtime/reference/OccupancyStatus-vp
 *
 * Constants proposed here:
 * https://github.com/OneBusAway/onebusaway-application-modules/issues/121
 */
public enum OccupancyStatus implements Serializable {

  /** proposed addition */
  UNKNOWN(-1),
  /**
   * The vehicle is considered empty by most measures, has few or no passengers
   * onboard, and is accepting passengers.
   */
  EMPTY(0),
  /**
   * The vehicle has a large percentage of seats available. What percentage of
   * free seats out of the total seats available is large enough to fall into
   * this category is determined by the producer.
   */
  MANY_SEATS_AVAILABLE(1),
  /**
   * The vehicle has a small percentage of seats available. What percentage
   * of free seats out of the total seats available is small enough to fall
   * into this category is determined by the producer.
   */
  FEW_SEATS_AVAILABLE(2),
  /**
   * The vehicle can accommodate only standing passengers.
   */
  STANDING_ROOM_ONLY(3),
  /**
   * The vehicle can accommodate only standing passengers but has limited
   * space for them.
   */
  CRUSHED_STANDING_ROOM_ONLY(4),
  /**
   * The vehicle is considered full by most measures but may still be allowing
   * passengers to board.
   */
  FULL(5),
  /**
   * The vehicle is not accepting passengers.
   */
  NOT_ACCEPTING_PASSENGERS(6);

  private static Logger _log = LoggerFactory.getLogger(OccupancyStatus.class);
  private int _status;
  OccupancyStatus() {_status = -1; }
  OccupancyStatus(int status) { _status = status; }
  public int valueOf() { return _status; }
  public static boolean contains(String status) {
    for (OccupancyStatus OccupancyStatus:values()) {
      if (OccupancyStatus.name().equalsIgnoreCase(status)) {
        return true;
      }
    }
    return false;
  }
  public String toString() {
    return toCamelCase(this.name());
  }

  public static OccupancyStatus toEnum(int status) {
    if (status == UNKNOWN.valueOf() || status < 0)
      return UNKNOWN;
    if (status == EMPTY.valueOf())
      return EMPTY;
    if (status == MANY_SEATS_AVAILABLE.valueOf())
      return MANY_SEATS_AVAILABLE;
    if (status == FEW_SEATS_AVAILABLE.valueOf())
      return FEW_SEATS_AVAILABLE;
    if (status == STANDING_ROOM_ONLY.valueOf())
      return STANDING_ROOM_ONLY;
    if (status == CRUSHED_STANDING_ROOM_ONLY.valueOf())
      return CRUSHED_STANDING_ROOM_ONLY;
    if (status == FULL.valueOf())
      return FULL;
    if (status == NOT_ACCEPTING_PASSENGERS.valueOf()) {
      _log.warn("Occupancy Status set to NotAcceptingPassengers");
      return NOT_ACCEPTING_PASSENGERS;
    }
    throw new IllegalArgumentException("unexpected value " + status);
  }

  public static OccupancyStatus toEnum(double rid) {
    int status;
    if(rid < 0.0) {
      status = -1;
    } else if (rid == 0.0) {
      status = 0;
    }else if(rid <= 25.0) {
      status = 1;
    } else if(rid <= 50.0) {
      status = 2;
    } else if(rid <= 75.0) {
      status = 3;
    } else if(rid <= 90.0) {
      status = 4;
    } else if(rid <= 100.0) {
      status = 5;
    } else {
      status = 6;
    }
    return OccupancyStatus.toEnum(status);
  }

  private String toCamelCase(String upperCase) {
    if (upperCase == null || upperCase.length() == 0) return upperCase;
    String[] parts = upperCase.split("_");
    StringBuffer camelCase = new StringBuffer();
    for (String part : parts) {
      camelCase.append(part.substring(0,1).toUpperCase());
      camelCase.append(part.substring(1).toLowerCase());
    }
    String result = camelCase.substring(0,1).toLowerCase() + camelCase.substring(1);
    return result;
  }
}