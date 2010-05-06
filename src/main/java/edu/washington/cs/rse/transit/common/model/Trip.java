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

import org.hibernate.annotations.Index;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_trips")
public class Trip extends IdentityBean implements Comparable<Trip> {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    @ManyToOne
    private ServicePattern servicePattern;

    private Date dbModDate;

    @Column(length = 20)
    private String directionName;

    @Column(length = 1)
    private String liftFlag;

    @Column(length = 1)
    private String peakFlag;

    @Column(length = 5)
    private String scheduleTripId;

    @Column(length = 10)
    @Index(name="scheduleTypeIndex")
    private String scheduleType;

    @Column(length = 2)
    private String exceptionCode;

    private Date updateDate;

    private int controlPointTime;

    private int patternIdFollowing;

    private int patternIdPrior;

    @Column(length = 7)
    private String tripLink;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ServicePattern getServicePattern() {
        return servicePattern;
    }

    public void setServicePattern(ServicePattern pattern) {
        this.servicePattern = pattern;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    public String getLiftFlag() {
        return liftFlag;
    }

    public void setLiftFlag(String liftFlag) {
        this.liftFlag = liftFlag;
    }

    public String getPeakFlag() {
        return peakFlag;
    }

    public void setPeakFlag(String peakFlag) {
        this.peakFlag = peakFlag;
    }

    public String getScheduleTripId() {
        return scheduleTripId;
    }

    public void setScheduleTripId(String scheduleTripId) {
        this.scheduleTripId = scheduleTripId;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public int getControlPointTime() {
        return controlPointTime;
    }

    public void setControlPointTime(int controlPointTime) {
        this.controlPointTime = controlPointTime;
    }

    public int getPatternIdFollowing() {
        return patternIdFollowing;
    }

    public void setPatternIdFollowing(int patternIdFollowing) {
        this.patternIdFollowing = patternIdFollowing;
    }

    public int getPatternIdPrior() {
        return patternIdPrior;
    }

    public void setPatternIdPrior(int patternIdPrior) {
        this.patternIdPrior = patternIdPrior;
    }

    public String getTripLink() {
        return tripLink;
    }

    public void setTripLink(String tripLink) {
        this.tripLink = tripLink;
    }

    public int compareTo(Trip o) {
        return compareToById(o);
    }
}
