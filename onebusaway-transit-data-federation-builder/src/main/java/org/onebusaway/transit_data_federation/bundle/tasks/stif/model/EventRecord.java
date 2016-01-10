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

public class EventRecord implements StifRecord {

  public enum BoardAlightFlag {
    NEITHER(0), BOARDING(1), ALIGHTING(2), BOTH(3), INVALID(4);

    private int flag;

    private BoardAlightFlag(int f) {
      flag = f;
    }

    public int getCode() {
      return flag;
    }

    public static BoardAlightFlag get(int boardAlightFlag) {
      switch (boardAlightFlag) {
        case 0:
          return NEITHER;
        case 1:
          return BOARDING;
        case 2:
          return ALIGHTING;
        case 3:
          return BOTH;
        default:
          return INVALID;
      }
    }

  }

  private String location;
  private int time;
  private boolean revenue;
  private boolean timepoint;
  private String locationTypeCode;
  private int boardAlightFlag;

  public String getLocation() {
    return location;
  }

  public int getTime() {
    return time;
  }

  public boolean isRevenue() {
    return revenue;
  }

  public boolean isTimepoint() {
    return timepoint;
  }

  public String getLocationTypeCode() {
    return locationTypeCode;
  }

  public BoardAlightFlag getBoardAlightFlag() {
    return BoardAlightFlag.get(boardAlightFlag);
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public void setRevenue(boolean revenue) {
    this.revenue = revenue;
  }

  public void setTimepoint(boolean timepoint) {
    this.timepoint = timepoint;
  }

  public void setLocationTypeCode(String locationTypeCode) {
    this.locationTypeCode = locationTypeCode;
  }

  public void setBoardAlightFlag(int flag) {
    this.boardAlightFlag = flag;
  }

  public void setDistanceFromStartOfTrip(int integer) {
    // TODO Auto-generated method stub

  }

}
