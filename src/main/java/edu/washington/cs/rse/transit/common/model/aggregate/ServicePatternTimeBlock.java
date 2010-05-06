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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.washington.cs.rse.transit.common.model.IdentityBean;
import edu.washington.cs.rse.transit.common.model.ServicePattern;

@Entity
@Table(name = "transit_service_pattern_time_blocks")
public class ServicePatternTimeBlock extends IdentityBean {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    private ServicePattern servicePattern;

    private String scheduleType;

    private int minPassingTime;

    private int maxPassingTime;

    public ServicePatternTimeBlock() {

    }

    public ServicePatternTimeBlock(ServicePattern servicePattern, String scheduleType, int minPassingTime,
            int maxPassingTime) {
        this.servicePattern = servicePattern;
        this.scheduleType = scheduleType;
        this.minPassingTime = minPassingTime;
        this.maxPassingTime = maxPassingTime;
    }

    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public ServicePattern getServicePattern() {
        return servicePattern;
    }

    public void setServicePattern(ServicePattern servicePattern) {
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
}
