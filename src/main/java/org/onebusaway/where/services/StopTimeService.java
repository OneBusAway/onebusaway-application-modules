package org.onebusaway.where.services;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.Date;
import java.util.List;

public interface StopTimeService {
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(Stop stop,
      Date from, Date to);
}
