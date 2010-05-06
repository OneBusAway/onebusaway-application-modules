package org.onebusaway.oba.web.standard.client.control.state;

import org.onebusaway.oba.web.standard.client.model.ResultsModel;

public class AllResultsModelChangedState extends State {

  private ResultsModel _model;

  public AllResultsModelChangedState(ResultsModel model) {
    _model = model;
  }

  public ResultsModel getModel() {
    return _model;
  }
}
