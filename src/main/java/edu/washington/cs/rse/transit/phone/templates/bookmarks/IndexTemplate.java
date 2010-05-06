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
package edu.washington.cs.rse.transit.phone.templates.bookmarks;

import java.util.List;

import org.traditionalcake.probablecalls.AgiActionName;
import org.traditionalcake.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.phone.templates.Messages;

@AgiTemplateId("/bookmarks/index")
public class IndexTemplate extends AbstractBookmarkTemplate {

    public IndexTemplate() {
        super(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void buildTemplate(ActionContext context) {

        ValueStack stack = context.getValueStack();
        List<StopLocation> bookmarks = (List<StopLocation>) stack.findValue("bookmarks");

        if (bookmarks.isEmpty()) {
            addMessage(Messages.BOOKMARKS_EMPTY);
        } else {
            int index = 1;

            for (StopLocation location : bookmarks) {

                String toPress = Integer.toString(index);

                addMessage(Messages.FOR);

                AgiActionName stopAction = addAction(toPress, "/stop/byId");
                stopAction.putParam("stopId", location.getId());

                addStopDescription(location);

                addMessage(Messages.PLEASE_PRESS);
                addText(toPress);

                index++;
            }
        }

        addAction("(#|0|.+\\*)", "/repeat");

        addMessage(Messages.HOW_TO_GO_BACK);
        addAction("\\*", "/back");

        addMessage(Messages.TO_REPEAT);
    }
}
