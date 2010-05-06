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
import java.text.DateFormat;

import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.Trip;

public class ScheduledArrivalTime implements Serializable, Comparable<ScheduledArrivalTime> {

    private static final long serialVersionUID = 1L;

    private static DateFormat _format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private ICommonStopTime _stopTime;

    private long _scheduledTime;

    private long _predictedTime = -1;

    public ScheduledArrivalTime(ICommonStopTime stopTime, long time) {
        _stopTime = stopTime;
        _scheduledTime = time;
    }

    public ICommonStopTime getStopTime() {
        return _stopTime;
    }

    public ServicePattern getServicePattern() {
        return _stopTime.getServicePattern();
    }

    public Route getRoute() {
        return getServicePattern().getRoute();
    }

    public Trip getTrip() {
        return _stopTime.getTrip();
    }

    /***************************************************************************
     * Timing
     **************************************************************************/

    public long getScheduledTime() {
        return _scheduledTime;
    }

    public void setScheduledTime(long time) {
        _scheduledTime = time;
    }

    public boolean hasPredictedTime() {
        return _predictedTime > 0;
    }

    public long getPredictedTime() {
        return _predictedTime;
    }

    public void setPredictedTime(long time) {
        if (time < 1210000000000L)
            throw new IllegalStateException("BAD");
        _predictedTime = time;
    }

    public long getBestTime() {
        if (hasPredictedTime())
            return getPredictedTime();
        return getScheduledTime();
    }

    public long getMinTime() {
        long t = getScheduledTime();
        if (hasPredictedTime())
            t = Math.min(t, getPredictedTime());
        return t;
    }

    public long getMaxTime() {
        long t = getScheduledTime();
        if (hasPredictedTime())
            t = Math.max(t, getPredictedTime());
        return t;
    }

    /***************************************************************************
     * {@link Comparable} Interface
     **************************************************************************/

    public int compareTo(ScheduledArrivalTime o) {
        long a = getBestTime();
        long b = getBestTime();
        return a == b ? 0 : (a < b ? -1 : 1);
    }

    /***************************************************************************
     * {@link Object} Interface
     **************************************************************************/

    @Override
    public boolean equals(Object obj) {

        if (obj == null && !(obj instanceof ScheduledArrivalTime))
            return false;

        if (!super.equals(obj))
            return false;

        ScheduledArrivalTime sad = (ScheduledArrivalTime) obj;

        return _scheduledTime == sad._scheduledTime;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + new Long(_scheduledTime).hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("route=").append(getRoute().getNumber());
        b.append(" dest=").append(getServicePattern().getSpecificDestination());
        b.append(" scheduled=").append(_format.format(_scheduledTime));
        if (hasPredictedTime())
            b.append(" predicted=").append(_format.format(_predictedTime));
        return b.toString();
    }
}
