package org.onebusaway.phone.actions;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.UserDataService;

import com.opensymphony.xwork2.ActionSupport;

import org.springframework.beans.factory.annotation.Autowired;

public class AbstractAction extends ActionSupport implements CurrentUserAware {

  private static final long serialVersionUID = 1L;

  public static final String NEEDS_DEFAULT_SEARCH_LOCATION = "needsDefaultSearchLocation";

  protected TransitDataService _transitDataService;

  protected UserDataService _userDataService;

  protected UserBean _currentUser;

  private ServiceAreaService _serviceAreaService; 

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setUserDataService(UserDataService userDataService) {
    _userDataService = userDataService;
  }
  
  @Autowired
  public void setServiceAreaService(ServiceAreaService serviceAreaService) {
    _serviceAreaService = serviceAreaService;
  }

  public void setCurrentUser(UserBean currentUser) {
    _currentUser = currentUser;
  }

  protected CoordinateBounds getDefaultSearchArea() {
    return _serviceAreaService.getServiceArea(_currentUser);
  }
}
