package org.onebusaway.phone.actions;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class AbstractAction extends ActionSupport implements CurrentUserAware {

  private static final long serialVersionUID = 1L;

  public static final String NEEDS_DEFAULT_SEARCH_LOCATION = "needsDefaultSearchLocation";

  protected TransitDataService _transitDataService;

  protected CurrentUserService _currentUserService;

  protected UserBean _currentUser;

  private ServiceAreaService _serviceAreaService;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService userDataService) {
    _currentUserService = userDataService;
  }

  @Autowired
  public void setServiceAreaService(ServiceAreaService serviceAreaService) {
    _serviceAreaService = serviceAreaService;
  }

  @Override
  public void setCurrentUser(UserBean currentUser) {
    _currentUser = currentUser;
  }
  
  public UserBean getCurrentUser() {
    return _currentUser;
  }

  protected CoordinateBounds getDefaultSearchArea() {

    ActionContext context = ActionContext.getContext();
    return _serviceAreaService.getServiceArea(_currentUser,
        context.getSession());
  }

}
