/*
 * Copyright 2008 Brian Ferris
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
package edu.washington.cs.rse.transit.common.model.aggregate;

import java.io.Serializable;

public class BusArrivalEstimateBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int _timepointId;

  private int _goalDeviation;

  private int _schedTime;

  private long _timestamp;

  private int _goalTime;

  private String _type;

  private int _distanceToGoal;

  private String _destination;

  private int _route;

  public BusArrivalEstimateBean() {

  }

  public void setTimepointId(int timepointId) {
    _timepointId = timepointId;
  }

  public int getTimepointId() {
    return _timepointId;
  }

  public String getDestination() {
    return _destination;
  }

  public void setDestination(String destination) {
    _destination = destination.toLowerCase();
  }

  public int getDistanceToGoal() {
    return _distanceToGoal;
  }

  public void setDistanceToGoal(int toGoal) {
    _distanceToGoal = toGoal;
  }

  public int getGoalDeviation() {
    return _goalDeviation;
  }

  public void setGoalDeviation(int deviation) {
    _goalDeviation = deviation;
  }

  /**
   * @return estimated arrival in seconds since midnight, or -1 if no estimate
   *         is available
   */
  public int getGoalTime() {
    return _goalTime;
  }

  public void setGoalTime(int time) {
    _goalTime = time;
  }

  public int getRoute() {
    return _route;
  }

  public void setRoute(int route) {
    _route = route;
  }

  /**
   * @return scheduled arrival in seconds since midnight
   */
  public int getSchedTime() {
    return _schedTime;
  }

  public void setSchedTime(int time) {
    _schedTime = time;
  }

  public long getTimestamp() {
    return _timestamp;
  }

  public void setTimestamp(long timestamp) {
    _timestamp = timestamp;
  }

  public String getType() {
    return _type;
  }

  public void setType(String type) {
    _type = type.toLowerCase();
  }

  /***************************************************************************
     * 
     **************************************************************************/

  public boolean hasPredictedArrivalTime() {
    return _goalTime != -1;
  }

  @Override
  public String toString() {
    return "route=" + _route + " destination=" + _destination + " scheduled="
        + _schedTime + " predicted=" + _goalTime;
  }
}
