package org.onebusaway.transit_data_federation.impl.otp;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;

public class OTPState {

  private final long initialWaitTime;

  public static void incrementInitialWaitTime(StateData.Editor editor, long time) {
    OTPState state = editor.getExtension(OTPState.class);
    Editor ourEditor = state == null ? new Editor() : state.edit();

    ourEditor.incrementInitialWaitTime(time);
    editor.putExtension(OTPState.class, ourEditor.save());
  }
  
  public static long getInitialWaitTime(State state) {
    StateData data = state.getData();
    OTPState ourState = data.getExtension(OTPState.class);
    if( ourState == null)
      return 0;
    return ourState.getInitialWaitTime();
  }

  /****
   * 
   ****/

  private OTPState(Editor editor) {
    this.initialWaitTime = editor.initialWaitTime;
  }

  public Editor edit() {
    return new Editor(this);
  }

  public long getInitialWaitTime() {
    return initialWaitTime;
  }

  public static class Editor {

    private long initialWaitTime;

    private Editor() {

    }

    private Editor(OTPState state) {
      this.initialWaitTime = state.initialWaitTime;
    }

    public void incrementInitialWaitTime(long time) {
      this.initialWaitTime += time;
    }

    public OTPState save() {
      return new OTPState(this);
    }
  }
}
