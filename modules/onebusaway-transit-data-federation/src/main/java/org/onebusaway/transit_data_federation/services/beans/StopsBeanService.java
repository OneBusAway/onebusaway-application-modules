package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.StopsBean;

public interface StopsBeanService {
  
  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2, double lon2, int maxCount) throws ServiceException;
  
  public StopsBean getStopsByBoundsAndQuery(double lat1, double lon1, double lat2, double lon2, String query, int maxCount) throws ServiceException;

}
