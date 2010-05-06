/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.phone.actions;

import com.opensymphony.xwork2.ActionSupport;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.where.services.BookmarkService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetBookmarksAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private String _userId;

  private BookmarkService _service;

  private List<Stop> _bookmarks;

  public void setBookmarkService(BookmarkService service) {
    _service = service;
  }

  public void setUserId(String userId) {
    _userId = userId;
  }

  public List<Stop> getBookmarks() {
    return _bookmarks;
  }

  @Override
  public String execute() throws Exception {
    _bookmarks = _service.getBookmarks(_userId);
    return SUCCESS;
  }

}
