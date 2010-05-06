package org.onebusaway.oba.web.standard.client.control.state;

public class SearchProgressState extends State {

  private double _percentComplete;

  public SearchProgressState(double percentComplete) {
    _percentComplete = percentComplete;
  }

  public double getPercentComplete() {
    return _percentComplete;
  }
}
