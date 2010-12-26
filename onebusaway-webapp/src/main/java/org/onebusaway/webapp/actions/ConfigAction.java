package org.onebusaway.webapp.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.services.cachecontrol.CacheControl;
import org.onebusaway.webapp.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results({@Result(type = "json", params = {
    "root", "result", "contentType", "text/javascript", "wrapPrefix",
    "var OBA = window.OBA || {}; OBA.Config = "})})
@CacheControl(maxAge = 60 * 60)
public class ConfigAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private ConfigurationService _configurationService;

  private boolean _forceRefresh = false;

  private Map<String, Object> _model = new HashMap<String, Object>();

  @Autowired
  public void setConfigurationService(ConfigurationService configurationService) {
    _configurationService = configurationService;
  }

  public void setForceRefresh(boolean forceRefresh) {
    _forceRefresh = forceRefresh;
  }

  public Map<String, Object> getResult() {
    return _model;
  }

  @Override
  public String execute() throws Exception {
    _model = _configurationService.getConfiguration(_forceRefresh);
    return SUCCESS;
  }
}
