/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;

import org.apache.struts2.rest.ContentTypeHandlerManager;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onebusaway.api.ResponseCodes;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.ApiKeyPermissionService.Status;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.config.entities.ActionConfig;

/**
 * Do a bunch of mocking to verify the correct HTTP return codes and status 
 * strings are returned by the interceptor
 * @author sheldonabrown
 *
 */
public class ApiKeyInterceptorTest  {

  private ApiKeyInterceptor _aki;
  private ActionInvocation _ai;
  private ContentTypeHandlerManager mockContentTypeHandlerManager;
  private ActionProxy _ap;
  private ActionConfig _ac;
  private ArgumentCaptor<DefaultHttpHeaders> methodResultCaptor;
  private ArgumentCaptor<ResponseBean> responseCaptor;

  @Before
  public void setup() throws Exception {
    _aki = new ApiKeyInterceptor();
    _ai = mock(ActionInvocation.class);

    mockContentTypeHandlerManager = mock(ContentTypeHandlerManager.class);
    _ap = mock(ActionProxy.class);
    _ac = mock(ActionConfig.class);
    
    when(_ai.getProxy()).thenReturn(_ap);
    when(_ap.getConfig()).thenReturn(_ac);
    _aki.setMimeTypeHandlerSelector(mockContentTypeHandlerManager);
    when(mockContentTypeHandlerManager.handleResult((ActionInvocation) anyObject(), anyObject(), anyObject())).thenReturn("SUCCESS");
    methodResultCaptor = ArgumentCaptor.forClass(DefaultHttpHeaders.class);
    responseCaptor = ArgumentCaptor.forClass(ResponseBean.class);
  }
  
  /**
  * ensure the unauthorized method throws IllegalStateException when 
  * encounters successful return code. 
  * @throws Exception IllegalStateException
  */
  @Test
  public void testUnauthorized200() throws Exception {
    //test illegal state exception 
    ApiKeyPermissionService.Status reason = Status.AUTHORIZED;
    try {
      _aki.unauthorized(_ai, reason);
      fail("expected illegal state exception");
    } catch (IllegalStateException ise) {}
    
  }
  
  /**
   * verify 401/permission denied served for Status.UNAUTHORIZED
   * @throws Exception
   */
  @Test
  public void testUnauthorized401() throws Exception {
    ApiKeyPermissionService.Status reason = Status.UNAUTHORIZED;
    
    // we don't care about the result, we need to validate the internal httpCode and reason
    assertEquals(_aki.unauthorized(_ai, reason), "SUCCESS");
    verify(mockContentTypeHandlerManager).handleResult((ActionInvocation) anyObject(), methodResultCaptor.capture(), responseCaptor.capture());
    
    assertThat(methodResultCaptor.getValue().getStatus(), equalTo(401));
    assertThat(responseCaptor.getValue().getText(), equalTo("permission denied"));
    assertThat(responseCaptor.getValue().getCode(), equalTo(401));
  }

  /**
   * verify 429/rate limit exceed served for Status.RATE_EXCEEDED
   * @throws Exception
   */
  @Test
  public void testUnauthorized429() throws Exception {
    
    ApiKeyPermissionService.Status reason = Status.RATE_EXCEEDED;

    // we don't care about the result, we need to validate the internal httpCode and reason
    assertEquals(_aki.unauthorized(_ai, reason), "SUCCESS");
    verify(mockContentTypeHandlerManager).handleResult((ActionInvocation) anyObject(), methodResultCaptor.capture(), responseCaptor.capture());
    
    assertThat(methodResultCaptor.getValue().getStatus(), equalTo(429));
    assertThat(responseCaptor.getValue().getText(), equalTo("rate limit exceeded"));
    assertThat(responseCaptor.getValue().getCode(), equalTo(429));
  }

  
}
