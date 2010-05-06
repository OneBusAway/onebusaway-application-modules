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
import edu.washington.cs.rse.transit.common.model.Trip;

public class InterpolatedStopTime implements Serializable, ICommonStopTime {

    private static final long serialVersionUID = 1L;

    private StopTimepointInterpolation interpolation;

    private Trip trip;

    private double passingTime;

    public InterpolatedStopTime(StopTimepointInterpolation interpolation, Trip trip, double passingTime) {
        this.interpolation = interpolation;
        this.trip = trip;
        this.passingTime = passingTime;
    }

    public StopTimepointInterpolation getInterpolation() {
        return interpolation;
    }

    public ServicePattern getServicePattern() {
        return interpolation.getServicePattern();
    }

    public Trip getTrip() {
        return trip;
    }

    public double getPassingTime() {
        return passingTime;
    }
    
    @Override
    public String toString() {
        return trip.getScheduleType() + " " + this.passingTime;
    }

}
