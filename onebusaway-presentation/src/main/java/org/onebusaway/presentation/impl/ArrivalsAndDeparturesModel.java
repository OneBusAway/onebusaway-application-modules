package org.onebusaway.presentation.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.utility.text.NaturalStringOrder;
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

  private Date _time = new Date();

  private int _minutesBefore = 5;

  private int _minutesAfter = 35;

  private StopsWithArrivalsAndDeparturesBean _result;

  private List<AgencyBean> _agencies;

  /**
   * True if we filtered the results (ex. only include particular set of routes)
   */
  private boolean _filtered = false;

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
    _time = time;
  }

  public void setMinutesBefore(int minutesBefore) {
    _minutesBefore = minutesBefore;
  }

  public void setMinutesAfter(int minutesAfter) {
    _minutesAfter = minutesAfter;
  }

  public boolean isMissingData() {
    return _stopIds == null || _stopIds.isEmpty();
  }

  public void process() {

    Calendar c = Calendar.getInstance();
    c.setTime(_time);
    c.add(Calendar.MINUTE, -_minutesBefore);
    Date timeFrom = c.getTime();

    c.setTime(_time);
    c.add(Calendar.MINUTE, _minutesAfter);
    Date timeTo = c.getTime();

    _result = _transitDataService.getStopsWithArrivalsAndDepartures(_stopIds,
        timeFrom, timeTo);

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

  private void orderResults() {
    if (_order != null)
      Collections.sort(_result.getArrivalsAndDepartures(), _order);
  }

  private void updateCurrentUser() {
    // Save the last selected stop id
    _currentUserService.setLastSelectedStopIds(_stopIds);

    UserBean user = _currentUserService.getCurrentUser();
    if (user == null || !user.hasDefaultLocation()) {
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
      int i = o1.getTrip().getTripHeadsign().compareTo(
          o2.getTrip().getTripHeadsign());
      if (i == 0)
        return SORT_BY_TIME.compare(o1, o2);
      return i;
    }
  }

}
