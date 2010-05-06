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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_pattern_timepoints")
public class PatternTimepoints extends IdentityBean {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    private ServicePattern servicePattern;

    @Index(name="patternTimepointPosition")
    private int patternTimepointPosition;

    private Date dbModDate;

    @ManyToOne(fetch=FetchType.LAZY)
    private Timepoint timepoint;

    @ManyToOne(fetch=FetchType.LAZY)
    private TPI tpi;

    private Date effectiveDate;

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

    public int getPatternTimepointPosition() {
        return patternTimepointPosition;
    }

    public void setPatternTimepointPosition(int patternTimepointPosition) {
        this.patternTimepointPosition = patternTimepointPosition;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public Timepoint getTimepoint() {
        return timepoint;
    }

    public void setTimepoint(Timepoint timepoint) {
        this.timepoint = timepoint;
    }

    public TPI getTpi() {
        return tpi;
    }

    public void setTpi(TPI tpi) {
        this.tpi = tpi;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getFirstLastFlag() {
        return firstLastFlag;
    }

    public void setFirstLastFlag(String firstLastFlag) {
        this.firstLastFlag = firstLastFlag;
    }
}
