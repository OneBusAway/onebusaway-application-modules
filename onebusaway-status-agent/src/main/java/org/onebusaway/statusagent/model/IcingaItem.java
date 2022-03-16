/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.statusagent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IcingaItem {



    /*
    create table icinga_items
    (
    ID int not null AUTO_INCREMENT,
    SERVICE_NAME varchar(255),
    SERVICE_DISPLAY_NAME varchar(255),
    SERVICE_CURRENT_STATE int,
    SERVICE_OUTPUT varchar(255),
    SERVICE_PERFDATA varchar(255),
    SERVICE_IS_PENDING int,
    UPDATED_ON timestamp,
    PRIMARY KEY (ID)
    )
    */

    @JsonProperty("SERVICE_NAME")
    String serviceName;

    @JsonProperty("SERVICE_DISPLAY_NAME")
    String serviceDisplayName;

    @JsonProperty("SERVICE_CURRENT_STATE")
    int serviceCurrentState;

    @JsonProperty("SERVICE_OUTPUT")
    String serviceOutput;

    @JsonProperty("SERVICE_PERFDATA")
    String servicePerfdata;

    @JsonProperty("SERVICE_IS_PENDING")
    int serviceIsPending;

    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public String getServiceDisplayName() {
        return serviceDisplayName;
    }
    public void setServiceDisplayName(String serviceDisplayName) {
        this.serviceDisplayName = serviceDisplayName;
    }
    public int getServiceCurrentState() {
        return serviceCurrentState;
    }
    public void setServiceCurrentState(int serviceCurrentState) {
        this.serviceCurrentState = serviceCurrentState;
    }
    public String getServiceOutput() {
        return serviceOutput;
    }
    public void setServiceOutput(String serviceOutput) {
        this.serviceOutput = serviceOutput;
    }
    public String getServicePerfdata() {
        return servicePerfdata;
    }
    public void setServicePerfdata(String servicePerfdata) {
        this.servicePerfdata = servicePerfdata;
    }
    public int getServiceIsPending() {
        return serviceIsPending;
    }
    public void setServiceIsPending(int serviceIsPending) {
        this.serviceIsPending = serviceIsPending;
    }

}