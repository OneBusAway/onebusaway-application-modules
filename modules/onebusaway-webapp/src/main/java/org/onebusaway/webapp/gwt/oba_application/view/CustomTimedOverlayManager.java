package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.StateEventListener;
import org.onebusaway.webapp.gwt.oba_application.control.state.PlacesChangedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchStartedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SelectedPlaceChangedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.State;
import org.onebusaway.webapp.gwt.oba_application.control.state.TripPlansState;
import org.onebusaway.webapp.gwt.oba_application.model.PagedResultsModel;
import org.onebusaway.webapp.gwt.oba_library.control.TimedOverlayManager;

public class CustomTimedOverlayManager extends TimedOverlayManager {

  public StateEventListener getStateEventHandler() {
    return new StateEventHandler();
  }

  private class StateEventHandler implements StateEventListener {

    public void handleUpdate(StateEvent event) {

      State state = event.getState();

      if (state instanceof SearchStartedState) {
        clear();
      } else if (state instanceof PlacesChangedState) {
        PlacesChangedState pcs = (PlacesChangedState) state;
        PagedResultsModel model = pcs.getModel();
        if (model.getSelectedResult() == null)
          setVisible(true);
      } else if (state instanceof SelectedPlaceChangedState) {
        SelectedPlaceChangedState pcs = (SelectedPlaceChangedState) state;
        if (pcs.getSelectedResult() == null) {
          setVisible(true);
        }
      } else if (state instanceof TripPlansState) {
        setVisible(false);
      }
    }
  }

}
