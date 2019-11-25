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
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.jsoup.helper.StringUtil;
import org.onebusaway.admin.model.assignments.ActiveBlock;
import org.onebusaway.admin.model.assignments.BlockSummary;
import org.onebusaway.admin.service.assignments.VehicleAssignmentService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Results({
        @Result(type = "redirectAction", name ="submitSuccess", params = {
                "actionName", "vehicle-block-assignment"})
})
@Namespace(value="/admin/vehicleblockassignment")
public class VehicleBlockAssignmentAction extends ActionSupport implements
        ModelDriven<List<BlockSummary>>, Preparable {

    @Autowired
    private VehicleAssignmentService vehicleAssignmentService;

    private String vehicleId;
    private String blockId;
    private List<BlockSummary> _model;
    private Set<String> _activeVehicleIds;

    public String execute() throws ExecutionException {

        _model = getBlockSummaries();

        return SUCCESS;
    }

    @Override
    public List<BlockSummary> getModel() {
        return _model;
    }

    public void setModel(List<BlockSummary> blockSummaries) {
        _model = blockSummaries;
    }

    @Override
    public void prepare() throws ExecutionException {
        _activeVehicleIds = new HashSet<>(vehicleAssignmentService.getActiveVehicles());
    }

    public String submit() throws ExecutionException {
        for(BlockSummary blockSummary : _model){
            String blockId = blockSummary.getBlockId();
            String vehicleId = blockSummary.getVehicleId();

            if(!StringUtil.isBlank(blockId) && (StringUtil.isBlank(vehicleId) || _activeVehicleIds.contains(vehicleId))) {
                vehicleAssignmentService.assign(blockId, vehicleId);
            }
        }
        return "submitSuccess";
    }

    public Set<String> getActiveVehicles(){
        return _activeVehicleIds;
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

    public Date getLastUpdated() { return vehicleAssignmentService.getLastUpdated();}


    private List<BlockSummary> getBlockSummaries() throws ExecutionException {
        List<BlockSummary> blockSummaries = new ArrayList<>();
        ServiceDate serviceDate = new ServiceDate(getCurrentDate());
        Map<String, Integer> vehicleIdCounts = new HashMap<>();

        List<ActiveBlock> activeBlocks = vehicleAssignmentService.getActiveBlocks(serviceDate);
        Map<String, String> assignments = vehicleAssignmentService.getAssignmentsAsMap();

        for (ActiveBlock activeBlock : activeBlocks) {
            String vehicleId = assignments.get(activeBlock.getBlockId());

            BlockSummary blockSummary = new BlockSummary();
            blockSummary.setBlockId(activeBlock.getBlockId());
            blockSummary.setRouteName(String.join(", ",  activeBlock.getRoutes()));
            blockSummary.setStartTime(activeBlock.getStartTime());
            blockSummary.setEndTime(activeBlock.getEndTime());
            blockSummary.setVehicleId(vehicleId);

            if(!StringUtils.isBlank(vehicleId)){
                // count vehicleIds
                Integer vehicleIdCount = vehicleIdCounts.get(vehicleId);
                if(vehicleIdCount == null){
                    vehicleIdCounts.put(vehicleId,1);
                } else {
                    vehicleIdCounts.put(vehicleId, vehicleIdCount + 1);
                }

                // add asterix for block list dropdown
                blockSummary.setFormattedBlockId(blockSummary.getBlockId() + "*");
            }
            else{
                blockSummary.setFormattedBlockId(blockSummary.getBlockId());
            }
            blockSummaries.add(blockSummary);
        }

        for(BlockSummary blockSummary : blockSummaries){
            String vehicleId = blockSummary.getVehicleId();
            if(StringUtils.isNotBlank(vehicleId)){
                Integer vehicleIdCount = vehicleIdCounts.get(vehicleId);
                if(vehicleIdCount > 1){
                    blockSummary.setDuplicateVehicleId(Boolean.TRUE);
                }
            }
        }

        return blockSummaries;
    }
}
