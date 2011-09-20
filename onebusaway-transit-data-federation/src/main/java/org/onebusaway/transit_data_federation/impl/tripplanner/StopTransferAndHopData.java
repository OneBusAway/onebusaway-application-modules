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
package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;

public class StopTransferAndHopData implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<AgencyAndId, List<StopTransferData>> transferData;

  private Map<AgencyAndId, List<StopHopData>> hopData;

  public Map<AgencyAndId, List<StopTransferData>> getTransferData() {
    return transferData;
  }

  public void setTransferData(
      Map<AgencyAndId, List<StopTransferData>> transferData) {
    this.transferData = transferData;
  }

  public Map<AgencyAndId, List<StopHopData>> getHopData() {
    return hopData;
  }

  public void setHopData(Map<AgencyAndId, List<StopHopData>> hopData) {
    this.hopData = hopData;
  }
}
