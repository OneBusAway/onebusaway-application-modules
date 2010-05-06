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

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_tpi_paths")
public class TPIPath extends EntityBean {

    private static final long serialVersionUID = 1L;

    @Id
    private TPIPathKey id;

    private Date effectiveDate;

    @ManyToOne
    private TransLink transLink;

    private Date dbModDate;

    private int flowDirection;

    @Column(length = 1)
    private String tpiPathStatus;

    public TPIPathKey getId() {
        return id;
    }

    public void setId(TPIPathKey id) {
        this.id = id;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public void setTransLink(TransLink transLink) {
        this.transLink = transLink;
    }

    public TransLink getTransLink() {
        return transLink;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public int getFlowDirection() {
        return flowDirection;
    }

    public void setFlowDirection(int direction) {
        this.flowDirection = direction;
    }

    public String getStatus() {
        return tpiPathStatus;
    }

    public void setStatus(String status) {
        this.tpiPathStatus = status;
    }
}
