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
package org.onebusaway.presentation.services;

import java.util.List;

import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.users.client.model.BookmarkBean;

public interface BookmarkPresentationService {
  
  public List<BookmarkWithStopsBean> getBookmarksWithStops(
      List<BookmarkBean> bookmarks);

  public String getNameForStops(List<StopBean> stops);

  public String getNameForStopIds(List<String> stopIds);

  public String getNameForBookmark(BookmarkWithStopsBean bookmark);
}
