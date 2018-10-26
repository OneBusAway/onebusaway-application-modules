package org.onebusaway.statusagent.model;

import org.codehaus.jackson.annotate.JsonProperty;

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