package org.onebusaway.api.actions.api;

import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.exceptions.NoSuchRouteServiceException;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.NoSuchTripServiceException;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.struts2.rest.ContentTypeHandlerManager;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionInterceptor extends AbstractInterceptor {

  private static Logger _log = LoggerFactory.getLogger(ExceptionInterceptor.class);

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private ContentTypeHandlerManager _handlerSelector;

  @Inject
  public void setMimeTypeHandlerSelector(ContentTypeHandlerManager sel) {
    _handlerSelector = sel;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    try {
      return invocation.invoke();
    } catch (Exception ex) {
      _log.warn("exception for action", ex);
      ActionProxy proxy = invocation.getProxy();
      ResponseBean response = getExceptionAsResponseBean(ex);
      DefaultHttpHeaders methodResult = new DefaultHttpHeaders().withStatus(response.getCode());
      return _handlerSelector.handleResult(proxy.getConfig(), methodResult, response);
    }
  }

  protected ResponseBean getExceptionAsResponseBean(Exception ex) {
    if (ex instanceof NoSuchStopServiceException || ex instanceof NoSuchTripServiceException
        || ex instanceof NoSuchRouteServiceException)
      return new ResponseBean(V1, ResponseCodes.RESPONSE_RESOURCE_NOT_FOUND, ex.getMessage(), null);
    else
      return new ResponseBean(V1, ResponseCodes.RESPONSE_SERVICE_EXCEPTION, ex.getMessage(), null);
  }

}
