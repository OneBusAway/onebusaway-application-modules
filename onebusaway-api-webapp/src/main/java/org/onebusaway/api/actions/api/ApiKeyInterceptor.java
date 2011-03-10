package org.onebusaway.api.actions.api;

import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.users.services.validation.KeyValidationService;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.struts2.rest.ContentTypeHandlerManager;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class ApiKeyInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  @Autowired
  private KeyValidationService _validationService;

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
    String[] key = (String[]) parameters.get("key");

    if (!isValidKey(key)) {
      ActionProxy proxy = invocation.getProxy();
      ResponseBean response = new ResponseBean(1, ResponseCodes.RESPONSE_UNAUTHORIZED, "invalid api key", null);
      DefaultHttpHeaders methodResult = new DefaultHttpHeaders().withStatus(response.getCode());
      return _handlerSelector.handleResult(proxy.getConfig(), methodResult, response);
    }

    return invocation.invoke();
  }

  private boolean isValidKey(String[] keys) {
    if (keys == null)
      return false;
    if (keys.length == 0)
      return false;
    return _validationService.isValidKey(keys[0]);
  }
}
