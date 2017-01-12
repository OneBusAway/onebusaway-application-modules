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
package org.onebusaway.users.impl.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.users.services.logging.UserInteractionLoggingIndicator;
import org.onebusaway.users.services.logging.UserInteractionLoggingOutlet;
import org.onebusaway.users.services.logging.UserInteractionLoggingService;
import org.onebusaway.utility.time.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserInteractionLoggingServiceImpl implements
    UserInteractionLoggingService {

  private static Logger _log = LoggerFactory.getLogger(UserInteractionLoggingServiceImpl.class);

  private CurrentUserService _currentUserService;

  private List<UserInteractionLoggingIndicator> _indicators = new ArrayList<UserInteractionLoggingIndicator>();

  private List<UserInteractionLoggingOutlet> _outlets = new ArrayList<UserInteractionLoggingOutlet>();

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }
  
  public void setIndicator(UserInteractionLoggingIndicator indicator) {
    _indicators = Arrays.asList(indicator);
  }

  public void setIndicators(List<UserInteractionLoggingIndicator> indicators) {
    _indicators = indicators;
  }

  public void setOutlet(UserInteractionLoggingOutlet outlet) {
    _outlets = Arrays.asList(outlet);
  }
  
  public void setOutlets(List<UserInteractionLoggingOutlet> outlets) {
    _outlets = outlets;
  }

  @Override
  public Map<String, Object> isInteractionLoggedForCurrentUser() {

    IndexedUserDetails details = _currentUserService.getCurrentUserDetails();

    if (details == null)
      return null;

    for (UserInteractionLoggingIndicator indicator : _indicators) {
      Map<String, Object> obj = indicator.isLoggingEnabledForUser(details);
      if (obj != null)
        return obj;
    }

    return null;
  }

  @Override
  public void logInteraction(Map<String, Object> entry) {
    try {
      JSONObject obj = getMapAsJSONObject(entry);
      obj.put("timestamp", SystemTime.currentTimeMillis());
      String serialized = obj.toString();
      for (UserInteractionLoggingOutlet outlet : _outlets)
        outlet.logInteraction(serialized);
    } catch (JSONException ex) {
      _log.error("error serializing logging entry to json", ex);
    }
  }

  private Object jsonifyValue(Object value) throws JSONException {
    if (value instanceof Map<?, ?>)
      return getMapAsJSONObject((Map<?, ?>) value);
    else if (value instanceof Iterable<?>)
      return getIterableAsJSONArray((Iterable<?>) value);

    return value;
  }

  private JSONObject getMapAsJSONObject(Map<?, ?> map) throws JSONException {
    JSONObject obj = new JSONObject();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String key = entry.getKey().toString();
      Object value = entry.getValue();
      value = jsonifyValue(value);
      obj.put(key, value);
    }
    return obj;
  }
  

  private JSONArray getIterableAsJSONArray(Iterable<?> iterable) throws JSONException {
    JSONArray array = new JSONArray();
    for( Object object : iterable)
      array.put(jsonifyValue(object));
    return array;
  }

}
