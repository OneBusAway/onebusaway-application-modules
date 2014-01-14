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
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.state.PlacesChangedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SelectedPlaceChangedState;
import org.onebusaway.webapp.gwt.oba_application.view.EResultsSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PagedResultsModel {

  private ModelEventSink<StateEvent> _events;

  private FilteredResultsModel _model;

  private int _totalSize = 0;

  private List<TimedLocalSearchResult> _activeEntries = new ArrayList<TimedLocalSearchResult>();

  private TimedLocalSearchResult _selectedResult;

  private EResultsSort _sort = EResultsSort.RATING;

  private boolean _reverseSort = false;

  private int _pageSize = 10;

  private int _pageIndex = 0;

  public void setEvents(ModelEventSink<StateEvent> events) {
    _events = events;
  }

  public void setModel(FilteredResultsModel model) {
    _model = model;
  }

  public ModelListener<FilteredResultsModel> getModelListener() {
    return new ModelHandler();
  }

  public int getTotalSize() {
    return _totalSize;
  }

  public int getActiveSize() {
    return _activeEntries.size();
  }

  public int getPageSize() {
    return _pageSize;
  }

  public void setPageSize(int pageSize) {
    if (_pageSize != pageSize) {
      _pageSize = pageSize;
      refresh();
    }
  }

  public void setPageIndex(int pageIndex) {
    if (_pageIndex != pageIndex) {
      _pageIndex = pageIndex;
      refresh();
    }
  }

  public int getPageIndex() {
    return _pageIndex;
  }

  public EResultsSort getSortMode() {
    return _sort;
  }

  public boolean isReverseSort() {
    return _reverseSort;
  }

  public void setSortMode(EResultsSort sort) {
    if (_sort.equals(sort))
      _reverseSort = !_reverseSort;
    _sort = sort;
    refresh();
    _pageIndex = 0;
    refresh();
  }

  public boolean getReverseSort() {
    return _reverseSort;
  }

  public List<TimedLocalSearchResult> getActiveEntries() {
    return _activeEntries;
  }

  public void setSelectedResult(TimedLocalSearchResult result) {
    _selectedResult = result;
    _events.fireModelChange(new StateEvent(new SelectedPlaceChangedState(_selectedResult)));
  }

  public void clearActiveSearchResult() {
    setSelectedResult(null);
  }

  public TimedLocalSearchResult getSelectedResult() {
    return _selectedResult;
  }

  private void refresh() {

    List<TimedLocalSearchResult> entries = _model.getResults();
    Collections.sort(entries, getComparator());

    _totalSize = entries.size();
    int indexFrom = _pageIndex * _pageSize;
    int indexTo = Math.min(indexFrom + _pageSize, _totalSize);
    int activeCount = indexTo - indexFrom;
    List<TimedLocalSearchResult> active = new ArrayList<TimedLocalSearchResult>(activeCount);
    for (int i = 0; i < activeCount; i++)
      active.add(entries.get(indexFrom + i));

    if (!_activeEntries.equals(active)) {
      _activeEntries = active;

      _events.fireModelChange(new StateEvent(new PlacesChangedState(this)));
    }
  }

  private Comparator<TimedLocalSearchResult> getComparator() {
    Comparator<TimedLocalSearchResult> c = _sort.getComparator();
    if (_reverseSort)
      c = new ReverseComparator<TimedLocalSearchResult>(c);
    return c;
  }

  /*****************************************************************************
   * Nested Classes
   ****************************************************************************/

  private class ModelHandler implements ModelListener<FilteredResultsModel> {

    public void handleUpdate(FilteredResultsModel model) {
      _selectedResult = null;
      refresh();
    }
  }

  private static class ReverseComparator<T> implements Comparator<T> {

    private Comparator<T> _comparator;

    public ReverseComparator(Comparator<T> comparator) {
      _comparator = comparator;
    }

    public int compare(T o1, T o2) {
      int rc = _comparator.compare(o1, o2);
      return rc == 0 ? 0 : (rc < 0 ? 1 : -1);
    }

  }

}
