package org.onebusaway.oba.web.standard.client.model;

import org.onebusaway.common.web.common.client.model.ModelEventSink;
import org.onebusaway.common.web.common.client.model.ModelListener;
import org.onebusaway.oba.web.standard.client.control.Filter;

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
