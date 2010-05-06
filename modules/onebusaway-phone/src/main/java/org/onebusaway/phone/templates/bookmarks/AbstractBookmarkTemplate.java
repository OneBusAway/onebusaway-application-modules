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
package org.onebusaway.phone.templates.bookmarks;

import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.StopBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;

public abstract class AbstractBookmarkTemplate extends AbstractAgiTemplate {

  private TextModification _destinationPronunciation;

  private TextModification _directionPronunciation;

  public AbstractBookmarkTemplate(boolean buildOnEachRequest) {
    super(buildOnEachRequest);
  }

  @Autowired
  public void setDestinationPronunciation(
      @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
    _destinationPronunciation = destinationPronunciation;
  }
  
  @Autowired
  public void setDirectionPronunciation(
      @Qualifier("directionPronunciation") TextModification directionPronunciation) {
    _directionPronunciation = directionPronunciation;
  }
  
  protected void addStopDescription(StopBean stop) {
    addMessage(Messages.STOP_NUMBER);
    addText(stop.getCode());
    addMessage(Messages.AT);

    addText(_destinationPronunciation.modify(stop.getName()));

    String direct = stop.getDirection();
    String direction = _directionPronunciation.modify(direct);
    addMessage(Messages.DIRECTION_BOUND, direction);
  }
}
