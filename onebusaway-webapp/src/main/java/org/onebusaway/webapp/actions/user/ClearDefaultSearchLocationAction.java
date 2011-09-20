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
package org.onebusaway.webapp.actions.user;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.springframework.beans.factory.annotation.Autowired;

public class ClearDefaultSearchLocationAction extends AbstractRedirectAction {

  private static final long serialVersionUID = 1L;

  private DefaultSearchLocationService _defaultSearchLocationService;

  @Autowired
  public void setDefaultSearchLocationService(
      DefaultSearchLocationService defaultSearchLocationService) {
    _defaultSearchLocationService = defaultSearchLocationService;
  }

  @Override
  @Actions( {
    @Action(value = "/user/clear-default-search-location"),
    @Action(value = "/where/iphone/user/clear-default-search-location"),
    @Action(value = "/where/text/user/clear-default-search-location")})
  public String execute() {
    _defaultSearchLocationService.clearDefaultLocationForCurrentUser();
    return SUCCESS;
  }
}
