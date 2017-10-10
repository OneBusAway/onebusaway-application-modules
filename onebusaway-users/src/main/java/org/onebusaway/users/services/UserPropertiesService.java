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
package org.onebusaway.users.services;

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.model.properties.RouteFilter;

public interface UserPropertiesService {

  public Class<? extends UserProperties> getUserPropertiesType();

  public UserBean getUserAsBean(User user, UserBean bean);

  public UserBean getAnonymousUserAsBean(UserBean bean);

  public void setRememberUserPreferencesEnabled(User user,
      boolean rememberUserPreferencesEnabled);

  public void setDefaultLocation(User user, String locationName, double lat,
      double lon);

  public void clearDefaultLocation(User user);

  public void setLastSelectedStopIds(User user, List<String> stopId);

  /**
   * 
   * @param user
   * @param name
   * @param stopIds
   * @param filter
   * @return the id for the newly created bookmark
   */
  public int addStopBookmark(User user, String name, List<String> stopIds,
      RouteFilter filter);

  public void updateStopBookmark(User user, int id, String name,
      List<String> stopIds, RouteFilter routeFilter);

  public void deleteStopBookmarks(User user, int id);

  /**
   * Authorize this user to use the api
   * 
   * @param User the user
   * @param minApiRequestInteval the minimum time between requests in
   *          milliseconds
   */
  public void authorizeApi(User user, long minApiRequestInteval);

  public void markServiceAlertAsRead(User user, String situationId, long time,
      boolean isRead);
  
  public void updateApiKeyContactInfo(User user, String contactName, 
      String contactCompany, String contactEmail, String contactDetails); 

  public void disableUser(User user);

  public void activateUser(User user);

  public void resetUser(User user);

  public void mergeProperties(User sourceUser, User targetUser);
}
