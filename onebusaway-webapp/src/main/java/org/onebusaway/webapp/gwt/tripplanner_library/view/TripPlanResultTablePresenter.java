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
package org.onebusaway.webapp.gwt.tripplanner_library.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.LegBean;
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.tripplanner_library.model.TripPlanModel;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerCssResource;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class TripPlanResultTablePresenter implements
    ModelListener<TripPlanModel> {

  private static TripPlannerCssResource _css = TripPlannerResources.INSTANCE.getCss();

  private DateTimeFormat _timeFormat = DateTimeFormat.getShortTimeFormat();

  private SingleTripPresenter _singleTripPresenter = new SingleTripPresenter();

  private TripBeanMapPresenter _tripMapPresenter = new TripBeanMapPresenter();

  private FlowPanel _panel = new FlowPanel();

  private MapWidget _map;

  private List<Overlay> _overlays = new ArrayList<Overlay>();

  private TripPlanModel _model;

  public TripPlanResultTablePresenter() {
    _panel.addStyleName(_css.tripPlanResultsTable());
    _panel.add(_singleTripPresenter.getWidget());
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
    _tripMapPresenter.setMapWidget(_map);
  }

  public Widget getWidget() {
    return _panel;
  }

  public void clear() {
    _panel.setVisible(false);
    _panel.clear();
    for (Overlay overlay : _overlays)
      _map.removeOverlay(overlay);
  }

  public void handleUpdate(TripPlanModel model) {

    _model = model;
    _panel.clear();
    _panel.setVisible(true);

    List<ItineraryBean> trips = model.getTrips();
    int selectedIndex = model.getSelectedIndex();

    Grid grid = new Grid(trips.size(), 4);
    _panel.add(grid);

    for (int index = 0; index < trips.size(); index++) {
      ItineraryBean trip = trips.get(index);
      Date start = new Date(trip.getStartTime());
      Date end = new Date(trip.getEndTime());

      String timeStartAndEndLabel = _timeFormat.format(start) + " - "
          + _timeFormat.format(end);
      String durationLabel = TripBeanSupport.getDurationLabel(end.getTime()
          - start.getTime());
      Widget tripTypeWidget = getTripTypeWidget(trip);

      ClickHandlerImpl handler = new ClickHandlerImpl(index);

      Anchor anchor = new Anchor(Integer.toString(index + 1) + ":");
      anchor.addClickHandler(handler);

      grid.setWidget(index, 0, anchor);
      grid.setText(index, 1, timeStartAndEndLabel);
      grid.setText(index, 2, durationLabel);
      grid.setWidget(index, 3, tripTypeWidget);

      if (index == selectedIndex) {
        displayTrip(trip, index);
        grid.getRowFormatter().addStyleName(index, "Selected");
      }

      _panel.add(_singleTripPresenter.getWidget());
    }
  }

  private Widget getTripTypeWidget(ItineraryBean trip) {

    FlowPanel panel = new FlowPanel();
    TripPlannerResources resources = TripPlannerResources.INSTANCE;

    for (LegBean segment : trip.getLegs()) {
      if( segment.getMode().equals("walk") ) {
        if (segment.getDistance() > 400) {
          DataResource walkIcon = resources.getWalkTripTypeIcon();
          Image image = new Image(walkIcon.getUrl());
          panel.add(image);
        }
      }
      else if( segment.getMode().equals("transit")) {
        DataResource busIcon = resources.getBusTripTypeIcon();
        Image image = new Image(busIcon.getUrl());
        panel.add(image);
      }
    }

    return panel;
  }

  private void displayTrip(ItineraryBean trip, int index) {

    _singleTripPresenter.displayTrip(trip, index + 1, "your destination");

    for (Overlay overlay : _overlays)
      _map.removeOverlay(overlay);
    _overlays.clear();
    _tripMapPresenter.displayTrip(trip, _overlays);
  }

  public void onFailure(Throwable ex) {
    System.err.println("error in directions handler");
    ex.printStackTrace();
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private class ClickHandlerImpl implements ClickHandler {

    private int _index;

    public ClickHandlerImpl(int index) {
      _index = index;
    }

    public void onClick(ClickEvent arg0) {
      if (_model != null)
        _model.setSelectedIndex(_index);
    }
  }
}
