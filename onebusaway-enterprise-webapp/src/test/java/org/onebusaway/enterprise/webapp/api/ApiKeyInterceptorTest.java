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
package org.onebusaway.enterprise.webapp.api;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.ApiKeyPermissionService.Status;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * Do a bunch of mocking to verify the correct HTTP return codes 
 * are returned by the interceptor
 * @author sheldonabrown
 *
 */
public class ApiKeyInterceptorTest {

  private ApiKeyInterceptor _aki;
  private ActionContext _ac;
  private ActionInvocation _ai;
  private ApiKeyPermissionService _ks;
  private Map<String, Object> _params = new HashMap<String, Object>();
  
  @Before
  public void setUp() throws Exception {
    _aki = new ApiKeyInterceptor();
    _ac = mock(ActionContext.class);
    _ai = mock(ActionInvocation.class);
    _ks = mock(ApiKeyPermissionService.class);
    _aki.setKeyService(_ks);
    
    _params = new HashMap<String, Object>();

    String[] keys = {"akey"};
    _params.put("key", keys);

    when(_ai.getInvocationContext()).thenReturn(_ac);
    when(_ac.getParameters()).thenReturn(_params);
  }

  @Test
  public void testIsAllowedNoUser() {
    // try it with now valid API key
    assertNotNull(_params.remove("key"));
    // here we return 401 as the authentication failed/key not found
    assertEquals(401, _aki.isAllowed(_ai));
  }

  @Test
  public void testIsAllowedValidUser() {
    when(_ks.getPermission((String)anyObject(), (String)anyObject())).thenReturn(Status.AUTHORIZED);
    assertEquals(200, _aki.isAllowed(_ai));
  }

  @Test
  public void testIsAllowedRateLimit() {
    when(_ks.getPermission((String)anyObject(), (String)anyObject())).thenReturn(Status.RATE_EXCEEDED);
    assertEquals(429, _aki.isAllowed(_ai));
  }

  @Test
  public void testIsAllowedUnauthorized() {
    when(_ks.getPermission((String)anyObject(), (String)anyObject())).thenReturn(Status.UNAUTHORIZED);
    /*
     * whoops!  The enterprise webapp chooses to return 403 instead of 401
     * We authenticated (we found the api key) but we are not authorized
     */
    assertEquals(403, _aki.isAllowed(_ai));
  }

}
