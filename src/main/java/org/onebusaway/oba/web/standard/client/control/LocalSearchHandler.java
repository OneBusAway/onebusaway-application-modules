package org.onebusaway.oba.web.standard.client.control;

import org.onebusaway.common.web.common.client.model.ModelEventSink;
import org.onebusaway.oba.web.common.client.model.LocalSearchResult;
import org.onebusaway.oba.web.common.client.model.LocationBounds;
import org.onebusaway.oba.web.common.client.model.TimedPlaceBean;
import org.onebusaway.oba.web.common.client.rpc.OneBusAwayWebServiceAsync;
import org.onebusaway.oba.web.standard.client.control.state.SearchCompleteState;
import org.onebusaway.oba.web.standard.client.control.state.SearchProgressState;
import org.onebusaway.oba.web.standard.client.model.ResultsModel;
import org.onebusaway.oba.web.standard.client.model.TimedLocalSearchResult;
import org.onebusaway.oba.web.standard.client.search.LocalSearchCallback;
import org.onebusaway.oba.web.standard.client.search.LocalSearchProvider;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalSearchHandler implements LocalSearchCallback {

  private Map<String, LocalSearchResult> _placeResults = new HashMap<String, LocalSearchResult>();

  private PlaceHandler _placeHandler = new PlaceHandler();

  private LocalSearchProvider _searchProvider;

  private ModelEventSink<StateEvent> _events;

  private ResultsModel _model;

  private String _resultId;

  private List<LocationBounds> _searchGrid;

  private String _query;

  private String _category;

  private int _gridIndex = 0;

  private boolean _complete = false;

  public LocalSearchHandler(String resultId, List<LocationBounds> searchGrid, String query, String category) {
    _resultId = resultId;
    _searchGrid = searchGrid;
    _query = query;
    _category = category;
  }

  public void setEventSink(ModelEventSink<StateEvent> events) {
    _events = events;
  }

  public void setLocalSearchProvider(LocalSearchProvider searchProvider) {
    _searchProvider = searchProvider;
  }

  public void setModel(ResultsModel model) {
    _model = model;
  }

  public void run() {
    for (int i = 0; i < 2; i++)
      nextGrid();
  }

  public void onSuccess(List<LocalSearchResult> results) {

    if (results.isEmpty()) {

      nextGrid();

    } else {

      for (LocalSearchResult result : results)
        _placeResults.put(result.getId(), result);

      OneBusAwayWebServiceAsync service = OneBusAwayWebServiceAsync.SERVICE;
      service.getLocalPaths(_resultId, results, _placeHandler);
    }
  }

  public void onFailure(Throwable ex) {
    ex.printStackTrace();
  }

  private void nextGrid() {

    checkCompletion();

    if (_gridIndex < _searchGrid.size()) {
      LocationBounds bounds = _searchGrid.get(_gridIndex++);
      _searchProvider.search(bounds, _query, _category, this);
    }
  }

  private void checkCompletion() {
    if (_gridIndex < _searchGrid.size()) {
      _events.fireModelChange(new StateEvent(new SearchProgressState((double) _gridIndex / _searchGrid.size())));
    } else if (!_complete) {
      _complete = true;
      _events.fireModelChange(new StateEvent(new SearchCompleteState()));
    }
  }

  private class PlaceHandler implements AsyncCallback<List<TimedPlaceBean>> {

    public void onSuccess(List<TimedPlaceBean> beans) {

      List<TimedLocalSearchResult> results = new ArrayList<TimedLocalSearchResult>(beans.size());

      for (final TimedPlaceBean bean : beans) {
        final LocalSearchResult result = _placeResults.get(bean.getPlaceId());
        TimedLocalSearchResult r = new TimedLocalSearchResult(_resultId, result, bean);
        results.add(r);
      }

      _model.addEntries(results);

      nextGrid();
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
      nextGrid();
    }
  }

}
