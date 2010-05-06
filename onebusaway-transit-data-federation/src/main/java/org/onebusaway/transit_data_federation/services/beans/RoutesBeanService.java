package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;

public interface RoutesBeanService {
  public RoutesBean getRoutesForQuery(SearchQueryBean query) throws ServiceException;

  public ListBean<String> getRouteIdsForAgencyId(String agencyId);
}
