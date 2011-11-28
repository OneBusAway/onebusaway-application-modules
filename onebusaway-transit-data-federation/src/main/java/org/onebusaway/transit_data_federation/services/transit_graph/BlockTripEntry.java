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
package org.onebusaway.transit_data_federation.services.transit_graph;

import org.onebusaway.transit_data_federation.services.blocks.AbstractBlockTripIndex;

public interface BlockTripEntry extends HasBlockStopTimes {

  public BlockConfigurationEntry getBlockConfiguration();

  public TripEntry getTrip();

  /**
   * @return the position of this trip in the parent trip collection
   *         {@link BlockConfigurationEntry#getTrips()}.
   */
  public short getSequence();

  public short getAccumulatedStopTimeIndex();

  /**
   * The amount of accumulated slack time from the start of the block to the
   * start of the trip
   * 
   * @return accumulated slack time, in seconds
   */
  public int getAccumulatedSlackTime();

  /**
   * @return distance, in meters, of the start of the trip from the start of the
   *         block
   */
  public double getDistanceAlongBlock();

  public BlockTripEntry getPreviousTrip();

  public BlockTripEntry getNextTrip();

  /****
   * Stop Methods
   ****/

  public int getArrivalTimeForIndex(int index);

  public int getDepartureTimeForIndex(int index);

  public double getDistanceAlongBlockForIndex(int blockSequence);

  /****
   * Pattern Methods
   ****/

  public AbstractBlockTripIndex getPattern();
}
