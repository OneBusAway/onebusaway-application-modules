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

import edu.washington.cs.rse.transit.common.model.aggregate.ICommonStopTime;

@Entity
@Table(name = "transit_stop_times")
public class StopTime extends IdentityBean implements ICommonStopTime {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    int id;

    @ManyToOne
    private ServicePattern servicePattern;

    @ManyToOne
    private Trip trip;

    @ManyToOne
    private Timepoint timepoint;

    @Index(name = "stopTimePosition")
    private int stopTimePosition;

    private double passingTime;

    private Date dbModeDate;

    private int patternTimepointPosition;

    @Column(length = 1)
    private String firstLastFlag;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ServicePattern getServicePattern() {
        return servicePattern;
    }

    public void setServicePattern(ServicePattern servicePattern) {
        this.servicePattern = servicePattern;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Timepoint getTimepoint() {
        return timepoint;
    }

    public void setTimepoint(Timepoint timepoint) {
        this.timepoint = timepoint;
    }

    public int getStopTimePosition() {
        return stopTimePosition;
    }

    public void setStopTimePosition(int stopTimePosition) {
        this.stopTimePosition = stopTimePosition;
    }

    public Date getDbModeDate() {
        return dbModeDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModeDate(Date dbModeDate) {
        this.dbModeDate = dbModeDate;
    }

    public double getPassingTime() {
        return passingTime;
    }

    public void setPassingTime(double passintTime) {
        this.passingTime = passintTime;
    }

    public int getPatternTimepointPosition() {
        return patternTimepointPosition;
    }

    public void setPatternTimepointPosition(int patternTimepointPosition) {
        this.patternTimepointPosition = patternTimepointPosition;
    }

    public String getFirstLastFlag() {
        return firstLastFlag;
    }

    public void setFirstLastFlag(String firstLastFlag) {
        this.firstLastFlag = firstLastFlag;
    }
}
