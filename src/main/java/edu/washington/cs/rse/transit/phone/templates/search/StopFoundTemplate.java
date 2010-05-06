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
package edu.washington.cs.rse.transit.phone.templates.search;

import org.traditionalcake.probablecalls.AgiActionName;
import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;
import org.traditionalcake.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

import edu.washington.cs.rse.transit.phone.PronunciationStrategy;
import edu.washington.cs.rse.transit.phone.templates.Messages;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopBean;

@AgiTemplateId("/search/stopFound")
public class StopFoundTemplate extends AbstractAgiTemplate {

    private static final long serialVersionUID = 1L;

    private PronunciationStrategy _strategy;

    public void setPronunciationStrategy(PronunciationStrategy strategy) {
        _strategy = strategy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void buildTemplate(ActionContext context) {

        ValueStack vs = context.getValueStack();

        StopBean stop = (StopBean) vs.findValue("stop");

        addMessage(Messages.THE_STOP_NUMBER_FOR);

        addText(_strategy.getDestinationAsText(stop.getMainStreet()));
        addMessage(Messages.AND);
        addText(_strategy.getDestinationAsText(stop.getCrossStreet()));

        String direction = _strategy.getDirectionAsText(stop.getDirection());
        addMessage(Messages.DIRECTION_BOUND, direction);

        addText(Messages.IS);
        addText(Integer.toString(stop.getId()));

        addMessage(Messages.STOP_FOUND_ARRIVAL_INFO);
        AgiActionName arrivalInfoAction = addAction("1", "/stop/byId");
        arrivalInfoAction.putParam("stopId", stop.getId());

        addMessage(Messages.STOP_FOUND_BOOKMARK_THIS_LOCATION);
        AgiActionName bookmarkAction = addAction("2", "/stop/bookmark");
        bookmarkAction.putParam("stopId", stop.getId());

        addMessage(Messages.STOP_FOUND_RETURN_TO_MAIN_MENU);
        addAction("3", "/index");
        
        addAction("[04-9]","/repeat");

        addMessage(Messages.HOW_TO_GO_BACK);
        addAction("\\*", "/back");

        addMessage(Messages.TO_REPEAT);
    }
}
