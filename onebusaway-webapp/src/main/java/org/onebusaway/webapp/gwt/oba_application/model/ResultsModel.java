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
