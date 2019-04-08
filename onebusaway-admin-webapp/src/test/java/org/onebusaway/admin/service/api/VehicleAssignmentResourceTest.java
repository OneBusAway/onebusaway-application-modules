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

import org.junit.Test;
import org.onebusaway.admin.service.impl.VehicleAssignmentServiceImpl;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VehicleAssignmentResourceTest {

    @Test
    public void testAddAssignment() {
        VehicleAssignmentResource var = new VehicleAssignmentResource();
        var.setVehicleAssignmentService(new VehicleAssignmentServiceImpl());

        String blockId = "block_1";
        String vehicleId = "vehicle_1";
        assertTrue(var.assign(blockId, vehicleId));

    }

    @Test
    public void testGetAssignment() {
        VehicleAssignmentResource var = new VehicleAssignmentResource();
        var.setVehicleAssignmentService(new VehicleAssignmentServiceImpl());

        String blockId = "block_1";
        String vehicleId = "vehicle_1";

        String assignment = var.getAssignmentByBlockId(blockId);
        assertNull(assignment);

        assertTrue(var.assign(blockId, vehicleId));

        assignment = var.getAssignmentByBlockId(blockId);
        assertNotNull(assignment);
        assertEquals(vehicleId, assignment);
    }


    @Test
    public void testGetActiveBlocks() {
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

        };
        vas.setTransitDataService(tds);
        var.setVehicleAssignmentService(vas);


        ServiceDate today = new ServiceDate();
        AgencyAndId filterRoute = new AgencyAndId("1", "route1");
        ArrayList<AgencyAndId> filterRoutes = new ArrayList<>();
        filterRoutes.add(filterRoute);
        List<BlockBean> beans = var.getActiveBlocks(today, filterRoutes);
        assertNotNull(beans);

    }

    @Test
    public void testGetAssignments() {
        VehicleAssignmentResource var = new VehicleAssignmentResource();
        var.setVehicleAssignmentService(new VehicleAssignmentServiceImpl());

        Map<String, String> assignments = var.getAssignments();
        assertNotNull(assignments);
        assertEquals(0, assignments.size());

        var.assign("1_block1", "1_vehicle1");
        assignments = var.getAssignments();
        assertNotNull(assignments);
        Iterator<String> iterator = assignments.keySet().iterator();

        String key = iterator.next();
        assertEquals("1_block1", key);
        assertEquals("1_vehicle1", assignments.get(key));

    }

}
