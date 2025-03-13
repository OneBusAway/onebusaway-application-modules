/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.impl.util;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationUtil {
  
  private String protocol = "http";
  private String transiTimeHost = "localhost";
  private String transiTimePort = "8080";
  private String transiTimeKey = "8a3273b0";
  private String baseUrlOverride = null;
  private String predictionCommand;
  private String tripUpdatesCommand;
  private String vehiclePositionsCommand;
  private Map<String, String> headersMap = new HashMap<>();
  private Map<String, String> paramsMap = new HashMap<>();

  private Map<String,String> agencyMapper = new HashMap<String,String>(1);
  private int httpTimeoutSeconds = 15;
  
  public ConfigurationUtil(){}

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getTransiTimeHost() {
    return transiTimeHost;
  }

  public void setTransiTimeHost(String transiTimeHost) {
    this.transiTimeHost = transiTimeHost;
  }

  public String getTransiTimePort() {
    return transiTimePort;
  }

  public void setTransiTimePort(String transitTimePort) {
    this.transiTimePort = transitTimePort;
  }

  public String getTransiTimeKey() {
    return transiTimeKey;
  }

  public void setTransiTimeKey(String transiTimeKey) {
    this.transiTimeKey = transiTimeKey;
  }

  public Map<String,String> getAgencyMapper() {
    return agencyMapper;
  }

  public void setAgencyMapper(Map<String,String> agencyMapper) {
    this.agencyMapper = agencyMapper;
  }
  
  public int getHttpTimeoutSeconds() {
    return httpTimeoutSeconds;
  }

  public void setHttpTimeoutSeconds(int httpTimeoutSeconds) {
    this.httpTimeoutSeconds = httpTimeoutSeconds;
  }

  public String getBaseUrlOverride() {
    return baseUrlOverride;
  }

  public void setBaseUrlOverride(String urlOverride) {
    this.baseUrlOverride = urlOverride;
  }

  public void setPredictionCommand(String predictionCommand) {
    this.predictionCommand = predictionCommand;
  }

  public void setTripUpdatesCommand(String tripUpdatesCommand) {
    this.tripUpdatesCommand = tripUpdatesCommand;
  }

  public void setVehiclePositionsCommand(String vehiclePositionsCommand) {
    this.vehiclePositionsCommand = vehiclePositionsCommand;
  }

  public Map<String, String> getHeadersMap() {
    return headersMap;
  }

  public void setHeadersMap(Map<String, String> headersMap) {
    this.headersMap = headersMap;
  }

  public Map<String, String> getParamsMap() {
    return paramsMap;
  }

  public void setParamsMap(Map<String, String> paramsMap) {
    this.paramsMap = paramsMap;
  }

  public String getPredictionCommand() {
    return predictionCommand;
  }

  public String getTripUpdatesCommand() {
    return tripUpdatesCommand;
  }

  public String getVehiclePositionsCommand() {
    return vehiclePositionsCommand;
  }

  public String toString() {
    return "{" + transiTimeHost + ":" + transiTimePort + "/" + transiTimeKey + "}";
  }

}
