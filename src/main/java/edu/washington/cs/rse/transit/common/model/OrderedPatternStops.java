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

import org.hibernate.annotations.Index;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_ordered_pattern_stops")
public class OrderedPatternStops extends IdentityBean {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    private Route route;

    @Index(name = "schedulePatternId")
    private int schedulePatternId;

    @ManyToOne
    private StopLocation stop;

    private int sequence;

    private Date dbModDate;

    @Column(length = 3)
    private String pptFlag;

    @Column(length = 10)
    private String signOfDestination;

    @Column(length = 10)
    private String signOfDash;

    @Column(length = 10)
    private String assignedToOns;

    @Column(length = 10)
    private String assignedToOffs;

    @Column(length = 6)
    private String routePartCode;

    @Column(length = 3)
    private String showThroughRouteNum;

    @Column(length = 6)
    private String localExpressCode;

    @Column(length = 3)
    private String directionCode;

    private Date effectiveBeginDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public String getPptFlag() {
        return pptFlag;
    }

    public void setPptFlag(String pptFlag) {
        this.pptFlag = pptFlag;
    }

    public String getSignOfDestination() {
        return signOfDestination;
    }

    public void setSignOfDestination(String signOfDestination) {
        this.signOfDestination = signOfDestination;
    }

    public String getSignOfDash() {
        return signOfDash;
    }

    public void setSignOfDash(String signOfDash) {
        this.signOfDash = signOfDash;
    }

    public String getAssignedToOns() {
        return assignedToOns;
    }

    public void setAssignedToOns(String assignedToOns) {
        this.assignedToOns = assignedToOns;
    }

    public String getAssignedToOffs() {
        return assignedToOffs;
    }

    public void setAssignedToOffs(String assignedToOffs) {
        this.assignedToOffs = assignedToOffs;
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

    public String getShowThroughRouteNum() {
        return showThroughRouteNum;
    }

    public void setShowThroughRouteNum(String showThroughRouteNum) {
        this.showThroughRouteNum = showThroughRouteNum;
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

    public void setSchedulePatternId(int schedPatternId) {
        this.schedulePatternId = schedPatternId;
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

    public StopLocation getStop() {
        return stop;
    }

    public void setStop(StopLocation stop) {
        this.stop = stop;
    }

}
