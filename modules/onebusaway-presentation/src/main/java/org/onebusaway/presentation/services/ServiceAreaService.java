package org.onebusaway.presentation.services;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.users.client.model.UserBean;

public interface ServiceAreaService {
  public CoordinateBounds getServiceArea(UserBean currentUser);
}
