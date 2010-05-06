package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.RoutesQueryBean;

public interface RoutesBeanService {
  public RoutesBean getRoutesForQuery(RoutesQueryBean query);
}
