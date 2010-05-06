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

import org.onebusaway.where.services.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;

@Component
public class BookmarkStopAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private BookmarkService _bookmarkService;

  private String _stopId;

  private String _userId;

  @Autowired
  public void setBookmarkService(BookmarkService service) {
    _bookmarkService = service;
  }

  public void setStopId(String stopId) {
    _stopId = stopId;
  }

  public String getStopId() {
    return _stopId;
  }

  public void setUserId(String userId) {
    _userId = userId;
  }

  @Override
  public String execute() throws Exception {

    _bookmarkService.addStopBookmark(_userId, _stopId);

    return SUCCESS;
  }
}
