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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_pattern_event_sequences")
public class PatternEventSequence extends EntityBean {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private int id;

    private Date dbModDate;

    @Column(length = 3)
    private String directionCode;

    private Date effectiveBeginDate;

    private Date effectiveEndDate;

    @ManyToOne
    private Route route;

    @Column(length = 6)
    private String routePartCode;

    @Column(length = 6)
    private String localExpressCode;

    private int schedulePatternId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public String getDirectionCode() {
        return directionCode;
    }

    public void setDirectionCode(String directionCode) {
        this.directionCode = directionCode;
    }

    public Date getEffectiveBeginDate() {
        return effectiveBeginDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setEffectiveBeginDate(Date effectiveBeginDate) {
        this.effectiveBeginDate = effectiveBeginDate;
    }

    public Date getEffectiveEndDate() {
        return effectiveEndDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setEffectiveEndDate(Date effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getRoutePartCode() {
        return routePartCode;
    }

    public void setRoutePartCode(String routePartCode) {
        this.routePartCode = routePartCode;
    }

    public String getLocalExpressCode() {
        return localExpressCode;
    }

    public void setLocalExpressCode(String localExpressCode) {
        this.localExpressCode = localExpressCode;
    }

    public int getSchedulePatternId() {
        return schedulePatternId;
    }

    public void setSchedulePatternId(int schedulePatternId) {
        this.schedulePatternId = schedulePatternId;
    }
}
