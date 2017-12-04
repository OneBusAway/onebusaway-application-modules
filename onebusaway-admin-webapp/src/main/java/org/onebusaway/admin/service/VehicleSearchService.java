/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service;

import java.util.List;
import java.util.Map;

import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.util.VehicleSearchParameters;

/**
 * Performs search operation on vehicle status records for the given parameters
 * @author abelsare
 *
 */
public interface VehicleSearchService {

	/**
	 * Performs search operation on given vehicle status records. Returns records that match the 
	 * search criteria with the given search parameters. Returns empty list if none of the records
	 * match the criteria
	 * @param vehicleStatusRecords the records to be searched
	 * @param searchParameters optional parameters
	 * @return records matching the paramters
	 */
	List<VehicleStatus> search(List<VehicleStatus> vehicleStatusRecords, 
			Map<VehicleSearchParameters, String> searchParameters);
	
	/**
	 * Searches vehicles reporting emergency from the given collection of the vehicles
	 * @param vehicleStatusRecords all vehicle records available at this point
	 * @return vehicles reporting emergency status
	 */
	List<VehicleStatus> searchVehiclesInEmergency(List<VehicleStatus> vehicleStatusRecords);
	
	/**
	 * Searches vehicles inferred in revenue service i.e buses whose inferred state is either
	 * IN PROGRESS or LAYOVER_*
	 * @param vehicleStatusRecords all vehicle records available at this point
	 * @return vehicles inferred in revenue service
	 */
	List<VehicleStatus> searchVehiclesInRevenueService(List<VehicleStatus> vehicleStatusRecords);
	
	/**
	 * Searches vehicles tracked in given time. The time can be specified by the caller
	 * @param minutes time period for results should be returned
	 * @return vehicles tracked in given time
	 */
	List<VehicleStatus> searchVehiclesTracked(int minutes, List<VehicleStatus> vehicleStatusRecords);
	
	/**
	 * Searches run/blocks scheduled to be active
	 * @return run/blocks scheduled to be active
	 */
	List<VehicleStatus> searchActiveRuns();
}
