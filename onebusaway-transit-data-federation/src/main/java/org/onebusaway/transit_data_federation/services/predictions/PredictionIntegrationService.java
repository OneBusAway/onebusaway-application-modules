/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.services.predictions;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.trips.TripStatusBean;

/**
 * A service that integrates predictions from a third-party source (as defined in PredictionGenerationService)
 * into the response of API calls.
 * 
 * @author jmaki
 *
 */
public interface PredictionIntegrationService {

  /**
   * Tell the integration service to refresh the records in cache for the given vehicle.
   * @param vehicleId
   */
  public void updatePredictionsForVehicle(AgencyAndId vehicleId);

  /**
   * A method to return predictions in a format suitable for injection into the TDS for the given
   * vehicle status.
   * 
   * @param tripStatus
   * @return
   */
  public List<TimepointPredictionRecord> getPredictionsForTrip(TripStatusBean tripStatus);

}