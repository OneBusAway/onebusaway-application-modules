package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public interface StopTimeBeanService {
  public StopTimeBean getStopTimeAsBean(StopTimeEntry stopTime);
  public StopTimeInstanceBean getStopTimeInstanceAsBean(StopTimeInstance instance);
}
