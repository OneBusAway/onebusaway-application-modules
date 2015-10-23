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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.presentation.services.BookmarkPresentationService;
import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class AbstractBookmarkAction extends TwilioSupport {
  private static Logger _log = LoggerFactory.getLogger(AbstractBookmarkAction.class);
  
  protected BookmarkPresentationService _bookmarkPresentationService;
  protected TextModification _destinationPronunciation;
  private List<BookmarkWithStopsBean> _bookmarks;
  private List<String> _stopIds;
  private Set<String> _routeIds;
  private int _index = -1;

  private boolean _isValidSelection = false;

  @Autowired
  public void setBookmarkPresentationService(
      BookmarkPresentationService bookmarkPresentationService) {
    _bookmarkPresentationService = bookmarkPresentationService;
  }

  @Autowired
  public void setDestinationPronunciation(
      @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
    _destinationPronunciation = destinationPronunciation;
  }

  public List<BookmarkWithStopsBean> getBookmarks() {
    return _bookmarks;
  }

  public List<String> getStopIds() {
    return _stopIds;
  }
  
  public void setStopIds(List<String> stopIds) {  
    _stopIds = stopIds;
  }

  public Set<String> getRouteIds() {
    return _routeIds;
  }
  
  public void setRouteIds(Set<String> routeIds) {
    _routeIds = routeIds;
  }

  public int getIndex() {
    return _index;
  }
  
  public void setIndex(int index) {
    _index = index;
  }
  
  public boolean isValidSelection() {
    return _isValidSelection;
  }
  
  protected void populateBookmarks(List<BookmarkWithStopsBean> bookmarks, String messageKey) {
    int index = 1;
    for (BookmarkWithStopsBean bookmark : bookmarks) {
      _log.debug("found bookmark=" + bookmark);
      String toPress = Integer.toString(index);

      addMessage(messageKey);

      List<String> stopIds = MappingLibrary.map(bookmark.getStops(), "id");
      Set<String> routeIds = new HashSet<String>(MappingLibrary.map(
          bookmark.getRoutes(), "id", String.class));
      addBookmarkDescription(bookmark);
      addText(", ");

      addMessage(Messages.PLEASE_PRESS);
      addText(toPress);
      addText(". ");

      index++;
    }
    addMessage(Messages.HOW_TO_GO_BACK);
  }
  
  protected void addBookmarkDescription(BookmarkWithStopsBean bookmark) {

    String name = bookmark.getName();
    
    if (name == null || name.length() == 0) {
      name = _bookmarkPresentationService.getNameForStops(bookmark.getStops());
    }
    
    String destination = _destinationPronunciation.modify(name);
    destination = destination.replaceAll("\\&", "and");
    addText(destination);
  }

  protected void setSelection() {
    List<BookmarkWithStopsBean> bookmarks = _bookmarkPresentationService.getBookmarksWithStops(_currentUser.getBookmarks());
    int selection = 0;
    try {
      selection = Integer.parseInt(getInput()) - 1;
    } catch (NumberFormatException nfe) {
      // bury
    }
    if (selection >= bookmarks.size()) {
      _log.warn("resetting bookmark size to " + (bookmarks.size()-1));
      selection = bookmarks.size()-1;
    }
    _log.debug("bookmark selection=" + selection);

    if (selection < 0) {
      _isValidSelection = false;
      setStopIds(null);
      setRouteIds(null);
    } else {
      _isValidSelection = true;
      BookmarkWithStopsBean bookmark = bookmarks.get(selection);
      _log.debug("selected stop=" + bookmark.getStops());
      List<String> stopIds = MappingLibrary.map(bookmark.getStops(), "id");
      Set<String> routeIds = new HashSet<String>(MappingLibrary.map(
          bookmark.getRoutes(), "id", String.class));
      setStopIds(stopIds);
      setRouteIds(routeIds);
      setIndex(selection);
    }
  }
}
