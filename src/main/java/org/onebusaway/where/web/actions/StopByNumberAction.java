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
package org.onebusaway.where.web.actions;

import edu.washington.cs.rse.text.NaturalStringOrder;

import com.opensymphony.xwork2.ActionSupport;

import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.rpc.WhereService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopByNumberAction extends ActionSupport {

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

  private WhereService _service;

  private WhereMessages _msgs;

  private String _id;

  private StopWithArrivalsBean _result;

  private String _order = ORDER_TIME;

  private boolean _filtered = false;

  private FilterConstraint _filter = new DefaultFilter();

  @Autowired
  public void setWhereService(WhereService service) {
    _service = service;
  }

  @Autowired
  public void setWhereMessages(WhereMessages messages) {
    _msgs = messages;
  }

  public void setId(String id) {
    _id = id;
  }

  public void setOrder(String order) {
    if (!_orderConstraints.containsKey(order))
      order = ORDER_TIME;
    _order = order;
  }

  public void setRoute(String routes) {
    if (routes.length() == 0 || routes.equals("all")) {
      _filter = new DefaultFilter();
    } else {
      Set<String> routeNames = new HashSet<String>();
      for (String token : routes.split(",")) {
        routeNames.add(token);
      }
      _filter = new RouteFilter(routeNames);
    }
  }

  public boolean isFiltered() {
    return _filtered;
  }

  public StopWithArrivalsBean getResult() {
    return _result;
  }

  @Override
  public String execute() throws ServiceException {

    StopWithArrivalsBean result = _service.getArrivalsByStopId(_id);
    StopBean stop = result.getStop();
    List<DepartureBean> arrivals = result.getPredictedArrivals();

    // Filter the results
    List<DepartureBean> filtered = new ArrayList<DepartureBean>(arrivals.size());
    for (Iterator<DepartureBean> it = arrivals.iterator(); it.hasNext();) {
      DepartureBean bean = it.next();
      if (_filter.isEnabled(bean))
        filtered.add(bean);
      else
        _filtered = true;
    }

    // Order the results
    OrderConstraint constraint = _orderConstraints.get(_order);
    if (constraint != null)
      Collections.sort(filtered, constraint);

    _result = new StopWithArrivalsBean(stop, filtered);

    return SUCCESS;
  }

  public Date getTimeAsDate(long time) {
    return new Date(time);
  }

  public long getNow() {
    return System.currentTimeMillis();
  }

  public String getArrivalLabel(DepartureBean pab, long now) {

    long predicted = pab.getPredictedTime();
    long scheduled = pab.getScheduledTime();

    if (predicted > 0) {

      double diff = ((pab.getPredictedTime() - pab.getScheduledTime()) / (1000.0 * 60));
      int minutes = (int) Math.abs(Math.round(diff));

      boolean departed = predicted < now;

      if (diff < -1.5) {
        return departed ? _msgs.departedEarly(minutes) : _msgs.early(minutes);
      } else if (diff < 1.5) {
        return departed ? _msgs.departedOnTime() : _msgs.onTime();
      } else {
        return departed ? _msgs.departedLate(minutes) : _msgs.delayed(minutes);
      }

    } else {
      if (scheduled < now)
        return _msgs.scheduledDeparture();
      else
        return _msgs.scheduledArrival();
    }
  }

  public String getArrivalStatusLabelStyle(DepartureBean pab, long now) {

    long predicted = pab.getPredictedTime();
    long scheduled = pab.getScheduledTime();

    if (predicted > 0) {

      if (predicted < now)
        return "arrivalStatusDeparted";

      double diff = ((pab.getPredictedTime() - pab.getScheduledTime()) / (1000.0 * 60));

      if (diff < -1.5) {
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

  public String getMinutesLabel(DepartureBean pab, long now) {
    boolean isNow = isArrivalNow(pab, now);
    long t = pab.getBestTime();
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    return isNow ? "NOW" : Integer.toString(minutes);
  }

  public boolean isArrivalNow(DepartureBean pab, long now) {
    long t = pab.getBestTime();
    int minutes = (int) Math.round((t - now) / (1000.0 * 60.0));
    return Math.abs(minutes) <= 1;
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private interface OrderConstraint extends Comparator<DepartureBean> {
    // public void addTargetParams(Map<String, String> params);
  }

  private static class SortByRoute implements OrderConstraint {
    public int compare(DepartureBean o1, DepartureBean o2) {
      if (o1.getRoute() == o2.getRoute())
        return SORT_BY_TIME.compare(o1, o2);
      String a = o1.getRoute();
      String b = o2.getRoute();
      return NaturalStringOrder.compareNaturalIgnoreCaseAscii(a, b);
    }

    public void addTargetParams(Map<String, String> params) {
      // params.put(ORDER_BY_KEY, ORDER_BY_ROUTE_VALUE);
    }
  }

  private static class SortByTime implements OrderConstraint {
    public int compare(DepartureBean o1, DepartureBean o2) {
      long a = o1.getBestTime();
      long b = o2.getBestTime();
      return a == b ? 0 : (a < b ? -1 : 1);
    }

    public void addTargetParams(Map<String, String> params) {
      // params.put(ORDER_BY_KEY, ORDER_BY_TIME_VALUE);
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
      // params.put(ORDER_BY_KEY, ORDER_BY_DEST_VALUE);
    }
  }

  /*****************************************************************************
   * 
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

    private Set<String> _routes;

    public RouteFilter(Set<String> routes) {
      _routes = routes;
    }

    public boolean isEnabled(DepartureBean bean) {
      return _routes.contains(bean.getRoute());
    }

    public void addTargetParams(Map<String, String> params) {
      // params.put(ROUTE_FILTER_KEY, Integer.toString(_route));
    }

  }
}
