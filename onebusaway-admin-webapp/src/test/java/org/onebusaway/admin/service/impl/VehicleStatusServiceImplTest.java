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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.service.RemoteConnectionService;
import org.onebusaway.admin.service.impl.VehicleStatusServiceImpl;
import org.onebusaway.util.services.configuration.ConfigurationService;

public class VehicleStatusServiceImplTest {

	@Mock
	private ConfigurationService configurationService;
	@Mock
	private RemoteConnectionService remoteConnectionService;
	
	private VehicleStatusServiceImpl service;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		service = new VehicleStatusServiceImpl();
		service.setConfigurationService(configurationService);
		service.setRemoteConnectionService(remoteConnectionService);
		
		System.setProperty("tdm.host", "tdm.dev.obanyc.com");
	}
	
	@Test
	@Ignore
	public void testGetVehicleStatus() {
		String pulloutData = "{\"pullouts\":[{\"vehicle-id\":\"5638\"," +
				"\"agency-id-tcip\":\"2008\",\"agency-id\":\"MTA NYCT\",\"depot\":\"OS\"," +
				"\"service-date\":\"2012-07-18\",\"pullout-time\":\"2012-07-18T05:51:00-04:00\"," +
				"\"run\":\"M15-012\",\"operator-id\":\"1663\",\"pullin-time\":\"2012-07-19T00:23:00-04:00\"" +
				"}],\"status\":\"OK\"}";
		String lastKnownData = "{\"records\":[{" +
				  "\"uuid\": \"67f843e0-d1a8-11e1-8614-123139243533\"," +
				  "\"vehicle-agency-id\": 2008," +
			      "\"time-reported\": \"2012-07-19T13:48:20.030Z\"," +
			      "\"time-received\": \"2012-07-19T13:48:22.686Z\"," +
			      "\"archive-time-received\": \"2012-07-19T13:48:24.657Z\"," +
			      "\"operator-id-designator\": \"913450\"," +
			      "\"route-id-designator\": \"61\"," +
			      "\"run-id-designator\": \"144\"," +
			      "\"dest-sign-code\": 4611," +
			      "\"latitude\": 40.683021," +
			      "\"longitude\": -74.003720," +
			      "\"speed\": 16.0," +
			      "\"direction-deg\": 207.72," +
			      "\"agency-id\": \"MTA NYCT\"," +
			      "\"vehicle-id\": 5638," +
			      "\"depot-id\": \"JG\"," +
			      "\"service-date\": \"2012-07-19\"," +
			      "\"inferred-run-id\": \"B61-14\"," +
			      "\"inferred-block-id\": \"MTA NYCT_20120701CC_JG_22200_B61-14-JG_1972\"," +
			      "\"inferred-trip-id\": \"MTA NYCT_20120701CC_057500_B61_0103_B61_14\"," +
			      "\"inferred-route-id\": \"MTA NYCT_B61\"," +
			      "\"inferred-direction-id\": \"1\"," +
			      "\"inferred-dest-sign-code\": 4611," +
			      "\"inferred-latitude\": 40.683001," +
			      "\"inferred-longitude\": -74.003678," +
			      "\"inferred-phase\": \"IN_PROGRESS\"," +
			      "\"inferred-status\": \"default\"," +
			      "\"inference-is-formal\": false," +
			      "\"distance-along-block\": 30650.503480107127," +
			      "\"distance-along-trip\": 2374.6309121986087," +
			      "\"next-scheduled-stop-id\": \"MTA NYCT_305226\"," +
			      "\"next-scheduled-stop-distance\": 26.791625996334915," +
			      "\"emergency-code\": \"1\"" +
			    "}],\"status\":\"OK\"}";
		
		when(remoteConnectionService.getContent("http://tdm.dev.obanyc.com/api/pullouts/list")).thenReturn(pulloutData);
		when(configurationService.getConfigurationValueAsString("operational-api.host", "archive")).thenReturn("archive.dev.obanyc.com");
		when(remoteConnectionService.getContent("http://archive.dev.obanyc.com/api/record/last-known/list")).thenReturn(lastKnownData);
		
		List<VehicleStatus> vehicleStatusRecords = service.getVehicleStatus(true);
		
		VehicleStatus vehicleStatus = vehicleStatusRecords.get(0);
		
		assertEquals("Mismatched vehicle id", vehicleStatus.getVehicleId(), "5638");
		assertEquals("Mismatched inferred phase", vehicleStatus.getInferredPhase(), "IN PROGRESS");
		assertEquals("Mismatched observed DSC", vehicleStatus.getObservedDSC(), "4611");
		// This has a dependency on Eastern Time!
//		assertEquals("Mismatched pull in time", vehicleStatus.getFormattedPullinTime(), "00:23 +1 day");
//		assertEquals("Mismatched pull out time", vehicleStatus.getFormattedPulloutTime(), "05:51");
		assertEquals("Mismatched inferred DSC", vehicleStatus.getInferredDestination(), "4611:B61 Direction: 1");
		assertEquals("Mismatched status image", vehicleStatus.getStatus(), "circle_orange_alert_18x18.png");
		assertEquals("Mismatched emergency status", vehicleStatus.getEmergencyStatus(), "1");
	}
	
	@Test
	@Ignore
	public void testVehicleStatusNoData() {
		when(remoteConnectionService.getContent("http://tdm.dev.obanyc.com/api/pullouts/list")).thenReturn("{\"pullouts\":[],\"status\":\"OK\"}");
		when(configurationService.getConfigurationValueAsString("operational-api.host", "archive")).thenReturn("archive.dev.obanyc.com");
		when(remoteConnectionService.getContent("http://archive.dev.obanyc.com/api/record/last-known/list")).thenReturn("{\"records\":[],\"status\":\"OK\"}");
		
		List<VehicleStatus> vehicleStatusRecords = service.getVehicleStatus(true);
		
		assertEquals("Not expecting any vehicle status records", 0, vehicleStatusRecords.size());
	}
	
	@Test
	@Ignore
	public void testVehicleStatusMismatchVehicle() {
		String pulloutData = "{\"pullouts\":[{\"vehicle-id\":\"5638\"," +
				"\"agency-id-tcip\":\"2008\",\"agency-id\":\"MTA NYCT\",\"depot\":\"OS\"," +
				"\"service-date\":\"2012-07-18\",\"pullout-time\":\"2012-07-18T05:51:00-04:00\"," +
				"\"run\":\"M15-012\",\"operator-id\":\"1663\",\"pullin-time\":\"2012-07-19T00:23:00-04:00\"" +
				"}],\"status\":\"OK\"}";
		String lastKnownData = "{\"records\":[{" +
				  "\"uuid\": \"67f843e0-d1a8-11e1-8614-123139243533\"," +
				  "\"vehicle-agency-id\": 2008," +
			      "\"time-reported\": \"2012-07-19T13:48:20.030Z\"," +
			      "\"time-received\": \"2012-07-19T13:48:22.686Z\"," +
			      "\"archive-time-received\": \"2012-07-19T13:48:24.657Z\"," +
			      "\"operator-id-designator\": \"913450\"," +
			      "\"route-id-designator\": \"61\"," +
			      "\"run-id-designator\": \"144\"," +
			      "\"dest-sign-code\": 4611," +
			      "\"latitude\": 40.683021," +
			      "\"longitude\": -74.003720," +
			      "\"speed\": 16.0," +
			      "\"direction-deg\": 207.72," +
			      "\"agency-id\": \"MTA NYCT\"," +
			      "\"vehicle-id\": 344," +
			      "\"depot-id\": \"JG\"," +
			      "\"service-date\": \"2012-07-19\"," +
			      "\"inferred-run-id\": \"B61-14\"," +
			      "\"inferred-block-id\": \"MTA NYCT_20120701CC_JG_22200_B61-14-JG_1972\"," +
			      "\"inferred-route-id\": \"MTA NYCT_B61\"," +
			      "\"inferred-direction-id\": \"1\"," +
			      "\"inferred-dest-sign-code\": 4611," +
			      "\"inferred-latitude\": 40.683001," +
			      "\"inferred-longitude\": -74.003678," +
			      "\"inferred-phase\": \"IN_PROGRESS\"," +
			      "\"inferred-status\": \"default\"," +
			      "\"inference-is-formal\": false," +
			      "\"distance-along-block\": 30650.503480107127," +
			      "\"distance-along-trip\": 2374.6309121986087," +
			      "\"next-scheduled-stop-id\": \"MTA NYCT_305226\"," +
			      "\"next-scheduled-stop-distance\": 26.791625996334915" +
			    "}],\"status\":\"OK\"}";
		
		when(remoteConnectionService.getContent("http://tdm.dev.obanyc.com/api/pullouts/list")).thenReturn(pulloutData);
		when(configurationService.getConfigurationValueAsString("operational-api.host", "archive")).thenReturn("archive.dev.obanyc.com");
		when(remoteConnectionService.getContent("http://archive.dev.obanyc.com/api/record/last-known/list")).thenReturn(lastKnownData);
		
		List<VehicleStatus> vehicleStatusRecords = service.getVehicleStatus(true);
		
		VehicleStatus vehicleStatus = vehicleStatusRecords.get(0);
		
		assertEquals("Mismatched vehicle id", vehicleStatus.getVehicleId(), "344");
		assertEquals("Mismatched inferred phase", vehicleStatus.getInferredPhase(), "IN PROGRESS");
		assertEquals("Mismatched observed DSC", vehicleStatus.getObservedDSC(), "4611");
		assertNull("No pull in time", vehicleStatus.getPullinTime());
		assertNull("No pull out time", vehicleStatus.getPulloutTime());
		assertEquals("Mismatched inferred destination information", vehicleStatus.getInferredDestination(), "4611:B61 Direction: 1");
		assertEquals("Mismatched status image", vehicleStatus.getStatus(), "circle_red18x18.png");
	}
	
	@Test
	@Ignore
	public void testVehicleSort() {
		VehicleStatus vehicle1 = new VehicleStatus();
		vehicle1.setVehicleId("1");
		vehicle1.setInferredPhase("IN PROGRESS");
		vehicle1.setLastUpdate("2012-07-19T13:48:20.030Z");
		vehicle1.setObservedDSC("4411");
		vehicle1.setPulloutTime("2012-07-18T05:51:00-04:00");
		vehicle1.setPullinTime("2012-07-19T00:23:00-04:00");
		
		VehicleStatus vehicle2 = new VehicleStatus();
		vehicle2.setVehicleId("2");
		vehicle2.setInferredPhase("LAYOVER");
		vehicle2.setLastUpdate("2012-07-19T13:50:20.030Z");
		vehicle2.setObservedDSC("4412");
		vehicle2.setPulloutTime("2012-07-18T05:54:00-04:00");
		vehicle2.setPullinTime("2012-07-19T00:25:00-04:00");
		
		List<VehicleStatus> vehicleStatusRecords = new ArrayList<VehicleStatus>();
		vehicleStatusRecords.add(vehicle1);
		vehicleStatusRecords.add(vehicle2);
		
		service.sort(vehicleStatusRecords, "vehicleId", "asc");
		assertEquals("1", vehicleStatusRecords.get(0).getVehicleId());
		service.sort(vehicleStatusRecords, "vehicleId", "desc");
		assertEquals("2", vehicleStatusRecords.get(0).getVehicleId());
		
		service.sort(vehicleStatusRecords, "lastUpdate", "asc");
		assertEquals("2012-07-19T13:48:20.030Z", vehicleStatusRecords.get(0).getLastUpdate());
		service.sort(vehicleStatusRecords, "lastUpdate", "desc");
		assertEquals("2012-07-19T13:50:20.030Z", vehicleStatusRecords.get(0).getLastUpdate());
		
		service.sort(vehicleStatusRecords, "inferredPhase", "asc");
		assertEquals("IN PROGRESS", vehicleStatusRecords.get(0).getInferredPhase());
		service.sort(vehicleStatusRecords, "inferredPhase", "desc");
		assertEquals("LAYOVER", vehicleStatusRecords.get(0).getInferredPhase());
		
		service.sort(vehicleStatusRecords, "observedDSC", "asc");
		assertEquals("4411", vehicleStatusRecords.get(0).getObservedDSC());
		service.sort(vehicleStatusRecords, "observedDSC", "desc");
		assertEquals("4412", vehicleStatusRecords.get(0).getObservedDSC());
		
		service.sort(vehicleStatusRecords, "pulloutTime", "asc");
		assertEquals("2012-07-18T05:51:00-04:00", vehicleStatusRecords.get(0).getPulloutTime());
		service.sort(vehicleStatusRecords, "pulloutTime", "desc");
		assertEquals("2012-07-18T05:54:00-04:00", vehicleStatusRecords.get(0).getPulloutTime());
		
		service.sort(vehicleStatusRecords, "pullinTime", "asc");
		assertEquals("2012-07-19T00:23:00-04:00", vehicleStatusRecords.get(0).getPullinTime());
		service.sort(vehicleStatusRecords, "pullinTime", "desc");
		assertEquals("2012-07-19T00:25:00-04:00", vehicleStatusRecords.get(0).getPullinTime());
	}
	
	@Test
	@Ignore
	public void testSortBlankPulloutFields() {
		VehicleStatus vehicle1 = new VehicleStatus();
		vehicle1.setVehicleId("1");
		vehicle1.setInferredPhase("IN PROGRESS");
		vehicle1.setLastUpdate("2012-07-19T13:48:20.030Z");
		vehicle1.setObservedDSC("4411");
		vehicle1.setPulloutTime("");
		vehicle1.setPullinTime("");
		
		VehicleStatus vehicle2 = new VehicleStatus();
		vehicle2.setVehicleId("2");
		vehicle2.setInferredPhase("LAYOVER");
		vehicle2.setLastUpdate("2012-07-19T13:50:20.030Z");
		vehicle2.setObservedDSC("4412");
		vehicle2.setPulloutTime("2012-07-18T05:54:00-04:00");
		vehicle2.setPullinTime("2012-07-19T00:25:00-04:00");
		
		List<VehicleStatus> vehicleStatusRecords = new ArrayList<VehicleStatus>();
		vehicleStatusRecords.add(vehicle1);
		vehicleStatusRecords.add(vehicle2);
		
		service.sort(vehicleStatusRecords, "pulloutTime", "asc");
		assertEquals("2012-07-18T05:54:00-04:00", vehicleStatusRecords.get(0).getPulloutTime());
		assertEquals("2", vehicleStatusRecords.get(0).getVehicleId());
		service.sort(vehicleStatusRecords, "pulloutTime", "desc");
		assertEquals("2012-07-18T05:54:00-04:00", vehicleStatusRecords.get(0).getPulloutTime());
		assertEquals("2", vehicleStatusRecords.get(0).getVehicleId());
		
		service.sort(vehicleStatusRecords, "pullinTime", "asc");
		assertEquals("2012-07-19T00:25:00-04:00", vehicleStatusRecords.get(0).getPullinTime());
		assertEquals("2", vehicleStatusRecords.get(0).getVehicleId());
		service.sort(vehicleStatusRecords, "pullinTime", "desc");
		assertEquals("2012-07-19T00:25:00-04:00", vehicleStatusRecords.get(0).getPullinTime());
		assertEquals("2", vehicleStatusRecords.get(0).getVehicleId());
	}

}
