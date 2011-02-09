package org.onebusaway.users.services.internal;

import java.util.List;

public interface LastSelectedStopService {
  
  public List<String> getLastSelectedStopsForUser(Integer userId);

  public void setLastSelectedStopsForUser(Integer userId, List<String> stopIds);
  
  public void clearLastSelectedStopForUser(Integer userId);
}
