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
package org.onebusaway.oba.web.common.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class TimedPlaceBean implements Serializable, IsSerializable {

  private static final long serialVersionUID = 1L;

  private String placeId;

  private String stopId;

  private int time;

  public TimedPlaceBean() {

  }

  public TimedPlaceBean(String placeId, String stopId, int time) {

    this.placeId = placeId;
    this.stopId = stopId;
    this.time = time;
  }

  public String getPlaceId() {
    return placeId;
  }

  public void setPlaceId(String placeId) {
    this.placeId = placeId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getTime() {
    return time;
  }
}
