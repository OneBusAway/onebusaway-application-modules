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
package org.onebusaway.where.phone.templates.bookmarks;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.where.impl.ApplicationBeanLibrary;
import org.onebusaway.where.phone.PronunciationStrategy;
import org.onebusaway.where.phone.templates.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;

public abstract class AbstractBookmarkTemplate extends AbstractAgiTemplate {

  protected PronunciationStrategy _strategy;

  public AbstractBookmarkTemplate(boolean buildOnEachRequest) {
    super(buildOnEachRequest);
  }

  @Autowired
  public void setPronunciationStrategy(PronunciationStrategy strategy) {
    _strategy = strategy;
  }

  protected void addStopDescription(Stop stop) {
    addMessage(Messages.STOP_NUMBER);
    addText(stop.getId());
    addMessage(Messages.AT);

    addText(_strategy.getStopNameAsText(stop.getName()));

    String direct = ApplicationBeanLibrary.getStopDirection(stop);
    String direction = _strategy.getDirectionAsText(direct);
    addMessage(Messages.DIRECTION_BOUND, direction);
  }
}
