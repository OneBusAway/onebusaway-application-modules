package org.onebusaway.webapp.actions.sms;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.users.client.model.UserBean;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@InterceptorRefs( {@InterceptorRef("onebusaway-webapp-textmarks-stack")})
@Results( {
    @Result(type = "chain", name = "arrivals-and-departures", location = "arrivals-and-departures"),
    @Result(type = "chain", name = "query-default-search-location", location = "query-default-search-location"),
    @Result(type = "chain", name = "set-default-search-location", location = "set-default-search-location")})
public class AbstractTextmarksAction extends ActionSupport implements
    TextmarksActionConstants, SessionAware, CurrentUserAware {

  private static final long serialVersionUID = 1L;

  private static final String NEXT_ACTION_STACK_SESSION_KEY = AbstractTextmarksAction.class.getName()
      + ".nextActionStack";

  protected Map<String, Object> _session;

  protected UserBean _currentUser;

  private ServiceAreaService _serviceAreaService;

  protected String _text;

  public void setSession(Map<String, Object> session) {
    _session = session;
  }

  public void setCurrentUser(UserBean currentUser) {
    _currentUser = currentUser;
  }

  @Autowired
  public void setServiceAreaService(ServiceAreaService serviceAreaService) {
    _serviceAreaService = serviceAreaService;
  }

  public void setMessage(String message) {
    if (_text == null)
      _text = message.trim();
  }

  public void setText(String text) {
    _text = text;
  }

  public String getText() {
    return _text;
  }

  /****
   * Protected Methods
   ****/

  protected String getNextActionOrSuccess() {
    List<NextAction> stack = getNextActionStack(false);
    if (stack == null || stack.isEmpty())
      return SUCCESS;
    NextAction next = stack.remove(stack.size() - 1);
    if (next.getText() != null)
      _text = next.getText();

    return next.getAction();
  }

  protected void pushNextAction(String action) {
    List<NextAction> stack = getNextActionStack(true);
    stack.add(new NextAction(action));
  }

  protected void pushNextAction(String action, String message) {
    List<NextAction> stack = getNextActionStack(true);
    stack.add(new NextAction(action, message));
  }

  protected CoordinateBounds getServiceArea() {
    CoordinatePoint location = (CoordinatePoint) _session.get(SESSION_KEY_DEFAULT_SEARCH_LOCATION);
    if (location != null)
      return SphericalGeometryLibrary.bounds(location.getLat(),
          location.getLon(), 10 * 1000);
    if (_serviceAreaService != null)
      return _serviceAreaService.getServiceArea(_currentUser);
    return null;
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

  public static class NextAction {

    private final String _action;

    private final String _text;

    public NextAction(String action) {
      this(action, null);
    }

    public NextAction(String action, String text) {
      _action = action;
      _text = text;
    }

    public String getAction() {
      return _action;
    }

    public String getText() {
      return _text;
    }
  }
}
