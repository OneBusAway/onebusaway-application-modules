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
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
        "Device",
        "Timestamp",
        "Timestamp_date",
        "Timestamp_time",
        "StatusCode",
        "StatusCode_hex",
        "StatusCode_desc",
        "GPSPoint",
        "GPSPoint_lat",
        "GPSPoint_lon",
        "Speed_kph",
        "Speed",
        "Speed_units",
        "Odometer_km",
        "Odometer",
        "Odometer_units",
        "Index"
})
public class EventDatum {

    @JsonProperty("Device")
    private String device;
    @JsonProperty("Timestamp")
    private Integer timestamp;
    @JsonProperty("Timestamp_date")
    private String timestampDate;
    @JsonProperty("Timestamp_time")
    private String timestampTime;
    @JsonProperty("StatusCode")
    private Integer statusCode;
    @JsonProperty("StatusCode_hex")
    private String statusCodeHex;
    @JsonProperty("StatusCode_desc")
    private String statusCodeDesc;
    @JsonProperty("GPSPoint")
    private String gPSPoint;
    @JsonProperty("GPSPoint_lat")
    private Double gPSPointLat;
    @JsonProperty("GPSPoint_lon")
    private Double gPSPointLon;
    @JsonProperty("Speed_kph")
    private Double speedKph;
    @JsonProperty("Speed")
    private Double speed;
    @JsonProperty("Speed_units")
    private String speedUnits;
    @JsonProperty("Odometer_km")
    private Double odometerKm;
    @JsonProperty("Odometer")
    private Double odometer;
    @JsonProperty("Odometer_units")
    private String odometerUnits;
    @JsonProperty("Index")
    private Integer index;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("Device")
    public String getDevice() {
        return device;
    }

    @JsonProperty("Device")
    public void setDevice(String device) {
        this.device = device;
    }

    @JsonProperty("Timestamp")
    public Integer getTimestamp() {
        return timestamp;
    }

    @JsonProperty("Timestamp")
    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("Timestamp_date")
    public String getTimestampDate() {
        return timestampDate;
    }

    @JsonProperty("Timestamp_date")
    public void setTimestampDate(String timestampDate) {
        this.timestampDate = timestampDate;
    }

    @JsonProperty("Timestamp_time")
    public String getTimestampTime() {
        return timestampTime;
    }

    @JsonProperty("Timestamp_time")
    public void setTimestampTime(String timestampTime) {
        this.timestampTime = timestampTime;
    }

    @JsonProperty("StatusCode")
    public Integer getStatusCode() {
        return statusCode;
    }

    @JsonProperty("StatusCode")
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    @JsonProperty("StatusCode_hex")
    public String getStatusCodeHex() {
        return statusCodeHex;
    }

    @JsonProperty("StatusCode_hex")
    public void setStatusCodeHex(String statusCodeHex) {
        this.statusCodeHex = statusCodeHex;
    }

    @JsonProperty("StatusCode_desc")
    public String getStatusCodeDesc() {
        return statusCodeDesc;
    }

    @JsonProperty("StatusCode_desc")
    public void setStatusCodeDesc(String statusCodeDesc) {
        this.statusCodeDesc = statusCodeDesc;
    }

    @JsonProperty("GPSPoint")
    public String getGPSPoint() {
        return gPSPoint;
    }

    @JsonProperty("GPSPoint")
    public void setGPSPoint(String gPSPoint) {
        this.gPSPoint = gPSPoint;
    }

    @JsonProperty("GPSPoint_lat")
    public Double getGPSPointLat() {
        return gPSPointLat;
    }

    @JsonProperty("GPSPoint_lat")
    public void setGPSPointLat(Double gPSPointLat) {
        this.gPSPointLat = gPSPointLat;
    }

    @JsonProperty("GPSPoint_lon")
    public Double getGPSPointLon() {
        return gPSPointLon;
    }

    @JsonProperty("GPSPoint_lon")
    public void setGPSPointLon(Double gPSPointLon) {
        this.gPSPointLon = gPSPointLon;
    }

    @JsonProperty("Speed_kph")
    public Double getSpeedKph() {
        return speedKph;
    }

    @JsonProperty("Speed_kph")
    public void setSpeedKph(Double speedKph) {
        this.speedKph = speedKph;
    }

    @JsonProperty("Speed")
    public Double getSpeed() {
        return speed;
    }

    @JsonProperty("Speed")
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    @JsonProperty("Speed_units")
    public String getSpeedUnits() {
        return speedUnits;
    }

    @JsonProperty("Speed_units")
    public void setSpeedUnits(String speedUnits) {
        this.speedUnits = speedUnits;
    }

    @JsonProperty("Odometer_km")
    public Double getOdometerKm() {
        return odometerKm;
    }

    @JsonProperty("Odometer_km")
    public void setOdometerKm(Double odometerKm) {
        this.odometerKm = odometerKm;
    }

    @JsonProperty("Odometer")
    public Double getOdometer() {
        return odometer;
    }

    @JsonProperty("Odometer")
    public void setOdometer(Double odometer) {
        this.odometer = odometer;
    }

    @JsonProperty("Odometer_units")
    public String getOdometerUnits() {
        return odometerUnits;
    }

    @JsonProperty("Odometer_units")
    public void setOdometerUnits(String odometerUnits) {
        this.odometerUnits = odometerUnits;
    }

    @JsonProperty("Index")
    public Integer getIndex() {
        return index;
    }

    @JsonProperty("Index")
    public void setIndex(Integer index) {
        this.index = index;
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