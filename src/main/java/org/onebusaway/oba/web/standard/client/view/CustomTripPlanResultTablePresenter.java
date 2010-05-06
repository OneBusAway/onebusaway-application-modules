package org.onebusaway.oba.web.standard.client.view;

import org.onebusaway.oba.web.standard.client.control.StateEvent;
import org.onebusaway.oba.web.standard.client.control.StateEventListener;
import org.onebusaway.oba.web.standard.client.control.state.SearchStartedState;
import org.onebusaway.oba.web.standard.client.control.state.SelectedPlaceChangedState;
import org.onebusaway.oba.web.standard.client.control.state.State;
import org.onebusaway.tripplanner.web.common.client.view.TripPlanResultTablePresenter;

public class CustomTripPlanResultTablePresenter extends TripPlanResultTablePresenter {

  public StateEventListener getStateEventHandler() {
    return new StateEventHandler();
  }

  private class StateEventHandler implements StateEventListener {

    public void handleUpdate(StateEvent event) {
      State state = event.getState();

      if (state instanceof SearchStartedState) {
        clear();
      } else if (state instanceof SelectedPlaceChangedState) {
        SelectedPlaceChangedState spcs = (SelectedPlaceChangedState) state;
        if (spcs.getSelectedResult() == null) {
          clear();
        }
      }
    }
  }
}
