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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.service.VehicleSearchService;
import org.onebusaway.admin.service.impl.VehicleSearchServiceImpl;
import org.onebusaway.admin.util.VehicleSearchParameters;

/**
 * Tests {@link VehicleSearchServiceImpl}
 * @author abelsare
 *
 */
public class VehicleSearchServiceImplTest {

	private VehicleSearchService service;
	
	@Before
	public void setUp() throws Exception {
		service = new VehicleSearchServiceImpl();
	}
	
	@Test
	public void testSearchNoParameters() {
		Map<VehicleSearchParameters, String> parameters = 
				buildSearchParameters(" ","","", "All","All","All","false","false");
		List<VehicleStatus> vehicleStatusRecords = buildVehicleStatusRecords();
		
		List<VehicleStatus> matchingRecords = service.search(vehicleStatusRecords, parameters);
		
		assertEquals("Expecting all records to match", matchingRecords.size(), 3);
	}
	
	@Test
	public void testSearchNoMatchingParameters() {
		Map<VehicleSearchParameters, String> parameters = 
				buildSearchParameters("240","B61","430", "All","All","All", "false","false");
		List<VehicleStatus> vehicleStatusRecords = buildVehicleStatusRecords();
		
		List<VehicleStatus> matchingRecords = service.search(vehicleStatusRecords, parameters);
		
		assertEquals("Expecting no records to match", matchingRecords.size(), 0);
	}
	
	@Test
	public void testSearchPartialMatchingParameters() {
		Map<VehicleSearchParameters, String> parameters = 
				buildSearchParameters("","B63","", "All","All","All", "false", "false");
		List<VehicleStatus> vehicleStatusRecords = buildVehicleStatusRecords();
		
		List<VehicleStatus> matchingRecords = service.search(vehicleStatusRecords, parameters);
		
		assertEquals("Expecting 2 records to match", matchingRecords.size(), 2);
	}
	
	@Test
	public void testSearchMatchingInferredState() {
		Map<VehicleSearchParameters, String> parameters = 
				buildSearchParameters("","B63","", "All","DEADHEAD","All", "false","false");
		List<VehicleStatus> vehicleStatusRecords = buildVehicleStatusRecords();
		
		List<VehicleStatus> matchingRecords = service.search(vehicleStatusRecords, parameters);
		
		assertEquals("Expecting 1 records to match", matchingRecords.size(), 1);
	}
	
	@Test
	public void testSearchExactMatchingParameters() {
		Map<VehicleSearchParameters, String> parameters = 
				buildSearchParameters("243","B62","437", "All","IN PROGRESS","All", "false","true");
		List<VehicleStatus> vehicleStatusRecords = buildVehicleStatusRecords();
		
		List<VehicleStatus> matchingRecords = service.search(vehicleStatusRecords, parameters);
		
		assertEquals("Expecting 1 records to match", matchingRecords.size(), 1);
	}
	
	@Test
	public void testSearchEmergencyVehicles() {
		Map<VehicleSearchParameters, String> parameters = 
				buildSearchParameters(""," ","", "All","All","All", "true", "false");
		List<VehicleStatus> vehicleStatusRecords = buildVehicleStatusRecords();
		
		List<VehicleStatus> matchingRecords = service.search(vehicleStatusRecords, parameters);
		
		assertEquals("Expecting 1 vehicle in emeergency", matchingRecords.size(), 1);
	}
	
	@Test
	public void testEmergencyVehicleCount() {
		List<VehicleStatus> vehicleStatusRecords = buildVehicleStatusRecords();
		
		List<VehicleStatus> vehiclesInemergency = service.searchVehiclesInEmergency(vehicleStatusRecords);
		
		assertEquals("Expecting 1 vehicle in emeergency", vehiclesInemergency.size(), 1);
	}
	
	@Test
	public void testRevenueServiceVehicleCount() {
		List<VehicleStatus> vehicleStatusRecords = buildVehicleStatusRecords();
		
		List<VehicleStatus> vehiclesInRevenueService = service.searchVehiclesInRevenueService(vehicleStatusRecords);
		
		assertEquals("Expecting 2 vehicles in emeergency", vehiclesInRevenueService.size(), 2);
	}
	
	private Map<VehicleSearchParameters, String> buildSearchParameters(String vehicleId, String route,
			String dsc, String depot, String inferredPhase, String pulloutStatus, 
			String emergencyStatus, String formalInferrence) {
		Map<VehicleSearchParameters, String> parameters = new HashMap<VehicleSearchParameters, String>();
		
		parameters.put(VehicleSearchParameters.VEHICLE_ID, vehicleId);
		parameters.put(VehicleSearchParameters.ROUTE, route);
		parameters.put(VehicleSearchParameters.DSC, dsc);
		parameters.put(VehicleSearchParameters.DEPOT, depot);
		parameters.put(VehicleSearchParameters.INFERRED_PHASE, inferredPhase);
		parameters.put(VehicleSearchParameters.PULLOUT_STATUS, pulloutStatus);
		parameters.put(VehicleSearchParameters.EMERGENCY_STATUS, emergencyStatus);
		parameters.put(VehicleSearchParameters.FORMAL_INFERRENCE, formalInferrence);
		
		return parameters;
	}
	
	private List<VehicleStatus> buildVehicleStatusRecords() {
		List<VehicleStatus> vehicleStatusRecords = new ArrayList<VehicleStatus>();
		
		VehicleStatus vehicle1 = mock(VehicleStatus.class);
		when(vehicle1.getVehicleId()).thenReturn("242");
		when(vehicle1.getRoute()).thenReturn("B63");
		when(vehicle1.getInferredPhase()).thenReturn("IN PROGRESS");
		when(vehicle1.getObservedDSC()).thenReturn("436");
		when(vehicle1.getEmergencyStatus()).thenReturn("");
		when(vehicle1.isInferrenceFormal()).thenReturn(false);
		
		VehicleStatus vehicle2 = mock(VehicleStatus.class);
		when(vehicle2.getVehicleId()).thenReturn("243");
		when(vehicle2.getRoute()).thenReturn("B62");
		when(vehicle2.getInferredPhase()).thenReturn("IN PROGRESS");
		when(vehicle2.getObservedDSC()).thenReturn("437");
		when(vehicle2.getEmergencyStatus()).thenReturn(" ");
		when(vehicle2.isInferrenceFormal()).thenReturn(true);
		
		VehicleStatus vehicle3 = mock(VehicleStatus.class);
		when(vehicle3.getVehicleId()).thenReturn("244");
		when(vehicle3.getRoute()).thenReturn("B63");
		when(vehicle3.getInferredPhase()).thenReturn("DEADHEAD");
		when(vehicle3.getObservedDSC()).thenReturn("437");
		when(vehicle3.getEmergencyStatus()).thenReturn("1");
		when(vehicle3.isInferrenceFormal()).thenReturn(true);
		
		vehicleStatusRecords.add(vehicle1);
		vehicleStatusRecords.add(vehicle2);
		vehicleStatusRecords.add(vehicle3);
		
		return vehicleStatusRecords;
	}

}
