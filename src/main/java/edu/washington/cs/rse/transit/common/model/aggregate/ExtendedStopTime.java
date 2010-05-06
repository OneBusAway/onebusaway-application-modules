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
package edu.washington.cs.rse.transit.common.model.aggregate;

import java.io.Serializable;

import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.StopTime;
import edu.washington.cs.rse.transit.common.model.Trip;

public class ExtendedStopTime implements Serializable, ICommonStopTime {

    private static final long serialVersionUID = 1L;

    private StopTime stopTime;

    private Trip trip;

    public ExtendedStopTime(StopTime stopTime, Trip trip) {
        this.stopTime = stopTime;
        this.trip = trip;
    }
    
    public StopTime getStopTime() {
        return stopTime;
    }

    /***************************************************************************
     * {@link ICommonStopTime} Interface
     **************************************************************************/

    public ServicePattern getServicePattern() {
        return stopTime.getServicePattern();
    }

    public Trip getTrip() {
        return trip;
    }

    public double getPassingTime() {
        return stopTime.getPassingTime();
    }

    /***************************************************************************
     * {@link Object} Interface
     **************************************************************************/

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ExtendedStopTime))
            return false;

        if (!super.equals(obj))
            return false;

        ExtendedStopTime est = (ExtendedStopTime) obj;
        return this.stopTime.equals(est.stopTime) && this.trip.equals(est.trip);
    }

    @Override
    public int hashCode() {
        return stopTime.hashCode() + trip.hashCode();
    }
}
