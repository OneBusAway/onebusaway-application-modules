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
public class DeleteBookmarkAction extends ActionSupport {

    private static final long serialVersionUID = 1L;

    private BookmarkService _service;

    private String _userId;

    private int _index = 0;

    @Autowired
    public void setBookmarkService(BookmarkService service) {
        _service = service;
    }

    public void setUserId(String userId) {
        _userId = userId;
    }

    public void setIndex(int index) {
        _index = index;
    }

    @Override
    public String execute() throws Exception {
        _service.deleteBookmarkByIndex(_userId, _index);
        return SUCCESS;
    }
}
