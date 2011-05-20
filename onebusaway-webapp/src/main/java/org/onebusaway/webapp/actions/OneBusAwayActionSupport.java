package org.onebusaway.webapp.actions;

import java.util.Map;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.onebusaway.presentation.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@ParentPackage("onebusaway-webapp-default")
public class OneBusAwayActionSupport extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private boolean _refreshConfiguration = false;

  private ConfigurationService _configurationService;

  @Autowired
  public void setConfigurationService(ConfigurationService configurationService) {
    _configurationService = configurationService;
  }

  public void setRefreshConfigurat(boolean refreshConfiguration) {
    _refreshConfiguration = refreshConfiguration;
  }

  public Map<String, Object> getConfiguration() {
    Map<String, Object> config = _configurationService.getConfiguration(_refreshConfiguration);
    return config;
  }
}
