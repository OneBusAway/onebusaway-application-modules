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

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.oba_application.control.CommonControl;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.StateEventListener;
import org.onebusaway.webapp.gwt.oba_application.control.state.PlacesChangedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchCompleteState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchProgressState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchStartedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SelectedPlaceChangedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.State;
import org.onebusaway.webapp.gwt.oba_application.control.state.TripPlansState;
import org.onebusaway.webapp.gwt.oba_application.model.PagedResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayCssResource;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayStandardResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class ResultsTablePresenter implements StateEventListener {
  
  private static OneBusAwayCssResource _css = OneBusAwayStandardResources.INSTANCE.getCss();

  private static final String ROW_LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  private PagedResultsModel _model;

  private CommonControl _control;

  private MapOverlayManager _mapOverlayManager;

  private FlowPanel _panel;

  private Widget _filterWidget;

  private FlowPanel _tablePanel = new FlowPanel();

  private FlowPanel _progressPanel = new FlowPanel();

  //private ProgressBar _progressBar = new ProgressBar(0, 1);

  private List<Overlay> _overlays = new ArrayList<Overlay>();

  private boolean _minimized = false;

  public ResultsTablePresenter() {
    initializeWidget();
  }

  public void setResultsModel(PagedResultsModel model) {
    _model = model;
  }

  public void setMapOverlayManager(MapOverlayManager manager) {
    _mapOverlayManager = manager;
  }

  public void setControl(CommonControl control) {
    _control = control;
  }

  public void setResultsFilterWidget(Widget widget) {
    _filterWidget = widget;
  }

  public Widget getWidget() {
    if (_panel == null)
      initializeWidget();
    return _panel;
  }

  public void handleUpdate(StateEvent event) {
    State state = event.getState();
    if (state instanceof SearchStartedState) {
      _progressPanel.setVisible(true);
      //_progressBar.setProgress(0);
      clear();
    } else if (state instanceof SearchProgressState) {
      SearchProgressState sps = (SearchProgressState) state;
      //_progressBar.setProgress(sps.getPercentComplete());
    } else if (state instanceof SearchCompleteState) {
      _progressPanel.setVisible(false);
    } else if (state instanceof PlacesChangedState) {
      PlacesChangedState pcs = (PlacesChangedState) state;
      PagedResultsModel model = pcs.getModel();
      if (model.getSelectedResult() == null)
        _minimized = false;
      refresh();
    } else if (state instanceof SelectedPlaceChangedState) {
      SelectedPlaceChangedState pcs = (SelectedPlaceChangedState) state;
      if (pcs.getSelectedResult() == null) {
        _minimized = false;
        refresh();
      }
    } else if (state instanceof TripPlansState) {
      _minimized = true;
      refresh();
    }
  }

  private void initializeWidget() {

    _panel = new FlowPanel();
    _panel.addStyleName(_css.ResultsTableWidget());

    //_progressBar.setTextFormatter(new SearchProgressTextFormatter());
    //_progressPanel.add(_progressBar);
    _progressPanel.setVisible(false);

    _panel.add(_progressPanel);

    _tablePanel.addStyleName(_css.ResultsTableWidgetTablePanel());
    _panel.add(_tablePanel);
  }

  private void refresh() {

    clear();

    if (_minimized) {

      DivPanel panel = new DivPanel();
      panel.addStyleName(_css.ResultsTableWidgetMinimized());
      _tablePanel.add(panel);

      Anchor showAllEntries = new Anchor("Show all results...");
      showAllEntries.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent arg0) {
          _control.clearActiveSearchResult();
        }
      });
      panel.add(showAllEntries);
      
      constructAttributionPanel();
      
    } else {

      _tablePanel.add(_filterWidget);

      List<TimedLocalSearchResult> entries = _model.getActiveEntries();

      Grid grid = new Grid(entries.size() + 1, 4);
      grid.addStyleName(_css.ResultsTableWidgetResultsTable());
      grid.setText(0, 0, "");
      grid.setWidget(0, 1, getTableHeaderWidget("Name", EResultsSort.NAME));
      grid.setWidget(0, 2, getTableHeaderWidget("Rating", EResultsSort.RATING));
      grid.setWidget(0, 3, getTableHeaderWidget("Distance", EResultsSort.DISTANCE));

      grid.getRowFormatter().addStyleName(0, "ResultsTableWidget-ResultsTable-HeaderRow");

      grid.getCellFormatter().addStyleName(0, 1, "ResultsTableWidget-ResultsTable-HeaderRow-Name");
      grid.getCellFormatter().addStyleName(0, 2, "ResultsTableWidget-ResultsTable-HeaderRow-Rating");
      grid.getCellFormatter().addStyleName(0, 3, "ResultsTableWidget-ResultsTable-HeaderRow-Distance");

      int index = 0;

      LatLngBounds bounds = LatLngBounds.newInstance();

      for (TimedLocalSearchResult tlsr : entries) {

        LocalSearchResult entry = tlsr.getLocalSearchResult();

        int tableRow = index + 1;
        EntryClickHandler handler = new EntryClickHandler(tlsr);

        String labelPre = index < ROW_LABELS.length() ? (ROW_LABELS.charAt(index) + "") : "";
        grid.setWidget(tableRow, 0, new SpanWidget(labelPre));
        grid.getCellFormatter().addStyleName(tableRow, 0, "ResultsTableWidget-ResultsTable-LabelColumn");

        Anchor name = new Anchor(entry.getName());
        name.addClickHandler(handler);
        grid.setWidget(tableRow, 1, name);
        grid.getCellFormatter().addStyleName(tableRow, 1, "ResultsTableWidget-ResultsTable-NameColumn");

        if (entry.getRatingUrlSmall() != null) {
          Image image = new Image(entry.getRatingUrlSmall());
          grid.setWidget(tableRow, 2, image);
        } else {
          String rating = entry.getRating() + "/" + entry.getMaxRating();
          grid.setText(tableRow, 2, rating);
        }
        grid.getCellFormatter().addStyleName(tableRow, 2, "ResultsTableWidget-ResultsTable-RatingColumn");

        int minutes = tlsr.getTime() / 60;
        String minLabel = minutes + " " + (minutes == 1 ? " min" : " mins");
        grid.setText(tableRow, 3, minLabel);
        grid.getCellFormatter().addStyleName(tableRow, 3, "ResultsTableWidget-ResultsTable-TimeColumn");

        grid.getRowFormatter().addStyleName(tableRow, "ResultsTableWidget-ResultsTable-ResultRow");

        Marker marker = getMarker(index, entry);
        marker.addMarkerClickHandler(handler);
        _mapOverlayManager.addOverlay(marker, 10, 20);
        _overlays.add(marker);

        index++;

        bounds.extend(marker.getLatLng());
      }

      if (!bounds.isEmpty()) {
        MapWidget map = _mapOverlayManager.getMapWidget();
        LatLngBounds currentView = map.getBounds();
        if (!currentView.containsBounds(bounds)) {
          _mapOverlayManager.setCenterAndZoom(bounds);
        }
      }

      _tablePanel.add(grid);

      constructItemNavigationPanel(entries);
      constructAttributionPanel();
    }
  }

  private void clear() {
    for (Overlay overlay : _overlays)
      _mapOverlayManager.removeOverlay(overlay);
    _overlays.clear();
    _tablePanel.clear();
  }

  private Widget getTableHeaderWidget(String name, final EResultsSort sortType) {

    Anchor widget = new Anchor(name);
    widget.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent arg0) {
        _model.setSortMode(sortType);
      }
    });

    if (_model.getSortMode().equals(sortType)) {
      SpanPanel panel = new SpanPanel();
      panel.add(widget);
      OneBusAwayStandardResources resources = OneBusAwayStandardResources.INSTANCE;
      ImageResource resource = _model.isReverseSort() ? resources.getTriangleUp() : resources.getTriangleDown();
      Image img = new Image(resource.getURL());
      panel.add(img);
      return panel;
    }

    return widget;
  }

  private Marker getMarker(int index, LocalSearchResult entry) {

    ImageResource resource = getMarkerResource(index);
    Icon icon = Icon.newInstance(resource.getURL());
    icon.setIconSize(Size.newInstance(24, 31));
    icon.setIconAnchor(Point.newInstance(12, 31));
    MarkerOptions opts = MarkerOptions.newInstance();
    opts.setClickable(true);
    opts.setIcon(icon);
    LatLng point = LatLng.newInstance(entry.getLat(), entry.getLon());
    return new Marker(point, opts);
  }

  private void constructItemNavigationPanel(List<TimedLocalSearchResult> entries) {

    int totalSize = _model.getTotalSize();
    int pageSize = _model.getPageSize();
    int pageIndex = _model.getPageIndex();
    int totalPages = (totalSize + 9) / pageSize;
    int fromIndex = pageIndex * pageSize + 1;
    int toIndex = fromIndex + entries.size() - 1;

    if (totalPages <= 1)
      return;

    Grid elements = new Grid(1, 3);
    elements.addStyleName(_css.ResultsTableWidgetNavigationWidget());
    String itemCountStyle = "ItemCount";
    DivWidget itemCount = new DivWidget("<span class=\"" + itemCountStyle + "\">" + fromIndex
        + "</span> to <span class=\"" + itemCountStyle + "\">" + toIndex + "</span> of <span class=\"" + itemCountStyle
        + "\">" + totalSize + "</span>");
    itemCount.addStyleName(_css.ItemCountPanel());
    elements.setWidget(0, 0, itemCount);

    FlowPanel pagesPanel = new FlowPanel();
    pagesPanel.addStyleName(_css.PagesPanel());
    pagesPanel.add(new SpanWidget("Go to page "));

    int lastPage = -1;

    for (int i = 0; i < totalPages; i++) {
      final int pIndex = i;

      boolean display = (i == 0) || (i == totalPages - 1) || (Math.abs(i - pageIndex) < 3);

      if (display) {

        if (lastPage != -1) {
          String value = pIndex - lastPage == 1 ? "|" : "...";
          pagesPanel.add(new SpanWidget(value));
        }

        String label = Integer.toString(pIndex + 1);
        Widget page = (i == pageIndex) ? new SpanWidget(label) : new Anchor(label);
        pagesPanel.add(page);
        page.addStyleName(_css.PageCount());
        if (i == pageIndex) {
          page.addStyleName(_css.CurrentPage());
        } else {
          ((Anchor) page).addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
              _model.setPageIndex(pIndex);
            }
          });
        }

        lastPage = pIndex;
      }
    }
    elements.setWidget(0, 1, pagesPanel);

    FlowPanel prevNextPagePanel = new FlowPanel();
    prevNextPagePanel.addStyleName(_css.PrevNextPanel());

    if (pageIndex > 0) {
      Anchor prev = new Anchor("Previous");
      prevNextPagePanel.add(prev);
      if (pageIndex > 0) {
        prev.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent arg0) {
            int pi = _model.getPageIndex();
            _model.setPageIndex(pi - 1);
          }
        });
      }
    }

    if (pageIndex > 0 && pageIndex < totalPages - 1)
      prevNextPagePanel.add(new SpanWidget("|"));

    if (pageIndex < totalPages - 1) {
      Anchor next = new Anchor("Next");
      prevNextPagePanel.add(next);
      if (pageIndex + 1 < totalPages) {
        next.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent arg0) {
            int pi = _model.getPageIndex();
            _model.setPageIndex(pi + 1);
          }
        });
      }
    }

    elements.setWidget(0, 2, prevNextPagePanel);

    _tablePanel.add(elements);
  }

  private void constructAttributionPanel() {
    DivPanel yelpAttribution = new DivPanel();
    yelpAttribution.addStyleName(_css.OneBusAwayYelpPanel());
    Anchor anchor = new Anchor(
        "<img src=\"http://static.px.yelp.com/static/20090202/i/new/developers/reviewsFromYelpRED.gif\"/>", true,
        "http://www.yelp.com/");
    anchor.addStyleName(_css.OneBusAwayYelpLink());
    yelpAttribution.add(anchor);

    _tablePanel.add(yelpAttribution);
  }

  /**
   * Man this is an ugly function, but kind of a necessity with the GWT
   * ResourceBundle method
   * 
   * @param index
   * @return
   */
  private ImageResource getMarkerResource(int index) {
    OneBusAwayStandardResources r = OneBusAwayStandardResources.INSTANCE;
    switch (index) {
      case 0:
        return r.getMarkerA();
      case 1:
        return r.getMarkerB();
      case 2:
        return r.getMarkerC();
      case 3:
        return r.getMarkerD();
      case 4:
        return r.getMarkerE();
      case 5:
        return r.getMarkerF();
      case 6:
        return r.getMarkerG();
      case 7:
        return r.getMarkerH();
      case 8:
        return r.getMarkerI();
      case 9:
        return r.getMarkerJ();
      case 10:
        return r.getMarkerK();
      case 11:
        return r.getMarkerL();
      case 12:
        return r.getMarkerM();
      case 13:
        return r.getMarkerN();
      case 14:
        return r.getMarkerO();
      case 15:
        return r.getMarkerP();
      case 16:
        return r.getMarkerQ();
      case 17:
        return r.getMarkerR();
      case 18:
        return r.getMarkerS();
      case 19:
        return r.getMarkerT();
      case 20:
        return r.getMarkerU();
      case 21:
        return r.getMarkerV();
      case 22:
        return r.getMarkerW();
      case 23:
        return r.getMarkerX();
      case 24:
        return r.getMarkerY();
      case 25:
        return r.getMarkerZ();
      default:
        return r.getMarker();
    }
  }

  private class EntryClickHandler implements MarkerClickHandler, ClickHandler {

    private TimedLocalSearchResult _result;

    public EntryClickHandler(TimedLocalSearchResult result) {
      _result = result;
    }

    public void onClick(MarkerClickEvent event) {
      _control.setActiveSearchResult(_result);
    }

    public void onClick(ClickEvent arg0) {
      _control.setActiveSearchResult(_result);
    }
  }

  /*
  private class SearchProgressTextFormatter extends TextFormatter {
    @Override
    protected String getText(ProgressBar bar, double curProgress) {
      return "Searching...";
    }
  }
  */

}
