/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

public class TripRecord implements StifRecord {
  private String signCode;
  private String blockNumber;
  private String route;
  private int originTime;
  private int tripType;
  private int destinationTime;
  private String originLocation;
  private String signCodeRoute;
  private String reliefRun;
  private String run;
  private String previousRun;
  private int reliefTime;

  public void setSignCode(String signCode) {
    this.signCode = signCode;
  }

  public String getSignCode() {
    return signCode;
  }

  public void setBlockNumber(String blockNumber) {
    this.blockNumber = blockNumber;
  }

  public String getBlockNumber() {
    return blockNumber;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public String getRoute() {
    return route;
  }

  public void setOriginTime(int seconds) {
    this.originTime = seconds;
  }

  public int getOriginTime() {
    return originTime;
  }

  public void setTripType(int tripType) {
    this.tripType = tripType;
  }

  public int getTripType() {
    return tripType;
  }

  public void setDestinationTime(int destinationTime) {
    this.destinationTime = destinationTime;
  }

  public int getDestinationTime() {
    return destinationTime;
  }

  public void setOriginLocation(String originLocation) {
    this.originLocation = originLocation;
  }

  public String getOriginLocation() {
    return originLocation;
  }

  public String getSignCodeRoute() {
    return signCodeRoute;
  }

  public void setSignCodeRoute(String signCodeRoute) {
    this.signCodeRoute = signCodeRoute;
  }

  public String getReliefRun() {
    if (reliefRun == null) {
      return run;
    }
    return reliefRun;
  }

  public void setReliefRun(String run) {
    this.reliefRun = run;
  }

  public String getRun() {
    return run;
  }

  public void setRun(String run) {
    this.run = run;
  }

  public String getPreviousRun() {
    if (previousRun == null) {
      return run;
    }
    return previousRun;
  }

  public void setPreviousRun(String previousRun) {
    this.previousRun = previousRun;
  }

  public void setReliefTime(int reliefTime) {
    this.reliefTime = reliefTime;
  }
  
  public int getReliefTime() {
    return reliefTime;
  }
}
