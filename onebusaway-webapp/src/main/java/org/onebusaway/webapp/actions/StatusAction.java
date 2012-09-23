/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.webapp.actions;

import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;

import org.onebusaway.presentation.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class StatusAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private static final Logger _log = LoggerFactory.getLogger(StatusAction.class);

  private ConfigurationService _configurationService;

  private boolean _forceRefresh = false;

  private Map<String, Object> _model = new TreeMap<String, Object>();

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
  public String execute() {

    Map<String, Object> configuration = _configurationService.getConfiguration(_forceRefresh, ERROR);
    _model.putAll(configuration);

    try {
      InetAddress address = InetAddress.getLocalHost();
      _model.put("hostAddress", address.getHostAddress());
      _model.put("hostName", address.getHostName());
    } catch (Exception ex) {
      _log.warn("error determining hostname", ex);
    }

    return SUCCESS;
  }
}
