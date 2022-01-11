/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onebusaway.admin.model.assignments.ActiveBlock;
import org.onebusaway.admin.model.assignments.Assignment;
import org.onebusaway.admin.model.assignments.AssignmentConfig;
import org.onebusaway.admin.service.assignments.AssignmentConfigService;
import org.onebusaway.admin.service.assignments.AssignmentDao;
import org.onebusaway.admin.service.assignments.AssignmentConfigDao;
import org.onebusaway.admin.service.assignments.impl.VehicleAssignmentServiceImpl;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.federated.TransitDataServiceImpl;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VehicleAssignmentResourceTest {

    @Mock
    private AssignmentDao assignmentDao;

    @Mock
    private AssignmentConfigService assignmentConfigService;

    Date currentDate = new Date();

    private Date today(){
        return currentDate;
    }

    @Test
    public void testAddAssignment() throws ParseException {

        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        VehicleAssignmentServiceImpl vasSpy = Mockito.spy(vas);
        vasSpy.setAssignmentDao(assignmentDao);
        vasSpy.setAssignmentConfigService(assignmentConfigService);

        Mockito.when(vasSpy.getCurrentDate()).thenReturn(today());

        VehicleAssignmentResource var = new VehicleAssignmentResource();
        var.setVehicleAssignmentService(vasSpy);

        doNothing().when(assignmentDao).save(any(Assignment.class));
        lenient().doNothing().when(assignmentConfigService).setConfigValue(anyString(), anyString());

        String blockId = "block_1";
        String vehicleId = "vehicle_1";
        assertTrue(var.assign(blockId, vehicleId));

    }

    @Test
    public void testGetAssignment() {
        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        VehicleAssignmentServiceImpl vasSpy = Mockito.spy(vas);
        vasSpy.setAssignmentDao(assignmentDao);
        vasSpy.setAssignmentConfigService(assignmentConfigService);

        Mockito.when(vasSpy.getCurrentDate()).thenReturn(today());

        VehicleAssignmentResource var = new VehicleAssignmentResource();
        var.setVehicleAssignmentService(vasSpy);

        String blockId = "block_1";
        String vehicleId = "vehicle_1";
        Assignment assignment = new Assignment(blockId, vehicleId, today());

        doNothing().when(assignmentDao).save(any(Assignment.class));
        lenient().doNothing().when(assignmentConfigService).setConfigValue(anyString(), anyString());

        String vehicleIdAssignment = var.getAssignmentByBlockId(blockId);

        assertNull(vehicleIdAssignment);

        assertTrue(var.assign(blockId, vehicleId));

        when(assignmentDao.getAssignment(blockId, today())).thenReturn(assignment);

        vehicleIdAssignment = var.getAssignmentByBlockId(blockId);

        assertNotNull(vehicleIdAssignment);
        assertEquals(vehicleId, vehicleIdAssignment);
    }


    @Test
    public void testGetActiveBlocks() throws ExecutionException {
        VehicleAssignmentResource var = new VehicleAssignmentResource();
        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        TransitDataService tds = new TransitDataServiceImpl() {

            public ListBean<RouteBean> getRoutesForAgencyId(String agencyId) {
                ListBean<RouteBean> lb = new ListBean<>();
                lb.setList(new ArrayList<RouteBean>());
                RouteBean.Builder builder = RouteBean.builder();
                builder.setId("1_route1");
                AgencyBean agency = new AgencyBean();
                agency.setTimezone("America/New_York");
                builder.setAgency(agency);

                RouteBean rb = builder.create();
                lb.getList().add(rb);
                return lb;
            }


            public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {
                ListBean<TripDetailsBean> lb = new ListBean<>();
                lb.setList(new ArrayList<TripDetailsBean>());
                TripDetailsBean tdb = new TripDetailsBean();
                tdb.setTrip(new TripBean());
                tdb.getTrip().setBlockId("1");
                tdb.getTrip().setServiceId("1_service1");
                lb.getList().add(tdb);
                return lb;
            }

            public BlockBean getBlockForId(String blockId) {
                BlockBean bb = new BlockBean();
                bb.setId(blockId);
                return bb;
            }

            public BlockInstanceBean getBlockInstance(String blockId, long serviceDate) {
                BlockInstanceBean bib = new BlockInstanceBean();
                bib.setBlockId(blockId);
                return bib;
            }

            @Override
            public RouteBean getRouteForId(String routeId) throws ServiceException {
                RouteBean.Builder builder = RouteBean.builder();
                builder.setId("1_route1");
                AgencyBean agency = new AgencyBean();
                agency.setTimezone("America/New_York");
                builder.setAgency(agency);
                return builder.create();
            }

            @Override
            public List<BlockInstanceBean> getActiveBlocksForRoute(AgencyAndId route, long timeFrom, long timeTo) {
                return Collections.EMPTY_LIST;
            }
        };
        vas.setTransitDataService(tds);
        var.setVehicleAssignmentService(vas);


        ServiceDate today = new ServiceDate();
        AgencyAndId filterRoute = new AgencyAndId("1", "route1");
        ArrayList<AgencyAndId> filterRoutes = new ArrayList<>();
        filterRoutes.add(filterRoute);

        List<ActiveBlock> beans = var.getActiveBlocks(today, filterRoutes);
        assertNotNull(beans);

    }

    @Test
    public void testGetAssignments() throws IOException {
        ObjectMapper _mapper = new ObjectMapper();
        _mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        VehicleAssignmentServiceImpl vas = new VehicleAssignmentServiceImpl();
        VehicleAssignmentServiceImpl vasSpy = Mockito.spy(vas);
        vasSpy.setAssignmentDao(assignmentDao);
        vasSpy.setAssignmentConfigService(assignmentConfigService);

        Mockito.when(vasSpy.getCurrentDate()).thenReturn(today());

        VehicleAssignmentResource var = new VehicleAssignmentResource();
        var.setVehicleAssignmentService(vasSpy);

        doNothing().when(assignmentDao).save(any(Assignment.class));
        lenient().doNothing().when(assignmentConfigService).setConfigValue(anyString(), anyString());

        String blockId = "1_block1";
        String vehicleId = "1_vehicle1";

        Assignment assignment = new Assignment(blockId, vehicleId, today());
        List<Assignment> assignmentsList = new ArrayList<>();

        when(assignmentDao.getAll(any(Date.class))).thenReturn(assignmentsList);

        Response response = var.getAssignmentsAsJson();
        String assignmentsJson = (String)response.getEntity();
        Assignment[]  assignments = _mapper.readValue(assignmentsJson, Assignment[].class);
        assertNotNull(assignments);
        assertEquals(0, assignments.length);

        assignmentsList.add(assignment);

        var.assign("1_block1", "1_vehicle1");
        response = var.getAssignmentsAsJson();
        assignmentsJson = (String)response.getEntity();
        assignments = _mapper.readValue(assignmentsJson, Assignment[].class);
        assertNotNull(assignments);

        assertEquals("1_block1", assignments[0].getBlockId());
        assertEquals("1_vehicle1", assignments[0].getVehicleId());

    }
}
