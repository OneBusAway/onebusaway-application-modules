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

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.utility.text.NaturalStringOrder;
import org.onebusaway.webapp.gwt.where_library.WhereMessages;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesMethods;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopAction extends ActionSupport {

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

  private ArrivalsAndDeparturesMethods _adMethods = new ArrivalsAndDeparturesMethods();

  private List<String> _ids;

  private StopsWithArrivalsAndDeparturesBean _result;

  private String _order = ORDER_TIME;

  private boolean _filtered = false;

  private FilterConstraint _filter = new DefaultFilter();

  @Autowired
  public void setWhereMessages(WhereMessages messages) {
    _adMethods.setMessages(messages);
  }
  
  @Autowired
  public void setTransitDataService(TransitDataService service) {
    _service = service;
  }

  /**
   * To give more than one Stop ID, the URL must specify id= more than once.
   */
  public void setId(List<String> ids) {
    _ids = ids;
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
    Set<String> routeNames = new HashSet<String>();

    for (String routes : routeLists) {
      if (routes.length() == 0 || routes.equals("all")) {
        isAll = true;
      } else {
        for (String token : routes.split(",")) {
          routeNames.add(token);
        }
      }
    }
    if (isAll) {
      _filter = new DefaultFilter();
    } else {
      _filter = new RouteFilter(routeNames);
    }
  }

  public boolean isFiltered() {
    return _filtered;
  }

  public StopsWithArrivalsAndDeparturesBean getResult() {
    return _result;
  }

  @Override
  @Actions( {
      @Action(value = "/where/standard/stop"),
      @Action(value = "/where/iphone/stop")})
  public String execute() throws ServiceException {

    // Stop ids needs to be something serializable across the wire
    // XWorks can use its own list implementation for _ids
    List<String> ids = new ArrayList<String>(_ids);
    try {
      _result = _service.getStopsWithArrivalsAndDepartures(ids, new Date());
    } catch (Throwable ex) {
      ex.printStackTrace();
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

    return SUCCESS;
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

  /*****************************************************************************
   * 
   ****************************************************************************/

  private interface OrderConstraint extends Comparator<ArrivalAndDepartureBean> {
    // public void addTargetParams(Map<String, String> params);
  }

  private static class SortByRoute implements OrderConstraint {
    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      if (o1.getRouteShortName().equals(o2.getRouteShortName()))
        return SORT_BY_TIME.compare(o1, o2);
      String a = o1.getRouteShortName();
      String b = o2.getRouteShortName();
      return NaturalStringOrder.compareNaturalIgnoreCaseAscii(a, b);
    }

    public void addTargetParams(Map<String, String> params) {
      // params.put(ORDER_BY_KEY, ORDER_BY_ROUTE_VALUE);
    }
  }

  private static class SortByTime implements OrderConstraint {
    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      long a = o1.computeBestDepartureTime();
      long b = o2.computeBestDepartureTime();
      return a == b ? 0 : (a < b ? -1 : 1);
    }

    public void addTargetParams(Map<String, String> params) {
      // params.put(ORDER_BY_KEY, ORDER_BY_TIME_VALUE);
    }
  }

  private static class SortByDestination implements OrderConstraint {
    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      int i = o1.getTripHeadsign().compareTo(o2.getTripHeadsign());
      if (i == 0)
        return SORT_BY_TIME.compare(o1, o2);
      return i;
    }

    public void addTargetParams(Map<String, String> params) {
      // params.put(ORDER_BY_KEY, ORDER_BY_DEST_VALUE);
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
      return _routes.contains(bean.getRouteId());
    }

    public void addTargetParams(Map<String, String> params) {
      // params.put(ROUTE_FILTER_KEY, Integer.toString(_route));
    }

  }
}
