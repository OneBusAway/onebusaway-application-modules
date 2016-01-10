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

import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.twilio.actions.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Results({
  @Result(name="back", type="redirectAction", params={"namespace", "/", "actionName", "index"}),
  @Result(name="arrival-and-departure-for-stop-id", type="chain",
      params={"From", "${phoneNumber}", "namespace", "/stops", "actionName", "arrivals-and-departures-for-stop-id"})
})
public class IndexAction extends AbstractBookmarkAction {

  private static Logger _log = LoggerFactory.getLogger(IndexAction.class);
  
  @Override
  public String execute() throws Exception {
    _log.debug("in bookmark execute! with input=" + getInput());
    
    // Check for "back" action
    if (PREVIOUS_MENU_ITEM.equals(getInput())) {
      return "back";
    }

    
    // if we have input (other than "back"), assume its the index of the bookmark
    if (getInput() != null) {
      clearNextAction();
      setSelection();
      return "arrival-and-departure-for-stop-id";
    }
    
    // no input, look for bookmarks
    List<BookmarkWithStopsBean> bookmarks = _bookmarkPresentationService.getBookmarksWithStops(_currentUser.getBookmarks());
    logUserInteraction();


    if (bookmarks == null || bookmarks.isEmpty()) {
      addMessage(Messages.BOOKMARKS_EMPTY);
      addMessage(Messages.HOW_TO_GO_BACK);
      addMessage(Messages.TO_REPEAT);
    } else {
      populateBookmarks(bookmarks, Messages.FOR);
    }
    
    setNextAction("bookmarks/index");
    return INPUT;
  }

}
