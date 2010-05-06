package org.onebusaway.webapp.gwt.oba_application.control.state;

import org.onebusaway.webapp.gwt.oba_application.model.ResultsModel;

public class AllResultsModelChangedState extends State {

  private ResultsModel _model;

  public AllResultsModelChangedState(ResultsModel model) {
    _model = model;
  }

  public ResultsModel getModel() {
    return _model;
  }
}
