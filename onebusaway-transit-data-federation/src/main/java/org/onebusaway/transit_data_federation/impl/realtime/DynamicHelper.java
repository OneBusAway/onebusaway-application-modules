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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.DynamicBlockEntry;

/**
 * helper methods for Dynamic Trips.
 */
public class DynamicHelper {
  public DynamicHelper() {

  }

  public boolean isServiceIdDynamic(String serviceId) {
    if (serviceId == null) return false;
    return serviceId.contains("DYN-");
  }

  public boolean isBlockDynamic(BlockEntry block) {
    if (block == null) return false;
    return block instanceof DynamicBlockEntry;
  }
}
