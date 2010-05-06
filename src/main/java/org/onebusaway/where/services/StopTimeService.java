package org.onebusaway.where.services;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.where.model.StopTimeInstance;

import java.util.Date;
import java.util.List;

public interface StopTimeService {
  public List<StopTimeInstance> getStopTimeInstancesInTimeRange(Stop stop,
      Date from, Date to);
}
