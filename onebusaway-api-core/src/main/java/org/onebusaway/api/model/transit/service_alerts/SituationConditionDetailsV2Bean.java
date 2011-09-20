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
package org.onebusaway.api.model.transit.service_alerts;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.EncodedPolylineBean;

public class SituationConditionDetailsV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private EncodedPolylineBean diversionPath;

  private List<String> diversionStopIds;

  public EncodedPolylineBean getDiversionPath() {
    return diversionPath;
  }

  public void setDiversionPath(EncodedPolylineBean diversionPath) {
    this.diversionPath = diversionPath;
  }

  public List<String> getDiversionStopIds() {
    return diversionStopIds;
  }

  public void setDiversionStopIds(List<String> diversionStopIds) {
    this.diversionStopIds = diversionStopIds;
  }
}
