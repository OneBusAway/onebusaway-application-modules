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
package org.onebusaway.twilio.actions.bookmarks;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.presentation.services.BookmarkPresentationService;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.onebusaway.users.model.properties.RouteFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Results({
  @Result(name="bookmarks-index", type="redirectAction",
      params={"namespace", "/bookmarks", "actionName", "index"}),
  @Result(name="index", type="redirectAction",
      params={"namespace", "/", "actionName", "index"})
})
public class BookmarkStopAction extends TwilioSupport {

  private static final int MAX_BOOKMARKS = 7; // allow menu options 8,9 to still work

  private static Logger _log = LoggerFactory.getLogger(BookmarkStopAction.class);
  
  private List<StopBean> _stops = new ArrayList<StopBean>();
  private List<String> _stopIds;
  
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

  public void setStopIds(List<String> stopIds) {
    _stopIds = stopIds;
  }

  

  @Override
  public String execute() throws Exception {
    _log.debug("in execute! with input=" + getInput() + " and stops=" + _stops
        + " and stopIds=" + _stopIds
        + " and user=" + _currentUser);
    
    if (PREVIOUS_MENU_ITEM.equals(getInput()) || "9".equals(getInput())) {
      clearNextAction();
      return "index";
    }
    
    if (_currentUser != null && !_currentUser.isRememberPreferencesEnabled())
      return "preferences_disabled";
    
    setNextAction("bookmarks/bookmark-stop");
    
    if (_currentUser == null) {
      // something went wrong, bail
      addMessage(Messages.BOOKMARKS_EMPTY);
      addMessage(Messages.HOW_TO_GO_BACK);
      addMessage(Messages.TO_REPEAT);
      return INPUT;
    }
    
    // from here we have a currentUser
    if (_stops.isEmpty()) {
      _log.debug("empty stops!");
      addMessage(Messages.BOOKMARKS_EMPTY);
      addMessage(Messages.HOW_TO_GO_BACK);
      addMessage(Messages.TO_REPEAT);
      return INPUT;
    }
    
    // to allow REPEAT/BACK choices to still work, we need to limit bookmarks to 7
    List<BookmarkWithStopsBean> bookmarks = _bookmarkPresentationService.getBookmarksWithStops(_currentUser.getBookmarks());
    if (bookmarks.size() > MAX_BOOKMARKS) {
      addMessage(Messages.BOOKMARKS_AT_CAPACITY);
      addMessage(Messages.HOW_TO_GO_BACK);
      return SUCCESS;
    }

    // make sure the bookmark isn't already there
    for (BookmarkWithStopsBean bookmark : bookmarks) {
      for (StopBean stopBean : _stops) {
        if (bookmark.getStops().contains(stopBean)) {
          addMessage(Messages.BOOKMARK_ALREADY_ADDED);
          addMessage(Messages.HOW_TO_GO_BACK);
          return SUCCESS;
        }
      }
    }
    
    // add the bookmark
    String name = _bookmarkPresentationService.getNameForStops(_stops);
    
    List<String> stopIds = MappingLibrary.map(_stops, "id");
    _currentUserService.addStopBookmark(name, stopIds, new RouteFilter());
    
    logUserInteraction("stopIds",stopIds);
    addMessage(Messages.BOOKMARK_ADDED);
    return SUCCESS;
  }

}
