package org.onebusaway.phone.actions;

import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.users.services.logging.UserInteractionLoggingService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;

public class AbstractAction extends ActionSupport implements CurrentUserAware {

  private static final long serialVersionUID = 1L;

  public static final String NEEDS_DEFAULT_SEARCH_LOCATION = "needsDefaultSearchLocation";

  protected TransitDataService _transitDataService;

  protected CurrentUserService _currentUserService;

  protected UserBean _currentUser;

  private ServiceAreaService _serviceAreaService;

  private UserInteractionLoggingService _userInteractionLoggingService;

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

  @Autowired
  public void setUserInteractionLoggingService(
      UserInteractionLoggingService userInteractionLoggingService) {
    _userInteractionLoggingService = userInteractionLoggingService;
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

  protected void logUserInteraction(Object... objects) {

    Map<String, Object> entry = _userInteractionLoggingService.isInteractionLoggedForCurrentUser();

    if (entry == null)
      return;

    ActionContext context = ActionContext.getContext();
    ActionInvocation invocation = context.getActionInvocation();
    ActionProxy proxy = invocation.getProxy();

    entry.put("interface", "phone");
    entry.put("namespace", proxy.getNamespace());
    entry.put("actionName", proxy.getActionName());
    entry.put("method", proxy.getMethod());
    
    if( objects.length % 2 != 0 )
      throw new IllegalStateException("expected an even number of arguments");
      
    for( int i=0; i<objects.length; i+= 2)
      entry.put(objects[i].toString(),objects[i+1]);

    _userInteractionLoggingService.logInteraction(entry);
  }
}
