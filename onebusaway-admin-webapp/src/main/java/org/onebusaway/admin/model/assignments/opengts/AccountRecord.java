/**
 * Copyright (C) 2019 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.model.assignments.opengts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "Account",
        "Account_desc",
        "TimeZone",
        "DeviceList"
})
public class AccountRecord {

    @JsonProperty("Account")
    private String account;
    @JsonProperty("Account_desc")
    private String accountDesc;
    @JsonProperty("TimeZone")
    private String timeZone;
    @JsonProperty("DeviceList")
    private List<DeviceList> deviceList = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("Account")
    public String getAccount() {
        return account;
    }

    @JsonProperty("Account")
    public void setAccount(String account) {
        this.account = account;
    }

    @JsonProperty("Account_desc")
    public String getAccountDesc() {
        return accountDesc;
    }

    @JsonProperty("Account_desc")
    public void setAccountDesc(String accountDesc) {
        this.accountDesc = accountDesc;
    }

    @JsonProperty("TimeZone")
    public String getTimeZone() {
        return timeZone;
    }

    @JsonProperty("TimeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty("DeviceList")
    public List<DeviceList> getDeviceList() {
        return deviceList;
    }

    @JsonProperty("DeviceList")
    public void setDeviceList(List<DeviceList> deviceList) {
        this.deviceList = deviceList;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}