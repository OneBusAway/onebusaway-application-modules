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
package org.onebusaway.webapp.gwt.oba_application.model;

import org.onebusaway.webapp.gwt.common.model.ModelEventSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsModel {

  private Map<String, TimedLocalSearchResult> _resultsById = new HashMap<String, TimedLocalSearchResult>();

  private ModelEventSink<ResultsModel> _events;

  public void setEvents(ModelEventSink<ResultsModel> events) {
    _events = events;
  }

  public void addEntries(List<TimedLocalSearchResult> results) {

    boolean update = false;

    for (TimedLocalSearchResult result : results) {
      TimedLocalSearchResult current = _resultsById.get(result.getId());
      if (current == null || current.getTime() > result.getTime()) {
        _resultsById.put(result.getId(), result);
        update = true;
      }
    }

    if (update)
      refresh();
  }

  public void clear() {
    _resultsById.clear();
    refresh();
  }

  public int getSize() {
    return _resultsById.size();
  }

  public List<TimedLocalSearchResult> getResults() {
    List<TimedLocalSearchResult> results = new ArrayList<TimedLocalSearchResult>(_resultsById.size());
    for (TimedLocalSearchResult result : _resultsById.values())
      results.add(result);
    return results;
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private void refresh() {
    _events.fireModelChange(this);
  }

}
