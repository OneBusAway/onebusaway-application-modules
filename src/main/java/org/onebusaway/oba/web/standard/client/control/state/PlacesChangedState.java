package org.onebusaway.oba.web.standard.client.control.state;

import org.onebusaway.oba.web.standard.client.model.PagedResultsModel;

public class PlacesChangedState extends State {

  private PagedResultsModel _model;

  public PlacesChangedState(PagedResultsModel model) {
    _model = model;
  }

  public PagedResultsModel getModel() {
    return _model;
  }
}
