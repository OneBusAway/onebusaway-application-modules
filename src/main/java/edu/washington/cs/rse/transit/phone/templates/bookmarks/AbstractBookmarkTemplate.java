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

import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;

import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.StreetName;
import edu.washington.cs.rse.transit.phone.PronunciationStrategy;
import edu.washington.cs.rse.transit.phone.templates.Messages;

public abstract class AbstractBookmarkTemplate extends AbstractAgiTemplate {

    protected PronunciationStrategy _strategy;

    public AbstractBookmarkTemplate(boolean buildOnEachRequest) {
        super(buildOnEachRequest);
    }

    public void setPronunciationStrategy(PronunciationStrategy strategy) {
        _strategy = strategy;
    }

    protected void addStopDescription(StopLocation stop) {
        addMessage(Messages.STOP_NUMBER);
        addText(Integer.toString(stop.getId()));
        addMessage(Messages.AT);

        StreetName main = stop.getMainStreetName();
        StreetName cross = stop.getCrossStreetName();

        if (main.getName().equals("Unnamed")) {
            addText(_strategy.getStreetAsText(cross));
        } else if (cross.getName().equals("Unnamed")) {
            addText(_strategy.getStreetAsText(main));
        } else {
            addText(_strategy.getStreetAsText(main));
            addMessage(Messages.AND);
            addText(_strategy.getStreetAsText(cross));
        }

        String direction = _strategy.getDirectionAsText(stop.getDirection());
        addMessage(Messages.DIRECTION_BOUND,direction);
    }
}
