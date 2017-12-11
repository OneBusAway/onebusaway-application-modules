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
package org.onebusaway.enterprise.webapp.actions;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class that is currently being used to hang stub data methods onto
 */
public abstract class OneBusAwayEnterpriseActionSupport extends NextActionSupport {

  private static final long serialVersionUID = 1L;

  private Long time = null;
  
  public void setTime(long time) {
    this.time = time;
  }
  
  public long getTime() {
    if(time != null) {
      return time;
    } else {
      return SystemTime.currentTimeMillis();
    }
  }
  
  @Autowired
  protected CurrentUserService _currentUserService;
  
  public boolean isAdminUser() {
	return _currentUserService.isCurrentUserAdmin();
  }
  
  public boolean isAnonymousUser() {
    return _currentUserService.isCurrentUserAnonymous();
  }

  public void setSession(Map<String, Object> session) {
    _session = session;
  }
  
  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  // hide or show MTA "weekender" link
  public boolean getShowWeekender() {
    return false;
  }
  
  // CSS + JS cache busting parameter
  public String getFrontEndVersion() {
	 return System.getProperty("front-end.version");
  }

  public String getCacheBreaker() {
	  return String.valueOf(SystemTime.currentTimeMillis());
  }

  protected UserBean getCurrentUser() {
    UserBean user = _currentUserService.getCurrentUser();
    if (user == null)
      user = _currentUserService.getAnonymousUser();
    return user;
  }

}
