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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.util.AgencyAndIdLibrary;

/**
 *  Check that stops listed in the stop consolidation file were actually consolidated, and
 *  output stops that are unexpected or missing. 
 *
 */
public class StopVerificationTask extends AbstractStopTask implements Runnable {

  protected void insertHeader() {
    _logger.header("stop_verification.csv", "root_stop_id,pass?,missing_stop_id,unexpected_stop_ids");
  }

  
  protected void verifyStops(String rootStopId, List<String> consolidatedStops) {
    AgencyAndId agencyAndId = AgencyAndIdLibrary.convertFromString(rootStopId);
    Stop expectedStop = _dao.getStopForId(agencyAndId);
    boolean pass = expectedStop != null;
    String missingStopId = (pass?"":rootStopId);
    String unexpectedStopIds = "";
    
    for (String consolidatedStop : consolidatedStops) {
      Stop unexpectedStop = _dao.getStopForId(AgencyAndIdLibrary.convertFromString(consolidatedStop));
      if (unexpectedStop != null) {
        pass = false;
        unexpectedStopIds += consolidatedStop + " ";
        }
    }
    if (!pass) {
      _logger.log("stop_verification.csv", rootStopId, String.valueOf(pass), missingStopId, unexpectedStopIds);
    }
  }

}
