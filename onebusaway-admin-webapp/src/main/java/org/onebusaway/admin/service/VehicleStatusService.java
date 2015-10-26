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

import org.onebusaway.admin.model.ui.VehicleDetail;
import org.onebusaway.admin.model.ui.VehicleStatistics;
import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.util.VehicleSearchParameters;

/**
 * Builds vehicle status data by querying TDM and report archiver. Makes web service calls to the
 * exposed APIs on these servers to fetch the required vehicle status data.
 * @author abelsare
 *
 */
public interface VehicleStatusService {
	
	/**
	 * Creates vehicle status data by making web service calls to TDM and report archive servers
	 * @param indicates whether new data should be loaded. The service returns cached data otherwise
	 * @return new/cached vehicle status data
	 */
	List<VehicleStatus> getVehicleStatus(boolean loadNew);
	
	/**
	 * Searches vehicles based on the given parameters. Uses vehicle cache for fetching vehicle data.
	 * Returns empty results if cache is empty
	 * @param searchParameters paramters for searching
	 * @param newSearch indicates this is a new search
	 * @return results matching the parameters, empty list if the cache is empty
	 */
	List<VehicleStatus> search(Map<VehicleSearchParameters, String> searchParameters, boolean newSearch);
	
	/**
	 * Returns statistics of the vehicles tracked such as vehicles reporting emergency, vehicles in
	 * revenue servie, vehicles tracked in past five minutes
	 * @param parameters optional parameters from client 
	 * @return vehicle statistics with the required count
	 */
	VehicleStatistics getVehicleStatistics(String... parameters);

	/**
	 * Make a web service call to report archive server about a specific vehicle.
	 * @param vehicleId
	 * @return
	 */
	VehicleDetail getVehicleDetail(String vehicleId);
	
	/**
	 * Sorts vehicle records on the given field by given order. 
	 * @param vehiclesPerPage vehicle records to be sorted
	 * @param field field on which the records need to be sorted
	 * @param order order of sorting
	 */
	void sort(List<VehicleStatus> vehiclesPerPage, String field, String order);

}
