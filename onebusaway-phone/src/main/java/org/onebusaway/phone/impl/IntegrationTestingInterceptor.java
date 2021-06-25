/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.phone.impl;

import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.fastagi.AgiRequest;
import org.onebusaway.presentation.impl.users.PhoneNumberLoginInterceptor;
import org.onebusaway.probablecalls.AgiEntryPoint;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class IntegrationTestingInterceptor extends AbstractInterceptor {

  public static final String RESET_USER = "oba_integration_test_reset_user";

  private static final long serialVersionUID = 1L;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    ActionContext context = invocation.getInvocationContext();
    AgiRequest request = AgiEntryPoint.getAgiRequest(context);
    Map<?, ?> r = request.getRequest();

    /**
     * This interceptor will be called multiple times in the course of
     * processing the actions for a call, so we only check the reset user param
     * once
     */
    Object value = r.remove(RESET_USER);

    if (value != null && value.equals("true")) {
      Map resetMap = new HashMap();
      resetMap.put(PhoneNumberLoginInterceptor.RESET_USER,
              Boolean.TRUE);
      context.getParameters().appendAll(resetMap);
    }
    return invocation.invoke();
  }

}
