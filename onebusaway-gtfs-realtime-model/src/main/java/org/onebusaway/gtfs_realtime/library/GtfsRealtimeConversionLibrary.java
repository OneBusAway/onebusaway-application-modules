/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.library;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.gtfs_realtime.model.AlertModel;
import org.onebusaway.gtfs_realtime.model.TripUpdateModel;
import org.onebusaway.gtfs_realtime.model.VehiclePositionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;


/**
 * Methods for converting GTFS-RT to models for serialization
 *
 */
public class GtfsRealtimeConversionLibrary {
  
  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeConversionLibrary.class);
  
  public static List<TripUpdateModel> readTripUpdates(FeedMessage tripUpdates) {
    return new TripUpdateConvertor().readAll(tripUpdates);
  }
  
  public static List<VehiclePositionModel> readVehiclePositions(FeedMessage vehiclePositions) {
    return new VehiclePositionConvertor().readAll(vehiclePositions);
  }
  
  public static List<AlertModel> readAlerts(FeedMessage alerts) {
    return new AlertConvertor().readAll(alerts);
  }
  
  /**
   * Combine startDate and startTime into a single combined Date.
   * 
   * @param startDate a String with format of "YYYYMMDD"
   * @param startTime a String with format of "HH:MM:SS"
   * @return a Date representing the combined date and time
   */
  public static Date parseDate(String startDate, String startTime) {
    Calendar combinedDateTime = null;
    if (StringUtils.isNotBlank(startDate)
        || StringUtils.isNotBlank(startTime)) {
      try {
        combinedDateTime = Calendar.getInstance();
        int year = Integer.parseInt(startDate.substring(0, 4));
        int month = Integer.parseInt(startDate.substring(4, 6)) - 1;
        int day = Integer.parseInt(startDate.substring(6));
        int hourOfDay = Integer.parseInt(startTime.substring(0, 2));
        int minute = Integer.parseInt(startTime.substring(3, 5));
        int second = Integer.parseInt(startTime.substring(6));
        combinedDateTime.set(year, month, day, hourOfDay, minute, second);
      } catch (Exception e) {
    	  _log.error("Error parsing date " + startDate + " and time " 
           + startTime);
      }
    }
    return combinedDateTime != null ? combinedDateTime.getTime() : null;
  }

  

}
