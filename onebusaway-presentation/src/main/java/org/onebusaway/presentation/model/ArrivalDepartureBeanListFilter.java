package org.onebusaway.presentation.model;

import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;

import java.util.List;

public interface ArrivalDepartureBeanListFilter {
  List<ArrivalAndDepartureBean> filter(
      List<ArrivalAndDepartureBean> beans);
}
