/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public class SituationConsequenceBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private EEffect effect;

  private String detourPath;

  private List<String> detourStopIds;

  public EEffect getEffect() {
    return effect;
  }

  public void setEffect(EEffect effect) {
    this.effect = effect;
  }

  public String getDetourPath() {
    return detourPath;
  }

  public void setDetourPath(String detourPath) {
    this.detourPath = detourPath;
  }

  public List<String> getDetourStopIds() {
    return detourStopIds;
  }

  public void setDetourStopIds(List<String> detourStopIds) {
    this.detourStopIds = detourStopIds;
  }
}
