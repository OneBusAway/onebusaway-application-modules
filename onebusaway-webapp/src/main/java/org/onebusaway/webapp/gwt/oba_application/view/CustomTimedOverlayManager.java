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
