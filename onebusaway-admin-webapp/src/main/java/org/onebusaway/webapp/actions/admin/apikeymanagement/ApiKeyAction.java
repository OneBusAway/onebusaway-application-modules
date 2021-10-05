/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.webapp.actions.admin.apikeymanagement;

import java.util.List;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates API key for the user. Also authorizes the user to use API. 
 * @author abelsare
 *
 */
@Results({@Result(type = "redirectAction", name = "redirect", params = {
	     "actionName", "api-key"})})
@AllowedMethods({"saveAPIKey", "searchAPIKey", "searchContactEmail", "deleteAPIKey"})
public class ApiKeyAction extends NextActionSupport{

	private static final long serialVersionUID = 1L;
  private static final long MIN_API_REQ_INT_DEFAULT = 100L;
	private Long minApiReqInt = 100L;
	private String contactName;
	private String contactCompany;
	private String contactEmail;
	private String contactDetails;
	private String key;
	private UserService userService;
	private UserPropertiesService userPropertiesService;

  /**
   * @return the minApiReqInt
   */
  public Long getMinApiReqInt() {
    return minApiReqInt;
  }

  /**
   * @param minApiReqInt the minApiReqInt to set
   */
  public void setMinApiReqInt(Long minApiReqInt) {
    this.minApiReqInt = minApiReqInt;
  }

  /**
   * @return the contactName
   */
  public String getContactName() {
    return contactName;
  }

  /**
   * @param contactName the contactName to set
   */
  public void setContactName(String contactName) {
    this.contactName = contactName;
  }

  /**
   * @return the contactCompany
   */
  public String getContactCompany() {
    return contactCompany;
  }

  /**
   * @param contactCompany the contactCompany to set
   */
  public void setContactCompany(String contactCompany) {
    this.contactCompany = contactCompany;
  }

  /**
   * @return the contactEmail
   */
  public String getContactEmail() {
    return contactEmail;
  }

  /**
   * @param contactEmail the contactEmail to set
   */
  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  /**
   * @return the contactDetails
   */
  public String getContactDetails() {
    return contactDetails;
  }

  /**
   * @param contactDetails the contactDetails to set
   */
  public void setContactDetails(String contactDetails) {
    this.contactDetails = contactDetails;
  }

  /**
   * Returns the key of the user being created
   * @return the key
   */
  //@RequiredStringValidator(message="API key is required")
  public String getKey() {
    return key;
  }

  /**
   * Injects the key of the user being created
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Injects {@link UserService}
   * @param userService the userService to set
   */
  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  /**
   * Injects {@link UserPropertiesService}
   * @param userPropertiesService the userPropertiesService to set
   */
  @Autowired
  public void setUserPropertiesService(UserPropertiesService userPropertiesService) {
    this.userPropertiesService = userPropertiesService;
  }

  /**
   * Handle the "Save" action for either creating a new key or updating
   * an existing one.
   * @return success message
   */
  public String saveAPIKey() {
    // Check if key already exists
    UserIndexKey userKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
    UserIndex userIndexForId = userService.getUserIndexForId(userKey);
    if (userIndexForId == null) {
      createAPIKey(key);
      addActionMessage("Key '" + key + "' created successfully");
    } else {
      updateAPIKey(userIndexForId);
      addActionMessage("Key '" + key + "' updated successfully");
    }

    clearContactInfoAndKey();
    return SUCCESS;
  }

	/**
	 * Creates API key in the database
	 * @return success message
	 */
	public void createAPIKey(String apiKey) {
		UserIndexKey userIndexKey = new UserIndexKey(UserIndexTypes.API_KEY, apiKey);
    UserIndex userIndex = userService.getOrCreateUserForIndexKey(userIndexKey,
        apiKey, false);
    if (minApiReqInt == null) {
      minApiReqInt = MIN_API_REQ_INT_DEFAULT;
    }
    userPropertiesService.authorizeApi(userIndex.getUser(), minApiReqInt);
    // Set the API Key contact info
    User user = userIndex.getUser();
    userPropertiesService.updateApiKeyContactInfo(user, contactName,
      contactCompany, contactEmail, contactDetails);

    // Clear the cached value here
    userService.getMinApiRequestIntervalForKey(apiKey, true);
		return;
	}

  public void updateAPIKey(UserIndex userIndexForId) {
    User user = userIndexForId.getUser();

    UserBean bean = userService.getUserAsBean(user);
    String keyContactName = bean.getContactName();
    String keyContactCompany = bean.getContactCompany();
    String keyContactEmail = bean.getContactEmail();
    String keyContactDetails = bean.getContactDetails();

    if (contactName != null) {
      keyContactName = contactName;
    }
    if (contactCompany != null) {
      keyContactCompany = contactCompany;
    }
    if (contactEmail != null) {
      keyContactEmail = contactEmail;
    }
    if (contactDetails != null) {
      keyContactDetails = contactDetails;
    }
    userPropertiesService.authorizeApi(user, minApiReqInt);
    userPropertiesService.updateApiKeyContactInfo(user, keyContactName,
      keyContactCompany, keyContactEmail, keyContactDetails);

    // Clear the cached value here
    userService.getMinApiRequestIntervalForKey(key, true);
    return;
  }

  public String searchAPIKey() {
    // Check if key already exists
    UserIndexKey userKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
    UserIndex userIndexForId = userService.getUserIndexForId(userKey);
    if (userIndexForId == null) {
      addActionMessage("Key '" + key + "' does not exist");
    } else {
      User user = userIndexForId.getUser();
      UserBean bean = userService.getUserAsBean(user);
      minApiReqInt = bean.getMinApiRequestInterval();
      contactName = bean.getContactName();
      contactCompany = bean.getContactCompany();
      contactEmail = bean.getContactEmail();
      contactDetails = bean.getContactDetails();
      addActionMessage("Key '" + key + "' found");
    }
    return SUCCESS;
  }

  public String searchContactEmail() {
    String searchResult = "Email address '" + contactEmail
        + "' could not be found";
    List<String> apiKeys =
        userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY);
    // clear other fields
    contactName = "";
    contactCompany = "";
    contactDetails = "";
    key = "";

    for (String apiKey : apiKeys) {
      // for each api key, check if contact email matches
      UserIndexKey userKey = new UserIndexKey(UserIndexTypes.API_KEY, apiKey);
      UserIndex userIndex = userService.getUserIndexForId(userKey);
      if (userIndex != null) {
        User user = userIndex.getUser();
        UserBean bean = userService.getUserAsBean(user);
        if (contactEmail.equals(bean.getContactEmail())) {
          minApiReqInt = bean.getMinApiRequestInterval();
          contactName = bean.getContactName();
          contactCompany = bean.getContactCompany();
          contactDetails = bean.getContactDetails();
          key = apiKey;
          searchResult = "Email address '" + contactEmail + "' found";
          break;
        }
      }
    }
    addActionMessage(searchResult);
    return SUCCESS;
  }

  public String deleteAPIKey() {
    // Check if key already exists
    UserIndexKey userKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
    UserIndex userIndexForId = userService.getUserIndexForId(userKey);
    if (userIndexForId == null) {
      addActionMessage("Key '" + key + "' does not exist");
    } else {
      User user = userIndexForId.getUser();
      boolean found = false;
      for (UserIndex index : user.getUserIndices()) {
        if (index.getId().getValue().equalsIgnoreCase(userKey.getValue())) {
          userIndexForId = index;
          found = true;
          break;
        }
      }
      if (!found) {
        addActionMessage("API key " + key + " not found (no exact match).");
      }
      userService.removeUserIndexForUser(user, userIndexForId.getId());
      if (user.getUserIndices().isEmpty()) {
        userService.deleteUser(user);
      }
      // Clear the cached value here
      userService.getMinApiRequestIntervalForKey(key, true);

      addActionMessage("Key '" + key + "' deleted");
      clearContactInfoAndKey();
    }
    return SUCCESS;
  }

	private void clearContactInfoAndKey() {
	  minApiReqInt = 100L;
    contactName = "";
    contactCompany = "";
    contactEmail = "";
    contactDetails = "";
	  key = "";
	  return;
	}
}
