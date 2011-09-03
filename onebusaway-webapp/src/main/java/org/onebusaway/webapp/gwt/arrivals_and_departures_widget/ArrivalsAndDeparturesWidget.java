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
package org.onebusaway.webapp.gwt.arrivals_and_departures_widget;

import java.util.Date;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.where_library.pages.WhereCommonPage;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesPresentaion;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class ArrivalsAndDeparturesWidget extends WhereCommonPage {

  private static DateTimeFormat _timeFormat = DateTimeFormat.getFormat("hh:mm");

  private DivPanel _stopPanel = new DivPanel("arrivalsStopInfo");

  private FlexTable _arrivalsAndDeparturesTable = new FlexTable();

  private ArrivalsAndDeparturesPresentaion _methods;

  private DivPanel _widget = new DivPanel();

  public ArrivalsAndDeparturesWidget(ContextManager contextManager) {
    _methods = new ArrivalsAndDeparturesPresentaion(true);
    setup();
  }

  public Widget getWidget() {
    return _widget;
  }

  public void refresh(StopWithArrivalsAndDeparturesBean bean) {
    updateStopPanel(bean.getStop());
    clearArrivalAndDepartureTable();
    for (ArrivalAndDepartureBean dep : bean.getArrivalsAndDepartures()) {
      addArrivalsAndDeparture(dep);
    }
  }

  /****
   * Private Methods
   ****/

  private void setup() {

    prepArrivalsAndDeparturesTable();

    _widget.addStyleName("panel");
    _widget.add(_stopPanel);
    _widget.add(_arrivalsAndDeparturesTable);
  }

  private void prepArrivalsAndDeparturesTable() {

    _arrivalsAndDeparturesTable.addStyleName("arrivalsTable");

    _arrivalsAndDeparturesTable.getRowFormatter().addStyleName(0,
        "arrivalsHeader");
    _arrivalsAndDeparturesTable.getRowFormatter().addStyleName(1, "arrivalsRow");

    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(0, 0,
        "arrivalsRouteColumn");
    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(0, 1,
        "arrivalsDestinationColumn");
    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(0, 2,
        "arrivalsStatusColumn");

    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(1, 0,
        "arrivalsRouteEntry");
    _arrivalsAndDeparturesTable.getCellFormatter().setStyleName(1, 2,
        "arrivalsStatusEntry");

    _arrivalsAndDeparturesTable.setText(0, 0, "route");
    _arrivalsAndDeparturesTable.setText(0, 1, "destination");
    _arrivalsAndDeparturesTable.setText(0, 2, "minutes");

    _arrivalsAndDeparturesTable.setText(1, 1, "loading arrival data...");
  }

  private void updateStopPanel(StopBean stop) {
    _stopPanel.clear();
    _stopPanel.add(new DivWidget(stop.getName(), "arrivalsStopAddress"));
    _stopPanel.add(new DivWidget("Stop # " + stop.getId() + " - "
        + stop.getDirection() + " bound"));
  }

  private void clearArrivalAndDepartureTable() {
    while (_arrivalsAndDeparturesTable.getRowCount() > 1)
      _arrivalsAndDeparturesTable.removeRow(_arrivalsAndDeparturesTable.getRowCount() - 1);
  }

  private void addArrivalsAndDeparture(ArrivalAndDepartureBean bean) {

    int rowIndex = _arrivalsAndDeparturesTable.getRowCount();

    TripBean trip = bean.getTrip();
    RouteBean route = trip.getRoute();
    String routeName = RoutePresenter.getNameForRoute(route);
    
    _arrivalsAndDeparturesTable.setText(rowIndex, 0, routeName);

    DivPanel divPanel = new DivPanel();

    DivPanel destinationPanel = new DivPanel("arrivalsDestinationEntry");
    divPanel.add(destinationPanel);
    String href = "trip.action?id=" + trip.getId() + "&stop="
        + bean.getStop().getId();
    destinationPanel.add(new Anchor(trip.getTripHeadsign(), href));

    DivPanel timeAndStatusPanel = new DivPanel("arrivalsTimePanel");
    divPanel.add(timeAndStatusPanel);
    String time = _timeFormat.format(new Date(bean.computeBestDepartureTime()));
    timeAndStatusPanel.add(new SpanWidget(time, "arrivalsTimeEntry"));
    timeAndStatusPanel.add(new SpanWidget(" - "));
    String arrivalStatusLabelStyle = _methods.getStatusLabelStyle(bean);
    timeAndStatusPanel.add(new SpanWidget(_methods.getStatusLabel(bean),
        arrivalStatusLabelStyle));
    _arrivalsAndDeparturesTable.setWidget(rowIndex, 1, divPanel);

    _arrivalsAndDeparturesTable.getCellFormatter().setStyleName(rowIndex, 2,
        "arrivalsStatusEntry");
    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(rowIndex, 2,
        arrivalStatusLabelStyle);
    if (_methods.isNow(bean))
      _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(rowIndex, 2,
          "arrivalStatusNow");

    _arrivalsAndDeparturesTable.setText(1, 2, _methods.getMinutesLabel(bean));
  }
}
