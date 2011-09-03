/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;

public class TransitShedConstraintsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private ConstraintsBean constraints = new ConstraintsBean();

  private long maxInitialWaitTime;
  
  public TransitShedConstraintsBean() {
    
  }
  
  public TransitShedConstraintsBean(TransitShedConstraintsBean c) {
    this.maxInitialWaitTime = c.maxInitialWaitTime;
    this.constraints = new ConstraintsBean(c.getConstraints());
  }

  public ConstraintsBean getConstraints() {
    return constraints;
  }

  public void setConstraints(ConstraintsBean constraints) {
    this.constraints = constraints;
  }

  public long getMaxInitialWaitTime() {
    return maxInitialWaitTime;
  }

  public void setMaxInitialWaitTime(long maxInitialWaitTime) {
    this.maxInitialWaitTime = maxInitialWaitTime;
  }
}
