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
package org.onebusaway.presentation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.utility.text.NaturalStringOrder;
import org.onebusaway.utility.text.StringLibrary;
import org.springframework.beans.factory.annotation.Autowired;

public class ArrivalsAndDeparturesModel {

  private static final OrderConstraint SORT_BY_TIME = new SortByTime();

  private static final OrderConstraint SORT_BY_DEST = new SortByDestination();

  private static final OrderConstraint SORT_BY_ROUTE = new SortByRoute();

  private TransitDataService _transitDataService;

  private CurrentUserService _currentUserService;

  private DefaultSearchLocationService _defaultSearchLocationService;

  private List<String> _stopIds;

  private Set<String> _routeFilter = new HashSet<String>();

  private OrderConstraint _order = SORT_BY_TIME;

  private TimeZone _timeZone;

  private ArrivalsAndDeparturesQueryBean _query = new ArrivalsAndDeparturesQueryBean();

  protected StopsWithArrivalsAndDeparturesBean _result;

  private List<AgencyBean> _agencies;

  /**
   * True if we filtered the results (ex. only include particular set of routes)
   */
  private boolean _filtered = false;

  protected UserBean _user;

  private boolean _onlyNext = false;

  private boolean _showArrivals = false;

  private int _refresh = 60;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  @Autowired
  public void setDefaultSearchLocationService(
      DefaultSearchLocationService defaultSearchLocationService) {
    _defaultSearchLocationService = defaultSearchLocationService;
  }

  public void setStopIds(List<String> stopIds) {
    _stopIds = stopIds;
  }

  public List<String> getStopIds() {
    return _stopIds;
  }

  public void setRouteFilter(Set<String> routeFilter) {
    _routeFilter = routeFilter;
  }

  public Set<String> getRouteFilter() {
    return _routeFilter;
  }

  public boolean setOrderFromString(String order) {
    if ("route".equals(order))
      _order = SORT_BY_ROUTE;
    else if ("dest".equals(order))
      _order = SORT_BY_DEST;
    else if ("time".equals(order))
      _order = SORT_BY_TIME;
    else
      return false;
    return true;
  }

  public void setTargetTime(Date time) {
    _query.setTime(time.getTime());
  }

  public void setMinutesBefore(int minutesBefore) {
    _query.setMinutesBefore(minutesBefore);
  }

  public void setMinutesAfter(int minutesAfter) {
    _query.setMinutesAfter(minutesAfter);
  }

  public void setFrequencyMinutesBefore(int frequencyMinutesBefore) {
    _query.setFrequencyMinutesBefore(frequencyMinutesBefore);
  }

  public void setFrequencyMinutesAfter(int frequencyMinutesAfter) {
    _query.setFrequencyMinutesAfter(frequencyMinutesAfter);
  }

  public void setOnlyNext(boolean onlyNext) {
    _onlyNext = onlyNext;
  }

  public void setShowArrivals(boolean showArrivals) {
    _showArrivals = showArrivals;
  }

  public void setRefresh(int refresh) {
    _refresh = refresh;
  }

  public int getRefresh() {
    return _refresh;
  }

  public boolean isMissingData() {
    return _stopIds == null || _stopIds.isEmpty();
  }

  public void process() {

    _result = _transitDataService.getStopsWithArrivalsAndDepartures(_stopIds,
        _query);

    checkForEmptyResult();
    filterResults();
    orderResults();

    _agencies = AgencyPresenter.getAgenciesForArrivalAndDepartures(_result.getArrivalsAndDepartures());

    _timeZone = TimeZone.getTimeZone(_result.getTimeZone());
    if (_timeZone == null)
      _timeZone = TimeZone.getDefault();

    updateCurrentUser();
  }

  public TimeZone getTimeZone() {
    return _timeZone;
  }

  public StopsWithArrivalsAndDeparturesBean getResult() {
    return _result;
  }

  public void setResult(StopsWithArrivalsAndDeparturesBean result) {
    _result = result;
  }

  public boolean isFiltered() {
    return _filtered;
  }

  public List<AgencyBean> getAgencies() {
    return _agencies;
  }

  /****
   * Private Methods
   ****/

  private void checkForEmptyResult() {
    if (_result == null) {
      if (_stopIds.size() == 1)
        throw new NoSuchStopServiceException(_stopIds.get(0));
      else
        throw new NoSuchStopServiceException(_stopIds.toString());
    }
  }

  private void filterResults() {

    applyRouteFilter();
    applyArrivalsVsDeparturesFilter();
    applyOnlyNextFilter();
  }

  // remove any duplicates that may have occured
  public void applyDedupe() {

    if (_result.getArrivalsAndDepartures() == null || _result.getArrivalsAndDepartures().size() < 2) {
      return;
    }

    Set<String> found = new HashSet<String>();
    
    List<ArrivalAndDepartureBean> arrivalsAndDepartures = new ArrayList<ArrivalAndDepartureBean>();
    
    
    for (ArrivalAndDepartureBean ad : _result.getArrivalsAndDepartures()) {
      if (found.contains(ad.toString())) {
        // quietly drop 
      } else {
        found.add(ad.toString());
        arrivalsAndDepartures.add(ad);
      }
    }
    _result.setArrivalsAndDepartures(arrivalsAndDepartures);
  }

  private void applyRouteFilter() {
    if (_routeFilter == null || _routeFilter.isEmpty())
      return;

    List<ArrivalAndDepartureBean> filtered = new ArrayList<ArrivalAndDepartureBean>();
    for (ArrivalAndDepartureBean bean : _result.getArrivalsAndDepartures()) {
      if (_routeFilter.contains(bean.getTrip().getRoute().getId()))
        filtered.add(bean);
      else
        _filtered = true;
    }

    _result.setArrivalsAndDepartures(filtered);
  }

  private void applyArrivalsVsDeparturesFilter() {

    List<ArrivalAndDepartureBean> filtered = new ArrayList<ArrivalAndDepartureBean>();

    for (ArrivalAndDepartureBean bean : _result.getArrivalsAndDepartures()) {
      if ((_showArrivals && bean.isArrivalEnabled())
          || (!_showArrivals && bean.isDepartureEnabled()))
        filtered.add(bean);
    }

    _result.setArrivalsAndDepartures(filtered);
  }

  private void applyOnlyNextFilter() {

    if (!_onlyNext)
      return;

    List<ArrivalAndDepartureBean> current = _result.getArrivalsAndDepartures();
    Collections.sort(current, SORT_BY_TIME);

    Map<String, ArrivalAndDepartureBean> keepers = new HashMap<String, ArrivalAndDepartureBean>();
    for (ArrivalAndDepartureBean bean : current) {
      String key = getRouteKeyForArrivalAndDeparture(bean);
      if (!keepers.containsKey(key))
        keepers.put(key, bean);
    }

    List<ArrivalAndDepartureBean> filtered = new ArrayList<ArrivalAndDepartureBean>(
        keepers.values());
    OrderConstraint c = _order == null ? SORT_BY_TIME : _order;
    Collections.sort(filtered, c);

    _result.setArrivalsAndDepartures(filtered);
  }

  private String getRouteKeyForArrivalAndDeparture(ArrivalAndDepartureBean bean) {
    String name = bean.getRouteShortName();
    if (name != null)
      return name;
    TripBean trip = bean.getTrip();
    name = trip.getRouteShortName();
    if (name != null)
      return name;
    RouteBean route = trip.getRoute();
    name = route.getShortName();
    if (name != null)
      return name;
    return route.getId();
  }

  private void orderResults() {
    if (_order != null)
      Collections.sort(_result.getArrivalsAndDepartures(), _order);
  }

  private void updateCurrentUser() {
    // Save the last selected stop id
    _currentUserService.setLastSelectedStopIds(_stopIds);

    _user = _currentUserService.getCurrentUser();
    if (_user == null || !_user.hasDefaultLocation()) {
      List<StopBean> stops = _result.getStops();
      StopBean stop = stops.get(0);
      _defaultSearchLocationService.setDefaultLocationForCurrentUser(
          stop.getName(), stop.getLat(), stop.getLon());
    }
  }

  /****
   *
   ****/

  private interface OrderConstraint extends Comparator<ArrivalAndDepartureBean> {
    // public void addTargetParams(Map<String, String> params);
  }

  private static class SortByRoute implements OrderConstraint {
    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      String a = RoutePresenter.getNameForRoute(o1.getTrip().getRoute());
      String b = RoutePresenter.getNameForRoute(o2.getTrip().getRoute());
      if (a.equals(b))
        return SORT_BY_TIME.compare(o1, o2);
      return NaturalStringOrder.compareNaturalIgnoreCaseAscii(a, b);
    }
  }

  private static class SortByTime implements OrderConstraint {
    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      long a = o1.computeBestDepartureTime();
      long b = o2.computeBestDepartureTime();
      return a == b ? 0 : (a < b ? -1 : 1);
    }
  }

  private static class SortByDestination implements OrderConstraint {
    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      String a = StringLibrary.getBestName(o1.getTripHeadsign(),
          o1.getTrip().getTripHeadsign(), "");
      String b = StringLibrary.getBestName(o2.getTripHeadsign(),
          o2.getTrip().getTripHeadsign(), "");
      int i = a.compareTo(b);
      if (i == 0)
        return SORT_BY_TIME.compare(o1, o2);
      return i;
    }
  }
}
