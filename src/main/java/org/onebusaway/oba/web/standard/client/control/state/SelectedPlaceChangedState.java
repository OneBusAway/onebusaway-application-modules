package org.onebusaway.oba.web.standard.client.control.state;

import org.onebusaway.oba.web.standard.client.model.TimedLocalSearchResult;

public class SelectedPlaceChangedState extends State {

  private TimedLocalSearchResult _selected;

  public SelectedPlaceChangedState(TimedLocalSearchResult model) {
    _selected = model;
  }

  public TimedLocalSearchResult getSelectedResult() {
    return _selected;
  }
}
