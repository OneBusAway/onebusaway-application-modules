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
package org.onebusaway.enterprise.webapp.actions.api;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusGroup;
import org.onebusaway.enterprise.webapp.actions.status.model.StatusItem;
import org.onebusaway.enterprise.webapp.actions.status.service.StatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class StatusAction extends OneBusAwayEnterpriseActionSupport {

  private static final Logger _log = LoggerFactory.getLogger(StatusAction.class);
  
  @Autowired
  private StatusProvider _statusProvider;
  
  private List<StatusGroup> groups;
  
  private StatusItem.Status overallStatus;
  
  private boolean showOK;

  @Override
  public String execute() {
    groups = createGroups();
    overallStatus = getOverallStatus();
    return SUCCESS;
  }
  
  public boolean isShowOK() {
    return showOK;
  }

  public void setShowOK(boolean showOK) {
    this.showOK = showOK;
  }
  
  public List<StatusGroup> getGroups() {
    return groups;
  }
  
  public String getOverallStyle() {
    if (overallStatus == StatusItem.Status.OK) {
      return "panel-success";
    }
    else if (overallStatus == StatusItem.Status.WARNING) {
      return "panel-warning";
    }
    return "panel-danger";
  }
  
  public String getOverallMessage() {
    if (overallStatus == StatusItem.Status.OK) {
      return "All systems operational.";
    }
    else if (overallStatus == StatusItem.Status.WARNING) {
      return "Some warnings.";
    }
    return "Warning: system errors!";
  }
  
  public boolean showItem(StatusItem.Status status) {
    return (status != StatusItem.Status.OK) || showOK;
  }
  
  private List<StatusGroup> createGroups() {
    groups = new ArrayList<StatusGroup>();
    groups.add(_statusProvider.getServiceAlertStatus());
    groups.add(_statusProvider.getIcingaStatus());
    return groups;
  }
  
  // OK, WARNING, or ERROR
  private StatusItem.Status getOverallStatus() {
    StatusItem.Status status = StatusItem.Status.OK;
    for (StatusGroup group : groups) {
      for (StatusItem item : group.getItems()) {
        if (item.getStatus().compareTo(status) > 0) {
          status = item.getStatus();
        }
      }
    }
    return status;
  }
  
}
