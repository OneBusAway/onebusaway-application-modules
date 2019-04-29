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
    @Temporal(TemporalType.DATE)
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
