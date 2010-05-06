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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_change_dates")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ChangeDate extends IdentityBean implements Serializable, Comparable<ChangeDate> {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    @Column(length = 6)
    private String bookingId;

    private Date startDate;

    private Date dbModDate;

    private Date minorChangeDate;

    private Date endDate;

    @Column(length = 8)
    private String currentNextCode;

    private Date effectiveBeginDate;

    private Date effectiveEndDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public Date getStartDate() {
        return startDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public Date getMinorChangeDate() {
        return minorChangeDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setMinorChangeDate(Date minorChangeDate) {
        this.minorChangeDate = minorChangeDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getCurrentNextCode() {
        return currentNextCode;
    }

    public void setCurrentNextCode(String currentNextCode) {
        this.currentNextCode = currentNextCode;
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

    public int compareTo(ChangeDate o) {
        return compareToById(o);
    }

    @Override
    public String toString() {
        return "cd=" + id;
    }
}
