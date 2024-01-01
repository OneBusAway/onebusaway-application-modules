/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.api.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StopGroupingV2Bean implements Serializable {

  private String type;

  private boolean ordered;

  private List<StopGroupV2Bean> stopGroups = new ArrayList<>();

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isOrdered() {
    return ordered;
  }

  public void setOrdered(boolean ordered) {
    this.ordered = ordered;
  }

  public List<StopGroupV2Bean> getStopGroups() {
    return stopGroups;
  }

  public void setStopGroups(List<StopGroupV2Bean> stopGroups) {
    this.stopGroups = stopGroups;
  }
}
