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
package org.onebusaway.admin.model.assignments;

public class BlockSummary {
    private String blockId;
    private String formattedBlockId;
    private String routeName;
    private String startTime;
    private String endTime;
    private String vehicleId;
    private boolean duplicateVehicleId;

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getFormattedBlockId() {
        return formattedBlockId;
    }

    public void setFormattedBlockId(String formattedBlockId) {
        this.formattedBlockId = formattedBlockId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public boolean isDuplicateVehicleId() {
        return duplicateVehicleId;
    }

    public void setDuplicateVehicleId(boolean duplicateVehicleId) {
        this.duplicateVehicleId = duplicateVehicleId;
    }
}
