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
package org.onebusaway.presentation.impl;

import org.onebusaway.presentation.model.DefaultSearchLocation;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class DefaultSearchLocationServiceImpl implements
    DefaultSearchLocationService {

  private static final String KEY_DEFAULT_SEARCH_LOCATION_FOR_SESSSION = DefaultSearchLocationService.class.getName()
      + ".defaultSearchLocationForSession";

  private CurrentUserService _currentUserService;

  @Autowired
  public void setCurrentUserService(CurrentUserService userDataService) {
    _currentUserService = userDataService;
  }

  @Override
  public DefaultSearchLocation getDefaultSearchLocationForCurrentUser() {

    UserBean user = _currentUserService.getCurrentUser();

    if (user != null && user.hasDefaultLocation()) {
      return new DefaultSearchLocation(user.getDefaultLocationName(),
          user.getDefaultLocationLat(), user.getDefaultLocationLon(), false);
    }

    RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
    DefaultSearchLocation location = (DefaultSearchLocation) attributes.getAttribute(
        KEY_DEFAULT_SEARCH_LOCATION_FOR_SESSSION,
        RequestAttributes.SCOPE_SESSION);
    return location;
  }

  @Override
  public void setDefaultLocationForCurrentUser(String locationName, double lat,
      double lon) {
    _currentUserService.setDefaultLocation(locationName, lat, lon);

    DefaultSearchLocation location = new DefaultSearchLocation(locationName,
        lat, lon, true);
    RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
    attributes.setAttribute(KEY_DEFAULT_SEARCH_LOCATION_FOR_SESSSION, location,
        RequestAttributes.SCOPE_SESSION);
  }

  @Override
  public void clearDefaultLocationForCurrentUser() {

    _currentUserService.clearDefaultLocation();

    RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
    attributes.removeAttribute(KEY_DEFAULT_SEARCH_LOCATION_FOR_SESSSION,
        RequestAttributes.SCOPE_SESSION);
  }
}
