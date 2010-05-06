package org.onebusaway.webapp.gwt.oba_application.control.state;

import org.onebusaway.webapp.gwt.oba_application.model.PagedResultsModel;

public class PlacesChangedState extends State {

  private PagedResultsModel _model;

  public PlacesChangedState(PagedResultsModel model) {
    _model = model;
  }

  public PagedResultsModel getModel() {
    return _model;
  }
}
