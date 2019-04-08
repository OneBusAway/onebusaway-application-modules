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

import org.onebusaway.admin.service.VehicleAssignmentService;
import org.onebusaway.admin.service.bundle.api.AuthenticatedResource;
import org.onebusaway.admin.service.impl.VehicleAssignmentServiceImpl;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Endpoint for vehicle assignment services, such as manually associating a vehicle
 * with a block, and managing that relationship life cycle.
 */
@Path("/vehicle-assign")
@Component
public class VehicleAssignmentResource extends AuthenticatedResource {

    private static Logger _log = LoggerFactory.getLogger(VehicleAssignmentResource.class);

    @Autowired
    private VehicleAssignmentService _service;
    public void setVehicleAssignmentService(VehicleAssignmentServiceImpl vehicleAssignmentService) {
        _service = vehicleAssignmentService;
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

    public List<BlockBean> getActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes) {
        return _service.getActiveBlocks(serviceDate, filterRoutes);
    }

    @Path("/active-blocks/{serviceDate}/{routeList}")
    @GET
    public Response getActiveBlocksAsCSV(@PathParam("serviceDate") String serviceDate, @PathParam("routeList") String routes) {
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

        List <BlockBean> blockBeans = getActiveBlocks(new ServiceDate(date), routeIds);
        _log.info("found " + blockBeans.size() + " active blocks");
        StringBuffer csv = new StringBuffer();
        csv.append("block").append('\n');
        for (BlockBean bb: blockBeans) {
            _log.debug("block=" + bb.getId());
            csv.append(bb.getId()).append('\n');
        }
        return Response.ok(csv.toString()).build();
    }

    public Map<String, String> getAssignments() {
        return _service.getAssignments();
    }

    @Path("/get/csv")
    @GET
    public Response getAssignmentsAsCSV() {
        if (!isAuthorized()) {
            return Response.noContent().build();
        }

        StringBuffer csv = new StringBuffer();
        csv.append("block").append(",").append("vehicle").append('\n');
        Map<String, String> assignments = getAssignments();
        for (String block: assignments.keySet()) {
            csv.append(block).append(",").append(assignments.get(block)).append('\n');
        }
        return Response.ok(csv.toString()).build();
    }

}
