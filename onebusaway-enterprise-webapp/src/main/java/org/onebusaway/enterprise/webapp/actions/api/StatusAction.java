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
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class StatusAction extends OneBusAwayEnterpriseActionSupport {

  private static final Logger _log = LoggerFactory.getLogger(StatusAction.class);
  
  @Autowired
  private ConfigurationService _config;

  @Autowired
  private StatusProvider _statusProvider;
  
  private List<StatusGroup> groups;
  
  private boolean showOK;
  private boolean showCustomHeader;
  private boolean showContactEmail;
  private String customHeader = "";
  private String contactEmail = "";

  @Override
  public String execute() {
    try {
      checkCustomHeader();
      checkContactEmail();
      groups = createGroups();
      return SUCCESS;
    } catch (Throwable t) {
      _log.error("Exception in execute:", t);
      return ERROR;
    }
  }
  
  public boolean isShowOK() {
    return showOK;
  }

  public void setShowOK(boolean showOK) {
    this.showOK = showOK;
  }
  
  public boolean isShowCustomHeader() {
    return showCustomHeader;
  }

  public void setShowCustomHeader(boolean showCustomHeader) {
    this.showCustomHeader = showCustomHeader;
  }

  public boolean isShowContactEmail() {
    return showContactEmail;
  }

  public void setShowContactEmail(boolean showContactEmail) {
    this.showContactEmail = showContactEmail;
  }

  public String getCustomHeader() {
    return customHeader;
  }

  public void setCustomHeader(String customHeader) {
    this.customHeader = customHeader;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public List<StatusGroup> getGroups() {
    return groups;
  }
  
  public boolean showItem(StatusItem.Status status) {
    return (status != StatusItem.Status.OK) || showOK;
  }
  
  private List<StatusGroup> createGroups() {
    groups = new ArrayList<StatusGroup>();
//    groups.add(_statusProvider.getIcingaStatus());
    groups.add(_statusProvider.getAgencyServiceAlertStatus());
    groups.add(_statusProvider.getAgencyMetadataStatus());
    
    // trim OKs
    for (StatusGroup group : groups) {
      List<StatusItem> items = new ArrayList<StatusItem>();
      for (StatusItem item : group.getItems()) {
        if (showItem(item.getStatus())) {
          items.add(item);
        }
      }
      group.setItems(items);
    }
    
    return groups;
  }

  private void checkCustomHeader() {
    String header = _config.getConfigurationValueAsString("status.customHeader", null);
    if (header != null && header != "") {
      setCustomHeader(header);
      setShowCustomHeader(true);
    } else {
      setCustomHeader("");
      setShowCustomHeader(false);
    }
  }

  private void checkContactEmail() {
    String email = _config.getConfigurationValueAsString("status.contactEmail", null);
    if (email != null && email != "") {
      setContactEmail(email);
      setShowContactEmail(true);
    } else {
      setContactEmail("");
      setShowContactEmail(false);
    }
  }

}
