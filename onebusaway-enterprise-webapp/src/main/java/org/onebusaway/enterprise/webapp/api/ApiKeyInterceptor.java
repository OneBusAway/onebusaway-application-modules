/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.enterprise.webapp.api;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.ApiKeyPermissionService.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.siri.siri.ErrorDescriptionStructure;
import uk.org.siri.siri.OtherErrorStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.ServiceDeliveryErrorConditionStructure;
import uk.org.siri.siri.Siri;
import uk.org.siri.siri.VehicleMonitoringDeliveryStructure;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class ApiKeyInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  //at this time ServletResponse does not support 429 so define it locally
  private static final int RESPONSE_TOO_MANY_REQUESTS = 429;

  private static Logger _log = LoggerFactory.getLogger(ApiKeyInterceptor.class);

  private String _type = "xml";

  private Siri _response;

  @Autowired
  private ApiKeyPermissionService _keyService;

  @Autowired
  private RealtimeService _realtimeService;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    determineResponseType();

    int allowed = isAllowed(invocation);

    if (allowed != HttpServletResponse.SC_OK) {
      try {
        _response = generateSiriResponse(new NotAuthorizedException(
            (allowed == HttpServletResponse.SC_UNAUTHORIZED
                ? "API key required."
                : "API key is not authorized or rate exceeded.")));
        HttpServletResponse servletResponse = ServletActionContext.getResponse();
        servletResponse.setStatus(allowed);
        servletResponse.getWriter().write(serializeResponse(servletResponse));
      } catch (Exception e) {
        _log.error(e.getMessage());
        e.printStackTrace();
      }
      return null;
    }

    return invocation.invoke();
  }

  private void determineResponseType() {
    HttpServletRequest request = ServletActionContext.getRequest();
    setType(request.getParameter("type"));
    // Not going to deal with Accept: header for now, although I should
    // But the API Action itself doesn't, either, so...
    // String acceptHeader = request.getHeader("Accept");
  }

  // package private for unit tests
  int isAllowed(ActionInvocation invocation) {
    ActionContext context = invocation.getInvocationContext();
    String[] keys = null;
    if (context.getParameters().containsKey("key")) {
      keys = (String[]) context.getParameters().get("key").getMultipleValues();
    }

    if (keys == null || keys.length == 0) {
      // 401:  we couldn't find the api key
      return HttpServletResponse.SC_UNAUTHORIZED;
    }

    Status status = _keyService.getPermission(keys[0], "api"); 
    if (status == Status.AUTHORIZED) {
      return HttpServletResponse.SC_OK;
    } 
    
    if (status == Status.RATE_EXCEEDED) {
      return RESPONSE_TOO_MANY_REQUESTS;  
    }
    
     // fall through is 403 Forbidden (we understood the key, but auth failed)
    return HttpServletResponse.SC_FORBIDDEN;

  }

  @Autowired
  public void setKeyService(ApiKeyPermissionService _keyService) {
    this._keyService = _keyService;
  }

  public ApiKeyPermissionService getKeyService() {
    return _keyService;
  }

  private Siri generateSiriResponse(Exception error) {

    VehicleMonitoringDeliveryStructure vehicleMonitoringDelivery = new VehicleMonitoringDeliveryStructure();
    vehicleMonitoringDelivery.setResponseTimestamp(new Date());

    ServiceDelivery serviceDelivery = new ServiceDelivery();
    serviceDelivery.setResponseTimestamp(new Date());
    serviceDelivery.getVehicleMonitoringDelivery().add(
        vehicleMonitoringDelivery);

    ServiceDeliveryErrorConditionStructure errorConditionStructure = new ServiceDeliveryErrorConditionStructure();

    ErrorDescriptionStructure errorDescriptionStructure = new ErrorDescriptionStructure();
    errorDescriptionStructure.setValue(error.getMessage());

    OtherErrorStructure otherErrorStructure = new OtherErrorStructure();
    otherErrorStructure.setErrorText(error.getMessage());

    errorConditionStructure.setDescription(errorDescriptionStructure);
    errorConditionStructure.setOtherError(otherErrorStructure);

    vehicleMonitoringDelivery.setErrorCondition(errorConditionStructure);

    Siri siri = new Siri();
    siri.setServiceDelivery(serviceDelivery);

    return siri;
  }

  public String serializeResponse(HttpServletResponse servletResponse) {
    try {
      if (_type.equals("xml")) {
        servletResponse.setContentType("application/xml");
        return _realtimeService.getSiriXmlSerializer().getXml(_response);
      } else {
        servletResponse.setContentType("application/json");
        return _realtimeService.getSiriJsonSerializer().getJson(_response,
            ServletActionContext.getRequest().getParameter("callback"));
      }
    } catch (Exception e) {
      return e.getMessage();
    }

  }

  public void setType(String type) {
    _type = type;
  }

}
