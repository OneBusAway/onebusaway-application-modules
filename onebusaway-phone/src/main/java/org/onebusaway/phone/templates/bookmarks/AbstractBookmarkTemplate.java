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

import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.presentation.services.BookmarkPresentationService;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.probablecalls.agitemplates.AbstractAgiTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class AbstractBookmarkTemplate extends AbstractAgiTemplate {

  private TextModification _destinationPronunciation;

  private BookmarkPresentationService _bookmarkPresentationService;

  public AbstractBookmarkTemplate(boolean buildOnEachRequest) {
    super(buildOnEachRequest);
  }

  @Autowired
  public void setDestinationPronunciation(
      @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
    _destinationPronunciation = destinationPronunciation;
  }

  @Autowired
  public void setBookmarkPresentationService(
      BookmarkPresentationService bookmarkPresentationService) {
    _bookmarkPresentationService = bookmarkPresentationService;
  }

  protected void addBookmarkDescription(BookmarkWithStopsBean bookmark) {

    String name = bookmark.getName();
    
    if (name == null || name.length() == 0)
      name = _bookmarkPresentationService.getNameForStops(bookmark.getStops());

    addText(_destinationPronunciation.modify(name));
  }
}
