/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.model;

/**
 * API queries generated from the parsed csv bundle check file to be used to test 
 * the Bundle Validation Checks.
 * @author jpearson
 *
 */
public class BundleValidateQuery {
  private int linenum;
  private String specificTest;
  private String routeOrStop;
  private String routeId;
  private String stopId;
  private String serviceDate;
  private String departureTime;
  private String query;
  private String queryResult;
  private String errorMessage;
  
  public int getLinenum() {
    return linenum;
  }
  public void setLinenum(int linenum) {
    this.linenum = linenum;
  }
  public String getSpecificTest() {
    return specificTest;
  }
  public void setSpecificTest(String specificTest) {
    this.specificTest = specificTest;
  }
  public String getRouteOrStop() {
    return routeOrStop;
  }
  public void setRouteOrStop(String routeOrStop) {
    this.routeOrStop = routeOrStop;
  }
  public String getRouteId() {
    return routeId;
  }
  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }
  public String getStopId() {
    return stopId;
  }
  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
  public String getServiceDate() {
    return serviceDate;
  }
  public void setServiceDate(String serviceDate) {
    this.serviceDate = serviceDate;
  }
  public String getDepartureTime() {
    return departureTime;
  }
  public void setDepartureTime(String departureTime) {
    this.departureTime = departureTime;
  }
  public String getQuery() {
    return query;
  }
  public void setQuery(String query) {
    this.query = query;
  }
  public String getQueryResult() {
    return queryResult;
  }
  public void setQueryResult(String queryResult) {
    this.queryResult = queryResult;
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
