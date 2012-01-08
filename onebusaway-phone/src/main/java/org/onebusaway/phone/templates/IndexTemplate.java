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
package org.onebusaway.phone.templates;

import org.onebusaway.probablecalls.agitemplates.AbstractAgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;

@AgiTemplateId("/index")
public class IndexTemplate extends AbstractAgiTemplate {

    @Override
    public void buildTemplate(ActionContext context) {

        addAction("0", "/help", "message", Messages.INDEX_HELP);

        addAction("1", "/stop/index");
        addAction("2", "/find_your_stop");
        addAction("3", "/bookmarks/index");
        addAction("4", "/bookmarks/manage");
        addAction("5", "/most_recent");
        addAction("6", "/search/index");
        addAction("7", "/settings/index");
        addAction("[#89*]", "/repeat");

        addMessage(Messages.INDEX_STOP_ACTION);
        addMessage(Messages.INDEX_FIND_YOUR_STOP_ACTION);
        addMessage(Messages.INDEX_BOOKMARKS_ACTION);
        addMessage(Messages.INDEX_BOOKMARKS_MANAGE_ACTION);
        addMessage(Messages.INDEX_MOST_RECENT_ACTION);
        addMessage(Messages.INDEX_SEARCH_ACTION);
        addMessage(Messages.INDEX_SETTINGS_ACTION);

        addMessage(Messages.HOW_TO_GET_HELP);
        addMessage(Messages.TO_REPEAT);
    }

}
