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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_timepoints")
@Cache(usage=CacheConcurrencyStrategy.READ_ONLY)
@AccessType("field")
public class Timepoint extends IdentityBean {

    private static final long serialVersionUID = 1L;

    /**
     * We make the id a "property" access element such that a call to
     * {@link #getId()} when the object is proxied does not result in proxy
     * insantiation
     */
    @Id
    @AccessType("property")
    private int id;

    @Column(length = 8)
    private String name8;

    @Column(length = 20)
    private String name20;

    @Column(length = 40)
    private String name40;

    private Date dbModDate;

    @Column(length = 1)
    private String timepointStatus;

    @Column(length = 2)
    private String timepointType;

    @ManyToOne(fetch=FetchType.LAZY)
    private TransNode transNode;

    @Column(length = 20)
    private String scheduledTimepointType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName8() {
        return name8;
    }

    public void setName8(String name8) {
        this.name8 = name8;
    }

    public String getName20() {
        return name20;
    }

    public void setName20(String name20) {
        this.name20 = name20;
    }

    public String getName40() {
        return name40;
    }

    public void setName40(String name40) {
        this.name40 = name40;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public String getTimepointStatus() {
        return timepointStatus;
    }

    public void setTimepointStatus(String status) {
        this.timepointStatus = status;
    }

    public String getTimepointType() {
        return timepointType;
    }

    public void setTimepointType(String type) {
        this.timepointType = type;
    }

    public TransNode getTransNode() {
        return transNode;
    }

    public void setTransNode(TransNode node) {
        this.transNode = node;
    }

    public String getScheduledTimepointType() {
        return scheduledTimepointType;
    }

    public void setScheduledTimepointType(String timepointType) {
        this.scheduledTimepointType = timepointType;
    }

    @Override
    public String toString() {
        return "Timepoint[id=" + id + " dbModDate=" + dbModDate + "]";
    }
}
