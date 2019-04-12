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
package org.onebusaway.webapp.actions.admin.vehicleblockassignment;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.convention.annotation.Namespace;
import org.jsoup.helper.StringUtil;
import org.onebusaway.admin.model.ActiveBlock;
import org.onebusaway.admin.model.BlockSummary;
import org.onebusaway.admin.service.VehicleAssignmentService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Namespace(value="/admin/vehicleblockassignment")
public class VehicleBlockAssignmentAction extends ActionSupport implements
        ModelDriven<List<BlockSummary>>, Preparable {

    @Autowired
    private VehicleAssignmentService vehicleAssignmentService;

    @Autowired
    private ConfigurationService configurationService;

    private String vehicleId;
    private String blockId;
    private List<BlockSummary> _model;

    public String execute(){
        return SUCCESS;
    }

    @Override
    public List<BlockSummary> getModel() {
        return _model;
    }

    @Override
    public void prepare() throws Exception {
        _model = getBlockSummaries();
    }

    public String submit() {
        for(BlockSummary blockSummary : _model){
            if(!StringUtil.isBlank(blockSummary.getBlockId())) {
                vehicleAssignmentService.assign(blockSummary.getBlockId(), blockSummary.getVehicleId());
            }
        }
        return SUCCESS;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public Date getCurrentDate(){
        return new Date();
    }

    private List<BlockSummary> getBlockSummaries(){
        List<BlockSummary> blockSummaries = new ArrayList<>();
        ServiceDate serviceDate = new ServiceDate(getCurrentDate());
        List<AgencyAndId> routes = getVehicleAssignmentRoutes();

        List<ActiveBlock> activeBlocks = vehicleAssignmentService.getActiveBlocks(serviceDate, routes);

        for (ActiveBlock activeBlock : activeBlocks) {
            BlockSummary blockSummary = new BlockSummary();
            blockSummary.setBlockId(activeBlock.getBlockId());
            blockSummary.setRouteName(String.join(", ",  activeBlock.getRoutes()));
            blockSummary.setStartTime(activeBlock.getStartTime());
            blockSummary.setEndTime(activeBlock.getEndTime());
            blockSummary.setVehicleId(vehicleAssignmentService.getAssignmentByBlockId(activeBlock.getBlockId()));
            blockSummaries.add(blockSummary);
        }


        return blockSummaries;
    }

    private List<AgencyAndId> getVehicleAssignmentRoutes(){
        List<AgencyAndId> routesAsAgencyAndId = new ArrayList<>();
        String vehicleAssignmentRoutes = configurationService.getConfigurationValueAsString("vehicleAssignmentRoutes", "");
        List<String> routes = Arrays.asList(vehicleAssignmentRoutes.split("\\s*,\\s*"));
        for(String route : routes){
            routesAsAgencyAndId.add(AgencyAndId.convertFromString(route));
        }
        return routesAsAgencyAndId;
    }

}
