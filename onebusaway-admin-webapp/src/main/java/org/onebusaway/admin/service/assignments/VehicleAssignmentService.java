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
package org.onebusaway.admin.service.assignments;

import org.onebusaway.admin.model.assignments.ActiveBlock;
import org.onebusaway.admin.model.assignments.Assignment;
import org.onebusaway.admin.model.assignments.TripSummary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface VehicleAssignmentService {
    Date getLastUpdated();

    boolean assign(String blockId, String vehicleId, Date date);

    boolean assign(String blockId, String vehicleId);

    Map<String, String> getAssignmentsAsMap(Date date);

    String getAssignmentByBlockId(String blockId, Date date);

    String getAssignmentByBlockId(String blockId);

    List<Assignment> getAssignments(Date date);

    List<String> getActiveVehicles(String query);

    List<String> getActiveVehicles();

    Map<String, String> getAssignmentsAsMap();

    List<Assignment> getAssignments();

    List<ActiveBlock> getActiveBlocks(ServiceDate serviceDate) throws ExecutionException;

    List<ActiveBlock> getActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes) throws ExecutionException;

    List<TripSummary> getTripsForBlock(String blockId) throws ExecutionException;
}
