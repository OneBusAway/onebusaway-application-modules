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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Assignment {

    @EmbeddedId
    private AssignmentId assignmentId;

    private String vehicleId;

    public Assignment() {
    }

    public Assignment(String blockId, String vehicleId, Date date) {
        this.assignmentId = new AssignmentId(blockId, date);
        this.vehicleId = vehicleId;
    }

    public AssignmentId getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(AssignmentId assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getBlockId(){
        if(this.assignmentId != null){
            return this.assignmentId.getBlockId();
        }
        return null;
    }

    public Date getAssignmentDate(){
        if(this.assignmentId != null){
            return this.assignmentId.getAssignmentDate();
        }
        return null;
    }

}
