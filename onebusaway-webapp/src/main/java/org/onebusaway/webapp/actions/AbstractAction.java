package org.onebusaway.webapp.actions;

import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAction extends NextActionSupport implements
    CurrentUserAware {

  private static final long serialVersionUID = 1L;

  protected UserBean _currentUser;

  protected TransitDataService _transitDataService;

  protected CurrentUserService _currentUserService;

  private ServiceAreaService _serviceAreaService;

  public void setSession(Map<String, Object> session) {
    _session = session;
  }

  public void setCurrentUser(UserBean currentUser) {
    _currentUser = currentUser;
  }

  public UserBean getUser() {
    return _currentUser;
  }

  @Autowired
  public void setServiceAreaService(ServiceAreaService serviceAreaService) {
    _serviceAreaService = serviceAreaService;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  /****
   * Protected Methods
   ****/

  protected CoordinateBounds getServiceArea() {
    return _serviceAreaService.getServiceArea(_currentUser, _session);
  }
}
