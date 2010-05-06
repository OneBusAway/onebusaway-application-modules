package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.StateEventListener;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchStartedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SelectedPlaceChangedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.State;
import org.onebusaway.webapp.gwt.tripplanner_library.view.TripPlanResultTablePresenter;

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
