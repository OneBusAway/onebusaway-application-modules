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
package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Arrays;

import org.onebusaway.transit_data_federation.impl.blocks.BlockSequence;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

/**
 * A trip sequence is a unique key used to differentiate on trip planning
 * itinerary from another. Typically, we use the {@link BlockTripEntry} as the
 * unique key segment. That way, if two itineraries are composed of the same
 * trip sequences we just keep the best one.
 * 
 * That said, this is not exactly perfect. There are places for interlined
 * routes where the block-trip changes near a departure or arrival point. Thus,
 * the planner will often find two itineraries:
 * 
 * 1) Where you board before the change.
 * 
 * 2) Where you board after the change.
 * 
 * Effectively, they are the same trip but they show up as unique because their
 * trip sequences are slightly different.
 * 
 * You might be tempted to use the underlying block itself as the key, but this
 * would cause problems where the next best itinerary is the one where the
 * vehicle has done a lap and is back again.
 * 
 * Instead, we use the underlying {@link BlockSequence} where available, since
 * this gets around this issue.
 * 
 * @author bdferris
 * 
 */
public class TripSequence {

  public static final TripSequence EMPTY = new TripSequence(new Object[] {});

  private final Object[] sequence;

  public TripSequence(Object blockTrip) {
    sequence = new Object[] {blockTrip};
  }

  private TripSequence(Object[] newSequence) {
    sequence = newSequence;
  }

  public TripSequence extend(Object blockTrip) {
    if (sequence.length > 0 && sequence[sequence.length - 1] == blockTrip)
      return this;
    Object[] newSequence = new Object[sequence.length + 1];
    System.arraycopy(sequence, 0, newSequence, 0, sequence.length);
    newSequence[newSequence.length - 1] = blockTrip;
    return new TripSequence(newSequence);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(sequence);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TripSequence other = (TripSequence) obj;
    return Arrays.equals(this.sequence, other.sequence);
  }
}
