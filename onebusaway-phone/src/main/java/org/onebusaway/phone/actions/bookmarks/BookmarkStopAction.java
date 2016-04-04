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
package org.onebusaway.phone.actions.bookmarks;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.presentation.services.BookmarkPresentationService;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.users.model.properties.RouteFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookmarkStopAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private List<StopBean> _stops = new ArrayList<StopBean>();

  private BookmarkPresentationService _bookmarkPresentationService;

  @Autowired
  public void setBookmarkPresentationService(
      BookmarkPresentationService bookmarkPresentationService) {
    _bookmarkPresentationService = bookmarkPresentationService;
  }

  public void setStop(StopBean stop) {
    _stops.add(stop);
  }

  public void setStops(List<StopBean> stops) {
    _stops.addAll(stops);
  }

  public List<StopBean> getStops() {
    return _stops;
  }

  @Override
  public String execute() throws Exception {
    
    if (_currentUser != null && !_currentUser.isRememberPreferencesEnabled())
      return "preferences_disabled";
    if (_stops.isEmpty())
      return INPUT;
    String name = _bookmarkPresentationService.getNameForStops(_stops);
    
    List<String> stopIds = MappingLibrary.map(_stops, "id");
    _currentUserService.addStopBookmark(name, stopIds, new RouteFilter());
    
    return SUCCESS;
  }
}
