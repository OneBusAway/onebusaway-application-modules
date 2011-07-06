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
