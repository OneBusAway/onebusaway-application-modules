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
package org.onebusaway.admin.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.search.Filter;
import org.onebusaway.admin.search.impl.DSCFilter;
import org.onebusaway.admin.search.impl.DepotFilter;
import org.onebusaway.admin.search.impl.EmergencyStatusFilter;
import org.onebusaway.admin.search.impl.FormalInferrenceFilter;
import org.onebusaway.admin.search.impl.InferredPhaseFilter;
import org.onebusaway.admin.search.impl.PulloutStatusFilter;
import org.onebusaway.admin.search.impl.RevenueServiceFilter;
import org.onebusaway.admin.search.impl.RouteFilter;
import org.onebusaway.admin.search.impl.TimeWindowFilter;
import org.onebusaway.admin.search.impl.VehicleIdFilter;
import org.onebusaway.admin.service.VehicleSearchService;
import org.onebusaway.admin.util.VehicleSearchParameters;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link VehicleSearchService}
 * @author abelsare
 *
 */
@Component
public class VehicleSearchServiceImpl implements VehicleSearchService {

	@Override
	public List<VehicleStatus> search(List<VehicleStatus> vehicleStatusRecords,
			Map<VehicleSearchParameters, String> searchParameters) {
		
		List<VehicleStatus> matchingRecords = new ArrayList<VehicleStatus>();

		//Build filters corresponding to the search parameters
		List<Filter<VehicleStatus>> filters = buildFilters(searchParameters);
		
		//Since there are no filters specified, return all the records as matched records
		if(filters.isEmpty()) {
			matchingRecords.addAll(vehicleStatusRecords);
		} else {
			//Apply each filter to each record
			for(VehicleStatus vehicleStatus : vehicleStatusRecords) {
				boolean matches = applyFilters(vehicleStatus, filters);
				if(matches) {
					matchingRecords.add(vehicleStatus);
				}
			}
		}
		
		return matchingRecords;
	}
	
	@Override
	public List<VehicleStatus> searchVehiclesInEmergency(
			List<VehicleStatus> vehicleStatusRecords) {
		Filter<VehicleStatus> emergencyFilter = new EmergencyStatusFilter();
		List<VehicleStatus> vehiclesInEmergency = new ArrayList<VehicleStatus>();
		
		for(VehicleStatus vehicleStatus : vehicleStatusRecords) {
			if(emergencyFilter.apply(vehicleStatus)) {
				vehiclesInEmergency.add(vehicleStatus);
			}
		}
		return vehiclesInEmergency;
	}

	@Override
	public List<VehicleStatus> searchVehiclesInRevenueService(
			List<VehicleStatus> vehicleStatusRecords) {
		Filter<VehicleStatus> revenueServiceFilter = new RevenueServiceFilter();
		List<VehicleStatus> vehiclesInRevenueService = new ArrayList<VehicleStatus>();
		
		for(VehicleStatus vehicleStatus : vehicleStatusRecords) {
			if(revenueServiceFilter.apply(vehicleStatus)) {
				vehiclesInRevenueService.add(vehicleStatus);
			}
		}
		return vehiclesInRevenueService;
	}

	@Override
	public List<VehicleStatus> searchVehiclesTracked(int minutes, List<VehicleStatus> vehicleStatusRecords) {
		Filter<VehicleStatus> timeWindowFilter = new TimeWindowFilter(minutes);
		List<VehicleStatus> vehiclesTracked = new ArrayList<VehicleStatus>();
		
		for(VehicleStatus vehicleStatus: vehicleStatusRecords) {
			if(timeWindowFilter.apply(vehicleStatus)) {
				vehiclesTracked.add(vehicleStatus);
			}
		}
		return vehiclesTracked;
	}

	@Override
	public List<VehicleStatus> searchActiveRuns() {
		return null;
	}
	
	private boolean applyFilters(VehicleStatus vehicleStatus, List<Filter<VehicleStatus>> filters) {
		boolean match = false;
		for(Filter<VehicleStatus> filter : filters) {
			if(filter.apply(vehicleStatus)) {
				match = true;
			} else {
				//Break on first non match
				match = false;
				break;
			}
		}
		return match;
	}
	
	private List<Filter<VehicleStatus>> buildFilters(
			Map<VehicleSearchParameters, String> searchParameters) {
		List<Filter<VehicleStatus>> filters = new ArrayList<Filter<VehicleStatus>>();
		
		//Since all parameters are optional we have to look for each one
		//To-do: there might be a better way of doing this
		String vehicleId = searchParameters.get(VehicleSearchParameters.VEHICLE_ID);
		if(StringUtils.isNotBlank(vehicleId)) {
			filters.add(new VehicleIdFilter(vehicleId));
		}
		String route = searchParameters.get(VehicleSearchParameters.ROUTE);
		if(StringUtils.isNotBlank(route)) {
			filters.add(new RouteFilter(route));
		}
		String inferredPhase = searchParameters.get(VehicleSearchParameters.INFERRED_PHASE);
		if(!inferredPhase.equalsIgnoreCase("All")) {
			filters.add(new InferredPhaseFilter(inferredPhase));
		}
		String dsc = searchParameters.get(VehicleSearchParameters.DSC);
		if(StringUtils.isNotBlank(dsc)) {
			filters.add(new DSCFilter(dsc));
		}
		String depot = searchParameters.get(VehicleSearchParameters.DEPOT);
		if(!depot.equalsIgnoreCase("All")) {
			filters.add(new DepotFilter(depot));
		}
		String pulloutStatus = searchParameters.get(VehicleSearchParameters.PULLOUT_STATUS);
		if(!pulloutStatus.equalsIgnoreCase("All")) {
			filters.add(new PulloutStatusFilter());
		}
		String emergencyStatus = searchParameters.get(VehicleSearchParameters.EMERGENCY_STATUS);
		if(emergencyStatus.equalsIgnoreCase("true")) {
			filters.add(new EmergencyStatusFilter());
		}
		String formalInferrence = searchParameters.get(VehicleSearchParameters.FORMAL_INFERRENCE);
		if(formalInferrence.equalsIgnoreCase("true")) {
			filters.add(new FormalInferrenceFilter());
		}
		return filters;
	}

}
