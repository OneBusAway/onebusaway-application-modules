/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.api;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates 'OBAKEY' API key on context initialization if key creation is enabled
 * by Maven profile. Used only in application context at this point.
 * 
 * @author abelsare
 * 
 */
public class CreateAPIKeyOnInitAction {

  private static Logger _log = LoggerFactory.getLogger(CreateAPIKeyOnInitAction.class);

  private UserService _userService;

  private UserPropertiesService _userPropertiesService;

  private String key;

  private String contactName;

  private String contactCompany;

  private String contactEmail;

  private String contactDetails;

  private int DEFAULT_LIMIT = 100;
  private int limit = DEFAULT_LIMIT;

  private boolean createAPIKey = false;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @Autowired
  public void setUserPropertiesService(
      UserPropertiesService userPropertiesService) {
    _userPropertiesService = userPropertiesService;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getContactName() {
    return contactName;
  }

  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  public String getContactCompany() {
    return contactCompany;
  }

  public void setContactCompany(String contactCompany) {
    this.contactCompany = contactCompany;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public String getContactDetails() {
    return contactDetails;
  }

  public void setContactDetails(String contactDetails) {
    this.contactDetails = contactDetails;
  }

  public void setLimit(String limit) {
    if (StringUtils.isBlank(limit)) {
      this.limit = DEFAULT_LIMIT;
      return;
    }
    try {
      this.limit = Integer.parseInt(limit);
    } catch (NumberFormatException nfe) {
      this.limit = DEFAULT_LIMIT;
    }
  }

  public void init() {
    // no-op, createAPIKey determines if execute is called or not
  }
  
  @PostConstruct
  public void execute() {
    if (createAPIKey) {
      _log.info("creating api key for " + key);
      UserIndexKey userIndexKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
      UserIndex userIndex = _userService.getOrCreateUserForIndexKey(
          userIndexKey, key, false);
      _userPropertiesService.authorizeApi(userIndex.getUser(), limit);

      if (StringUtils.isNotBlank(contactName)
              || StringUtils.isNotBlank(contactCompany)
              || StringUtils.isNotBlank(contactEmail)
              || StringUtils.isNotBlank(contactDetails)) {
        _log.info("updating user contact info "
                + contactName + ", "
                + contactCompany + ", "
                + contactEmail + ", "
                + contactDetails + ", limit="
                + limit);
        _userPropertiesService.updateApiKeyContactInfo(userIndex.getUser(),
                contactName,
                contactCompany,
                contactEmail,
                contactDetails);
      }
    }
  }

  /**
   * @param createAPIKey the createAPIKey to set
   */
  public void setCreateAPIKey(boolean createAPIKey) {
    this.createAPIKey = createAPIKey;
  }

}
