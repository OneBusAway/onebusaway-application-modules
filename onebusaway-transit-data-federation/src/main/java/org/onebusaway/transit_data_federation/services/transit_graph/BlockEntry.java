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
package org.onebusaway.transit_data_federation.services.transit_graph;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;

import java.util.List;

public interface BlockEntry {

  public AgencyAndId getId();

  /**
   * The configurations are sorted by decreasing service id count, and then by
   * service id for configurations with the same number of service ids. So for
   * example, if a block has trips with service ids A, B, and C,
   * 
   * <ol>
   * <li>A,B,C</li>
   * <li>A,B</li>
   * <li>A,C</li>
   * <li>B,C</li>
   * <li>A</li>
   * <li>B</li>
   * <li>C</li>
   * </ol>
   * 
   * This order was selected so that when iterating of configurations to
   * determine which is active, a config where service ids A,B, and C are all
   * active should be selected before a config where just A and B are active.
   * 
   * See
   * {@link BlockCalendarService#getBlockConfigurationIndex(AgencyAndId, long)}
   * for more info.
   * 
   * @return the list of block configurations for a particular block
   */
  public List<BlockConfigurationEntry> getConfigurations();

  // public List<TripEntry> getTrips();

  // public List<StopTimeEntry> getStopTimes();

  /**
   * @return distance, in meters
   */
  // public double getTotalBlockDistance();
}
