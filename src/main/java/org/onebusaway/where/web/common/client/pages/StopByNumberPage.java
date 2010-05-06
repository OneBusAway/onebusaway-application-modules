/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.web.common.client.pages;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

import org.onebusaway.where.web.common.client.AbstractPageSource;
import org.onebusaway.where.web.common.client.Context;
import org.onebusaway.where.web.common.client.OneBusAwayMessages;
import org.onebusaway.where.web.common.client.PageException;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.widgets.DivPanel;
import org.onebusaway.where.web.common.client.widgets.DivWidget;
import org.onebusaway.where.web.common.client.widgets.SpanWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StopByNumberPage extends AbstractPageSource implements
    AsyncCallback<StopWithArrivalsBean> {

  private static final String ID_KEY = "id";

  /*****************************************************************************
   * Ordering
   ****************************************************************************/

  private static final String ORDER_BY_KEY = "o";

  private static final String ORDER_BY_TIME_VALUE = "time";

  private static final String ORDER_BY_DEST_VALUE = "dest";

  private static final String ORDER_BY_ROUTE_VALUE = "route";

  private static final String ROUTE_FILTER_KEY = "r";

  private static final OrderConstraint SORT_BY_TIME = new SortByTime();

  private static final OrderConstraint SORT_BY_DEST = new SortByDestination();

  private static final OrderConstraint SORT_BY_ROUTE = new SortByRoute();

  /*****************************************************************************
   * 
   ****************************************************************************/

  private static final int CACHE_DURATION = 1000 * 30;

  private static DateTimeFormat _updateFormat = DateTimeFormat.getShortTimeFormat();

  private String _stopId = null;

  private OrderConstraint _orderBy = SORT_BY_TIME;

  private FilterConstraint _filter = new DefaultFilter();

  private StopWithArrivalsBean _data = null;

  private long _lastUpdate = -1;

  /*****************************************************************************
   * Widgets
   ****************************************************************************/

  protected FlowPanel _panel;

  protected DivPanel _stopInfo;

  private FlexTable _table;

  private SpanWidget _update;

  private boolean _first = true;

  private FlowPanel _nearbyStops;

  private FlowPanel _filterPanel;

  public Widget create(final Context context) throws PageException {

    _stopId = getStopId(context);
    _orderBy = getSortOrder(context);
    _filter = getFilter(context);
    _first = true;

    /***************************************************************************
     * Construct the Panels
     **************************************************************************/

    _panel = new FlowPanel();
    _panel.addStyleName("panel");

    _stopInfo = new DivPanel();
    _stopInfo.addStyleName("arrivalsStopInfo");
    _panel.add(_stopInfo);

    _filterPanel = new FlowPanel();
    _filterPanel.addStyleName("arrivalsFilterPanel");
    _filterPanel.setVisible(false);
    _panel.add(_filterPanel);

    _table = new FlexTable();
    _table.addStyleName("arrivalsTable");
    _panel.add(_table);

    SpanWidget routeHeader = new SpanWidget(_msgs.stopByNumberPageRoute());
    routeHeader.addClickListener(new OrderClickHandler(ORDER_BY_ROUTE_VALUE));

    SpanWidget destHeader = new SpanWidget(_msgs.stopByNumberPageDestination());
    destHeader.addClickListener(new OrderClickHandler(ORDER_BY_DEST_VALUE));

    SpanWidget timeHeader = new SpanWidget(_msgs.stopByNumberPageMinutes());
    timeHeader.addClickListener(new OrderClickHandler(ORDER_BY_TIME_VALUE));

    _table.setWidget(0, 0, routeHeader);
    _table.setWidget(0, 1, destHeader);
    _table.setWidget(0, 2, timeHeader);

    CellFormatter formatter = _table.getCellFormatter();

    formatter.setStyleName(0, 0, "arrivalsRouteColumn");
    formatter.setStyleName(0, 1, "arrivalsDestinationColumn");
    formatter.setStyleName(0, 2, "arrivalsStatusColumn");

    RowFormatter rowFormatter = _table.getRowFormatter();
    rowFormatter.addStyleName(0, "arrivalsHeader");

    SpanWidget updatesHeader = new SpanWidget("<b>"
        + _msgs.stopByNumberPageLastUpdate() + "</b> ");
    _update = new SpanWidget(_msgs.stopByNumberPageWaitingForUpdate());

    Button refreshButton = new Button(_msgs.stopByNumberPageRefresh());
    refreshButton.addStyleName("arrivalsRefreshButton");

    FlowPanel update = getUpdatesPanel(updatesHeader, _update, refreshButton);

    _panel.add(update);

    _nearbyStops = new FlowPanel();
    _nearbyStops.addStyleName("arrivalsNearbyStops");
    _panel.add(_nearbyStops);

    handlePostNearbyStops();

    refreshButton.addClickListener(new ClickListener() {
      public void onClick(Widget arg0) {
        _service.getArrivalsByStopId(_stopId, StopByNumberPage.this);
      }
    });
    refreshButton.addStyleName("arrivalsRefreshButton");

    Timer t = new Timer() {
      @Override
      public void run() {
        if (_panel.isAttached()) {
          _service.getArrivalsByStopId(_stopId, StopByNumberPage.this);
        } else {
          // The page is no longer active... cancel the time
          cancel();
        }
      }
    };

    t.scheduleRepeating(60000);

    _service.getArrivalsByStopId(_stopId, this);

    return _panel;
  }

  @Override
  public Widget update(Context context) throws PageException {

    String stopId = getStopId(context);

    if (_stopId == null || !stopId.equals(_stopId))
      return create(context);

    _orderBy = getSortOrder(context);
    _filter = getFilter(context);

    if (_data == null
        || _lastUpdate + CACHE_DURATION < System.currentTimeMillis()) {
      _service.getArrivalsByStopId(_stopId, this);
    } else {
      this.onSuccess(_data);
    }

    return null;
  }

  /*****************************************************************************
   * {@link AsyncCallback} Interface
   ****************************************************************************/

  public void onSuccess(StopWithArrivalsBean bean) {

    if (_first) {

      _first = false;

      handleInitialStopInformation(bean, _msgs);
    }

    List<DepartureBean> arrivals = bean.getPredictedArrivals();

    CellFormatter formatter = _table.getCellFormatter();
    RowFormatter rowFormatter = _table.getRowFormatter();

    // Clear the table
    while (_table.getRowCount() > 1)
      _table.removeRow(_table.getRowCount() - 1);

    boolean filtered = false;
    List<DepartureBean> active = new ArrayList<DepartureBean>();

    for (DepartureBean pab : arrivals) {
      if (_filter.isEnabled(pab))
        active.add(pab);
      else
        filtered = true;
    }

    Collections.sort(active, _orderBy);

    int index = 1;
    long now = System.currentTimeMillis();

    for (DepartureBean pab : active) {

      rowFormatter.addStyleName(index, "arrivalsRow");

      /*************************************************************************
       * Route In Column #1
       ************************************************************************/

      String route = pab.getRoute();

      SpanWidget routeWidget = new SpanWidget(route);
      routeWidget.addClickListener(new RouteFilterClickHandler(pab.getRoute()));

      _table.setWidget(index, 0, routeWidget);
      formatter.addStyleName(index, 0, "arrivalsRouteEntry");

      FlowPanel destPanel = new FlowPanel();
      DivWidget dest = new DivWidget(pab.getDestination());
      dest.addStyleName("arrivalsDestinationEntry");
      destPanel.add(dest);

      String arrivalTime = _updateFormat.format(new Date(pab.getBestTime()));
      String arrivalLabel = getArrivalLabel(pab, now);
      DivPanel timePanel = new DivPanel();
      timePanel.addStyleName("arrivalsTimePanel");
      SpanWidget timeWidget = new SpanWidget(arrivalTime + " - ");
      timeWidget.addStyleName("arrivalsTimeEntry");
      timePanel.add(timeWidget);
      SpanWidget arrivalLabelWidget = new SpanWidget(arrivalLabel);
      timePanel.add(arrivalLabelWidget);
      destPanel.add(timePanel);

      _table.setWidget(index, 1, destPanel);

      long t = pab.getPredictedTime() > 0 ? pab.getPredictedTime()
          : pab.getScheduledTime();
      int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
      boolean isNow = Math.abs(minutes) <= 1;
      String status = isNow ? "NOW" : Integer.toString(minutes);
      String statusStyle = getArrivalStatusLabelStyle(pab, now);

      arrivalLabelWidget.addStyleName(statusStyle);
      arrivalLabelWidget.addStyleName("arrivalsLabelEntry");

      _table.setText(index, 2, status);
      formatter.addStyleName(index, 2, statusStyle);
      formatter.addStyleName(index, 2, "arrivalsStatusEntry");
      if (isNow)
        formatter.addStyleName(index, 2, "arrivalStatusNow");
      index++;
    }

    _update.setHTML(_updateFormat.format(new Date()));

    _filterPanel.setVisible(filtered);
    if (filtered) {
      while (_filterPanel.getWidgetCount() > 0)
        _filterPanel.remove(0);
      SpanWidget span = new SpanWidget(_msgs.stopByNumberPageShowAllArrivals());
      span.addClickListener(new NoFilterClickHandler());
      _filterPanel.add(span);
    }
  }

  public void onFailure(Throwable ex) {
    if (ex instanceof ServiceException) {
      handleException(ex);
    } else {
      ex.printStackTrace();
      _update.setText("Communications error");
    }
  }

  /*****************************************************************************
   * Protected
   ****************************************************************************/

  protected void handlePostNearbyStops() {

  }

  protected FlowPanel getUpdatesPanel(SpanWidget updatesHeader, SpanWidget u,
      Button refreshButton) {

    FlowPanel panel = new FlowPanel();
    panel.addStyleName("arrivalsStatusUpdates");

    FlowPanel statusRow = new FlowPanel();
    panel.add(statusRow);

    statusRow.add(updatesHeader);
    statusRow.add(u);

    panel.add(refreshButton);

    return panel;
  }

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private String getStopId(final Context context) throws PageException {
    if (!context.hasParam(ID_KEY))
      throw new PageException(_msgs.stopByNumberPageNoStopNumberSpecified());
    return context.getParam(ID_KEY);
  }

  private OrderConstraint getSortOrder(Context context) {

    if (!context.hasParam(ORDER_BY_KEY))
      return SORT_BY_TIME;

    String orderBy = context.getParam(ORDER_BY_KEY);

    if (orderBy.equals(ORDER_BY_ROUTE_VALUE))
      return SORT_BY_ROUTE;

    if (orderBy.equals(ORDER_BY_DEST_VALUE))
      return SORT_BY_DEST;

    if (orderBy.equals(ORDER_BY_TIME_VALUE))
      return SORT_BY_TIME;

    return SORT_BY_TIME;
  }

  private FilterConstraint getFilter(Context context) throws PageException {
    if (context.hasParam(ROUTE_FILTER_KEY)) {
      try {
        int route = Integer.parseInt(context.getParam(ROUTE_FILTER_KEY));
        return new RouteFilter(route);
      } catch (NumberFormatException ex) {
        throw new PageException(
            _msgs.stopByNumberPageInvalidRouteNumberSpecified());
      }
    }

    return new DefaultFilter();
  }

  protected void handleInitialStopInformation(StopWithArrivalsBean swab,
      OneBusAwayMessages constants) {
    StopBean stop = swab.getStop();

    String street = stop.getName();
    DivWidget stopAddress = new DivWidget(street);
    stopAddress.addStyleName("arrivalsStopAddress");
    _stopInfo.add(stopAddress);

    String stopDescription = constants.stopByNumberPageStopNumberShebang()
        + " " + stop.getId() + " - " + stop.getDirection() + " "
        + constants.stopByNumberPageBound();

    DivWidget stopNumber = new DivWidget(stopDescription);
    stopNumber.addStyleName("arrivalsStopNumber");
    _stopInfo.add(stopNumber);

    Window.setTitle(street + " - " + stop.getDirection() + " "
        + constants.stopByNumberPageBound() + " - "
        + constants.stopByNumberPageStopNumberShebang() + " " + stop.getId());

    if (!stop.getNearbyStops().isEmpty()) {
      _nearbyStops.add(new DivWidget(constants.stopByNumberPageNearbyStops()));
      for (StopBean s : stop.getNearbyStops()) {

        String target = getTarget("stop", ID_KEY, s.getId());
        String label = s.getName() + " - " + s.getDirection() + " "
            + constants.stopByNumberPageBound();
        DivWidget row = new DivWidget("<a href=\"#" + target + "\">" + label
            + "</a>");
        _nearbyStops.add(row);
      }
    }
  }

  private String getArrivalLabel(DepartureBean pab, long now) {

    long predicted = pab.getPredictedTime();
    long scheduled = pab.getScheduledTime();

    if (predicted > 0) {

      double diff = ((pab.getPredictedTime() - pab.getScheduledTime()) / (1000.0 * 60));
      int minutes = (int) Math.abs(Math.round(diff));

      boolean departed = predicted < now;

      if (diff < -1.5) {
        return departed ? _msgs.stopByNumberPageDepartedEarly(minutes)
            : _msgs.stopByNumberPageEarly(minutes);
      } else if (diff < 1.5) {
        return departed ? _msgs.stopByNumberPageDepartedOnTime()
            : _msgs.stopByNumberPageOnTime();
      } else {
        return departed ? _msgs.stopByNumberPageDepartedLate(minutes)
            : _msgs.stopByNumberPageDelayed(minutes);
      }

    } else {
      if (scheduled < now)
        return _msgs.stopByNumberPageScheduledDeparture();
      else
        return _msgs.stopByNumberPageScheduledArrival();
    }
  }

  private String getArrivalStatusLabelStyle(DepartureBean pab, long now) {

    long predicted = pab.getPredictedTime();
    long scheduled = pab.getScheduledTime();

    if (predicted > 0) {

      if (predicted < now)
        return "arrivalStatusDeparted";

      double diff = ((pab.getPredictedTime() - pab.getScheduledTime()) / (1000.0 * 60));

      if (diff < -1.0) {
        return "arrivalStatusEarly";
      } else if (diff < 1.5) {
        return "arrivalStatusDefault";
      } else {
        return "arrivalStatusDelayed";
      }

    } else {
      if (scheduled < now)
        return "arrivalStatusDeparted";
      else
        return "arrivalStatusNoInfo";
    }
  }

  /*****************************************************************************
   * Click Handlers
   ****************************************************************************/

  private class OrderClickHandler implements ClickListener {

    private String _orderByValue;

    public OrderClickHandler(String orderByValue) {
      _orderByValue = orderByValue;
    }

    public void onClick(Widget arg0) {
      Map<String, String> params = new LinkedHashMap<String, String>();
      params.put(ID_KEY, _stopId);
      params.put(ORDER_BY_KEY, _orderByValue);
      _filter.addTargetParams(params);
      newTargetWithMap("stop", params);
    }

  }

  private class RouteFilterClickHandler implements ClickListener {

    private String _route;

    public RouteFilterClickHandler(String route) {
      _route = route;
    }

    public void onClick(Widget arg0) {
      Map<String, String> params = new LinkedHashMap<String, String>();
      params.put(ID_KEY, _stopId);
      _orderBy.addTargetParams(params);
      params.put(ROUTE_FILTER_KEY, _route);
      newTargetWithMap("stop", params);
    }

  }

  private class NoFilterClickHandler implements ClickListener {

    public void onClick(Widget arg0) {
      Map<String, String> params = new LinkedHashMap<String, String>();
      params.put(ID_KEY, _stopId);
      _orderBy.addTargetParams(params);
      newTargetWithMap("stop", params);
    }
  }

  /*****************************************************************************
   * Sort Strategies
   ****************************************************************************/

  private interface OrderConstraint extends Comparator<DepartureBean> {
    public void addTargetParams(Map<String, String> params);
  }

  private static class SortByRoute implements OrderConstraint {
    public int compare(DepartureBean o1, DepartureBean o2) {
      if (o1.getRoute() == o2.getRoute())
        return SORT_BY_TIME.compare(o1, o2);
      String a = o1.getRoute();
      String b = o2.getRoute();
      return a.compareTo(b);
    }

    public void addTargetParams(Map<String, String> params) {
      params.put(ORDER_BY_KEY, ORDER_BY_ROUTE_VALUE);
    }
  }

  private static class SortByTime implements OrderConstraint {
    public int compare(DepartureBean o1, DepartureBean o2) {
      long a = o1.getBestTime();
      long b = o2.getBestTime();
      return a == b ? 0 : (a < b ? -1 : 1);
    }

    public void addTargetParams(Map<String, String> params) {
      params.put(ORDER_BY_KEY, ORDER_BY_TIME_VALUE);
    }
  }

  private static class SortByDestination implements OrderConstraint {
    public int compare(DepartureBean o1, DepartureBean o2) {
      int i = o1.getDestination().compareTo(o2.getDestination());
      if (i == 0)
        return SORT_BY_TIME.compare(o1, o2);
      return i;
    }

    public void addTargetParams(Map<String, String> params) {
      params.put(ORDER_BY_KEY, ORDER_BY_DEST_VALUE);
    }
  }

  /*****************************************************************************
   * Filters
   ****************************************************************************/

  private static interface FilterConstraint {
    public boolean isEnabled(DepartureBean bean);

    public void addTargetParams(Map<String, String> params);
  }

  private static class DefaultFilter implements FilterConstraint {

    public boolean isEnabled(DepartureBean bean) {
      return true;
    }

    public void addTargetParams(Map<String, String> params) {

    }
  }

  private static class RouteFilter implements FilterConstraint {

    private int _route;

    public RouteFilter(int route) {
      _route = route;
    }

    public boolean isEnabled(DepartureBean bean) {
      return bean.getRoute().equals(_route);
    }

    public void addTargetParams(Map<String, String> params) {
      params.put(ROUTE_FILTER_KEY, Integer.toString(_route));
    }

  }
}