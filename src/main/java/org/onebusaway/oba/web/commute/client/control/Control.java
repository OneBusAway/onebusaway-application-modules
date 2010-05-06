package org.onebusaway.oba.web.commute.client.control;

import org.onebusaway.oba.web.commute.client.model.CommuteConstraints;

public interface Control {
  public void performQuery(String address, CommuteConstraints constraints);
}
