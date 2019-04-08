package org.onebusaway.admin.service;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.blocks.BlockBean;

import java.util.List;
import java.util.Map;

public interface VehicleAssignmentService {
    boolean assign(String blockId, String vehicleId);

    String getAssignmentByBlockId(String blockId);

    List<BlockBean> getActiveBlocks(ServiceDate serviceDate, List<AgencyAndId> filterRoutes);

    Map<String, String> getAssignments();
}
