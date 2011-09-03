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
package org.onebusaway.webapp.gwt.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.webapp.gwt.common.PageException;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.where_library.WhereLibrary;
import org.onebusaway.webapp.gwt.where_library.pages.WhereCommonPage;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryStandardStopCssResource;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesPresentaion;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class NotificationWidgetPage extends WhereCommonPage {

  private static DateTimeFormat _timeFormat = DateTimeFormat.getFormat("hh:mm");

  private static NotificationCssResource _notificationCss = NotificationResources.INSTANCE.getCss();

  private static WhereLibraryStandardStopCssResource _stopCss = WhereLibrary.INSTANCE.getStandardStopCss();

  private static final int DEFAULT_NOTIFY_MINUTES_BEFORE = 5;

  private static final String PARAM_TRIP_ID = "tripId";

  private static final String PARAM_STOP_ID = "stopId";

  private NotificationStateDAO _dao = new NotificationStateDAO();

  private DataRequestTimer _dataRequestTimer = new DataRequestTimer();

  private ArrivalsAndDeparturesHandler _arrivalsAndDeparturesHandler = new ArrivalsAndDeparturesHandler();

  private NotificationTimer _notificationTimer = new NotificationTimer();

  private String _tripId;

  private String _stopId;

  private DivPanel _stopPanel = new DivPanel(_stopCss.arrivalsStopInfo());

  private Grid _arrivalsAndDeparturesTable = new Grid(2, 3);

  private ArrivalsAndDeparturesPresentaion _methods;

  private List<NotificationMethod> _notificationMethods = new ArrayList<NotificationMethod>();

  private TextBox _minutesBeforeTextBox;

  private int _minutesBefore = DEFAULT_NOTIFY_MINUTES_BEFORE;

  private ArrivalAndDepartureBean _departureBean = null;

  public NotificationWidgetPage(ContextManager contextManager) {
    _methods = new ArrivalsAndDeparturesPresentaion(true);
    _notificationMethods.add(new SoundNotificationMethod());
    _notificationMethods.add(new PopupNotificationMethod());
  }

  public Widget create(final Context context) throws PageException {

    prepArrivalsAndDeparturesTable();

    DivPanel panel = new DivPanel();
    panel.addStyleName("panel");
    panel.add(_stopPanel);
    panel.add(_arrivalsAndDeparturesTable);

    DivPanel notificationOptionsPanel = new DivPanel(
        _notificationCss.notificationOptions());
    panel.add(notificationOptionsPanel);

    DivPanel minutesRow = getNotificationMinutesBeforePanel();
    notificationOptionsPanel.add(minutesRow);

    DivPanel methodsPanel = getMethodsPanel();
    notificationOptionsPanel.add(methodsPanel);

    // DivPanel saveDefaultsPanel = getSaveDefaultsPanel();
    // notificationOptionsPanel.add(saveDefaultsPanel);

    update(context);

    return panel;
  }

  private DivPanel getNotificationMinutesBeforePanel() {

    DivPanel minutesRow = new DivPanel(
        _notificationCss.notificationMinutesBeforePanel());

    minutesRow.add(new SpanWidget("Notify me"));

    _minutesBeforeTextBox = new TextBox();
    _minutesBeforeTextBox.addStyleName(_notificationCss.notificationMinutesBeforeTextBox());
    _minutesBeforeTextBox.setText(Integer.toString(DEFAULT_NOTIFY_MINUTES_BEFORE));
    MinutesBeforeHandler handler = new MinutesBeforeHandler();
    _minutesBeforeTextBox.addKeyUpHandler(handler);

    minutesRow.add(_minutesBeforeTextBox);

    minutesRow.add(new SpanWidget("minutes before arrival"));
    return minutesRow;
  }

  private DivPanel getMethodsPanel() {
    DivPanel methodsPanel = new DivPanel(
        _notificationCss.notificationMethodsPanel());

    for (NotificationMethod method : _notificationMethods) {
      DivPanel row = new DivPanel(_notificationCss.notificationMethodPanel());
      if (method.getSelectionRow(row))
        methodsPanel.add(row);
    }

    return methodsPanel;
  }

  private DivPanel getSaveDefaultsPanel() {
    DivPanel panel = new DivPanel(_notificationCss.notificationDefaultsPanel());
    panel.add(new SpanWidget("Make this the default for:",
        _notificationCss.notificationDefaultsText()));

    Button justThisStop = new Button("This Stop");
    justThisStop.addStyleName(_notificationCss.notificationDefaultsButton());
    justThisStop.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent arg0) {
        NotificationState state = getState();
        _dao.setState(_stopId, state, false);
      }
    });
    panel.add(justThisStop);

    Button allStops = new Button("All Stops");
    allStops.addStyleName(_notificationCss.notificationDefaultsButton());
    allStops.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent arg0) {
        NotificationState state = getState();
        _dao.setState(_stopId, state, true);
      }
    });
    panel.add(allStops);

    return panel;
  }

  @Override
  public Widget update(Context context) throws PageException {

    _tripId = context.getParam(PARAM_TRIP_ID);
    _stopId = context.getParam(PARAM_STOP_ID);

    System.out.println("tripId=" + _tripId + " stopId=" + _stopId);

    if (_tripId == null || _stopId == null) {
      _dataRequestTimer.cancel();
    } else {

      NotificationState state = _dao.getState(_stopId);
      _minutesBeforeTextBox.setText(Integer.toString(state.getMinutesBefore()));
      _minutesBefore = state.getMinutesBefore();

      Map<String, NotificationMethodState> methodStates = new HashMap<String, NotificationMethodState>();
      for (NotificationMethodState methodState : state.getMethodStates())
        methodStates.put(methodState.getId(), methodState);

      for (NotificationMethod method : _notificationMethods) {
        NotificationMethodState methodState = methodStates.get(method.getId());
        if (methodState != null)
          method.loadFromState(methodState);
      }

      _dataRequestTimer.run();
      _dataRequestTimer.scheduleRepeating(60 * 1000);
      refreshAlarm();
    }

    return null;
  }

  private void prepArrivalsAndDeparturesTable() {
    _arrivalsAndDeparturesTable.addStyleName(_stopCss.arrivalsTable());

    _arrivalsAndDeparturesTable.getRowFormatter().addStyleName(0,
        _stopCss.arrivalsHeader());
    _arrivalsAndDeparturesTable.getRowFormatter().addStyleName(1,
        _stopCss.arrivalsRow());

    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(0, 0,
        _stopCss.arrivalsRouteColumn());
    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(0, 1,
        _stopCss.arrivalsDestinationColumn());
    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(0, 2,
        _stopCss.arrivalsStatusColumn());

    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(1, 0,
        _stopCss.arrivalsRouteEntry());
    _arrivalsAndDeparturesTable.getCellFormatter().setStyleName(1, 2,
        _stopCss.arrivalsStatusEntry());

    _arrivalsAndDeparturesTable.setText(0, 0, "route");
    _arrivalsAndDeparturesTable.setText(0, 1, "destination");
    _arrivalsAndDeparturesTable.setText(0, 2, "minutes");

    _arrivalsAndDeparturesTable.setText(1, 1, "loading arrival data...");
  }

  private void refreshInterface(StopWithArrivalsAndDeparturesBean bean) {
    updateStopPanel(bean.getStop());
    for (ArrivalAndDepartureBean dep : bean.getArrivalsAndDepartures()) {
      TripBean trip = dep.getTrip();
      if (trip.getId().equals(_tripId)) {
        updateArrivalsAndDeparturePanel(dep);
        _departureBean = dep;
      }
    }
  }

  private void updateStopPanel(StopBean stop) {
    _stopPanel.clear();

    String url = "index.html#p(index)m(location)lat(" + stop.getLat() + ")lon("
        + stop.getLon() + ")accuracy(8)stop(" + stop.getId() + ")";

    DivPanel namePanel = new DivPanel(_stopCss.arrivalsStopAddress());
    _stopPanel.add(namePanel);

    Anchor nameLink = new Anchor(stop.getName(), url);
    namePanel.add(nameLink);

    DivPanel numberPanel = new DivPanel(_stopCss.arrivalsStopNumber());
    _stopPanel.add(numberPanel);
    Anchor numberLink = new Anchor("Stop # " + stop.getCode() + " - "
        + stop.getDirection() + " bound", url);
    numberPanel.add(numberLink);
  }

  private void updateArrivalsAndDeparturePanel(ArrivalAndDepartureBean bean) {

    long now = System.currentTimeMillis();

    TripBean trip = bean.getTrip();
    RouteBean route = trip.getRoute();
    String routeName = RoutePresenter.getNameForRoute(route);
    _arrivalsAndDeparturesTable.setText(1, 0, routeName);

    if (RoutePresenter.isRouteNameLong(routeName))
      _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(1, 0,
          _stopCss.arrivalsRouteLongNameEntry());

    DivPanel divPanel = new DivPanel();

    DivPanel destinationPanel = new DivPanel(
        _stopCss.arrivalsDestinationEntry());
    divPanel.add(destinationPanel);
    String href = "trip.action?id=" + trip.getId() + "&stop="
        + bean.getStop().getId();
    destinationPanel.add(new Anchor(trip.getTripHeadsign(), href));

    DivPanel timeAndStatusPanel = new DivPanel(_stopCss.arrivalsTimePanel());
    divPanel.add(timeAndStatusPanel);
    String time = _timeFormat.format(new Date(bean.computeBestDepartureTime()));
    timeAndStatusPanel.add(new SpanWidget(time, _stopCss.arrivalsTimeEntry()));
    timeAndStatusPanel.add(new SpanWidget(" - "));
    String arrivalStatusLabelStyle = _methods.getStatusLabelStyle(bean);
    timeAndStatusPanel.add(new SpanWidget(_methods.getStatusLabel(bean),
        arrivalStatusLabelStyle));
    _arrivalsAndDeparturesTable.setWidget(1, 1, divPanel);

    _arrivalsAndDeparturesTable.getCellFormatter().setStyleName(1, 2,
        _stopCss.arrivalsStatusEntry());
    _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(1, 2,
        arrivalStatusLabelStyle);
    if (_methods.isNow(bean))
      _arrivalsAndDeparturesTable.getCellFormatter().addStyleName(1, 2,
          _stopCss.arrivalStatusNow());

    _arrivalsAndDeparturesTable.setText(1, 2, _methods.getMinutesLabel(bean));
  }

  private void refreshAlarm() {

    System.out.println("reseting alarm");
    _notificationTimer.cancel();
    _minutesBefore = getMinutesBefore();

    if (_minutesBefore < 0) {
      System.out.println("invalid minutes before: " + _minutesBefore);
      _minutesBeforeTextBox.addStyleName("invalidValue");
    } else {
      System.out.println("good minutes before: " + _minutesBefore);
      _minutesBeforeTextBox.removeStyleName("invalidValue");
      _notificationTimer.schedule(5 * 1000);
    }
  }

  private int getMinutesBefore() {
    String minutesBeforeString = _minutesBeforeTextBox.getText();
    System.out.println("minutes before raw=" + minutesBeforeString);
    try {
      return Integer.parseInt(minutesBeforeString);
    } catch (NumberFormatException ex) {
      return -1;
    }
  }

  private void checkAlarm() {
    System.out.println("checking alarm");
    if (_departureBean != null) {
      System.out.println("we have a bean");
      if (_departureBean.computeBestDepartureTime() - _minutesBefore * 60
          * 1000 < System.currentTimeMillis()) {
        System.out.println("we have an alarm!");
        _notificationTimer.cancel();
        NotificationContextImpl context = new NotificationContextImpl();
        for (NotificationMethod method : _notificationMethods)
          method.handleNotification(context);
        return;
      }
    }
    _notificationTimer.schedule(30 * 1000);
  }

  private void resetNotifications() {
    System.out.println("notification reset");
    for (NotificationMethod method : _notificationMethods)
      method.handleNotificationReset();
  }

  private NotificationState getState() {
    NotificationState state = new NotificationState();
    state.setMinutesBefore(_minutesBefore);
    for (NotificationMethod method : _notificationMethods) {
      NotificationMethodState methodState = new NotificationMethodState();
      methodState.setId(method.getId());
      methodState.setEnabled(method.isEnabled());
      state.getMethodStates().add(methodState);
    }
    return state;
  }

  /****
   * Internal Classes
   ****/

  private class DataRequestTimer extends Timer {
    @Override
    public void run() {
      WebappServiceAsync service = WebappServiceAsync.SERVICE;
      service.getArrivalsByStopId(_stopId, _arrivalsAndDeparturesHandler);
    }
  }

  private class ArrivalsAndDeparturesHandler implements
      AsyncCallback<StopWithArrivalsAndDeparturesBean> {

    public void onSuccess(StopWithArrivalsAndDeparturesBean bean) {
      refreshInterface(bean);
    }

    public void onFailure(Throwable arg0) {

    }
  }

  private class MinutesBeforeHandler implements KeyUpHandler {
    public void onKeyUp(KeyUpEvent arg0) {
      refreshAlarm();
    }
  }

  private class NotificationTimer extends Timer {
    @Override
    public void run() {
      checkAlarm();
    }
  }

  private class NotificationContextImpl implements NotificationContext {
    public void reset() {
      resetNotifications();
    }
  }
}
