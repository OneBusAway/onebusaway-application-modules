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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results({@Result(type = "json", params = {
    "root", "result", "contentType", "text/javascript", "wrapPrefix",
    "var OBA = window.OBA || {}; OBA.Config = "})})
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
    HttpServletRequest request = ServletActionContext.getRequest();    
    _model = _configurationService.getConfiguration(_forceRefresh, request.getContextPath());
    return SUCCESS;
  }
}
