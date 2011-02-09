package org.onebusaway.webapp.gwt.oba_application.control.state;

import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;

public class SelectedPlaceChangedState extends State {

  private TimedLocalSearchResult _selected;

  public SelectedPlaceChangedState(TimedLocalSearchResult model) {
    _selected = model;
  }

  public TimedLocalSearchResult getSelectedResult() {
    return _selected;
  }
}
