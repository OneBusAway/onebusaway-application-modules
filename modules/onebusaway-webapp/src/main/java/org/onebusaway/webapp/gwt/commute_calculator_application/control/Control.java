package org.onebusaway.webapp.gwt.commute_calculator_application.control;

import org.onebusaway.webapp.gwt.commute_calculator_application.model.CommuteConstraints;

public interface Control {
  public void performQuery(String address, CommuteConstraints constraints);
}
