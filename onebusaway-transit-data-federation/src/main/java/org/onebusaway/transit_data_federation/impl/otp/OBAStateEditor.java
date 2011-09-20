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

import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.StateEditor;

public class OBAStateEditor extends StateEditor {

  public OBAStateEditor(OBAState parent, Edge e) {
    this(parent, e, (EdgeNarrative) e);
  }

  public OBAStateEditor(OBAState parent, Edge edge, EdgeNarrative edgeNarrative) {
    super(parent, edge, edgeNarrative);
  }

  public void setMaxBlockSequence(int maxBlockSequence) {
    ((OBAState) child).maxBlockSequence = maxBlockSequence;
  }

  public void incrementInitialWaitTime(long time) {
    ((OBAState) child).initialWaitTime += time;
  }

  public void appendTripSequence(Object blockTrip) {
    OBAState state = ((OBAState) child);
    if (state.tripSequence == null)
      state.tripSequence = new TripSequence(blockTrip);
    else
      state.tripSequence = state.tripSequence.extend(blockTrip);
  }

  public void setLookaheadItinerary() {
    ((OBAState) child).lookaheadItinerary = true;
  }
}
