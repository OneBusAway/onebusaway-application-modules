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
package org.onebusaway.webapp.actions.where;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.webapp.actions.AbstractAction;

@Results( {@Result(type = "redirectAction", params = {"actionName", "bookmarks"})})
public class BookmarkDeleteAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private int _id;

  public void setId(int id) {
    _id = id;
  }

  @Override
  @Actions( {
    @Action(value = "/where/standard/bookmark-delete"),
    @Action(value = "/where/iphone/bookmark-delete"),
    @Action(value = "/where/text/bookmark-delete")})
  public String execute() {
    _currentUserService.deleteStopBookmarks(_id);
    return SUCCESS;
  }
}