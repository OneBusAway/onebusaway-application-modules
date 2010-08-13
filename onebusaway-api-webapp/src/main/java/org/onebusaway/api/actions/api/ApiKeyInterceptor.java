package org.onebusaway.api.actions.api;

import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserService;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.struts2.rest.ContentTypeHandlerManager;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * Ensures that there is a valid API key for the API request, and that a given API key
 * is not used more frequently than it is permitted to.
 */
public class ApiKeyInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  @Autowired
  private UserService _userService;

  @Autowired
  private ApiKeyPermissionService _keyService;
  
  private ContentTypeHandlerManager _handlerSelector;

  @Inject
  public void setMimeTypeHandlerSelector(ContentTypeHandlerManager sel) {
    _handlerSelector = sel;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    Object action = invocation.getAction();
    Class<? extends Object> actionType = action.getClass();
    ApiKeyAuthorization annotation = actionType.getAnnotation(ApiKeyAuthorization.class);

    if (annotation != null) {
      if (!annotation.enabled())
        return invocation.invoke();
    }

    ActionContext context = invocation.getInvocationContext();
    Map<String, Object> parameters = context.getParameters();
    String[] keys = (String[]) parameters.get("key");

    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY, keys[0]);
    UserIndex userIndex = _userService.getUserIndexForId(key);
    
    if (userIndex == null) {
      return unauthorized(invocation, "invalid api key");
    }

    User user = userIndex.getUser();

    boolean allowed = _keyService.getPermission(user, "api");
    _keyService.usedKey(user, "api");
    if (!allowed) {
      //this user is not authorized to use the API, at least for now
      return unauthorized(invocation, "permission denied");
    }
        
    return invocation.invoke();
  }

  private String unauthorized(ActionInvocation invocation, String reason) throws IOException {
    ActionProxy proxy = invocation.getProxy();
    ResponseBean response = new ResponseBean(1, ResponseCodes.RESPONSE_UNAUTHORIZED, reason, null);
    DefaultHttpHeaders methodResult = new DefaultHttpHeaders().withStatus(response.getCode());
    return _handlerSelector.handleResult(proxy.getConfig(), methodResult, response);
  }

  @Autowired
  public void setKeyService(ApiKeyPermissionService _keyService) {
    this._keyService = _keyService;
  }

  public ApiKeyPermissionService getKeyService() {
    return _keyService;
  }

}
