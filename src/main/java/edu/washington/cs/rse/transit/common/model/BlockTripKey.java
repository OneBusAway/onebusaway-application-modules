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

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Embeddable
public class BlockTripKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    private ChangeDate changeDate;

    @ManyToOne(fetch=FetchType.EAGER)
    private Trip trip;

    private int id;

    public BlockTripKey() {

    }

    public BlockTripKey(ChangeDate changeDate, Trip trip, int id) {
        this.changeDate = changeDate;
        this.trip = trip;
        this.id = id;
    }

    public ChangeDate getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(ChangeDate changeDate) {
        this.changeDate = changeDate;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BlockTripKey))
            return false;
        BlockTripKey key = (BlockTripKey) obj;
        return this.changeDate.equals(key.changeDate) && this.trip.equals(key.trip) && this.id == key.id;
    }

    @Override
    public int hashCode() {
        return this.trip.hashCode() + 3 * this.id;
    }
}
