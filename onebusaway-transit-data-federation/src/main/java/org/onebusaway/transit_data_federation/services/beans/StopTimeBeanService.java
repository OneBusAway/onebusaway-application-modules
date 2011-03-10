package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public interface StopTimeBeanService {
  public StopTimeBean getStopTimeAsBean(StopTimeEntry stopTime);
}
