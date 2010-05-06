package org.onebusaway.where.web.common.client.model;

import org.onebusaway.common.web.common.client.model.RouteBean;
import org.onebusaway.common.web.common.client.model.StopBean;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyRoutesBean implements IsSerializable, Serializable {

  private static final long serialVersionUID = 1L;

  private List<RouteBean> _nearbyRoutes = new ArrayList<RouteBean>();

  private Map<RouteBean, List<StopBean>> _nearbyStopsByRoute = new HashMap<RouteBean, List<StopBean>>();

  public List<RouteBean> getRoutes() {
    return _nearbyRoutes;
  }

  public List<StopBean> getNearbyStopsForRoute(RouteBean route) {
    return _nearbyStopsByRoute.get(route);
  }

  public void addRouteAndStop(RouteBean route, StopBean stop) {
    List<StopBean> stops = _nearbyStopsByRoute.get(route);
    if (stops == null) {
      stops = new ArrayList<StopBean>();
      _nearbyStopsByRoute.put(route, stops);
      _nearbyRoutes.add(route);
    }
    if (!stops.contains(stop))
      stops.add(stop);
  }
}
