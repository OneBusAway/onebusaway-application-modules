package org.onebusaway.presentation.impl.configuration;

import java.util.Map;

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

    Map<String, Object> configuration = _configurationService.getConfiguration(forceRefresh);

    stack.setValue("#configuration", configuration);

    return invocation.invoke();
  }
}
