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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.model.assignments.ActiveBlock;
import org.onebusaway.admin.model.assignments.Assignment;
import org.onebusaway.admin.service.assignments.ActiveVehiclesService;
import org.onebusaway.admin.service.assignments.VehicleAssignmentService;
import org.onebusaway.admin.service.assignments.impl.ActiveVehiclesServiceImpl;
import org.onebusaway.admin.service.bundle.api.AuthenticatedResource;
import org.onebusaway.admin.service.assignments.impl.VehicleAssignmentServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Endpoint for vehicle assignment services, such as manually associating a vehicle
 * with a block, and managing that relationship life cycle.
 */
@Path("/vehicle-assign")
@RestController
public class VehicleAssignmentResource extends AuthenticatedResource {

    private static Logger _log = LoggerFactory.getLogger(VehicleAssignmentResource.class);
    private ObjectMapper _mapper = new ObjectMapper();


    @Autowired
    private VehicleAssignmentService _service;
    public void setVehicleAssignmentService(VehicleAssignmentServiceImpl vehicleAssignmentService) {
        _service = vehicleAssignmentService;
    }

    @Autowired
    private ActiveVehiclesService _activeVehiclesService;
    public void setActiveVehiclesService(ActiveVehiclesServiceImpl activeVehiclesService){
        _activeVehiclesService = activeVehiclesService;
    }

    public boolean assign(String blockId, String vehicleId) {
        return _service.assign(blockId, vehicleId);
    }

    @Path("/assign/block/{block}/vehicle/{vehicle}")
    @GET
    public Response assignVehicleToBlock(@PathParam("block") String block, @PathParam("vehicle") String vehicle) {
        if (!isAuthorized()) {
            return Response.noContent().build();
        }

        assign(block, vehicle);
        return Response.ok(block + "," + vehicle).build();
    }

    public String getAssignmentByBlockId(String blockId) {
        return _service.getAssignmentByBlockId(blockId);
    }

    public List<ActiveBlock> getActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes) throws ExecutionException {
        return _service.getActiveBlocks(serviceDate, filterRoutes);
    }

    @Path("/active-blocks/{serviceDate}/{routeList}")
    @GET
    public Response getActiveBlocksAsCSV(@PathParam("serviceDate") String serviceDate, @PathParam("routeList") String routes) throws ExecutionException {
        _log.info("getActiveBlocksAsCSV(" + serviceDate + ", " + routes);
        if (!isAuthorized()) {
            return Response.noContent().build();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(serviceDate);
        } catch (ParseException pe) {
            _log.error("exception for date input " + serviceDate, pe);
            return Response.serverError().build();
        }

        List<AgencyAndId> routeIds = new ArrayList<>();
        try {
            for (String rawId : routes.split(",")) {
                routeIds.add(AgencyAndIdLibrary.convertFromString(rawId));
            }
        } catch (IllegalStateException ise) {
            _log.error(ise.toString(), ise);
            return Response.serverError().build();
        }

        List<ActiveBlock> blockBeans = getActiveBlocks(new ServiceDate(date), routeIds);
        _log.info("found " + blockBeans.size() + " active blocks");
        StringBuffer csv = new StringBuffer();
        csv.append("block").append('\n');
        for (ActiveBlock bb: blockBeans) {
            _log.debug("block=" + bb.getBlockId());
            csv.append(bb.getBlockId()).append('\n');
        }
        return Response.ok(csv.toString()).build();
    }


    @Path("/get/csv")
    @GET
    public Response getAssignmentsAsCSV() {
        StringBuffer csv = new StringBuffer();
        csv.append("block").append(",").append("vehicle").append('\n');
        for (Assignment assignment: _service.getAssignments()) {
            csv.append(assignment.getAssignmentId().getBlockId()).append(",").append(assignment.getVehicleId()).append('\n');
        }
        return Response.ok(csv.toString()).build();
    }

    @Path("/list")
    @GET @Produces("application/json")
    public Response getAssignmentsAsJson() throws IOException {
        String assignmentListAsString = _mapper.writeValueAsString(_service.getAssignments());
        return Response.ok(assignmentListAsString).build();
    }

    @Path("/active-vehicles/list")
    @GET @Produces("application/json")
    public Response getActiveVehiclesAsJson(@QueryParam("q") String query) throws IOException {
        String activeVehiclesList = _mapper.writeValueAsString(_activeVehiclesService.getActiveVehicles(query));
        return Response.ok(activeVehiclesList).build();
    }

    @Path("/trips/block/{blockId}")
    @GET @Produces("application/json")
    public Response getTripsForBlockAsJson(@PathParam("blockId") String blockId) throws IOException, ExecutionException {
        String activeVehiclesList = _mapper.writeValueAsString(_service.getTripsForBlock(blockId));
        return Response.ok(activeVehiclesList).build();
    }

}
