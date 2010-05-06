package org.onebusaway.oba.web.standard.client.view;

import org.onebusaway.oba.web.common.client.control.TimedRegionOverlayManager;
import org.onebusaway.oba.web.standard.client.control.StateEvent;
import org.onebusaway.oba.web.standard.client.control.StateEventListener;
import org.onebusaway.oba.web.standard.client.control.state.SearchStartedState;
import org.onebusaway.oba.web.standard.client.control.state.State;

public class CustomTimedRegionOverlayManager extends TimedRegionOverlayManager {

  public StateEventListener getStateEventHandler() {
    return new StateEventHandler();
  }

  private class StateEventHandler implements StateEventListener {

    public void handleUpdate(StateEvent model) {

      State state = model.getState();

      if (state instanceof SearchStartedState) {
        clear();
      }
    }

  }

}
