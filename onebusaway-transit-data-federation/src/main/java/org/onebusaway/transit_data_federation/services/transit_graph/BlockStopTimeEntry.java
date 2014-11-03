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

public interface BlockStopTimeEntry {

  public StopTimeEntry getStopTime();
  
  public BlockTripEntry getTrip();

  public int getBlockSequence();

  /**
   * 
   * @return distance, in meters, from the start of the block
   */
  public double getDistanceAlongBlock();
  

  /**
   * The amount of accumulated slack time from the start of the block to the
   * arrival time at this stop. Slack time accumulates when there is scheduled
   * time between the arrival and departure of a vehicle at a stop that could
   * potentially be shortened if the vehicle is running late.
   * 
   * @return the accumulated slack time, in seconds
   */
  public int getAccumulatedSlackTime();
  
  public boolean hasPreviousStop();
  
  public boolean hasNextStop();
  
  public BlockStopTimeEntry getPreviousStop();
  
  public BlockStopTimeEntry getNextStop();
}