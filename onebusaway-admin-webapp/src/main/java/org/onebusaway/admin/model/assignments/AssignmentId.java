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
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class AssignmentId implements Serializable {
    @Column(name="block_id")
    private String blockId;

    @Column(name="assignment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignmentDate;

    public AssignmentId(){}

    public AssignmentId(String blockId, Date assignmentDate){
        this.blockId = blockId;
        this.assignmentDate = assignmentDate;
    }

    public String getBlockId() {
        return blockId;
    }

    public Date getAssignmentDate() {
        return assignmentDate;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public void setAssignmentDate(Date assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentId that = (AssignmentId) o;
        return blockId.equals(that.blockId) &&
                assignmentDate.equals(that.assignmentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockId, assignmentDate);
    }
}
