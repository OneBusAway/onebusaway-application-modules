package org.onebusaway.webapp.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.webapp.model.NextAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public abstract class AbstractAction extends ActionSupport implements
    SessionAware, CurrentUserAware {

  private static final long serialVersionUID = 1L;

  private static final String NEXT_ACTION_STACK_SESSION_KEY = AbstractAction.class.getName()
      + ".nextActionStack";

  protected Map<String, Object> _session;

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
  
  protected void clearNextActions() {
    List<NextAction> stack = getNextActionStack(false);
    if( stack != null)
      stack.clear();
    stack = getNextActionStack(false);
    System.out.println(stack);
  }

  protected String getNextActionOrSuccess() {
    List<NextAction> stack = getNextActionStack(false);
    if (stack == null || stack.isEmpty())
      return SUCCESS;

    NextAction next = stack.remove(stack.size() - 1);

    Map<String, String[]> params = next.getParameters();
    if (params != null && !params.isEmpty()) {
      ActionContext context = ActionContext.getContext();
      Map<String, Object> contextParameters = context.getParameters();
      contextParameters.putAll(params);
    }

    return next.getAction();
  }

  protected void pushNextAction(String action) {
    List<NextAction> stack = getNextActionStack(true);
    stack.add(new NextAction(action));
  }

  protected void pushNextAction(String action, String key, String value) {
    List<NextAction> stack = getNextActionStack(true);
    stack.add(new NextAction(action, key, value));
  }

  protected CoordinateBounds getServiceArea() {
    return _serviceAreaService.getServiceArea(_currentUser, _session);
  }

  /****
   * Private Methods
   ****/

  @SuppressWarnings("unchecked")
  private List<NextAction> getNextActionStack(boolean create) {
    List<NextAction> stack = (List<NextAction>) _session.get(NEXT_ACTION_STACK_SESSION_KEY);
    if (stack == null && create) {
      stack = new ArrayList<NextAction>();
      _session.put(NEXT_ACTION_STACK_SESSION_KEY, stack);
    }
    return stack;
  }
}
