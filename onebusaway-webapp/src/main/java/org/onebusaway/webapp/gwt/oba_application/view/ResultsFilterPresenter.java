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
package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.common.widgets.SpanPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.oba_application.control.CommonControl;
import org.onebusaway.webapp.gwt.oba_application.control.Filter;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.model.ResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayCssResource;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayStandardResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultsFilterPresenter {
  
  private static OneBusAwayCssResource _css = OneBusAwayStandardResources.INSTANCE.getCss();

  private FlowPanel _widget = new FlowPanel();

  private FilterImpl _filter = new FilterImpl();

  private List<String> _categories = new ArrayList<String>();

  private CommonControl _control;

  private Set<String> _activeCategories = new HashSet<String>();

  private boolean _minimized = true;

  public ResultsFilterPresenter() {
    this(true);
  }

  public ResultsFilterPresenter(boolean minimized) {
    _widget.addStyleName(_css.ResultsFilter());
    _minimized = minimized;
  }

  public void setControl(CommonControl control) {
    _control = control;
  }

  public Widget getWidget() {
    return _widget;
  }

  public ModelListener<ResultsModel> getResultsModelHandler() {
    return new PlacesModelHandler();
  }

  public ModelListener<StateEvent> getStateEventHandler() {
    throw new IllegalStateException();
  }

  private void refresh() {

    _widget.clear();

    _activeCategories.clear();
    if (!_categories.isEmpty())
      _activeCategories.add(_categories.get(0));

    refreshFilter();

    if (_minimized)
      buildMinimizedWidget();
    else {
      buildMaxmizedWidget();
    }
  }

  private void buildMinimizedWidget() {
    _widget.add(new SpanWidget("Categories:", "ResultsFilter-Label"));

    for (int i = 0; i < Math.min(3, _categories.size()); i++) {
      SpanPanel panel = new SpanPanel();

      final String categoryName = _categories.get(i);

      final CheckBox box = new CheckBox();
      box.addStyleName(_css.ResultsFilterButton());
      box.setText(categoryName);
      box.setValue(_activeCategories.contains(categoryName));
      box.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent arg0) {

          boolean update = false;
          
          if (box.getValue())
            update = _activeCategories.add(categoryName);
          else
            update = _activeCategories.remove(categoryName);

          if (update) {
            refreshFilter();
          }
        }

      });
      panel.add(box);

      _widget.add(panel);
    }
  }

  private void buildMaxmizedWidget() {
    _widget.add(new SpanWidget("Categories:", "ResultsFilter-Label"));

    for (int i = 0; i < Math.min(3, _categories.size()); i++) {
      SpanPanel panel = new SpanPanel();

      final String categoryName = _categories.get(i);

      final CheckBox box = new CheckBox();
      box.addStyleName(_css.ResultsFilterButton());
      box.setText(categoryName);
      box.setValue(_activeCategories.contains(categoryName));
      box.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent arg0) {

          boolean update = false;
          if (box.getValue())
            update = _activeCategories.add(categoryName);
          else
            update = _activeCategories.remove(categoryName);

          if (update) {
            refreshFilter();
          }
        }

      });
      panel.add(box);

      _widget.add(panel);
    }
  }

  private void refreshFilter() {
    _control.filterResults(_filter);
  }

  private class PlacesModelHandler implements ModelListener<ResultsModel> {

    public void handleUpdate(ResultsModel model) {

      List<TimedLocalSearchResult> results = model.getResults();

      Map<String, Integer> counts = new HashMap<String, Integer>();
      for (TimedLocalSearchResult result : results) {
        LocalSearchResult r = result.getLocalSearchResult();
        for (String category : r.getCategories()) {
          Integer count = counts.get(category);
          if (count == null)
            count = 0;
          counts.put(category, count + 1);
        }
      }

      _categories.clear();
      _categories.addAll(counts.keySet());
      Collections.sort(_categories, new MComparator<String, Integer>(counts, false));

      refresh();
    }

  }

  private static class MComparator<K, V extends Comparable<V>> implements Comparator<K> {

    private Map<K, V> _m;
    private boolean _ascending;

    public MComparator(Map<K, V> m, boolean ascending) {
      _m = m;
      _ascending = ascending;
    }

    public int compare(K o1, K o2) {
      V v1 = _m.get(o1);
      V v2 = _m.get(o2);
      int c = v1.compareTo(v2);
      return _ascending ? c : -c;
    }

  }

  private class FilterImpl implements Filter<TimedLocalSearchResult> {

    public boolean isEnabled(TimedLocalSearchResult element) {

      if (_activeCategories.isEmpty())
        return true;

      LocalSearchResult result = element.getLocalSearchResult();

      for (String category : _activeCategories) {
        if (result.getCategories().contains(category))
          return true;
      }

      return false;
    }

  }

}
