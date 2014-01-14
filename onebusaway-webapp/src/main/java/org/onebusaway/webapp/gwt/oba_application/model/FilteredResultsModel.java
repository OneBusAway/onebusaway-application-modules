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
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.oba_application.control.Filter;

import java.util.ArrayList;
import java.util.List;

public class FilteredResultsModel {

  private List<TimedLocalSearchResult> _results = new ArrayList<TimedLocalSearchResult>();

  private ResultsModel _model;

  private ModelEventSink<FilteredResultsModel> _events;

  private Filter<TimedLocalSearchResult> _filter = null;

  public void setEvents(ModelEventSink<FilteredResultsModel> events) {
    _events = events;
  }

  public void setResultsModel(ResultsModel model) {
    _model = model;
  }

  public ModelListener<ResultsModel> getResultsModelHandler() {
    return new ResultsModelHandler();
  }

  public List<TimedLocalSearchResult> getResults() {
    return _results;
  }

  public void setFilter(Filter<TimedLocalSearchResult> filter) {
    _filter = filter;
    refresh();
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private void refresh() {
    _results.clear();
    for (TimedLocalSearchResult result : _model.getResults()) {
      if (_filter == null || _filter.isEnabled(result))
        _results.add(result);
    }
    _events.fireModelChange(this);
  }

  private class ResultsModelHandler implements ModelListener<ResultsModel> {

    public void handleUpdate(ResultsModel model) {
      refresh();
    }
  }
}
