package org.onebusaway.gtfs_realtime.archiver.service;

import java.util.Date;

public interface TimeService {

  Date getCurrentTime(String session);
  
  void setCurrentTime(String session, Date time);
  
  boolean isTimeSet(String session);
  
  void clear(String session);
  
}
