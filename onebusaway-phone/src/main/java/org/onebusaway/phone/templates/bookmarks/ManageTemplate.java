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
package org.onebusaway.phone.templates.bookmarks;

import java.util.List;

import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@AgiTemplateId("/bookmarks/manage")
public class ManageTemplate extends AbstractBookmarkTemplate {

  public ManageTemplate() {
    super(true);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack stack = context.getValueStack();
    List<BookmarkWithStopsBean> bookmarks = (List<BookmarkWithStopsBean>) stack.findValue("bookmarks");

    if (bookmarks.isEmpty()) {
      addMessage(Messages.BOOKMARKS_EMPTY);
    } else {
      int index = 1;
      for (BookmarkWithStopsBean bookmark : bookmarks) {

        addMessage(Messages.BOOKMARKS_TO_DELETE_THE_BOOKMARK_FOR);

        addBookmarkDescription(bookmark);

        addMessage(Messages.PLEASE_PRESS);
        String toPress = Integer.toString(index);
        addText(toPress);

        AgiActionName deleteAction = addAction(toPress,
            "/bookmarks/deleteByIndex");
        deleteAction.putParam("index", index - 1);

        index++;
      }
    }

    addAction("(#|0|.+\\*)", "/repeat");

    addMessage(Messages.HOW_TO_GO_BACK);
    addAction("\\*", "/back");

    addMessage(Messages.TO_REPEAT);
  }
}
