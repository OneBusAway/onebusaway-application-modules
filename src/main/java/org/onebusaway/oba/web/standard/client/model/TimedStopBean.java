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
package org.onebusaway.oba.web.standard.client.model;

import edu.washington.cs.rse.transit.web.oba.common.client.model.ApplicationBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopBean;

public class TimedStopBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  private Integer time;

  public TimedStopBean() {

  }

  public TimedStopBean(StopBean stop, int time) {
    this.stop = stop;
    this.time = time;
  }

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public Integer getTime() {
    return time;
  }

  public void setTime(Integer time) {
    this.time = time;
  }

}
