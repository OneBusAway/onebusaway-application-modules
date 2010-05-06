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
import javax.persistence.Id;
import javax.persistence.Table;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_block_trips")
public class BlockTrip extends EntityBean {

    private static final long serialVersionUID = 1L;

    @Id
    private BlockTripKey id;

    private int tripPosition;

    private Date dbModDate;

    private int tripStartTime;

    private int tripEndTime;

    public BlockTripKey getId() {
        return id;
    }

    public void setId(BlockTripKey id) {
        this.id = id;
    }

    public int getTripPosition() {
        return tripPosition;
    }

    public void setTripPosition(int tripPosition) {
        this.tripPosition = tripPosition;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public int getTripStartTime() {
        return tripStartTime;
    }

    public void setTripStartTime(int tripStartTime) {
        this.tripStartTime = tripStartTime;
    }

    public int getTripEndTime() {
        return tripEndTime;
    }

    public void setTripEndTime(int tripEndTime) {
        this.tripEndTime = tripEndTime;
    }
}
