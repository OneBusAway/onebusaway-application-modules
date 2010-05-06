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
package edu.washington.cs.rse.transit.phone.actions;

import java.util.List;

import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.ActionSupport;

import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.services.BookmarkService;

@Component
public class GetBookmarksAction extends ActionSupport {

    private static final long serialVersionUID = 1L;

    private String _userId;

    private BookmarkService _service;

    private List<StopLocation> _bookmarks;

    public void setBookmarkService(BookmarkService service) {
        _service = service;
    }

    public void setUserId(String userId) {
        _userId = userId;
    }

    public List<StopLocation> getBookmarks() {
        return _bookmarks;
    }

    @Override
    public String execute() throws Exception {
        _bookmarks = _service.getBookmarks(_userId);
        return SUCCESS;
    }

}
