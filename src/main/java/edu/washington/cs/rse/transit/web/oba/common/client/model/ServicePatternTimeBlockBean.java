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
package edu.washington.cs.rse.transit.web.oba.common.client.model;

public class ServicePatternTimeBlockBean extends ApplicationBean implements Comparable<ServicePatternTimeBlockBean> {

    private static final long serialVersionUID = 1L;

    private ServicePatternBean servicePattern;

    private String scheduleType;

    private int minPassingTime;

    private int maxPassingTime;

    public ServicePatternBean getServicePattern() {
        return servicePattern;
    }

    public void setServicePattern(ServicePatternBean servicePattern) {
        this.servicePattern = servicePattern;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public int getMinPassingTime() {
        return minPassingTime;
    }

    public void setMinPassingTime(int minPassingTime) {
        this.minPassingTime = minPassingTime;
    }

    public int getMaxPassingTime() {
        return maxPassingTime;
    }

    public void setMaxPassingTime(int maxPassingTime) {
        this.maxPassingTime = maxPassingTime;
    }

    /***************************************************************************
     * {@link Comparable} Interface
     **************************************************************************/

    public int compareTo(ServicePatternTimeBlockBean o) {
        int a = this.minPassingTime;
        int b = o.minPassingTime;
        return a == b ? 0 : (a < b ? -1 : 1);
    }
}
