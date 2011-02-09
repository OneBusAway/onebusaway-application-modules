package org.onebusaway.webapp.gwt.arrivals_and_departures_widget;

import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;

public interface ArrivalAndDepartureFilter {
  public boolean isIncluded(ArrivalAndDepartureBean bean);
}
