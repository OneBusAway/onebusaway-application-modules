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
package org.onebusaway.webapp.gwt.mobile_application.control;

import java.util.Date;
import java.util.List;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.mobile_application.model.Bookmark;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationCssResource;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationResources;
import org.onebusaway.webapp.gwt.mobile_application.view.BookmarkViewController;
import org.onebusaway.webapp.gwt.viewkit.ListViewController;
import org.onebusaway.webapp.gwt.viewkit.ListViewModel;
import org.onebusaway.webapp.gwt.viewkit.ListViewRow;
import org.onebusaway.webapp.gwt.viewkit.NavigationController;
import org.onebusaway.webapp.gwt.viewkit.ListViewRow.ListViewRowStyle;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesPresentaion;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

public class StopWithArrivalsAndDeparturesListViewModel extends ListViewModel {

  private static final MobileApplicationCssResource _css = MobileApplicationResources.INSTANCE.getCSS();

  private static final ArrivalsAndDeparturesPresentaion _methods = new ArrivalsAndDeparturesPresentaion(
      true);

  private static final DateTimeFormat _timeFormat = DateTimeFormat.getShortTimeFormat();

  private StopWithArrivalsAndDeparturesBean _data = null;

  public StopWithArrivalsAndDeparturesListViewModel() {
    setWillRespondToRowClicks(true);
  }

  public void setData(StopWithArrivalsAndDeparturesBean data) {
    _data = data;
  }

  /****
   * {@link ListViewModel} Interface
   ****/

  @Override
  public int getNumberOfSections() {
    if (_data == null)
      return 0;
    return 3;
  }

  @Override
  public int getNumberOfRowsInSection(int sectionIndex) {
    switch (sectionIndex) {
      case 0:
        return 1;
      case 1:
        return _data.getArrivalsAndDepartures().size();
      case 2:
        return 1;
      default:
        return 0;
    }
  }

  @Override
  public ListViewRow getListViewRowForSectionAndRow(int sectionIndex,
      int rowIndex) {

    ListViewRow row = new ListViewRow();

    switch (sectionIndex) {
      case 0: {

        StopBean stop = _data.getStop();
        row.setStyle(ListViewRowStyle.DEFAULT);
        row.setText(stop.getName());

        if (stop.getDirection() != null || stop.getCode() != null) {
          row.setStyle(ListViewRowStyle.DETAIL);
          StringBuilder b = new StringBuilder();
          if (stop.getDirection() != null)
            b.append(stop.getDirection()).append(" bound - ");
          if (stop.getCode() != null)
            b.append("Stop # ").append(stop.getCode());
          row.setDetailText(b.toString());
        }
        break;
      }
      case 1: {

        List<ArrivalAndDepartureBean> arrivalsAndDepartures = _data.getArrivalsAndDepartures();
        ArrivalAndDepartureBean arrival = arrivalsAndDepartures.get(rowIndex);

        Widget view = getCustomViewForArrivalAndDeparture(arrival);
        row.setCustomView(view);

        break;
      }
      case 2: {
        row.setStyle(ListViewRowStyle.DEFAULT);
        row.setText("Add bookmark");
        break;
      }
    }

    return row;
  }

  @Override
  public void onRowClick(ListViewController listViewController,
      int sectionIndex, int rowIndex) {

    if (sectionIndex == 2) {
      Bookmark bookmark = new Bookmark();
      bookmark.setStopId(_data.getStop().getId());
      bookmark.setName(_data.getStop().getName());
      BookmarkViewController vc = new BookmarkViewController(bookmark, true);

      ListViewController lvc = getListViewController();
      NavigationController nav = lvc.getNavigationController();
      nav.pushViewController(vc);
    }
  }

  private Widget getCustomViewForArrivalAndDeparture(
      ArrivalAndDepartureBean bean) {

    if (bean == null)
      System.err.println("null bean!");

    long currentTime = System.currentTimeMillis();
    long bestTime = getBestTime(bean);

    String arrivalTime = _timeFormat.format(new Date(bestTime));
    String arrivalLabel = _methods.getStatusLabel(bean);
    String minuteLabel = _methods.getMinutesLabel(bean);
    String arrivalLabelStyle = _methods.getStatusLabelStyle(bean);

    Grid row = new Grid(1, 3);
    row.addStyleName(_css.ArrivalEntry());

    TripBean trip = bean.getTrip();
    RouteBean route = trip.getRoute();

    String routeName = RoutePresenter.getNameForRoute(route);
    row.setWidget(0, 0, new SpanWidget(routeName, _css.ArrivalEntryRouteName()));

    FlowPanel center = new FlowPanel();
    row.setWidget(0, 1, center);

    center.add(new DivWidget(trip.getTripHeadsign(),
        _css.ArrivalEntryTripName()));

    FlowPanel arrivalTimePanel = new FlowPanel();
    arrivalTimePanel.addStyleName(_css.ArrivalEntryArrivalTimeDetail());
    center.add(arrivalTimePanel);

    arrivalTimePanel.add(new SpanWidget(arrivalTime));
    arrivalTimePanel.add(new SpanWidget(" - "));
    arrivalTimePanel.add(new SpanWidget(arrivalLabel, arrivalLabelStyle));

    row.setWidget(0, 2, new SpanWidget(minuteLabel, arrivalLabelStyle,
        _css.ArrivalEntryMinutes()));

    row.getCellFormatter().addStyleName(0, 0, _css.ArrivalEntryColumnA());
    row.getCellFormatter().addStyleName(0, 1, _css.ArrivalEntryColumnB());
    row.getCellFormatter().addStyleName(0, 2, _css.ArrivalEntryColumnC());

    return row;
  }

  private long getBestTime(ArrivalAndDepartureBean bean) {
    long time = bean.getScheduledArrivalTime();
    if (bean.hasPredictedArrivalTime())
      time = bean.getPredictedArrivalTime();
    return time;
  }

}
