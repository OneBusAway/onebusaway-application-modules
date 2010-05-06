/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_tpi")
@AccessType("field")
public class TPI extends IdentityBean {

    private static final long serialVersionUID = 1L;

    /**
     * We make the id a "property" access element such that a call to
     * {@link #getId()} when the object is proxied does not result in proxy
     * insantiation
     */
    @Id
    @AccessType("property")
    private int id;

    private Date effectiveDate;

    private Date dbModDate;

    @ManyToOne
    private Timepoint fromTimepoint;

    @ManyToOne
    private Timepoint toTimepoint;

    @Column(length = 1)
    private String tpiPathStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public void setFromTimepoint(Timepoint timepoint) {
        this.fromTimepoint = timepoint;
    }

    public Timepoint getFromTimepoint() {
        return fromTimepoint;
    }

    public void setToTimepoint(Timepoint timepoint) {
        this.toTimepoint = timepoint;
    }

    public Timepoint getToTimepoint() {
        return toTimepoint;
    }

    public String getStatus() {
        return tpiPathStatus;
    }

    public void setStatus(String status) {
        this.tpiPathStatus = status;
    }
}
