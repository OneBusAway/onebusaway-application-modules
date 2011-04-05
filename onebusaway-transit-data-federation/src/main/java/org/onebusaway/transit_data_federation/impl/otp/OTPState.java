package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;

public class OTPState {

  private final long initialWaitTime;

  private final TripSequence tripSequence;

  public static void incrementInitialWaitTime(StateData.Editor editor, long time) {
    OTPState state = editor.getExtension(OTPState.class);
    Editor ourEditor = state == null ? new Editor() : state.edit();

    ourEditor.incrementInitialWaitTime(time);
    editor.putExtension(OTPState.class, ourEditor.save());
  }

  public static long getInitialWaitTime(State state) {
    StateData data = state.getData();
    OTPState ourState = data.getExtension(OTPState.class);
    if (ourState == null)
      return 0;
    return ourState.getInitialWaitTime();
  }

  public static void appendTripSequence(StateData.Editor editor,
      BlockTripEntry blockTrip) {
    OTPState state = editor.getExtension(OTPState.class);
    Editor ourEditor = state == null ? new Editor() : state.edit();

    ourEditor.appendTripSequence(blockTrip);
    editor.putExtension(OTPState.class, ourEditor.save());
  }

  public static TripSequence getTripSequence(State state) {
    StateData data = state.getData();
    OTPState ourState = data.getExtension(OTPState.class);
    if (ourState == null)
      return TripSequence.EMPTY;
    return ourState.getTripSequence();
  }

  /****
   * 
   ****/

  private OTPState(Editor editor) {
    this.initialWaitTime = editor.initialWaitTime;
    this.tripSequence = editor.tripSequence;
  }

  public Editor edit() {
    return new Editor(this);
  }

  public long getInitialWaitTime() {
    return initialWaitTime;
  }

  public TripSequence getTripSequence() {
    return tripSequence;
  }

  public static class Editor {

    private long initialWaitTime;

    private TripSequence tripSequence;

    private Editor() {

    }

    private Editor(OTPState state) {
      this.initialWaitTime = state.initialWaitTime;
      this.tripSequence = state.tripSequence;
    }

    public void incrementInitialWaitTime(long time) {
      this.initialWaitTime += time;
    }

    public void appendTripSequence(BlockTripEntry blockTrip) {
      if (tripSequence == null)
        tripSequence = new TripSequence(blockTrip);
      else
        tripSequence = tripSequence.extend(blockTrip);
    }

    public OTPState save() {
      return new OTPState(this);
    }
  }

}
