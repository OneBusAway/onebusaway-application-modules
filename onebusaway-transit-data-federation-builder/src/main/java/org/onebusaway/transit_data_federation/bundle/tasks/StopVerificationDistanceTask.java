/**
 * Copyright (C) 2012 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.util.List;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.util.AgencyAndIdLibrary;

/**
 * Check stop consolidate file for distance between stops, and output stops that
 * are above STOP_DISTANCE_THRESHOLD.
 * 
 *  Note that this require running the LoadGTFS task with stop consolidation turned
 *  off so the consolidated stops will be available.
 *
 */
public class StopVerificationDistanceTask extends AbstractStopTask implements
    Runnable {
  private static final double STOP_DISTANCE_THRESHOLD = 100.0; // in meters

  protected void insertHeader() {
    _logger.header("stop_verification_distance.csv",
        "root_stop_id,consolidated_stop_id,stop_distance,root_lat_lon,consolidated_lat_lon");
  }

  protected void verifyStops(String rootStopId, List<String> consolidatedStops) {
    AgencyAndId agencyAndId = AgencyAndIdLibrary.convertFromString(rootStopId);
    Stop expectedStop = _dao.getStopForId(agencyAndId);

    for (String consolidatedStop : consolidatedStops) {
      Stop unexpectedStop = _dao.getStopForId(AgencyAndIdLibrary.convertFromString(consolidatedStop));
      if (unexpectedStop != null) {
        if (expectedStop != null) {
          double distance = SphericalGeometryLibrary.distanceFaster(
              expectedStop.getLat(), expectedStop.getLon(),
              unexpectedStop.getLat(), unexpectedStop.getLon());
          if (distance > STOP_DISTANCE_THRESHOLD) {
            _logger.log("stop_verification_distance.csv", rootStopId, consolidatedStop,
                distance, toOrd(expectedStop.getLat(), expectedStop.getLon()), 
                toOrd(unexpectedStop.getLat(), unexpectedStop.getLon()));
          }
        }
      } 
    }
  }

  private String toOrd(double lat, double lon) {
    return lat + "," + lon;
  }
}
