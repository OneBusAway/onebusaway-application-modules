package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopsBean;

public interface StopsBeanService {

  public StopsBean getStops(SearchQueryBean query) throws ServiceException;

  public ListBean<String> getStopsIdsForAgencyId(String agencyId);
}
