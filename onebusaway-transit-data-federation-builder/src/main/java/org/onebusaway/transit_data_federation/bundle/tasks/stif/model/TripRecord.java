/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

public class TripRecord implements StifRecord {
  private String signCode;
  private String blockNumber;
  private int originTime;
  private int tripType;
  private int destinationTime;
  private String originLocation;
  private String signCodeRoute;
  private String reliefRun;
  private String runNumber;
  private String previousRunNumber;
  private int reliefTime;
  private String reliefRunRoute;
  private String runRoute;
  private String nextTripOperatorRunNumber;
  private String nextTripOperatorRunRoute;
  private String nextTripOperatorDepotCode;
  private String previousRunRoute;
  private String destinationLocation;
  private int recoveryTime;
  private boolean lastTripInSequence;
  private boolean firstTripInSequence;
  private String depotCode;
  private String gtfsTripId;

  public void setSignCode(String signCode) {
    this.signCode = signCode.replaceAll("^0+", "");
  }

  public String getSignCode() {
    return signCode;
  }

  public void setBlockNumber(String blockNumber) {
    if (!"".equals(blockNumber))
      this.blockNumber = blockNumber;
  }

  public String getBlockNumber() {
    return blockNumber;
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
    this.signCodeRoute = signCodeRoute.replaceFirst("^([a-zA-Z]+)0+", "$1").toUpperCase();
  }

  public String getReliefRunNumber() {
    if (reliefRun == null) {
      return runNumber;
    }
    return reliefRun;
  }

  public void setReliefRunNumber(String run) {
    this.reliefRun = run;
  }

  public String getRunNumber() {
    return runNumber;
  }

  public void setRunNumber(String run) {
    this.runNumber = run;
  }

  public String getPreviousRunNumber() {
    return previousRunNumber;
  }

  public String getPreviousRunRoute() {
    return previousRunRoute;
  }

  public String getPreviousRunId() {
    return RunTripEntry.createId(getPreviousRunRoute(), getPreviousRunNumber());
  }

  public void setPreviousRunNumber(String previousRun) {
    this.previousRunNumber = previousRun;
  }

  public void setReliefTime(int reliefTime) {
    this.reliefTime = reliefTime;
  }

  public int getReliefTime() {
    return reliefTime;
  }

  public String getRunRoute() {
    return runRoute;
  }

  public String getReliefRunRoute() {
    return reliefRunRoute;
  }

  public String getRunId() {
    return RunTripEntry.createId(getRunRoute(), getRunNumber());
  }

  public String getRunIdWithDepot() {
    if ("MISC".equals(getRunRoute())) {
      return RunTripEntry.createId(getRunRoute() + "-" + getDepotCode(), getRunNumber());
    }
    return RunTripEntry.createId(getRunRoute(), getRunNumber());
  }
  
  public String getReliefRunId() {
    return RunTripEntry.createId(getReliefRunRoute(), getReliefRunNumber());
  }

  public void setReliefRunRoute(String reliefRunRoute) {
    this.reliefRunRoute = reliefRunRoute;
  }

  public void setRunRoute(String runRoute) {
    this.runRoute = runRoute;
  }

  public void setNextTripOperatorRunNumber(String runNumber) {
    this.nextTripOperatorRunNumber = runNumber;
  }

  public String getNextTripOperatorRunId() {
    return RunTripEntry.createId(getNextTripOperatorRunRoute(),
        getNextTripOperatorRunNumber());
  }

  public String getNextTripOperatorRunIdWithDepot() {
    if ("MISC".equals(getNextTripOperatorRunRoute())) {
      return RunTripEntry.createId(getNextTripOperatorRunRoute() + "-" + getDepotCode(), getNextTripOperatorRunNumber());
    }
    return RunTripEntry.createId(getNextTripOperatorRunRoute(), getNextTripOperatorRunNumber());
  }

  
  public String getNextTripOperatorRunNumber() {
    return nextTripOperatorRunNumber;
  }

  public String getNextTripOperatorRunRoute() {
    return nextTripOperatorRunRoute;
  }

  public void setNextTripOperatorRunRoute(String route) {
    this.nextTripOperatorRunRoute = route;
  }

  public void setPreviousRunRoute(String route) {
    this.previousRunRoute = route;
  }

  public String getDestinationLocation() {
    return destinationLocation;
  }

  public void setDestinationLocation(String destinationLocation) {
    this.destinationLocation = destinationLocation;
  }

  public int getRecoveryTime() {
    return recoveryTime;
  }

  public void setRecoveryTime(int recoveryTime) {
    this.recoveryTime = recoveryTime;
  }

  public void setLastTripInSequence(boolean last) {
    this.lastTripInSequence = last;
  }

  public void setFirstTripInSequence(boolean first) {
    this.firstTripInSequence = first;
  }

  public boolean isFirstTripInSequence() {
    return firstTripInSequence;
  }

  public boolean isLastTripInSequence() {
    return lastTripInSequence;
  }

  public String getDepotCode() {
    return depotCode;
  }

  public void setDepotCode(String depotCode) {
    this.depotCode = depotCode;
  }

  public void setGtfsTripId(String gtfsTripId) {
    this.gtfsTripId = gtfsTripId;
  }

  public String getGtfsTripId() {
    return gtfsTripId;
  }

  public String getNextTripOperatorDepotCode() {
    return nextTripOperatorDepotCode;
  }
  
  public void setNextTripOperatorDepotCode(String depotCode) {
    this.nextTripOperatorDepotCode = depotCode;
  }

}
