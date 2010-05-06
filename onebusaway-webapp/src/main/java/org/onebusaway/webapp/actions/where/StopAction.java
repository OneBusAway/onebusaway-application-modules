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
package org.onebusaway.webapp.actions.where;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.impl.AgencyPresenter;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.utility.text.NaturalStringOrder;
import org.onebusaway.webapp.gwt.where_library.WhereMessages;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryCssResource;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryResources;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesMethods;
import org.onebusaway.webapp.impl.resources.ClientBundleFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

public class StopAction extends AbstractWhereAction {

  private static final long serialVersionUID = 1L;

  private static final String ORDER_ROUTE = "route";

  private static final String ORDER_DEST = "dest";

  private static final String ORDER_TIME = "time";

  private static final OrderConstraint SORT_BY_TIME = new SortByTime();

  private static final OrderConstraint SORT_BY_DEST = new SortByDestination();

  private static final OrderConstraint SORT_BY_ROUTE = new SortByRoute();

  private static final Map<String, OrderConstraint> _orderConstraints = new HashMap<String, OrderConstraint>();

  static {
    _orderConstraints.put(ORDER_ROUTE, SORT_BY_ROUTE);
    _orderConstraints.put(ORDER_DEST, SORT_BY_DEST);
    _orderConstraints.put(ORDER_TIME, SORT_BY_TIME);
  }

  private TransitDataService _service;

  private CurrentUserService _currentUserService;

  private DefaultSearchLocationService _defaultSearchLocationService;

  private ArrivalsAndDeparturesMethods _adMethods = new ArrivalsAndDeparturesMethods();

  private List<String> _ids;

  private Set<String> _routes;

  private Date _time = new Date();

  private StopsWithArrivalsAndDeparturesBean _result;

  private String _order = ORDER_TIME;

  private int _minutesBefore = 5;

  private int _minutesAfter = 35;

  private boolean _filtered = false;

  private FilterConstraint _filter = new DefaultFilter();

  private List<AgencyBean> _agencies;

  private TimeZone _timeZone;

  private boolean _needsRedirect = false;

  @Autowired
  public void setWhereMessages(WhereMessages messages) {
    _adMethods.setMessages(messages);
  }

  @Autowired
  public void setTransitDataService(TransitDataService service) {
    _service = service;
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

  @Autowired
  public void setClientBundleFactory(ClientBundleFactory factory) {
    WhereLibraryResources resources = factory.getBundleForType(WhereLibraryResources.class);
    WhereLibraryCssResource css = resources.getCss();
    _adMethods.setCss(css);
  }

  /**
   * To give more than one Stop ID, the URL must specify id= more than once.
   */
  public void setId(List<String> ids) {
    // Stop ids needs to be something serializable across the wire
    // XWorks can use its own list implementation for ids
    // We also check for legacy stop ids
    _ids = new ArrayList<String>();
    for (String id : ids) {
      if (!id.contains("_")) {
        id = "1_" + id;
        _needsRedirect = true;
      }
      _ids.add(id);
    }
  }

  public List<String> getId() {
    return _ids;
  }

  public void setOrder(String order) {
    if (!_orderConstraints.containsKey(order))
      order = ORDER_TIME;
    _order = order;
  }

  /**
   * Supports two styles of URL. It can be comma-seperated, or route= can be
   * given multiple times.
   */
  public void setRoute(List<String> routeLists) {
    boolean isAll = routeLists.isEmpty();
    _routes = new HashSet<String>();

    for (String routes : routeLists) {
      if (routes.length() == 0 || routes.equals("all")) {
        isAll = true;
      } else {
        for (String token : routes.split(",")) {
          if (!token.contains("_"))
            _needsRedirect = true;
          _routes.add(token);
        }
      }
    }
    if (isAll) {
      _filter = new DefaultFilter();
    } else {
      _filter = new RouteFilter(_routes);
    }
  }

  @TypeConversion(converter = "org.onebusaway.webapp.actions.where.DateTimeConverter")
  public void setTime(Date time) {
    _time = time;
  }

  public void setMinutesBefore(int minutesBefore) {
    _minutesBefore = minutesBefore;
  }

  public void setMinutesAfter(int minutesAfter) {
    _minutesAfter = minutesAfter;
  }

  public boolean isFiltered() {
    return _filtered;
  }

  public StopsWithArrivalsAndDeparturesBean getResult() {
    return _result;
  }

  public List<AgencyBean> getAgencies() {
    return _agencies;
  }

  public TimeZone getTimeZone() {
    return _timeZone;
  }

  @Override
  @Actions( {
      @Action(value = "/where/standard/stop"),
      @Action(value = "/where/iphone/stop"),
      @Action(value = "/where/text/stop")})
  public String execute() throws ServiceException {

    if (_ids == null || _ids.isEmpty())
      return INPUT;

    if (_needsRedirect)
      return "redirect";

    Calendar c = Calendar.getInstance();
    c.setTime(_time);
    c.add(Calendar.MINUTE, -_minutesBefore);
    Date timeFrom = c.getTime();

    c.setTime(_time);
    c.add(Calendar.MINUTE, _minutesAfter);
    Date timeTo = c.getTime();

    _result = _service.getStopsWithArrivalsAndDepartures(_ids, timeFrom, timeTo);

    if (_result == null) {
      if (_ids.size() == 1)
        throw new NoSuchStopServiceException(_ids.get(0));
      else
        throw new NoSuchStopServiceException(_ids.toString());
    }

    // Filter the results
    List<ArrivalAndDepartureBean> filtered = new ArrayList<ArrivalAndDepartureBean>();
    for (ArrivalAndDepartureBean bean : _result.getArrivalsAndDepartures()) {
      if (_filter.isEnabled(bean))
        filtered.add(bean);
      else
        _filtered = true;
    }

    // Order the results
    OrderConstraint constraint = _orderConstraints.get(_order);
    if (constraint != null)
      Collections.sort(filtered, constraint);

    _result.setArrivalsAndDepartures(filtered);

    _agencies = AgencyPresenter.getAgenciesForArrivalAndDepartures(filtered);

    _timeZone = TimeZone.getTimeZone(_result.getTimeZone());
    if (_timeZone == null)
      _timeZone = TimeZone.getDefault();

    // Save the last selected stop id
    if (_currentUserService.hasCurrentUser()) {

      _currentUserService.setLastSelectedStopIds(_ids);

      UserBean user = _currentUserService.getCurrentUser();
      if (!user.hasDefaultLocation()) {
        List<StopBean> stops = _result.getStops();
        StopBean stop = stops.get(0);
        _defaultSearchLocationService.setDefaultLocationForCurrentUser(
            stop.getName(), stop.getLat(), stop.getLon());
      }
    }

    return SUCCESS;
  }

  public boolean isLongRouteName(RouteBean route) {
    String name = RoutePresenter.getNameForRoute(route);
    return RoutePresenter.isRouteNameLong(name);
  }

  public Date getTimeAsDate(long time) {
    return new Date(time);
  }

  public long getNow() {
    return System.currentTimeMillis();
  }

  public String getArrivalLabel(ArrivalAndDepartureBean pab, long now) {
    return _adMethods.getArrivalLabel(pab, now);
  }

  public String getArrivalStatusLabelStyle(ArrivalAndDepartureBean pab, long now) {
    return _adMethods.getArrivalStatusLabelStyle(pab, now);
  }

  public String getMinutesLabel(ArrivalAndDepartureBean pab, long now) {
    return _adMethods.getMinutesLabel(pab, now);
  }

  public boolean isArrivalNow(ArrivalAndDepartureBean pab, long now) {
    return _adMethods.isArrivalNow(pab, now);
  }

  public boolean testAgenciesWithDisclaimers(List<AgencyBean> agencies) {
    for (AgencyBean agency : agencies) {
      String disclaimer = agency.getDisclaimer();
      if (disclaimer != null && disclaimer.length() > 0)
        return true;
    }
    return false;
  }

  /**
   * Build URL of stops and routes for the Refined Search page.
   */
  public String getRefineSearchUrl() {
    StringBuilder sb = new StringBuilder("refineview.html?");
    for (String id : _ids) {
      sb.append("id=" + id + "&");
    }
    if (_routes != null) {
      for (String route : _routes) {
        sb.append("route=" + route + "&");
      }
    }
    sb.setLength(sb.length() - 1); // trim the final "&"
    return sb.toString();
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

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

  /*****************************************************************************
   * 
   ****************************************************************************/

  private static interface FilterConstraint {
    public boolean isEnabled(ArrivalAndDepartureBean bean);

    public void addTargetParams(Map<String, String> params);
  }

  private static class DefaultFilter implements FilterConstraint {

    public boolean isEnabled(ArrivalAndDepartureBean bean) {
      return true;
    }

    public void addTargetParams(Map<String, String> params) {

    }
  }

  private static class RouteFilter implements FilterConstraint {

    private Set<String> _routes;

    public RouteFilter(Set<String> routes) {
      _routes = routes;
    }

    public boolean isEnabled(ArrivalAndDepartureBean bean) {
      return _routes.contains(bean.getTrip().getRoute().getId());
    }

    public void addTargetParams(Map<String, String> params) {
      // params.put(ROUTE_FILTER_KEY, Integer.toString(_route));
    }

  }
}
