package org.onebusaway.presentation.client;

import org.onebusaway.transit_data.model.StopBean;

public class StopPresenter {
  public static String getCodeForStop(StopBean stop) {
    if( stop.getCode() != null)
      return stop.getCode();
    return stop.getId();
  }
}
