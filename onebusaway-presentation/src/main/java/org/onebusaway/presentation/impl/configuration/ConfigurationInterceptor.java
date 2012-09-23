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
package org.onebusaway.presentation.impl.configuration;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.onebusaway.presentation.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.ValueStack;

public class ConfigurationInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private ConfigurationService _configurationService;

  @Autowired
  public void setConfigurationService(ConfigurationService configurationService) {
    _configurationService = configurationService;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    ValueStack stack = invocation.getStack();

    boolean forceRefresh = false;

    Object v = stack.findValue("refreshConfiguration");
    if (v != null)
      forceRefresh = Boolean.parseBoolean(v.toString());

    HttpServletRequest request = ServletActionContext.getRequest();
    Map<String, Object> configuration = _configurationService.getConfiguration(forceRefresh, request.getContextPath());

    stack.setValue("#configuration", configuration);

    return invocation.invoke();
  }
}
