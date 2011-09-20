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
package org.onebusaway.presentation.impl.users;

import org.onebusaway.presentation.services.InitialSetupService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Ensures that the webapp has been set up with at least one admin user
 */
public class IsSetupInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private InitialSetupService _initialSetupService;

  @Autowired
  public void setInitialSetupService(InitialSetupService initialSetupService) {
    _initialSetupService = initialSetupService;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    Object action = invocation.getAction();

    Class<? extends Object> actionType = action.getClass();
    SetupAction annotation = actionType.getAnnotation(SetupAction.class);

    boolean needsSetup = _initialSetupService.isInitialSetupRequired(false);

    if (annotation != null) {
      if (annotation.onlyAllowIfNotSetup() && !needsSetup)
        return "AlreadySetup";
      return invocation.invoke();
    }

    if (needsSetup) {
      /* not set up and not trying to; redirect to setup page */
      return "NotSetup";
    }

    return invocation.invoke();
  }

}
