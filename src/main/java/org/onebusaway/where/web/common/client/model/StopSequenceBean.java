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
package org.onebusaway.where.web.common.client.model;

import java.util.ArrayList;
import java.util.List;

public class StopSequenceBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private double startLat;

  private double startLon;

  private double endLat;

  private double endLon;

  private List<PathBean> _paths = new ArrayList<PathBean>();

  private List<StopBean> _stops;

  public StopSequenceBean() {

  }

  public void addPath(PathBean path) {
    _paths.add(path);
  }

  public List<PathBean> getPaths() {
    return _paths;
  }

  public double getStartLat() {
    return startLat;
  }

  public void setStartLat(double startLat) {
    this.startLat = startLat;
  }

  public double getStartLon() {
    return startLon;
  }

  public void setStartLon(double startLon) {
    this.startLon = startLon;
  }

  public double getEndLat() {
    return endLat;
  }

  public void setEndLat(double endLat) {
    this.endLat = endLat;
  }

  public double getEndLon() {
    return endLon;
  }

  public void setEndLon(double endLon) {
    this.endLon = endLon;
  }

  public List<PathBean> get_paths() {
    return _paths;
  }

  public void set_paths(List<PathBean> _paths) {
    this._paths = _paths;
  }

  public List<StopBean> getStops() {
    return _stops;
  }

  public void setStopBeans(List<StopBean> stops) {
    _stops = stops;
  }

}
