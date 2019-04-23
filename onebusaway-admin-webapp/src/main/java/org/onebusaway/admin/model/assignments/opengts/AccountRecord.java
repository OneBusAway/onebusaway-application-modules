
package org.onebusaway.admin.model.assignments.opengts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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