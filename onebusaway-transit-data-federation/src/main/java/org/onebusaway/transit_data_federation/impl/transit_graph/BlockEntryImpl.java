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
package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;

public class BlockEntryImpl implements BlockEntry, Serializable {

  private static final long serialVersionUID = 3L;

  private AgencyAndId _id;

  private List<BlockConfigurationEntry> _configurations;

  public void setId(AgencyAndId id) {
    _id = id;
  }
  
  public void setConfigurations(List<BlockConfigurationEntry> configurations) {
    _configurations = configurations;
  }

  /****
   * {@link BlockEntry} Interface
   ****/

  @Override
  public AgencyAndId getId() {
    return _id;
  }
  

  @Override
  public List<BlockConfigurationEntry> getConfigurations() {
    return _configurations;
  }

  @Override
  public String toString() {
    return _id.toString();
  }
}
