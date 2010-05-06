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
package edu.washington.cs.rse.transit.web.actions;

public class ScheduleByStopBean implements Comparable<ScheduleByStopBean> {

    private int _passingTime;

    private String _passingTimeAsString;

    private boolean _peakFlag = false;

    public int getPassingTime() {
        return _passingTime;
    }

    public void setPassingTime(int passingTime) {
        _passingTime = passingTime;
    }

    public String getPassingTimeAsString() {
        return _passingTimeAsString;
    }

    public void setPassingTimeAsString(String passingTimeAsString) {
        _passingTimeAsString = passingTimeAsString;
    }

    public void setPeakFlag(boolean isPeakFlag) {
        _peakFlag = isPeakFlag;
    }

    public boolean getPeakFlag() {
        return _peakFlag;
    }

    /***************************************************************************
     * {@link Comparable} Interface
     **************************************************************************/

    public int compareTo(ScheduleByStopBean o) {
        if (_passingTime == o.getPassingTime())
            return 0;
        return _passingTime < o.getPassingTime() ? -1 : 1;
    }
}
